package jp.co.humane.rtc.juliusclient.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Juliusのソケット通信で受信するXMLのSHYPOタグの情報。
 * @author terada
 *
 */
@XmlRootElement(name = "SHYPO")
@XmlAccessorType(XmlAccessType.FIELD)
public class ShypoDto {

    /** RANK属性 */
    @XmlAttribute(name = "RANK")
    private String rank = null;

    /** SCORE属性 */
    @XmlAttribute(name = "SCORE")
    private String score = null;

    /** WHYPO要素 */
    @XmlElement(name = "WHYPO")
    private List<WhypoDto> whypoList = null;

    /**
     * rankを取得する。
     * @return rank rank.
     */
    public String getRank() {
        return rank;
    }

    /**
     * rankを設定する。
     * @param rank rank.
     */
    public void setRank(String rank) {
        this.rank = rank;
    }

    /**
     * scoreを取得する。
     * @return score score.
     */
    public String getScore() {
        return score;
    }

    /**
     * scoreを設定する。
     * @param score score.
     */
    public void setScore(String score) {
        this.score = score;
    }

    /**
     * whypoListを取得する。
     * @return whypoList whypoList.
     */
    public List<WhypoDto> getWhypoList() {
        return whypoList;
    }

    /**
     * whypoListを設定する。
     * @param whypoList whypoList.
     */
    public void setWhypoList(List<WhypoDto> whypoList) {
        this.whypoList = whypoList;
    }

    /**
     * 文字列に変換する。
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < whypoList.size(); i++) {
            WhypoDto dto = whypoList.get(i);
            sb.append("WHYPO" + i + "=" + dto.toString() + ", ");
        }
        if (0 < sb.length()) {
            sb.setLength(sb.length() - 2);
        }

        return   "[RANK="   + rank + ", "
                + "SCORE="   + score + ", "
                + sb.toString() + "]";
    }

}
