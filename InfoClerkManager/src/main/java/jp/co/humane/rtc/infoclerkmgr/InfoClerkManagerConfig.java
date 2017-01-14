package jp.co.humane.rtc.infoclerkmgr;

import jp.co.humane.rtc.common.starter.bean.ConfigBase;

/**
 * InfoClerkManagerの設定情報。
 * @author terada
 *
 */
public class InfoClerkManagerConfig extends ConfigBase {

    /** 動体検知にかける時間（秒）*/
    private Integer motionDetectTime = 30;

    /** 顔検知にかける時間（秒） */
    private Integer faceDetectTime = 10;

    /** 音声認識にかける時間（秒）*/
    private Integer voiceRecogTime = 30;

    /**
     * motionDetectTimeを取得する。
     * @return motionDetectTime motionDetectTime。
     */
    public Integer getMotionDetectTime() {
        return motionDetectTime;
    }

    /**
     * motionDetectTimeを設定する。
     * @param motionDetectTime motionDetectTime.
     */
    public void setMotionDetectTime(Integer motionDetectTime) {
        this.motionDetectTime = motionDetectTime;
    }

    /**
     * faceDetectTimeを取得する。
     * @return faceDetectTime faceDetectTime。
     */
    public Integer getFaceDetectTime() {
        return faceDetectTime;
    }

    /**
     * faceDetectTimeを設定する。
     * @param faceDetectTime faceDetectTime.
     */
    public void setFaceDetectTime(Integer faceDetectTime) {
        this.faceDetectTime = faceDetectTime;
    }

    /**
     * voiceRecogTimeを取得する。
     * @return voiceRecogTime voiceRecogTime。
     */
    public Integer getVoiceRecogTime() {
        return voiceRecogTime;
    }

    /**
     * voiceRecogTimeを設定する。
     * @param voiceRecogTime voiceRecogTime.
     */
    public void setVoiceRecogTime(Integer voiceRecogTime) {
        this.voiceRecogTime = voiceRecogTime;
    }

}
