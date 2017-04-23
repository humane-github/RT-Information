package jp.co.humane.rtc.juliusclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import RTC.ReturnCode_t;
import RTC.TimedOctetSeq;
import RTC.TimedWString;
import jp.co.humane.rtc.common.component.DataFlowComponent;
import jp.co.humane.rtc.common.io.notify.NotifyReader;
import jp.co.humane.rtc.common.io.notify.impl.ReadLoggingListener;
import jp.co.humane.rtc.common.logger.RtcLogger;
import jp.co.humane.rtc.common.port.RtcInPort;
import jp.co.humane.rtc.common.port.RtcOutPort;
import jp.co.humane.rtc.common.starter.RtcStarter;
import jp.co.humane.rtc.common.util.CorbaObj;
import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.port.ConnectorDataListenerType;

/**
 * Juliusを起動し、入力された音声情報をテキストに変換し出力ポートに送信する。
 * @author terada
 *
 */
public class JuliusClientImpl extends DataFlowComponent<JuliusClientConfig> {

    /** コマンドファイルのエンコード */
    private static final String CHARSET = "UTF-8";

    /** コマンドファイルのコメント文字 */
    private static final String COMMENT_STRING = "#";

    /** コマンドファイルのオーディオポート置換文字 */
    private static final String AUDIO_PORT = "${audio}";

    /** コマンドファイルのモジュールポート置換文字 */
    private static final String MODULE_PORT = "${module}";

    /** 音声情報を受け取るポート */
    private RtcInPort<TimedOctetSeq> voiceDataIn = new RtcInPort<>("voiceData", CorbaObj.newTimedOctetSeq());

    /** テキスト情報を出力するポート */
    private RtcOutPort<TimedWString> resultOut = new RtcOutPort<>("result", CorbaObj.newTimedWString());

    /** Juliusサーバのプロセス */
    private Process juliusProcess = null;

    /** Juliusとの通信を行うインスタンス */
    private JuliusCommunicator communicator = null;

    /** Juliusコンソールの出力内容を読み込むインスタンス */
    private NotifyReader juliusConsoleReader = null;

    /**
     * コンストラクタ。
     * @param manager RTCマネージャ。
     */
    public JuliusClientImpl(Manager manager) {
        super(manager);
    }

    /**
     * 初期化処理。
     * システムエディタに表示する情報を設定する。
     * @return リターンコード。
     */
    @Override
    protected ReturnCode_t onRtcInitialize() {

        // Juliusとの通信を行うクラスを作成
        communicator = new JuliusCommunicator(config, resultOut);

        // 音声情報ポートへのバッファ書き込み時の処理を追加
        voiceDataIn.addConnectorDataListener(ConnectorDataListenerType.ON_BUFFER_WRITE, communicator);

        return ReturnCode_t.RTC_OK;
    }

    /**
     * アクティブ時の処理。
     * Juliusサーバの起動及び通信開始処理を行う。
     *
     * @param ec_id ExecutionContext ID.
     * @return リターンコード。
     */
    @Override
    protected ReturnCode_t onRtcActivated(int ec_id) {

        // Julius起動コマンドを取得する
        List<String> command = getJuliusStartCommand();
        if (null == command) {
            return ReturnCode_t.RTC_ERROR;
        }

        // Juliusサーバを起動する
        try {
            juliusProcess = new ProcessBuilder(command).start();
        } catch (IOException ex) {
            logger.error("Juliusの起動に失敗しました。", ex);
        }
        logger.info("juliusプロセスを開始しました。");

        // Juliusの標準出力をログに書き込む
        ReadLoggingListener listener = new ReadLoggingListener(logger, RtcLogger.DEBUG_H);
        listener.setFormat("Juliusコンソール出力：{0}");
        juliusConsoleReader = new NotifyReader(listener);
        juliusConsoleReader.watch(juliusProcess.getInputStream());

        // Juliusとの通信を開始する
        communicator.start();

        return ReturnCode_t.RTC_OK;
    }

    /**
     * Juliusの起動コマンドを取得する。
     * @return 起動コマンドを表す文字列のリスト。
     */
    private List<String> getJuliusStartCommand() {

        List<String> commandList = new ArrayList<>();

        // Juliusコマンドファイルの記述からコマンド情報を作成
        Path file = Paths.get(config.getJuliusCommandFile());
        try (BufferedReader br = Files.newBufferedReader(file, Charset.forName(CHARSET))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith(COMMENT_STRING)) {
                    String command = convertPortString(line);
                    commandList.add(command);
                }
            }
        } catch (IOException e) {
            logger.error(file.toString() + "からJulius起動コマンドの読み込み処理に失敗しました。");
            return null;
        }
        return commandList;
    }

    /**
     * ポートの文字を置換する。
     * @param line 置換対象文字列。
     * @return 置換後文字列。
     */
    private String convertPortString(String line) {

        // オーディオポートの置換
        if (line.contains(AUDIO_PORT)) {
            line = StringUtils.replace(line, AUDIO_PORT, String.valueOf(config.getJuliusAudioPort()), -1);
        }

        // モジュールポートの置換
        if (line.contains(MODULE_PORT)) {
            line = StringUtils.replace(line, MODULE_PORT, String.valueOf(config.getJuliusModulePort()), -1);
        }
        return line;
    }


    /**
     * 周期的な処理。
     */
    @Override
    protected ReturnCode_t onRtcExecute(int ec_id) {

        // Juliusサーバが停止している場合は例外をスローする
        if (!juliusConsoleReader.isAlive()) {
            if (null == juliusConsoleReader.getException()) {
                throw new RuntimeException("Juliusサーバが停止しているため処理を中止します。");
            } else {
                throw new RuntimeException("Juliusサーバが停止しているため処理を中止します。",
                                            juliusConsoleReader.getException());
            }
        }

        return ReturnCode_t.RTC_OK;
    }

    /**
     * 非アクティブ化時の処理。
     */
    @Override
    protected ReturnCode_t onRtcDeactivated(int ec_id) {

        // Juliusとの通信を停止
        communicator.stop();

        // Juliusプロセスの停止
        if (null != juliusProcess) {
            juliusProcess.destroy();
            logger.info("juliusプロセスを停止しました。");
        }

        return ReturnCode_t.RTC_OK;
    }

    /**
     * メイン処理。
     * @param args 起動引数。
     */
    public static void main(String[] args) {

        RtcStarter.init(args)
                  .setConfig(new JuliusClientConfig())
                  .start(JuliusClientImpl.class);
    }

}
