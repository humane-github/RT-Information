package jp.co.humane.rtc.juliusclient.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Juliusのソケット通信で受信するXMLのRECOGOUTタグの情報。
 * @author terada
 *
 */
@XmlRootElement(name = "RECOGOUT")
@XmlAccessorType(XmlAccessType.FIELD)
public class RecogOutDto {

    /** SHYPO要素 */
    @XmlElement(name = "SHYPO")
    private List<ShypoDto> shypoList = null;

    /**
     * shypoListを取得する。
     * @return shypoList shypoList.
     */
    public List<ShypoDto> getShypoList() {
        return shypoList;
    }

    /**
     * shypoListを設定する。
     * @param shypoList shypoList.
     */
    public void setShypoList(List<ShypoDto> shypoList) {
        this.shypoList = shypoList;
    }

    /**
     * 文字列に変換する。
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < shypoList.size(); i++) {
            ShypoDto dto = shypoList.get(i);
            sb.append("SHYPO" + i + "=" + dto.toString() + ", ");
        }
        if (0 < sb.length()) {
            sb.setLength(sb.length() - 2);
        }

        return   "RECOGOUT=[" + sb.toString() + "]";
    }

}
