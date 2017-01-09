package jp.co.humane.rtc.juliusclient.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Juliusのソケット通信で受信するXMLのWHYPOタグの情報。
 * @author terada
 *
 */
@XmlRootElement(name = "WHYPO")
@XmlAccessorType(XmlAccessType.FIELD)
public class WhypoDto {

    /** WORD属性 */
    @XmlAttribute(name = "WORD")
    private String word = null;

    /** CLASSID属性 */
    @XmlAttribute(name = "CLASSID")
    private String classId = null;

    /** PHONE属性 */
    @XmlAttribute(name = "PHONE")
    private String phone = null;

    /** CM属性 */
    @XmlAttribute(name = "CM")
    private String cm = null;

    /**
     * wordを取得する。
     * @return word word.
     */
    public String getWord() {
        return word;
    }

    /**
     * wordを設定する。
     * @param word word.
     */
    public void setWord(String word) {
        this.word = word;
    }

    /**
     * classIdを取得する。
     * @return classId classId.
     */
    public String getClassId() {
        return classId;
    }

    /**
     * classIdを設定する。
     * @param classId classId.
     */
    public void setClassId(String classId) {
        this.classId = classId;
    }

    /**
     * phoneを取得する。
     * @return phone phone.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * phoneを設定する。
     * @param phone phone.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * cmを取得する。
     * @return cm cm.
     */
    public String getCm() {
        return cm;
    }

    /**
     * cmを設定する。
     * @param cm cm.
     */
    public void setCm(String cm) {
        this.cm = cm;
    }

    /**
     * 文字列に変換する。
     */
    @Override
    public String toString() {
        return   "[WORD="   + word + ", "
                + "CLASSID=" + classId + ", "
                + "PHONE="   + phone+ ", "
                + "CM="      + cm + "]";
    }

}
