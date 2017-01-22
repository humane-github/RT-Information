/**
 *
 */
package jp.co.humane.rtc.tool.connector.bean;

import RTC.RTObject;

/**
 * RTCのオブジェクト参照の格納用Bean.
 * @author terada.
 *
 */
public class ObjRefHolder {

    /** RTCのオブジェクト参照 */
    private RTObject object = null;

    /** ディレクトリパス */
    private String directory = null;

    /** ID */
    private String id = null;

    /** KIND */
    private String kind = null;

    /**
     * コンストラクタ。
     * @param object オブジェクト参照。
     */
    public ObjRefHolder(String directory, RTObject object, String id, String kind) {
        this.directory = directory;
        this.object = object;
        this.id = id;
        this.kind = kind;
    }

    /**
     * objectを取得する。
     * @return object object。
     */
    public RTObject getObject() {
        return object;
    }

    /**
     * objectを設定する。
     * @param object object.
     */
    public void setObject(RTObject object) {
        this.object = object;
    }

    /**
     * idを取得する。
     * @return id id。
     */
    public String getId() {
        return id;
    }

    /**
     * idを設定する。
     * @param id id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * directoryを取得する。
     * @return directory directory。
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * directoryを設定する。
     * @param directory directory.
     */
    public void setDirectory(String directory) {
        this.directory = directory;
    }

}
