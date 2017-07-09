package com.zhg.bluetooth.ifc;


import com.zhg.bluetooth.entity.ConnectModel;

/**
 * 链接状态回调接口
 */
public interface ConnectListener {
    /**
     * 蓝牙链接状态
     * @param connectModel 蓝牙链接状态
     */
    void onBluetoothConnect(ConnectModel connectModel);
}
