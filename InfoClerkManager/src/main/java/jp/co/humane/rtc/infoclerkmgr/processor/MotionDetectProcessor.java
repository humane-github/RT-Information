package jp.co.humane.rtc.infoclerkmgr.processor;

import java.util.concurrent.TimeUnit;

import RTC.TimedBoolean;
import RTC.TimedLong;
import jp.co.humane.rtc.common.component.state.StateProcessResult;
import jp.co.humane.rtc.common.component.state.StateProcessor;
import jp.co.humane.rtc.common.logger.RtcLogger;
import jp.co.humane.rtc.common.port.RtcInPort;
import jp.co.humane.rtc.common.port.RtcOutPort;
import jp.co.humane.rtc.common.util.CorbaObj;
import jp.co.humane.rtc.common.util.ElapsedTimer;
import jp.co.humane.rtc.infoclerkmgr.InfoClerkManagerConfig;

/**
 * 動体検知中ステータスでの処理クラス。
 * @author terada.
 *
 */
public class MotionDetectProcessor extends StateProcessor {

    /**
     * 処理結果を表すENUM。
     * @author terada.
     */
    public enum Result {
        NOT_DETECT,
        DETECT,
        TIMEOUT
    }

    /** ロガー */
    private RtcLogger logger = new RtcLogger("InfoClerkManager");

    /** 動体検知結果の入力ポート */
    private RtcInPort<TimedBoolean> detectMotionIn = null;

    /** 動体検知開始通知用の出力ポート */
    private RtcOutPort<TimedLong> detectMotionStartOut = null;

    /** 設定情報 */
    private InfoClerkManagerConfig config = null;

    /** 経過時間タイマー */
    private ElapsedTimer timer = new ElapsedTimer();

    /**
     * コンストラクタ。
     * @param detectMotionIn       動体検知結果の入力ポート。
     * @param detectMotionStartOut 動体検知開始通知用の出力ポート。
     * @param config               設定情報。
     */
    public MotionDetectProcessor(RtcInPort<TimedBoolean> detectMotionIn,
                                  RtcOutPort<TimedLong> detectMotionStartOut,
                                  InfoClerkManagerConfig config) {
        this.detectMotionIn = detectMotionIn;
        this.detectMotionStartOut = detectMotionStartOut;
        this.config = config;
    }


    /**
     * 動体検知中ステータスでの処理。
     * タイムアウト時はTIMEOUT、動体検出できればDETECT、それ以外はNOT_DETECTを返す。
     * @param ec_id ExecutionContext ID.
     * @return  処理結果。
     */
    @Override
    public StateProcessResult onExecute(int ec_id) {

        // 検出継続時間を過ぎた場合はTIMEOUTを返す
        if (config.getMotionDetectTime() < timer.getElapsedTime(TimeUnit.SECONDS)) {
            logger.debug("指定時間" + config.getMotionDetectTime() + "秒を経過しても動体検出できませんでした。");
            // 再度動体検知の開始を準備する
            acceptPreResult(null);
            return new StateProcessResult(Result.TIMEOUT);
        }

        // データが入力ポートにない場合はNOT_DETECTを返す
        if (!detectMotionIn.isNew() || detectMotionIn.isEmpty()) {
            return new StateProcessResult(Result.NOT_DETECT);
        }

        // 入力データが取得できない場合はNOT_DETECTを返す
        TimedBoolean detectResult = detectMotionIn.readData();
        if (null == detectResult) {
            return new StateProcessResult(Result.NOT_DETECT);
        }

        // 取得できた場合はDETECTを返す
        logger.info("動体検知結果を受け取りました。");
        return new StateProcessResult(Result.DETECT);
    }


    /**
     * 前の処理から引き渡される情報を受け取る。
     * @param 無し。
     */
    @Override
    public void acceptPreResult(StateProcessResult result) {

        // 待機中に受け取ったデータは破棄する
        detectMotionIn.clear();

        // 動体検知の開始を通知する
        detectMotionStartOut.write(CorbaObj.newTimedLong(config.getMotionDetectTime()));
        logger.info("動体検知の開始指示を送信しました。");

        // タイマーのベースタイムを設定
        timer.setBaseTime();
    }


}
