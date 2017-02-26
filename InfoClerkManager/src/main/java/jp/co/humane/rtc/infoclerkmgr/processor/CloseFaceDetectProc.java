package jp.co.humane.rtc.infoclerkmgr.processor;

import java.util.concurrent.TimeUnit;

import RTC.TimedBoolean;
import RTC.TimedLong;
import jp.co.humane.rtc.common.component.state.StateProcessResult;
import jp.co.humane.rtc.common.component.state.StateProcessor;
import jp.co.humane.rtc.common.port.RtcInPort;
import jp.co.humane.rtc.common.port.RtcOutPort;
import jp.co.humane.rtc.common.util.CorbaObj;
import jp.co.humane.rtc.common.util.ElapsedTimer;
import jp.co.humane.rtc.infoclerkmgr.InfoClerkManagerConfig;
import jp.co.humane.rtc.infoclerkmgr.tool.ImageViewer;

/**
 * 画面クローズ用顔認識中ステータスでの処理クラス。
 * @author terada.
 *
 */
public class CloseFaceDetectProc extends StateProcessor {

    /**
     * 処理結果を表すENUM。
     * @author terada.
     */
    public enum Result {
        NOT_CLOSE,
        CLOSE,
        TIMEOUT
    }

    /** 顔検知結果の入力ポート */
    private RtcInPort<TimedBoolean> detectFaceResultIn = null;

    /** 顔検知開始通知用の出力ポート */
    private RtcOutPort<TimedLong> detectFaceStartOut = null;

    /** 設定情報 */
    private InfoClerkManagerConfig config = null;

    /** 経過時間タイマー */
    private ElapsedTimer timer = new ElapsedTimer();

    /** 顔検出実施回数 */
    private int faceDetectTryCount = 0;

    /** 連続顔非検出回数 */
    private int faceNotDetectCount = 0;

    /** イメージビューア */
    private ImageViewer viewer = new ImageViewer("Info Clerk");

    /**
     * コンストラクタ。
     * @param detectFaceResultIn 顔検知結果の入力ポート。
     * @param detectFaceStartOut 顔検知開始通知用の出力ポート。
     * @param config             設定情報。
     */
    public CloseFaceDetectProc(RtcInPort<TimedBoolean> detectFaceResultIn,
                                RtcOutPort<TimedLong> detectFaceStartOut,
                                InfoClerkManagerConfig config) {
        this.detectFaceResultIn = detectFaceResultIn;
        this.detectFaceStartOut = detectFaceStartOut;
        this.config = config;
    }

    /**
     * 非アクティブ時の処理。
     * ビューアを非表示にする。
     * @inheritDoc
     */
    @Override
    public boolean onDeactivated(int ec_id) {
        viewer.hide();
        return super.onDeactivated(ec_id);
    }

    /**
     * 画面クローズ用顔認識中ステータスでの処理。
     * タイムアウト時はTIMEOUT、顔が検出できればDETECT、それ以外はNOT_DETECTを返す。
     * @param ec_id ExecutionContext ID.
     * @return  処理結果。
     */
    @Override
    public StateProcessResult onExecute(int ec_id) {

        if (config.getCloseFaceInterval() < timer.getElapsedTime(TimeUnit.SECONDS)) {

            // インターバルを過ぎた場合はカウンタをインクリメント
            faceDetectTryCount++;
            faceNotDetectCount++;
            logger.debug("指定時間" + config.getCloseFaceInterval() + "秒を経過しても顔を検出できませんでした。[" + faceNotDetectCount + "回目]");

        } else {

            // データが入力ポートにない場合はNOT_CLOSEを返す
            if (!detectFaceResultIn.isNew() || detectFaceResultIn.isEmpty()) {
                return new StateProcessResult(Result.NOT_CLOSE);
            }

            // 入力データが取得できない場合はNOT_CLOSEを返す
            TimedBoolean detectResult = detectFaceResultIn.readData();
            if (null == detectResult) {
                return new StateProcessResult(Result.NOT_CLOSE);
            }

            // 顔認識できた場合はカウンタを更新
            logger.info("顔認識結果を受け取りました。");
            faceDetectTryCount++;
            faceNotDetectCount = 0;

            // インターバルが経過するまで待機
            timer.wait(TimeUnit.SECONDS, config.getCloseFaceInterval());

        }

        // 顔認識の連続失敗回数が閾値を超えた場合は画面をクローズ
        if (config.getCloseFaceThreshold() <= faceNotDetectCount) {
            logger.info(config.getCloseFaceThreshold() + "回連続して顔認識に失敗したため画面をクローズします。");
            viewer.hide();
            return new StateProcessResult(Result.CLOSE);
        }

        // 顔認識のリトライ回数を超えた場合は画面をクローズ
        if (config.getCloseFaceRetryOut() <= faceDetectTryCount) {
            logger.info(faceDetectTryCount + "回の顔認識処理を行ったため画面をクローズします。");
            viewer.hide();
            return new StateProcessResult(Result.TIMEOUT);
        }

        // 再度顔認識処理を試行する
        logger.debug("再度顔認識処理を実施します。");
        timer.setBaseTime();
        detectFaceStartOut.write(CorbaObj.newTimedLong(config.getCloseFaceInterval()));
        return new StateProcessResult(Result.NOT_CLOSE);

    }


    /**
     * 前の処理から引き渡される情報を受け取る。
     * @param 無し。
     */
    @Override
    public void acceptPreResult(StateProcessResult result) {

        // 待機中に受け取ったデータは破棄する
        detectFaceResultIn.clear();

        // 前回の検出状態をクリア
        faceDetectTryCount = 0;
        faceNotDetectCount = 0;

        // 指定パスのイメージを表示する
        String path = (String)result.getResultData();
        viewer.setImage(path);

        // 顔検知の開始を通知する
        detectFaceStartOut.write(CorbaObj.newTimedLong(config.getCloseFaceInterval()));
        logger.info("画面クローズ用顔検知の開始指示を送信しました。");

        // タイマーのベースタイムを設定
        timer.setBaseTime();
    }
}
