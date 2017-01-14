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
 * 顔認識中ステータスでの処理クラス。
 * @author terada.
 *
 */
public class FaceDetectProcessor extends StateProcessor {

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

    /** 顔検知結果の入力ポート */
    private RtcInPort<TimedBoolean> detectFaceResultIn = null;

    /** 顔検知開始通知用の出力ポート */
    private RtcOutPort<TimedLong> detectFaceStartOut = null;

    /** 設定情報 */
    private InfoClerkManagerConfig config = null;

    /** 経過時間タイマー */
    private ElapsedTimer timer = new ElapsedTimer();

    /**
     * コンストラクタ。
     * @param detectFaceResultIn 顔検知結果の入力ポート。
     * @param detectFaceStartOut 顔検知開始通知用の出力ポート。
     * @param config             設定情報。
     */
    public FaceDetectProcessor(RtcInPort<TimedBoolean> detectFaceResultIn,
                                RtcOutPort<TimedLong> detectFaceStartOut,
                                InfoClerkManagerConfig config) {
        this.detectFaceResultIn = detectFaceResultIn;
        this.detectFaceStartOut = detectFaceStartOut;
        this.config = config;
    }

    /**
     * 顔認識中ステータスでの処理。
     * タイムアウト時はTIMEOUT、顔が検出できればDETECT、それ以外はNOT_DETECTを返す。
     * @param ec_id ExecutionContext ID.
     * @return  処理結果。
     */
    @Override
    public StateProcessResult onExecute(int ec_id) {

        // 検出継続時間を過ぎた場合はTIMEOUTを返す
        if (config.getMotionDetectTime() < timer.getElapsedTime(TimeUnit.SECONDS)) {
            logger.debug("指定時間" + config.getMotionDetectTime() + "秒を経過しても顔を検出できませんでした。");
            return new StateProcessResult(Result.TIMEOUT);
        }

        // データが入力ポートにない場合はNOT_DETECTを返す
        if (!detectFaceResultIn.isNew() || detectFaceResultIn.isEmpty()) {
            return new StateProcessResult(Result.NOT_DETECT);
        }

        // 入力データが取得できない場合はNOT_DETECTを返す
        TimedBoolean detectResult = detectFaceResultIn.readData();
        if (null == detectResult) {
            return new StateProcessResult(Result.NOT_DETECT);
        }

        // 取得できた場合はDETECTを返す
        logger.info("顔認検知果を受け取りました。");
        return new StateProcessResult(Result.DETECT);
    }


    /**
     * 前の処理から引き渡される情報を受け取る。
     * @param 無し。
     */
    @Override
    public void acceptPreResult(StateProcessResult result) {

        // 待機中に受け取ったデータは破棄する
        detectFaceResultIn.clear();

        // 顔検知の開始を通知する
        detectFaceStartOut.write(CorbaObj.newTimedLong(config.getFaceDetectTime()));
        logger.info("顔検知の開始指示を送信しました。");

        // タイマーのベースタイムを設定
        timer.setBaseTime();
    }


}
