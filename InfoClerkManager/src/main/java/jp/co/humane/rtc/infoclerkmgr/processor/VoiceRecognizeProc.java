package jp.co.humane.rtc.infoclerkmgr.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import RTC.TimedString;
import RTC.TimedWString;
import jp.co.humane.rtc.common.component.state.StateProcessResult;
import jp.co.humane.rtc.common.component.state.StateProcessor;
import jp.co.humane.rtc.common.port.RtcInPort;
import jp.co.humane.rtc.common.port.RtcOutPort;
import jp.co.humane.rtc.common.util.ElapsedTimer;
import jp.co.humane.rtc.infoclerkmgr.InfoClerkManagerConfig;

/**
 * 音声認識中ステータスでの処理クラス。
 * @author terada.
 *
 */
public class VoiceRecognizeProc extends StateProcessor {

    /**
     * 処理結果を表すENUM。
     * @author terada.
     */
    public enum Result {
        NOT_RECOGNIZE,
        RECOGNIZE,
        TIMEOUT
    }

    /** CSV区切り文字 */
    private static final String CSV_SEPARATOR = ",";

    /** CSVの文字コード */
    private static final String CSV_CHARSET = "UTF-8";

    /** 音声認識結果の入力ポート */
    private RtcInPort<TimedWString> voiceTexResulttIn = null;

    /** 音声合成用の出力ポート */
    private RtcOutPort<TimedString> voiceTextOut = null;

    /** 設定情報 */
    private InfoClerkManagerConfig config = null;

    /** 経過時間タイマー */
    private ElapsedTimer timer = new ElapsedTimer();

    /** キーワード・地図イメージパスのマップ */
    private Map<String, String> imageMap = null;

    /**
     * コンストラクタ。
     * @param voiceTexResulttIn 音声認識結果の入力ポート。
     * @param voiceTextOut      音声合成用の出力ポート。
     */
    public VoiceRecognizeProc(RtcInPort<TimedWString> voiceTexResulttIn,
                                   RtcOutPort<TimedString> voiceTextOut,
                                   InfoClerkManagerConfig config) {
        this.voiceTexResulttIn = voiceTexResulttIn;
        this.voiceTextOut = voiceTextOut;
        this.config = config;
    }


    /**
     * アクティブ時の処理。
     * CSVファイルを読み込みなおす。
     * @inheritDoc
     */
    @Override
    public boolean onActivated(int ec_id) {
        imageMap = new HashMap<>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(config.getMapCsvPath()), CSV_CHARSET);
            while (scanner.hasNext()) {
                String[] data = scanner.nextLine().split(CSV_SEPARATOR);
                imageMap.put(data[0], data[1]);
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("地図のCSVファイル読み込みに失敗しました。", ex);
        } finally {
            if (null != scanner) {
                scanner.close();
            }
        }
        return true;
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

        logger.info("音声認識結果[" + voiceText.data + "]を受け取りました。");

        // 話された内容に場所が含まれている場合は対応するイメージファイルを取得
        String imagePath = null;
        for (String keyword : imageMap.keySet()) {
            if(-1 != voiceText.data.indexOf(keyword)) {
                imagePath = imageMap.get(keyword);
                logger.debug("[" + keyword + "]を検出しました。" + imagePath + "を表示します。");
                break;
            }
        }

        // 処理が正常終了した場合はRECOGNIZE、それ以外はNOT_RECOGNIZE
        if (null != imagePath) {
            return new StateProcessResult(Result.RECOGNIZE, imagePath);
        } else {
            return new StateProcessResult(Result.NOT_RECOGNIZE);
        }
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
