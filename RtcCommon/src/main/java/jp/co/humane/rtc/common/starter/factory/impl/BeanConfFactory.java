package jp.co.humane.rtc.common.starter.factory.impl;

import jp.co.humane.rtc.common.starter.factory.RtcConfFactory;
import jp.co.humane.rtc.common.util.ConfigUtils;
import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.util.Properties;

/**
 * 設定情報をBeanクラスで定義する場合のConfFactory。
 * @author terada.
 */
public class BeanConfFactory implements RtcConfFactory {

    /** コンポーネント名 */
    private String componentName = null;

    /** 設定情報のBeanインスタンス */
    private Object bean = null;

    /** 設定値の上書き情報 */
    private String[] addParams = null;

    /**
     * コンストラクタ。
     * @param bean      設定情報が定義されたBeanインスタンス。
     * @param addParams デフォルトの設定値を上書きする情報。以下の形式で指定すること。<br>
     *                   "キー1", "値1", "キー2", "値2", ...
     */
    public BeanConfFactory(Object bean, String ...addParams) {
        this.bean = bean;
        this.addParams = addParams;
    }

    /**
     * 設定情報がBeanに定義されているものとして設定情報の生成処理を行う。
     * @param manager RTCマネージャ。
     * @return 設定情報。
     */
    @Override
    public Properties create(Manager arg0) {
        String[] confArr = ConfigUtils.getRtcConf(componentName, bean, addParams);
        return new Properties(confArr);
    }

    /**
     * 設定情報のbeanインスタンスを取得する。
     * @return 設定情報のbeanインスタンス。
     */
    public Object getBean() {
        return bean;
    }

    /**
     * コンポーネント名を設定する。
     * @param componentName コンポーネント名。
     */
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

}
