package jp.co.humane.rtc.common.component.state;

/**
 * 特定の状態に対する処理を定義するクラス。
 * @author terada.
 *
 */
public abstract class StateProcessor {

    /**
     * アクティブ化時の処理を実行する。
     *
     * @param ec_id ExecutionContext ID.
     * @return  処理結果。true:正常終了, false:異常終了。
     */
    public boolean onActivated(int ec_id) {
        return true;
    }

    /**
     * 非アクティブ化時の処理を実行する。
     *
     * @param ec_id ExecutionContext ID.
     * @return  処理結果。true:正常終了, false:異常終了。
     */
    public boolean onDeactivated(int ec_id) {
        return true;
    }

    /**
     * リセット時の処理を実行する。
     *
     * @param ec_id ExecutionContext ID.
     * @return  処理結果。true:正常終了, false:異常終了。
     */
    public boolean onReset(int ec_id) {
        return true;
    }

    /**
     * 処理を実行する。
     * @param ec_id ExecutionContext ID.
     * @return  処理結果。
     */
    public abstract StateProcessResult onExecute(int ec_id);

    /**
     * 前の処理から引き渡される情報を受け取る。
     * @param data 前の処理から引き渡される情報。
     */
    public void acceptPreResult(StateProcessResult result) {
        return;
    }

}
