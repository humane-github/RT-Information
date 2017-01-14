package jp.co.humane.rtc.juliusclient;

import jp.co.humane.rtc.common.starter.bean.ConfigBase;

/**
 * JuliusClientの設定情報。
 * @author terada
 *
 */
public class JuliusClientConfig extends ConfigBase {

    /** Juliusの音声情報送信用ポート番号 */
    private Integer juliusAudioPort = 23972;

    /** Juliusのテキスト受信用ポート番号 */
    private Integer juliusModulePort = 23973;

    /** Juliusホスト名 */
    private String juliusHostname = "localhost";

    /** Juliusの実行ファイルのパス */
    private String juliusExePath = "C:\\DEV\\17.InformationClerk\\20.bin\\julius-4.3-win32bin\\bin\\julius.exe";

    /** Juliusの設定ファイルのパス */
    private String juliusConfPath = "C:\\DEV\\17.InformationClerk\\20.bin\\julius-4.3-win32bin\\gram\\conf.jconf";

    /** 有効スコア */
    private Double availableScore = 0.5;

    /**
     * コンストラクタ。
     */
    public JuliusClientConfig() {
        interval = 500000L;
    }

    /**
     * juliusAudioPortを取得する。
     * @return juliusAudioPort juliusAudioPort.
     */
    public Integer getJuliusAudioPort() {
        return juliusAudioPort;
    }

    /**
     * juliusAudioPortを設定する。
     * @param juliusAudioPort juliusAudioPort.
     */
    public void setJuliusAudioPort(Integer juliusAudioPort) {
        this.juliusAudioPort = juliusAudioPort;
    }

    /**
     * juliusModulePortを取得する。
     * @return juliusModulePort juliusModulePort.
     */
    public Integer getJuliusModulePort() {
        return juliusModulePort;
    }

    /**
     * juliusModulePortを設定する。
     * @param juliusModulePort juliusModulePort.
     */
    public void setJuliusModulePort(Integer juliusModulePort) {
        this.juliusModulePort = juliusModulePort;
    }

    /**
     * juliusHostnameを取得する。
     * @return juliusHostname juliusHostname.
     */
    public String getJuliusHostname() {
        return juliusHostname;
    }

    /**
     * juliusHostnameを設定する。
     * @param juliusHostname juliusHostname.
     */
    public void setJuliusHostname(String juliusHostname) {
        this.juliusHostname = juliusHostname;
    }

    /**
     * juliusExePathを取得する。
     * @return juliusExePath juliusExePath.
     */
    public String getJuliusExePath() {
        return juliusExePath;
    }

    /**
     * juliusExePathを設定する。
     * @param juliusExePath juliusExePath.
     */
    public void setJuliusExePath(String juliusExePath) {
        this.juliusExePath = juliusExePath;
    }

    /**
     * juliusConfPathを取得する。
     * @return juliusConfPath juliusConfPath.
     */
    public String getJuliusConfPath() {
        return juliusConfPath;
    }

    /**
     * juliusConfPathを設定する。
     * @param juliusConfPath juliusConfPath.
     */
    public void setJuliusConfPath(String juliusConfPath) {
        this.juliusConfPath = juliusConfPath;
    }

    /**
     * availableScoreを取得する。
     * @return availableScore availableScore.
     */
    public Double getAvailableScore() {
        return availableScore;
    }

    /**
     * availableScoreを設定する。
     * @param availableScore availableScore.
     */
    public void setAvailableScore(Double availableScore) {
        this.availableScore = availableScore;
    }
}
