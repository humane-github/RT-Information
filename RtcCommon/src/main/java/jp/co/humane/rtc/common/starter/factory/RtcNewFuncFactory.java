package jp.co.humane.rtc.common.starter.factory;

import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.RtcNewFunc;

/**
 * RtcNewFuncのファクトリクラス。
 * @author terada.
 *
 */
public interface RtcNewFuncFactory {

    /**
     * Manager経由でRtcNewFuncを生成するファクトリメソッド。
     * @param manager RTCマネージャ。
     * @return RTC生成処理。
     */
    public RtcNewFunc create(Manager manager);

}
