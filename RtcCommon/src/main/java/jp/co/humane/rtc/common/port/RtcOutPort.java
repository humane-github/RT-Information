package jp.co.humane.rtc.common.port;

import jp.go.aist.rtm.RTC.buffer.BufferBase;
import jp.go.aist.rtm.RTC.buffer.RingBuffer;
import jp.go.aist.rtm.RTC.port.OutPort;
import jp.go.aist.rtm.RTC.util.DataRef;

/**
 * DataTypeとDataRefを別に管理しなくて済むようにOutPortを拡張したクラス。
 * @author terada.
 *
 * @param <DataType> 送信データの型。
 */
public class RtcOutPort<DataType> extends OutPort<DataType> {

    /** DataRefインスタンスの一時格納場所 */
    private static ThreadLocal<DataRef> temporaryDataRef = new ThreadLocal<>();

    /** 送信データ格納情報 */
    private DataRef<DataType> dataRef = null;

    /**
     * DataRefインスタンスを一時的に格納する。
     *
     * @param dataRef DataRefインスタンス。
     * @return 引数で渡されたDataRefインスタンス。
     */
    @SuppressWarnings("unused")
    private static DataRef store(DataRef dataRef) {
        temporaryDataRef.set(dataRef);
        return dataRef;
    }

    /**
     * コンストラクタ。
     * 内部的にバッファが生成されて割り当てられる。
     *
     * @param name ポート名。
     * @param valueRef 本ポートにバインドするデータ変数を内包するDataRefオブジェクト。
     */
    public RtcOutPort(String name, DataType value) {

        // スーパークラスのコンストラクタを呼び出す
        super(name, store(new DataRef<>(value)));

        // 使用したDataRefをフィールドに配置
        this.dataRef = temporaryDataRef.get();
        temporaryDataRef.set(null);
    }

    /**
     * コンストラクタ。
     * 指定されたデータ長で内部的にバッファが生成されて割り当てられvyる。
     *
     * @param name ポート名。
     * @param valueRef 本ポートにバインドするデータ変数を内包するDataRefオブジェクト。
     * @param length バッファ長。
     */
    public RtcOutPort(String name, DataType value, int length) {

        // スーパークラスのコンストラクタを呼び出す
        super(new RingBuffer<DataType>(length), name, store(new DataRef<>(value)));

        // 使用したDataRefをフィールドに配置
        this.dataRef = temporaryDataRef.get();
        temporaryDataRef.set(null);
    }

    /**
     * コンストラクタ。
     * 指定されたバッファを割り当てる。
     *
     * @param buffer   割り当てるバッファ。
     * @param name     ポート名。
     * @param valueRef 本ポートにバインドするデータ変数を内包するDataRefオブジェクト
     */
    public RtcOutPort(BufferBase<DataType> buffer,
            final String name, DataType value) {

        // スーパークラスのコンストラクタを呼び出す
        super (buffer, name, new DataRef<>(value));

        // 使用したDataRefをフィールドに配置
        this.dataRef = temporaryDataRef.get();
        temporaryDataRef.set(null);
    }

    /**
     * 送信データを取得する。
     * @return 送信データ。
     */
    public DataType geData() {
        return this.dataRef.v;
    }

}
