package jp.co.humane.rtc.cameradevice;

import jp.co.humane.rtc.common.starter.bean.ConfigBase;

/**
 * CameraDeviceの設定情報。
 * @author terada
 *
 */
public class CameraDeviceConfig extends ConfigBase {

    /** デバイスID */
    private Integer deviceid = -1;

    /** ビューア */
    private Boolean enableViewer = false;

    /**
     * デフォルトコンストラクタ。
     */
    public CameraDeviceConfig() {
        // 実行周期のデフォルト値を100ミリ秒に設定
        this.interval = 100000L;
    }

    /**
     * deviceidを取得する。
     * @return deviceid deviceid。
     */
    public Integer getDeviceid() {
        return deviceid;
    }

    /**
     * deviceidを設定する。
     * @param deviceid deviceid.
     */
    public void setDeviceid(Integer deviceid) {
        this.deviceid = deviceid;
    }

    /**
     * enableViewerを取得する。
     * @return enableViewer enableViewer。
     */
    public Boolean getEnableViewer() {
        return enableViewer;
    }

    /**
     * enableViewerを設定する。
     * @param enableViewer enableViewer.
     */
    public void setEnableViewer(Boolean enableViewer) {
        this.enableViewer = enableViewer;
    }
}
