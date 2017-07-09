package com.zhg.bluetoothdemo0630;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zhg.bluetooth.entity.ConnectModel;
import com.zhg.bluetooth.entity.MeasuringModel;
import com.zhg.bluetooth.ifc.ConnectListener;
import com.zhg.bluetooth.ifc.DeviceListener;
import com.zhg.bluetooth.ifc.MeasuringListener;
import com.zhg.bluetooth.service.BluetoothService2;


public class BluetoothActivity extends AppCompatActivity
        implements ConnectListener,MeasuringListener,DeviceListener{

    private String TAG = this.getClass().getSimpleName()+"---";
    public static final int PERMISSION_LOCATION = 100;
    /**
     * 需要链接的设备名字
     */
    private final String DEVICE_NAME = "BluetoothDeviceName";

    private TextView tv_text;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothService2 mBluetoothService;
    private BluetoothAdapter mBluetoothAdapter;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothService = ((BluetoothService2.LocalBinder) service).getService();
            initListener();
            mBluetoothAdapter = mBluetoothService.initBluetoothAdapter(BluetoothActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mBluetoothService){
            unbindService(mServiceConnection);
            Intent intent = new Intent();
            intent.setAction("com.zhg.bluetooth.service");
            intent.setPackage(getPackageName());
            stopService(intent);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        initUI();
        initService();
        requestPermission();
    }

    private  void initUI(){
        tv_text = (TextView) findViewById(R.id.tv_text);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= 23 && !isLocationOpen(BluetoothActivity.this)) {
                        Intent enableLocate = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(enableLocate);
                    }
                } else {
                    Toast.makeText(this,"读取位置权限被禁用", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void initService() {
        Intent gattServiceIntent = new Intent(this, BluetoothService2.class);
        gattServiceIntent.setAction("com.zhg.bluetooth.service");
        startService(gattServiceIntent);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
        }
    }

    private void initListener(){
        if (null != mBluetoothService){
            mBluetoothService.setConnectListener(this);
            mBluetoothService.setDeviceListener(this);
            mBluetoothService.setMeasuringListener(this);
        }
    }

    public void onBluetoothClick(View view){
        switch (view.getId()){

            case R.id.btn_disCover:
                //扫描
                if (null != mBluetoothAdapter){
                    mBluetoothAdapter.startDiscovery();
                }else {
                    Log.e(TAG,"134 bluetoothAdapter is null");
                }
                break;

            case R.id.btn_connect:
                //链接
                mBluetoothService.connect(mBluetoothDevice);
                break;

            case R.id.btn_disConnect:
                //断开链接
                mBluetoothService.disConnect();
                break;
            case R.id.btn_sendCMD:
                //TODO 向蓝牙设备发送命令
                mBluetoothService.writeCharacteristic("BEB001C036");//十六进制数命令
                break;
            default:
                break;
        }
    }

    private boolean isLocationOpen(final Context context){
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //gps定位
        boolean isGpsProvider = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //网络定位
        boolean isNetWorkProvider = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGpsProvider|| isNetWorkProvider;
    }


    @Override
    public void onBluetoothDevice(BluetoothDevice device) {
        Log.i(TAG,"deviceName is "+device.getName());
        if (DEVICE_NAME.equals(device.getName())){
            mBluetoothDevice = device;
            mBluetoothAdapter.cancelDiscovery();
            tv_text.setText(device.getName());
        }
    }

    @Override
    public void onBluetoothConnect(final ConnectModel connectModel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (connectModel.getConnectStatus() == BluetoothProfile.STATE_CONNECTED){
                    tv_text.setText("" +mBluetoothDevice.getName()+" is connect");
                }else {
                    tv_text.setText(""+mBluetoothDevice.getName()+" is disConnect");
                }

            }
        });

    }

    @Override
    public void onCharacteristicChanged(MeasuringModel measuringModel) {
         //在这里处理返回的数据，如果是多个设备同时兼容，建议使用Adapter模式
    }
}
