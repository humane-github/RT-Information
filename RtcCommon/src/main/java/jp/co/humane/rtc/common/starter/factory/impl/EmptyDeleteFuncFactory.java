package jp.co.humane.rtc.common.starter.factory.impl;

import jp.co.humane.rtc.common.starter.factory.RtcDeleteFuncFactory;
import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.RTObject_impl;
import jp.go.aist.rtm.RTC.RtcDeleteFunc;

/**
 * 終了処理を特に行わないファクトリクラス。
 * @author terada.
 *
 */
public class EmptyDeleteFuncFactory implements RtcDeleteFuncFactory {

    /**
     * デフォルトコンストラクタ。
     */
    public EmptyDeleteFuncFactory() {
        super();
    }

    /**
     * Manager経由でRtcDeleteFuncを生成するファクトリメソッド。
     * @param manager RTCマネージャ。
     * @return RTC生成処理。
     */
    @Override
    public RtcDeleteFunc create(Manager manager) {

        RtcDeleteFunc rtcDeleteFunc = new RtcDeleteFunc() {

            /**
             * 特に処理は行わない。
             * @param rtcBase RTCインスタンス。
             */
            @Override
            public void deleteRtc(RTObject_impl rtcBase) {
                rtcBase = null;
            }
        };

        return rtcDeleteFunc;
    }
}
