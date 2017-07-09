package com.zhg.bluetooth.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


import com.zhg.bluetooth.entity.ConnectModel;
import com.zhg.bluetooth.entity.MeasuringModel;
import com.zhg.bluetooth.ifc.ConnectListener;
import com.zhg.bluetooth.ifc.DeviceListener;
import com.zhg.bluetooth.ifc.MeasuringListener;
import com.zhg.bluetooth.util.BluetoothValueUtil;

import java.util.List;


public class BluetoothService2 extends Service {

    private final String TAG = this.getClass().getName() + ">>>";
    private IBinder mBinder = new LocalBinder();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mCharacteristic;

    private ConnectListener mConnectListener;
    private MeasuringListener mMeasuringListener;
    private DeviceListener mDeviceListener;
    MeasuringModel measuringModel = new MeasuringModel();
    ConnectModel connectModel = new ConnectModel();


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (null != mBinder) {
            mBinder = null;
        }

    }

    public class LocalBinder extends Binder {
        public BluetoothService2 getService() {
            return BluetoothService2.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        mBinder = null;
        return super.onUnbind(intent);
    }

    /**
     * 关闭BluetoothGatt = null
     */
    public void close() {
        if (null != mReceiver) {
            unregisterReceiver(mReceiver);
        }

        if (mBluetoothGatt == null) {
            return;
        }

        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public BluetoothAdapter initBluetoothAdapter(Context context) {
        BluetoothManager mBluetoothManager;
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        if (null != mBluetoothManager) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }

        if (null == mBluetoothAdapter) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (null == mBluetoothAdapter) {
                return null;
            }
        }
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                context.startActivity(enableBtIntent);
            }
        }
        registerBroadcast();
        return mBluetoothAdapter;
    }

    private void registerBroadcast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, intentFilter);
    }

    public void discoveryBluetooth() {
        if (null == mBluetoothAdapter) {
            Log.e(TAG,"method discoveryBluetooth \n mBluetoothAdapter is null");
            return;
        }
        mBluetoothAdapter.startDiscovery();
    }

    /**
     * 链接蓝牙
     */
    public boolean connect(BluetoothDevice bluetoothDevice) {
        if (null == bluetoothDevice || null==bluetoothDevice.getAddress() || null == mBluetoothAdapter) {
            Log.e(TAG, "method connect \n connect Bluetooth bluetoothDevice, address or BluetoothAdapter is null : that not initial");
            return false;
        }

        mBluetoothGatt = bluetoothDevice.connectGatt(this, false, mGattCallback);
        return true;
    }

    /**
     * 断开蓝牙链接
     */
    public void disConnect() {
        if (null == mBluetoothAdapter || null == mBluetoothGatt) {
            return;
        }
        mBluetoothGatt.disconnect();
    }


    public void readCharacteristic(String hex) {
        if (null != mCharacteristic && null != mBluetoothGatt) {
            mCharacteristic.setValue(BluetoothValueUtil.getInstance().HexStringToBytes(hex));
            mBluetoothGatt.readCharacteristic(mCharacteristic);
        } else {
            Log.e(TAG, "method readCharacteristic \n read fail : characteristic or bluetoothGatt is null");
        }
    }

    /**
     * 向蓝牙写入命令
     *
     * @param hex 十六进制字符串
     */
    public void writeCharacteristic(String hex) {
        hex = hex.replaceAll(" ","");
        if (null == mCharacteristic || null == mBluetoothGatt){
            Log.e(TAG, "method writeCharacteristic \n write fail : characteristic or bluetoothGatt is null");

            return;
        }
        mCharacteristic.setValue(BluetoothValueUtil.getInstance().HexStringToBytes(hex));
        mBluetoothGatt.writeCharacteristic(mCharacteristic);
        mBluetoothGatt.setCharacteristicNotification(mCharacteristic, true);

    }

    public BluetoothGattCharacteristic getBluetoothGattCharacteristic() {
        return mCharacteristic;
    }


    /**
     * 蓝牙链接回调
     */
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {


            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    if (null != mBluetoothGatt) {
                        mBluetoothGatt.discoverServices();
                    } else throw new NullPointerException("bluetoothGatt is null");

                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    if (null != mConnectListener){
                        connectModel.setConnectStatus(BluetoothProfile.STATE_DISCONNECTED);
                        connectModel.setConnectMessage("disConnected");
                        mConnectListener.onBluetoothConnect(connectModel);
                    }
                    break;
                default:
                    Log.e(TAG,"method : onConnectionStateChange \n newState is fail : newstate = "+newState);
                    break;
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (BluetoothGatt.GATT_SUCCESS == status) {
                displayGattServices(gatt.getServices());
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i(TAG,"write is successful");
//            Log.i(TAG, "write is successful value = "+ Arrays.toString(characteristic.getValue()));

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            Log.i(TAG, "receive Data is successful uuid = "+characteristic.getUuid());
            measuringModel.setMeasuringData(BluetoothValueUtil.getInstance().bytesToDemicals(characteristic.getValue()));
            mMeasuringListener.onCharacteristicChanged(measuringModel);
        }
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }
    };


    /**
     * 获取特征值（特征UUID） 用来做与蓝牙通讯的唯一通道
     *
     * @param gattServices BluetoothGattService
     */
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {
            Log.e(TAG,"BluetoothGattService list is null");

            return;
        }
        for (BluetoothGattService gattService : gattServices) {

            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : gattCharacteristics) {
                String uuid = characteristic.getUuid().toString();
                String device = mBluetoothGatt.getDevice().getName();
//                Log.i(TAG,"characteristic,,,, uuid ="+uuid);
                int property = characteristic.getProperties();
                if ((property & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0 || (property & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0 ){
                    this.mCharacteristic = characteristic;
                    notification(characteristic);
                }
                if ((property & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    notification(characteristic);
                }
                if ((property & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    notification(characteristic);
                }

            }
        }

        connectModel.setConnectStatus(BluetoothProfile.STATE_CONNECTED);
        connectModel.setConnectMessage("已链接");
        mConnectListener.onBluetoothConnect(connectModel);

    }

    private void notification(BluetoothGattCharacteristic gattCharacteristic) {
        boolean success = mBluetoothGatt.setCharacteristicNotification(gattCharacteristic, true);

        if (!success) {
            Log.e(TAG, "BluetoothGatt setCharacteristicNotification is fail : bluetoothGattCharacteristicUUID = " + gattCharacteristic.getUuid());
            return;
        }
        for (BluetoothGattDescriptor dp : gattCharacteristic.getDescriptors()) {
            if (dp == null) {
                continue;
            }
            if ((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            }
            mBluetoothGatt.writeDescriptor(dp);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (null != device && null != mDeviceListener) {
                        mDeviceListener.onBluetoothDevice(device);
                    }

                    break;
                default:
                    Log.e(TAG, "BroadcastReceiver getAction is not registerReceiver : action = " + intent.getAction());
                    break;
            }
        }
    };


    public void setConnectListener(ConnectListener connectListener){
        this.mConnectListener = connectListener;
    }

    public void setMeasuringListener (MeasuringListener measuringListener){
        this.mMeasuringListener = measuringListener;
    }

    public void setDeviceListener(DeviceListener deviceListener){
        this.mDeviceListener = deviceListener;
    }

}
