package jp.co.humane.rtc.common.starter.factory;

import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.RtcDeleteFunc;

/**
 * RtcDeleteFuncのファクトリクラス。
 * @author terada.
 *
 */
public interface RtcDeleteFuncFactory {

    /**
     * Manager経由でRtcDeleteFuncを生成するファクトリメソッド。
     * @param manager RTCマネージャ。
     * @return RTC生成処理。
     */
    public RtcDeleteFunc create(Manager manager);

}
