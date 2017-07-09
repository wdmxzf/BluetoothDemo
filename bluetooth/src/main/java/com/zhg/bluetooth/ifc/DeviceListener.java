package com.zhg.bluetooth.ifc;


import android.bluetooth.BluetoothDevice;

public interface DeviceListener {

    /**
     * 扫描到的蓝牙设备
     * @param device 设备
     */
    void onBluetoothDevice(BluetoothDevice device);

}
