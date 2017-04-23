package jp.co.humane.rtc.juliusclient;

import java.io.File;

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

    /** Julius接続リトライ間隔(sec) */
    private Integer connectRetrySecond = 30;

    /** Julius接続リトライアウト回数 */
    private Integer connectRetryOutCount = 20;

    /** Juliusホスト名 */
    private String juliusHostname = "localhost";

    /** Juliusコマンドファイル */
    private String juliusCommandFile = System.getProperty("user.dir") + File.separator + "JuliusCommand.txt";

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
     * connectRetrySecondを取得する。
     * @return connectRetrySecond connectRetrySecond。
     */
    public Integer getConnectRetrySecond() {
        return connectRetrySecond;
    }

    /**
     * connectRetrySecondを設定する。
     * @param connectRetrySecond connectRetrySecond.
     */
    public void setConnectRetrySecond(Integer connectRetrySecond) {
        this.connectRetrySecond = connectRetrySecond;
    }

    /**
     * connectRetryOutCountを取得する。
     * @return connectRetryOutCount connectRetryOutCount。
     */
    public Integer getConnectRetryOutCount() {
        return connectRetryOutCount;
    }

    /**
     * connectRetryOutCountを設定する。
     * @param connectRetryOutCount connectRetryOutCount.
     */
    public void setConnectRetryOutCount(Integer connectRetryOutCount) {
        this.connectRetryOutCount = connectRetryOutCount;
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
     * juliusCommandFileを取得する。
     * @return juliusCommandFile juliusCommandFile。
     */
    public String getJuliusCommandFile() {
        return juliusCommandFile;
    }

    /**
     * juliusCommandFileを設定する。
     * @param juliusCommandFile juliusCommandFile.
     */
    public void setJuliusCommandFile(String juliusCommandFile) {
        this.juliusCommandFile = juliusCommandFile;
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
