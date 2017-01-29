/**
 *
 */
package jp.co.humane.rtc.tool.connector.dao;

import java.util.List;

import jp.co.humane.rtc.tool.connector.dto.RtcInfo;

/**
 * ネーミングサービスのデータにアクセスするDAO。
 * @author terada.
 *
 */
public interface NamingServerDao {

    /**
     * RTC情報の一覧を取得する。
     * @return RTC情報の一覧。
     */
    public List<RtcInfo> getRtcList();

    /**
     * オブジェクト参照のマップを更新する。
     */
    public void reflesh();


}
