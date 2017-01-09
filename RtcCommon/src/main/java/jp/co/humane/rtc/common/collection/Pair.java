package jp.co.humane.rtc.common.collection;

/**
 * 2要素を組にして扱うクラス。
 * jp.go.aist.rtm.RTC.util.Pairはequals()、hashCode()が定義されていないので作成。
 *
 * @author terada.
 *
 * @param <K> 格納する要素。
 * @param <V> 格納する要素。
 */
public class Pair<K, V> {

    /** 要素1 */
    private K key;

    /** 要素2 */
    private V value;

    /**
     * コンストラクタ。
     * @param key    要素1。
     * @param value  要素2。
     */
    public Pair(final K key, final V value) {

        this.key = key;
        this.value = value;
    }

    /**
     * 要素1を取得。
     * @return 要素1。
     */
    public K getKey() {

        return this.key;
    }

    /**
     * 要素2を取得。
     * @return 要素2
     */
    public V getValue() {

        return this.value;
    }

    /**
     * ハッシュコードを生成する。
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    /**
     * 要素が同じか確認する。
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pair other = (Pair) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
