package jp.co.humane.rtc.common.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import jp.go.aist.rtm.RTC.ConfigAdmin;
import jp.go.aist.rtm.RTC.RTObject_impl;
import jp.go.aist.rtm.RTC.util.StringHolder;

/**
 * 設定情報に関する共通処理を定義したクラス。
 * @author terada.
 *
 */
public class ConfigUtils {

    /** 除外するフィールド名 */
    private static final String EXCLUDE_FIELD = "class";

    /** バインド情報を格納するマップ */
    private static Map<Object, Map<String, StringHolder>> bindMap = new HashMap<>();

    /**
     * RTCの設定情報を生成する。
     * 使用例：<br/>
     * <code>
     *   ConfigUtils.getRtcConf("componentName", new XXXConfBean(), "version", "1.0.1", "max_instance", "3");
     * </code>
     *
     * @param name     コンポーネント名。
     * @param bean     設定情報のBean。
     * @param strings  追加の設定。
     * @return RTCの設定情報。
     */
    public static String[] getRtcConf(String name, Object bean, String ... strings) {

        // デフォルトの設定
        Map<String, String> configMap = new LinkedHashMap<>();
        configMap.put("implementation_id", name);
        configMap.put("type_name",         name);
        configMap.put("description",       name);
        configMap.put("version",           "1.0.0");
        configMap.put("vendor",            "Humane Systems");
        configMap.put("category",          "Category");
        configMap.put("activity_type",     "STATIC");
        configMap.put("max_instance",      "1");
        configMap.put("language",          "Java");
        configMap.put("lang_type",         "compile");

        // 追加設定があれば追加
        for (int index = 0; index < strings.length; index += 2) {
            configMap.put(strings[index], strings[index + 1]);
        }

        // bean情報をテキストとして追加
        Map<String, String> fieldMap = getFieldMap(bean);
        for (String fieldName : fieldMap.keySet()) {
            configMap.put("conf.default." + fieldName, fieldMap.get(fieldName));
            configMap.put("conf.__widget__." + fieldName, "text");
        }

        // Mapを配列にして返却
        List<String> list = new ArrayList<>();
        for (String key : configMap.keySet()) {
            list.add(key);
            list.add(configMap.get(key));
        }
        list.add("");

        String[] ret = new String[list.size()];
        list.toArray(ret);
        return ret;
    }

    /**
     * Beanのフィールド情報をパラメータとしてバインドする。
     * @param component RTCインスタンス。
     * @param bean Beanインスタンス。
     * @return
     */
    public static boolean bindParameters(RTObject_impl component, Object bean) {

        boolean ret = false;
        Map<String, StringHolder> bindParamMap = new HashMap<>();

        // Beanに設定されている値をデフォルト値として設定値にバインドする
        Map<String, String> fieldMap = getFieldMap(bean);
        for (String fieldName : fieldMap.keySet()) {
            StringHolder sh = new StringHolder();
            bindParamMap.put(fieldName, sh);
            ret &= component.bindParameter(fieldName, sh, fieldMap.get(fieldName));
        }
        bindMap.put(bean, bindParamMap);

        return ret;
    }

    /**
     * ConfigAdminの設定情報をBeanに反映させる。
     * @param configAdmin 設定情報。
     * @param bean        Beanインスタンス。
     */
    public static void updateConfig(ConfigAdmin configAdmin, Object bean) {

        // バインドした設定情報を取得
        Map<String, StringHolder> bindParamMap = bindMap.get(bean);
        if (null == bindParamMap) {
            throw new RuntimeException("bean is not bind.");
        }

        // Beanのフィールドにバインド変数の値を格納
        try {
            PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(bean);
            for (PropertyDescriptor pd : pds) {
                String fieldName = pd.getName();
                if (bindParamMap.containsKey(fieldName)) {
                    String fieldValue = bindParamMap.get(fieldName).value;
                    Object value = convertValue(pd.getPropertyType(), fieldValue);
                    pd.getWriteMethod().invoke(bean, value);
                }
            }
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 文字列を指定のクラスの型に変換して返す。
     *
     * @param clazz クラス。
     * @param value 文字列。
     * @return 変換後のクラス。
     */
    private static Object convertValue(Class clazz, String value) {

        // String
        if (String.class.isAssignableFrom(clazz)) {
            return value;
        }

        // 文字列以外で空文字の場合はnullを返す
        if (value.equals("")) {
            return null;
        }

        // Boolean
        if (Boolean.class.isAssignableFrom(clazz)) {
            return Boolean.valueOf(value);
        }

        // Integer
        if (Integer.class.isAssignableFrom(clazz)) {
            return Integer.valueOf(value);
        }

        // Long
        if (Long.class.isAssignableFrom(clazz)) {
            return Long.valueOf(value);
        }

        // Double
        if (Double.class.isAssignableFrom(clazz)) {
            return Double.valueOf(value);
        }

        return null;
    }


    /**
     * Beanのフィールド名と値のマップ情報を取得する。
     * @param bean Beanインスタンス。
     * @return マップ情報。
     */
    private static Map<String, String> getFieldMap(Object bean) {

        Map<String, String> map = new LinkedHashMap<>();

        // bean情報をテキストとして追加
        try {
            PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(bean);
            for (PropertyDescriptor pd : pds) {
                String field = pd.getName();
                if (EXCLUDE_FIELD.equals(field)) {
                    continue;
                }
                Object fieldValue = BeanUtils.getProperty(bean, field);
                String value = (null == fieldValue) ? "" : fieldValue.toString();
                map.put(field, value);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }

        return map;
    }

}
