package jp.co.humane.rtc.juliusclient;

import java.io.IOException;

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

    /** 音声情報を受け取るポート */
    private RtcInPort<TimedOctetSeq> voiceDataIn = null;

    /** テキスト情報を出力するポート */
    private RtcOutPort<TimedWString> resultOut = null;

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

        // 音声情報を受け取るポートを追加
        voiceDataIn = new RtcInPort<TimedOctetSeq>("voiceData", CorbaObj.newTimedOctetSeq());
        addInPort("voiceData", voiceDataIn);

        // テキスト情報を出力するポートを追加
        resultOut = new RtcOutPort<TimedWString>("result", CorbaObj.newTimedWString());
        addOutPort("result", resultOut);

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

        // Juliusサーバを起動する
        try {
            juliusProcess = new ProcessBuilder(config.getJuliusExePath(),
                                               "-C",      config.getJuliusConfPath(),
                                               "-input",  "adinnet",
                                               "-adport", config.getJuliusAudioPort().toString(),
                                               "-module", config.getJuliusModulePort().toString())
                            .start();

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
        juliusProcess.destroy();

        logger.info("juliusプロセスを停止しました。");

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
