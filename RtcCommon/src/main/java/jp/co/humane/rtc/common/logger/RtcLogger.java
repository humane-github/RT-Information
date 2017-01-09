package jp.co.humane.rtc.common.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import jp.go.aist.rtm.RTC.log.Logbuf;

/**
 * RTC用のロガー。
 * 使用方法：<br/>
 * <code>
 *    RtcLogger logger = new RtcLogger("XXComponent");
 *    logger.debug("デバッグメッセージ");
 *    logger.info("インフォメッセージ");
 *    logger.warn("ワーニングメッセージ");
 *    logger.error("エラーメッセージ", throwable);
 * </code>
 * @author terada.
 *
 */
public class RtcLogger extends Logbuf {

    /** ログレベル：ERROR */
    private static final int LEVEL_ERROR = 0;

    /** ログレベル：WARN */
    private static final int LEVEL_WARN = 1;

    /** ログレベル：INFO */
    private static final int LEVEL_INFO = 2;

    /** ログレベル：DEBUG */
    private static final int LEVEL_DEBUG = 3;

    /** ログレベル */
    private int logLevel = LEVEL_INFO;

    /** コンソール出力制御用のシステムプロパティキー */
    private static final String CONSOLE_ECHO_PROP_KEY = "disable.console.echo";

    /** コンソール出力の有無 */
    private boolean enableConsoleEcho = true;

    /**
     * コンストラクタ。
     * @param name 名前。
     */
    public RtcLogger(String name) {
        super(name);
    }

    /**
     * コンストラクタ。
     * @param name   名前。
     * @param parent 親ノード名称。
     */
    public RtcLogger(String name, String parent) {
        super(name, parent);

        // 「-Ddisable.console.echo」の起動引数がある場合はコンソール出力しない
        if (null != System.getProperty(CONSOLE_ECHO_PROP_KEY)) {
            enableConsoleEcho = false;
        }

    }

    /**
     * ログレベルを設定する。
     * 不正なレベルの場合はデフォルトでINFOを設定する。
     *
     * @param level ログレベル。
     */
    @Override
    public void setLevel(String level) {
        String targetLevel = "INFO";
        if (Arrays.asList("ERROR", "WARN", "INFO", "DEBUG").contains(level.toUpperCase())) {
            targetLevel = level.toUpperCase();
        }
        super.setLevel(targetLevel);
        this.logLevel = "ERROR".equals(targetLevel) ? LEVEL_ERROR :
                         "WARN".equals(targetLevel) ? LEVEL_WARN :
                         "INFO".equals(targetLevel) ? LEVEL_INFO : LEVEL_DEBUG;
    }

    /**
     * デバッグログを出力する。
     * @param message メッセージ。
     */
    public void debug(String message) {
        println(Logbuf.DEBUG, message);
        consoleOut(LEVEL_DEBUG, message);
    }

    /**
     * デバッグログを出力する。
     * @param message   メッセージ。
     * @param throwable 例外。
     */
    public void debug(String message, Throwable throwable) {
        String trace = getStackTrace(throwable);
        println(Logbuf.DEBUG, message + "\n" + trace);
        consoleOut(LEVEL_DEBUG, message + "\n" + trace);

    }

    /**
     * インフォログを出力する。
     * @param message メッセージ。
     */
    public void info(String message) {
        println(Logbuf.INFO, message);
        consoleOut(LEVEL_INFO, message);
    }

    /**
     * インフォログを出力する。
     * @param message   メッセージ。
     * @param throwable 例外。
     */
    public void info(String message, Throwable throwable) {
        String trace = getStackTrace(throwable);
        println(Logbuf.INFO, message + "\n" + trace);
        consoleOut(LEVEL_INFO, message + "\n" + trace);
    }

    /**
     * ワーニングログを出力する。
     * @param message メッセージ。
     */
    public void warn(String message) {
        println(Logbuf.WARN, message);
        consoleOut(LEVEL_WARN, message);
    }

    /**
     * ワーニングログを出力する。
     * @param message   メッセージ。
     * @param throwable 例外。
     */
    public void warn(String message, Throwable throwable) {
        String trace = getStackTrace(throwable);
        println(Logbuf.WARN, message + "\n" + trace);
        consoleOut(LEVEL_WARN, message + "\n" + trace);
    }

    /**
     * エラーログを出力する。
     * @param message メッセージ。
     */
    public void error(String message) {
        println(Logbuf.ERROR, message);
        consoleOut(LEVEL_ERROR, message);
    }

    /**
     * エラーログを出力する。
     * @param message   メッセージ。
     * @param throwable 例外。
     */
    public void error(String message, Throwable throwable) {
        String trace = getStackTrace(throwable);
        println(Logbuf.ERROR, message + "\n" + trace);
        consoleOut(LEVEL_ERROR, message + "\n" + trace);
    }

    /**
     * 例外からスタックトレースを取得する。
     * @param throwable 例外。
     * @return スタックトレース。
     */
    protected String getStackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    /**
     * メッセージをコンソールに出力する。
     * @param message メッセージ。
     */
    protected void consoleOut(int level, String message) {
        if (enableConsoleEcho) {
            if (level <= logLevel) {
                System.out.println(message);
            }
        }
    }

}
