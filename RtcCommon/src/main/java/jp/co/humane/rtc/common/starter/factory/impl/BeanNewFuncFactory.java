package jp.co.humane.rtc.common.starter.factory.impl;

import java.lang.reflect.Constructor;

import jp.co.humane.rtc.common.component.DataFlowComponent;
import jp.co.humane.rtc.common.starter.bean.ConfigBase;
import jp.co.humane.rtc.common.starter.factory.RtcNewFuncFactory;
import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.RTObject_impl;
import jp.go.aist.rtm.RTC.RtcNewFunc;

/**
 * 設定情報をBeanクラスで定義する場合のNewFactory。
 * @author terada.
 */
@SuppressWarnings("rawtypes")
public class BeanNewFuncFactory implements RtcNewFuncFactory {

    /** 生成対象RTCクラス */
    private Class<? extends DataFlowComponent> clazz = null;

    /** 設定情報Bean */
    private ConfigBase bean = null;

    /** コンポーネント名 */
    private String componentName = null;

    /**
     * コンストラクタ。
     * @param clazz 生成対象RTCクラス。
     */
    public BeanNewFuncFactory(Class<? extends DataFlowComponent> clazz, ConfigBase bean) {
        super();
        this.clazz = clazz;
        this.bean = bean;
    }

    /**
     * クラスからRTCを生成する。
     */
    @Override
    public RtcNewFunc create(Manager manager) {

        final String name = componentName;

        RtcNewFunc rtcNewFunc = new RtcNewFunc() {

            /**
             * 「new clazz(manager)」で生成されるインスタンスを返す。
             *  @param RTCマネージャ。
             *  @return RTC。
             */
            @Override
            public RTObject_impl createRtc(Manager manager) {
                DataFlowComponent component = null;
                Constructor<? extends DataFlowComponent> constructor;
                try {
                    constructor = clazz.getConstructor(Manager.class);
                    component = constructor.newInstance(manager);
                    component.setComponentName(name);
                    component.setConfigBean(bean);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
                return component;
            }
        };

        return rtcNewFunc;
    }

    /**
     * コンポーネント名を設定する。
     * @param componentName コンポーネント名。
     */
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

}
