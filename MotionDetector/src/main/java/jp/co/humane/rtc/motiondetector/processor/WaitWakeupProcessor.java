package jp.co.humane.rtc.motiondetector.processor;

import RTC.TimedLong;
import jp.co.humane.rtc.common.component.state.StateProcessResult;
import jp.co.humane.rtc.common.component.state.StateProcessor;
import jp.co.humane.rtc.common.port.RtcInPort;

/**
 * 待機中ステータスでの処理クラス。
 * @author terada.
 *
 */
public class WaitWakeupProcessor extends StateProcessor {

    /**
     * 処理結果を表すENUM。
     * @author terada.
     */
    public enum Result {
        NOT_RECEIVE,
        RECEIVE
    }

    /** 顔検出の開始指示入力ポート */
    private RtcInPort<TimedLong> wakeupIn = null;

    /**
     * コンストラクタ。
     * @param wakeupIn 入力ポート。
     */
    public WaitWakeupProcessor(RtcInPort<TimedLong> wakeupIn) {
        this.wakeupIn = wakeupIn;
    }

    /**
     * 検出中ステータスの処理。
     * 入力があればRECEIVE、入力がなければNOT_RECEIVEを返す。
     *
     * @param ec_id ExecutionContext ID.
     * @return  処理結果。
     */
    @Override
    public StateProcessResult onExecute(int ec_id) {

        // ポートにデータが入力されていない場合はNOT_RECEIVEを返す
        if (!wakeupIn.isNew()) {
            return new StateProcessResult(Result.NOT_RECEIVE);
        }

        // ポートから読み取ったデータがnullの場合はNOT_RECEIVEを返す
        TimedLong receiveData = wakeupIn.readData();
        if (null == receiveData) {
            return new StateProcessResult(Result.NOT_RECEIVE);
        }

        // ポートにデータが入力されている場合は検出時間を渡してRECEIVEを返す
        return new StateProcessResult(Result.RECEIVE,
                                  Integer.valueOf(receiveData.data));
    }
}
