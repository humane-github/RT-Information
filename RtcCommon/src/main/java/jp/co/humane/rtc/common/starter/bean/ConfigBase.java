package jp.co.humane.rtc.common.starter.bean;

/**
 * 共通の設定情報。
 * @author terada.
 *
 */
public class ConfigBase {

    /** インターバル(マイクロ秒) */
    protected Long interval = 100000L;

    /** ログレベル */
    protected String logLevel = "INFO";

    /**
     * intervalを取得する。
     * @return interval interval。
     */
    public Long getInterval() {
        return interval;
    }

    /**
     * intervalを設定する。
     * @param interval interval.
     */
    public void setInterval(Long interval) {
        this.interval = interval;
    }

    /**
     * logLevelを取得する。
     * @return logLevel logLevel。
     */
    public String getLogLevel() {
        return logLevel;
    }

    /**
     * logLevelを設定する。
     * @param logLevel logLevel.
     */
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

}
