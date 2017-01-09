package jp.co.humane.rtc.common.starter.factory.impl;

import java.lang.reflect.Field;

import jp.co.humane.rtc.common.starter.factory.RtcConfFactory;
import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.util.Properties;

/**
 * Staticフィールドから設定情報を生成するファクトリクラス。
 * 
 * 指定クラスのpublic staticフィールドから文字列配列を取得し、
 * 設定情報として利用する。
 * 
 * @author terada.
 *
 */
public class StaticFieldConfFactory implements RtcConfFactory {

    /** 設定情報が定義されているクラス */
    private Class<?> clazz = null;

    /** 配列が定義されているフィールド名 */
    private String fieldName = "component_conf";

    /**
     * コンストラクタ。
     * @param clazz 設定情報が定義されているクラス。
     */
    public StaticFieldConfFactory(Class<?> clazz) {
        super();
        this.clazz = clazz;
    }

    /**
     * コンストラクタ。
     * @param clazz     設定情報が定義されているクラス。
     * @param fieldName 設定情報が定義されているフィールド名。
     */
    public StaticFieldConfFactory(Class<?> clazz, String fieldName) {
        super();
        this.clazz = clazz;
        this.fieldName = fieldName;
    }

    /**
     * 設定情報がstaticなフィールドに定義されているものとして生成処理を行う。
     * @param manager RTCマネージャ。
     * @return 設定情報。
     */
    @Override
    public Properties create(Manager manager) {

        String[] confArr = new String[]{};
        try {
            Field field = clazz.getField(fieldName);
            confArr = (String[]) field.get(null);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        return new Properties(confArr);
    }


}
