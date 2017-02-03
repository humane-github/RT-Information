/**
 *
 */
package jp.co.humane.rtc.tool.connector.dao;

import java.util.List;
import java.util.Map;

import jp.co.humane.rtc.tool.connector.dto.PortInfo;
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
     * ポート情報のマップを取得する。
     * @return ポート情報のマップ。
     */
    public Map<String, List<PortInfo>> getPortMap();

    /**
     * ポートの接続数を取得する。
     * @param rtcName   RTC名。
     * @param portName  ポート名。
     * @return 接続数。
     */
    public int getConnectNum(String rtcName, String portName);

    /**
     * オブジェクト参照のマップを更新する。
     */
    public void reflesh();


}
