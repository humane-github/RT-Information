package jp.co.humane.rtc.infoclerkmgr.processor;

import java.util.concurrent.TimeUnit;

import RTC.TimedString;
import RTC.TimedWString;
import jp.co.humane.rtc.common.component.state.StateProcessResult;
import jp.co.humane.rtc.common.component.state.StateProcessor;
import jp.co.humane.rtc.common.logger.RtcLogger;
import jp.co.humane.rtc.common.port.RtcInPort;
import jp.co.humane.rtc.common.port.RtcOutPort;
import jp.co.humane.rtc.common.util.CorbaObj;
import jp.co.humane.rtc.common.util.ElapsedTimer;
import jp.co.humane.rtc.infoclerkmgr.InfoClerkManagerConfig;

/**
 * 音声認識中ステータスでの処理クラス。
 * @author terada.
 *
 */
public class VoiceRecognizeProcessor extends StateProcessor {

    /**
     * 処理結果を表すENUM。
     * @author terada.
     */
    public enum Result {
        NOT_RECOGNIZE,
        RECOGNIZE,
        TIMEOUT
    }

    /** ロガー */
    private RtcLogger logger = new RtcLogger("InfoClerkManager");

    /** 音声認識結果の入力ポート */
    private RtcInPort<TimedWString> voiceTexResulttIn = null;

    /** 音声合成用の出力ポート */
    private RtcOutPort<TimedString> voiceTextOut = null;

    /** 設定情報 */
    private InfoClerkManagerConfig config = null;

    /** 経過時間タイマー */
    private ElapsedTimer timer = new ElapsedTimer();

    /**
     * コンストラクタ。
     * @param voiceTexResulttIn 音声認識結果の入力ポート。
     * @param voiceTextOut      音声合成用の出力ポート。
     */
    public VoiceRecognizeProcessor(RtcInPort<TimedWString> voiceTexResulttIn,
                                   RtcOutPort<TimedString> voiceTextOut,
                                   InfoClerkManagerConfig config) {
        this.voiceTexResulttIn = voiceTexResulttIn;
        this.voiceTextOut = voiceTextOut;
        this.config = config;
    }

    /**
     * 音声認識中ステータスでの処理。
     * タイムアウト時はTIMEOUT、音声認識できればRECOGNIZE、それ以外はNOT_RECOGNIZEを返す。
     * @param ec_id ExecutionContext ID.
     * @return  処理結果。
     */
    @Override
    public StateProcessResult onExecute(int ec_id) {

        // 検出継続時間を過ぎた場合はTIMEOUTを返す
        if (config.getVoiceRecogTime() < timer.getElapsedTime(TimeUnit.SECONDS)) {
            logger.debug("指定時間" + config.getMotionDetectTime() + "秒を経過しても音声を認識できませんでした。");
            return new StateProcessResult(Result.TIMEOUT);
        }

        // データが入力ポートにない場合はNOT_RECOGNIZEを返す
        if (!voiceTexResulttIn.isNew() || voiceTexResulttIn.isEmpty()) {
            return new StateProcessResult(Result.NOT_RECOGNIZE);
        }

        // 入力データが取得できない場合はNOT_RECOGNIZEを返す
        TimedWString voiceText = voiceTexResulttIn.readData();
        if (null == voiceText || null == voiceText.data) {
            return new StateProcessResult(Result.NOT_RECOGNIZE);
        }

        // 話された内容から場所を特定して地図を表示し音声を出力する
        logger.info("音声認識結果[" + voiceText.data + "]を受け取りました。");
        boolean isSuccess = doOutputMapAndVoice(voiceText.data);

        // 処理が正常終了した場合はRECOGNIZE、それ以外はNOT_RECOGNIZE
        if (isSuccess) {
            return new StateProcessResult(Result.RECOGNIZE);
        } else {
            return new StateProcessResult(Result.NOT_RECOGNIZE);
        }
    }

    /**
     * 話された内容から場所を特定して地図を表示し音声を出力する。
     * @param voiceText 音声認識されたテキスト。
     * @return 処理結果。true：テキストの意味が読み取れ正常に処理ができた。false：それ以外。
     */
    private boolean doOutputMapAndVoice(String voiceText) {

        voiceTextOut.write(CorbaObj.newTimedString(voiceText + "を認識しました"));
        return true;
    }

    /**
     * 前の処理から引き渡される情報を受け取る。
     * @param 無し。
     */
    @Override
    public void acceptPreResult(StateProcessResult result) {

        // 待機中に受け取ったデータは破棄する
        voiceTexResulttIn.clear();

        // タイマーのベースタイムを設定
        timer.setBaseTime();
    }


}
