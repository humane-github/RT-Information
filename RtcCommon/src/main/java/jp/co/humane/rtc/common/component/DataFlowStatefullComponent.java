package jp.co.humane.rtc.common.component;

import java.util.HashMap;
import java.util.Map;

import RTC.ReturnCode_t;
import jp.co.humane.rtc.common.collection.Pair;
import jp.co.humane.rtc.common.component.state.StateProcessResult;
import jp.co.humane.rtc.common.component.state.StateProcessor;
import jp.co.humane.rtc.common.starter.bean.ConfigBase;
import jp.go.aist.rtm.RTC.Manager;

/**
 * 状態に応じて処理を変えるRTCのスーパークラス。
 *
 * @author terada.
 * @param <T> 設定情報が記載されたBean。
 */
public abstract class DataFlowStatefullComponent<T extends ConfigBase> extends DataFlowComponent<T> {

    /** 現在の状態を表すEnum */
    protected Enum<?> state = null;

    /** 現在状態、処理インスタンスのマップ */
    protected Map<Enum<?>, StateProcessor> stateProcMap = new HashMap<>();

    /** 現在状態、処理結果、遷移先状態のマップ */
    protected Map<Pair<Enum<?>, Enum<?>>, Enum<?>> stateMoveMap = new HashMap<>();

    /**
     * コンストラクタ。
     * @param manager RTCマネージャ。
     */
    public DataFlowStatefullComponent(Manager manager) {
        super(manager);
    }

    /**
     * アクティブ化処理。
     * @param ec_id ExecutionContext ID.
     * @return リターンコード。
     */
    @Override
    protected ReturnCode_t onRtcActivated(int ec_id) {
        boolean isSuccess = true;
        for (StateProcessor sp : stateProcMap.values()) {
            isSuccess &= sp.onActivated(ec_id);
        }
        return isSuccess ? ReturnCode_t.RTC_OK : ReturnCode_t.RTC_ERROR;
    }

    /**
     * 非アクティブ化処理。
     * @param ec_id ExecutionContext ID.
     * @return リターンコード。
     */
    @Override
    protected ReturnCode_t onRtcDeactivated(int ec_id) {
        boolean isSuccess = true;
        for (StateProcessor sp : stateProcMap.values()) {
            isSuccess &= sp.onDeactivated(ec_id);
        }
        return isSuccess ? ReturnCode_t.RTC_OK : ReturnCode_t.RTC_ERROR;
    }

    /**
     * リセット時処理。
     * @param ec_id ExecutionContext ID.
     * @return リターンコード。
     */
    @Override
    protected ReturnCode_t onRtcReset(int ec_id) {
        boolean isSuccess = true;
        for (StateProcessor sp : stateProcMap.values()) {
            isSuccess &= sp.onReset(ec_id);
        }
        return isSuccess ? ReturnCode_t.RTC_OK : ReturnCode_t.RTC_ERROR;
    }

    /**
     * 定期実行処理。
     * @param ec_id ExecutionContext ID.
     * @return リターンコード。
     */
    @Override
    protected ReturnCode_t onRtcExecute(int ec_id) {

        Enum<?> currentState = state;

        // 状態に応じた処理クラスに処理をさせる
        StateProcessor processor = getProcessorByState(currentState);
        StateProcessResult result = processor.onExecute(ec_id);

        // 処理結果の判定を行う
        if (isInvalidResult(result)) {
            return ReturnCode_t.RTC_ERROR;
        }

        // 現在の状態と処理結果から次の処理結果を取得
        Enum<?> nextState = getMoveToStatus(currentState, result);
        if (!currentState.equals(nextState)) {
            logger.info("状態が" + currentState.name() + "から" + nextState.name() + "に変わりました。");
        }

        // 処理内容が変わる場合は引継ぎを行う
        StateProcessor nextProcessor = getProcessorByState(nextState);
        if (processor != nextProcessor) {
            nextProcessor.acceptPreResult(result);
        }

        state = nextState;
        return ReturnCode_t.RTC_OK;
    }

    /**
     * 現在状態、処理結果、遷移先状態のマップに情報を追加する。
     *
     * @param currentState  現在の状態。
     * @param processResult 処理結果。
     * @param moveToState   遷移先の状態
     */
    protected void addStateMoveMap(Enum<?> currentState, Enum<?> processResult, Enum<?> moveToState) {
        stateMoveMap.put(new Pair<Enum<?>, Enum<?>>(currentState, processResult), moveToState);
    }

    /**
     * 処理結果が不正か否かを判定する。
     *
     * @param result 状態に応じた処理結果。
     * @return 不正か否か。true：不正, false：正常。
     */
    protected boolean isInvalidResult(StateProcessResult result) {
        return false;
    }

    /**
     * 指定された状態に対応する処理を返す。
     *
     * @param state 状態。
     * @return 状態に応じた処理。
     */
    protected StateProcessor getProcessorByState(Enum<?> state) {

        // マップから指定の状態に対応する処理を返す
        StateProcessor processor = stateProcMap.get(state);

        // 登録されていない場合は例外をスローする
        if (null == processor) {
            throw new RuntimeException("想定しない状態が検出されました。状態：" + state.name());
        }

        return processor;
    }

    /**
     * 現在の状態とその状態に対する処理の結果をもとに次の遷移先状態を判定する。
     *
     * @param currentState 現在の状態。
     * @param procResult   処理結果。
     * @return 次の遷移先状態。
     */
    protected Enum<?> getMoveToStatus(Enum<?> currentState, StateProcessResult procResult) {

        // マップから対応する遷移先を取得して返す
        Enum<?> resultEnum = procResult.getResultEnum();
        Enum<?> nextState = stateMoveMap.get(new Pair<Enum<?>, Enum<?>>(currentState, procResult.getResultEnum()));

        // 登録されていない組み合わせの場合は例外をスローする
        if (null == nextState) {
            throw new RuntimeException("想定しない状態が検出されました。"
                    + "状態：" + currentState.name() + "、処理結果：" + resultEnum.name());
        }

        return nextState;
    }

}
