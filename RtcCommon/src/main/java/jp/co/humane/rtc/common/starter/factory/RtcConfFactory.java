package jp.co.humane.rtc.common.starter.factory;

import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.util.Properties;

/**
 * RTCの設定情報を生成するファクトリクラス。
 * @author terada.
 *
 */
public interface RtcConfFactory {

    /**
     * RTCの設定情報を生成する。
     * @param manager RTCマネージャ。
     * @return RTCの設定情報。
     */
    public Properties create(Manager manager);

}
