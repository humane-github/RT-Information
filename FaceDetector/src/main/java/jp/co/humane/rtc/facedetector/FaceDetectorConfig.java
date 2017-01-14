package jp.co.humane.rtc.facedetector;

import java.io.File;

import jp.co.humane.rtc.common.starter.bean.ConfigBase;

/**
 * 顔検出処理の設定情報。
 * @author terada.
 *
 */
public class FaceDetectorConfig extends ConfigBase {

    /** 顔の特徴量を定義したカスケードファイルのパス1 */
    private String cascadePath1 = System.getProperty("user.dir") + File.separator + "haarcascade_frontalface_alt.xml";

    /** 顔の特徴量を定義したカスケードファイルのパス2 */
    private String cascadePath2 = "";

    /** 顔の特徴量を定義したカスケードファイルのパス3 */
    private String cascadePath3 = "";

    /** ビューアの有効状態 */
    private Boolean enableViewer = false;

    /** 画像縮小率 */
    private Double zoomOutRatio = 4.0;

    /** 顔検出数の閾値 */
    private Integer detectThreshold = 1;

    /**
     * デフォルトコンストラクタ。
     */
    public FaceDetectorConfig() {
        // 実行周期のデフォルト値を500ミリ秒に設定
        this.interval = 500000L;
    }

    /**
     * cascadePath1を取得する。
     * @return cascadePath1 cascadePath1。
     */
    public String getCascadePath1() {
        return cascadePath1;
    }

    /**
     * cascadePath1を設定する。
     * @param cascadePath1 cascadePath1.
     */
    public void setCascadePath1(String cascadePath1) {
        this.cascadePath1 = cascadePath1;
    }

    /**
     * cascadePath2を取得する。
     * @return cascadePath2 cascadePath2。
     */
    public String getCascadePath2() {
        return cascadePath2;
    }

    /**
     * cascadePath2を設定する。
     * @param cascadePath2 cascadePath2.
     */
    public void setCascadePath2(String cascadePath2) {
        this.cascadePath2 = cascadePath2;
    }

    /**
     * cascadePath3を取得する。
     * @return cascadePath3 cascadePath3。
     */
    public String getCascadePath3() {
        return cascadePath3;
    }

    /**
     * cascadePath3を設定する。
     * @param cascadePath3 cascadePath3.
     */
    public void setCascadePath3(String cascadePath3) {
        this.cascadePath3 = cascadePath3;
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

    /**
     * zoomOutRatioを取得する。
     * @return zoomOutRatio zoomOutRatio。
     */
    public Double getZoomOutRatio() {
        return zoomOutRatio;
    }

    /**
     * zoomOutRatioを設定する。
     * @param zoomOutRatio zoomOutRatio.
     */
    public void setZoomOutRatio(Double zoomOutRatio) {
        this.zoomOutRatio = zoomOutRatio;
    }

    /**
     * detectThresholdを取得する。
     * @return detectThreshold detectThreshold。
     */
    public Integer getDetectThreshold() {
        return detectThreshold;
    }

    /**
     * detectThresholdを設定する。
     * @param detectThreshold detectThreshold.
     */
    public void setDetectThreshold(Integer detectThreshold) {
        this.detectThreshold = detectThreshold;
    }

    /**
     * intervalを取得する。
     * @return interval interval。
     */
    public Long getInterval() {
        return interval;
    }

    /**
     * intervalを設定する。
     * @param interval interval.
     */
    public void setInterval(Long interval) {
        this.interval = interval;
    }

    /**
     * logLevelを取得する。
     * @return logLevel logLevel。
     */
    public String getLogLevel() {
        return logLevel;
    }

    /**
     * logLevelを設定する。
     * @param logLevel logLevel.
     */
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

}
