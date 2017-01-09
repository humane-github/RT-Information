package jp.co.humane.rtc.common.starter.factory.impl;

import java.lang.reflect.Constructor;

import jp.co.humane.rtc.common.starter.factory.RtcNewFuncFactory;
import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.RTObject_impl;
import jp.go.aist.rtm.RTC.RtcNewFunc;

/**
 * 一般的な生成ファクトリクラス。
 * @author terada.
 *
 */
public class NormalNewFuncFactory implements RtcNewFuncFactory {

    /** 生成対象RTCクラス */
    private Class<? extends RTObject_impl> clazz = null;

    /**
     * コンストラクタ。
     * @param clazz 生成対象RTCクラス。
     */
    public NormalNewFuncFactory(Class<? extends RTObject_impl> clazz) {
        super();
        this.clazz = clazz;
    }

    /**
     * クラスからRTCを生成する。
     */
    @Override
    public RtcNewFunc create(Manager manager) {

        RtcNewFunc rtcNewFunc = new RtcNewFunc() {

            /**
             * 「new clazz(manager)」で生成されるインスタンスを返す。
             *  @param RTCマネージャ。
             *  @return RTC。
             */
            @Override
            public RTObject_impl createRtc(Manager manager) {
                RTObject_impl ret = null;
                Constructor<? extends RTObject_impl> constructor;
                try {
                    constructor = clazz.getConstructor(Manager.class);
                    ret = constructor.newInstance(manager);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
                return ret;
            }
        };

        return rtcNewFunc;
    }

}
