/**
 *
 */
package jp.co.humane.rtc.tool.connector.shell.command.arg;

/**
 * ポートのパラメータ情報。
 * @author terada
 *
 */
public class PortParam {

    /** ポート名 */
    private String name = null;

    /**
     * nameを取得する。
     * @return name name。
     */
    public String getName() {
        return name;
    }

    /**
     * nameを設定する。
     * @param name name.
     */
    public void setName(String name) {
        this.name = name;
    }

}
