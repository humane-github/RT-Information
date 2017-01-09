package jp.co.humane.rtc.common.util;

import RTC.CameraImage;
import RTC.Time;
import RTC.TimedBoolean;
import RTC.TimedLong;
import RTC.TimedOctetSeq;
import RTC.TimedWString;

/**
 * CORBAオブジェクトの生成を行うユーティリティクラス。
 * IDLから生成されたオブジェクトのデフォルトコンストラクタがいまいちなので作成。
 *
 * @author terada.
 *
 */
public class CorbaObj {

    /**
     * 無害な値で初期化されたCameraImageを生成する。
     * @return CameraImage.
     */
    public static CameraImage newCameraImage() {
        return new CameraImage(new Time(0, 0), (short)0, (short)0, (short)0, "", 0.0, new byte[255]);
    }

    /**
     * 指定のバッファサイズで初期化されたCameraImageを生成する。
     * @return CameraImage.
     */
    public static CameraImage newCameraImage(int size) {
        return new CameraImage(new Time(0, 0), (short)0, (short)0, (short)0, "", 0.0, new byte[size]);
    }

    /**
     * 無害な値で初期化されたTimedLongを生成する。
     * @return TimedLong.
     */
    public static TimedLong newTimedLong() {
        return new TimedLong(new Time(0, 0), 0);
    }

    /**
     * 指定の値で初期化されたTimedLongを生成する。
     * @return TimedLong.
     */
    public static TimedLong newTimedLong(int val) {
        return new TimedLong(new Time(0, 0), val);
    }

    /**
     * 無害な値で初期化されたTimedBooleanを生成する。
     * @return TimedBoolean.
     */
    public static TimedBoolean newTimedBoolean() {
        return new TimedBoolean(new Time(0, 0), true);
    }

    /**
     * 指定の値で初期化されたTimedBooleanを生成する。
     * @return TimedBoolean.
     */
    public static TimedBoolean newTimedBoolean(boolean val) {
        return new TimedBoolean(new Time(0, 0), val);
    }

    /**
     * 無害な値で初期化されたTimedOctetSeqを生成する。
     * @return TimedOctetSeq
     */
    public static TimedOctetSeq newTimedOctetSeq() {
        return new TimedOctetSeq(new Time(0, 0), new byte[]{});
    }

    /**
     * 指定の値で初期化されたTimedOctetSeqを生成する。
     * @return TimedOctetSeq
     */
    public static TimedOctetSeq newTimedOctetSeq(byte[] val) {
        return new TimedOctetSeq(new Time(0, 0), val);
    }

    /**
     * 無害な値で初期化されたTimedWStringを生成する。
     * @return TimedWString
     */
    public static TimedWString newTimedWString() {
        return new TimedWString(new Time(0, 0), "");
    }

    /**
     * 指定の値で初期化されたTimedWStringを生成する。
     * @return TimedWString
     */
    public static TimedWString newTimedWString(String val) {
        return new TimedWString(new Time(0, 0), val);
    }

}
