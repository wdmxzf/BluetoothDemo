package com.zhg.bluetooth.ifc;


import com.zhg.bluetooth.entity.MeasuringModel;

/**
 * 测量中回调接口
 */
public interface MeasuringListener {
    /**
     * 测量中回调方法
     * @param measuringModel 测量中的值
     */
    void onCharacteristicChanged(MeasuringModel measuringModel);
}
