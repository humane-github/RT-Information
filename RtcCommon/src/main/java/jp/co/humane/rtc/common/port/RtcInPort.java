package jp.co.humane.rtc.common.port;

import jp.go.aist.rtm.RTC.buffer.BufferBase;
import jp.go.aist.rtm.RTC.buffer.RingBuffer;
import jp.go.aist.rtm.RTC.port.InPort;
import jp.go.aist.rtm.RTC.util.DataRef;

/**
 * DataTypeとDataRefを別に管理しなくて済むようにInPortを拡張したクラス。
 * @author terada.
 *
 * @param <DataType> 受信データの型。
 */
public class RtcInPort<DataType> extends InPort<DataType> {

    /** DataRefインスタンスの一時格納場所 */
    private static ThreadLocal<DataRef> temporaryDataRef = new ThreadLocal<>();

    /** 受信データ格納情報 */
    private DataRef<DataType> dataRef = null;

    /**
     * DataRefインスタンスを一時的に格納する。
     *
     * @param dataRef DataRefインスタンス。
     * @return 引数で渡されたDataRefインスタンス。
     */
    @SuppressWarnings("rawtypes")
    private static DataRef store(DataRef dataRef) {
        temporaryDataRef.set(dataRef);
        return dataRef;
    }

    /**
     * コンストラクタ。
     *
     * @param superClass データ格納バッファ
     * @param name ポート名称
     * @param value このポートにバインドされるDataType型の変数
     * @param read_block データ読み込み時に未読データがない場合に、データ受信までブロックする場合はtrue、さもなくばfalse
     * @param write_block データ書き込み時にバッファがフルであった場合に、バッファに空きができるまでブロック場合はtrue、さもなくばfalse
     * @param read_timeout 非ブロック指定の場合の、データ読み取りのタイムアウト時間 (ミリ秒)
     * @param write_timeout 非ブロック指定の場合の、データ書き込みのタイムアウト時間 (ミリ秒)
     */
    @SuppressWarnings("unchecked")
    public RtcInPort(BufferBase<DataType> superClass,
            String name, DataType value,
            boolean read_block, boolean write_block,
            long read_timeout, long write_timeout) {

        // スーパークラスのコンストラクタを呼び出す
        super(superClass, name, store(new DataRef<DataType>(value)), read_block, write_block, read_timeout, write_timeout);

        // 使用したDataRefをフィールドに配置
        this.dataRef = temporaryDataRef.get();
        temporaryDataRef.set(null);
    }

    /**
     * コンストラクタ。
     *
     * @param name ポート名称
     * @param value このポートにバインドされるDataType型の変数
     * @param read_block データ読み込み時に未読データがない場合に、データ受信までブロックする場合はtrue、さもなくばfalse
     * @param read_timeout 非ブロック指定の場合の、データ読み取りのタイムアウト時間 (ミリ秒)
     */
    @SuppressWarnings("unchecked")
    public RtcInPort(String name, DataType value,
            boolean read_block, long read_timeout) {

        // スーパークラスのコンストラクタを呼び出す
        super(new RingBuffer<DataType>(8), name, store(new DataRef<>(value)), read_block, false, read_timeout, 0L);

        // 使用したDataRefをフィールドに配置
        this.dataRef = temporaryDataRef.get();
        temporaryDataRef.set(null);
    }


    /**
     * コンストラクタ。
     * 読み取り・書き込みともに非ブロックモードとなり、タイムアウト時間は0で設定されます。</p>
     *
     * @param name ポート名称
     * @param value このポートにバインドされるDataType型の変数
     */
    @SuppressWarnings("unchecked")
    public RtcInPort(String name, DataType value) {

        // スーパークラスのコンストラクタを呼び出す
        super(name, store(new DataRef<>(value)));

        // 使用したDataRefをフィールドに配置
        this.dataRef = temporaryDataRef.get();
        temporaryDataRef.set(null);
    }


    /**
     * {@.ja DataPort から値を読み出す}
     * {@.en Readout the value from DataPort}
     * <p>
     * {@.ja InPortに書き込まれたデータを読みだす。接続数が0、またはバッファに
     * データが書き込まれていない状態で読みだした場合の戻り値は不定である。
     * バッファが空の状態のとき、
     * 事前に設定されたモード (readback, do_nothing, block) に応じて、
     * 以下のような動作をする。
     *
     * - readback: 最後の値を読みなおす。
     *
     * - do_nothing: 何もしない
     *
     * - block: ブロックする。タイムアウトが設定されている場合は、
     *       タイムアウトするまで待つ。
     *
     * バッファが空の状態では、InPortにバインドされた変数の値が返される。
     * したがって、初回読み出し時には不定値を返す可能性がある。
     * この関数を利用する際には、
     *
     * - isNew(), isEmpty() と併用し、事前にバッファ状態をチェックする。
     *
     * - 初回読み出し時に不定値を返さないようにバインド変数を事前に初期化する
     *
     * - ReturnCode read(DataType& data) 関数の利用を検討する。
     *
     * ことが望ましい。
     *
     * 各コールバック関数は以下のように呼び出される。
     * - OnRead: read() 関数が呼ばれる際に必ず呼ばれる。
     *
     * - OnReadConvert: データの読み出しが成功した場合、読みだしたデータを
     *       引数としてOnReadConvertが呼び出され、戻り値をread()が戻り値
     *       として返す。
     *
     * - OnEmpty: バッファが空のためデータの読み出しに失敗した場合呼び出される。
     *        OnEmpty の戻り値を read() の戻り値として返す。
     *
     * - OnBufferTimeout: データフロー型がPush型の場合に、読み出し
     *        タイムアウトのためにデータの読み出しに失敗した場合に呼ばれる。
     *
     * - OnRecvTimeout: データフロー型がPull型の場合に、読み出しタイムアウト
     *        のためにデータ読み出しに失敗した場合に呼ばれる。
     *
     * - OnReadError: 上記以外の理由で読みだしに失敗した場合に呼ばれる。
     *        理由としては、バッファ設定の不整合、例外の発生などが考えられる
     *        が通常は起こりえないためバグの可能性がある。}
     * </p>
     * @return
     *   {@.ja 読み出し結果(読み出し成功:受信データ, 読み出し失敗:null)}
     *
     */
    public DataType readData() {
        boolean isSuccess = super.read();
        if (!isSuccess) {
            return null;
        } else {
            return dataRef.v;
        }
    }

    /**
     * 受信データを取得する。
     * @return 受信データ。
     */
    public DataType getData() {
        return dataRef.v;
    }

}
