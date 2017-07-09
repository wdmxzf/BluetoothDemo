package com.zhg.bluetooth.entity;


public class ConnectModel {

    private int connectStatus;
    private String connectMessage;

    public int getConnectStatus() {
        return connectStatus;
    }

    /**
     * @param connectStatus 蓝牙链接状态
     */
    public void setConnectStatus( int connectStatus) {
        this.connectStatus = connectStatus;
    }

    public String getConnectMessage() {
        return connectMessage;
    }

    public void setConnectMessage(String connectMessage) {
        this.connectMessage = connectMessage;
    }
}
