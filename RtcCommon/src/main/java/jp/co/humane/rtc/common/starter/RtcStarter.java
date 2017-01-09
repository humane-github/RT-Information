package jp.co.humane.rtc.common.starter;
import jp.co.humane.rtc.common.component.DataFlowComponent;
import jp.co.humane.rtc.common.starter.bean.ConfigBase;
import jp.co.humane.rtc.common.starter.factory.RtcConfFactory;
import jp.co.humane.rtc.common.starter.factory.RtcDeleteFuncFactory;
import jp.co.humane.rtc.common.starter.factory.RtcNewFuncFactory;
import jp.co.humane.rtc.common.starter.factory.impl.BeanConfFactory;
import jp.co.humane.rtc.common.starter.factory.impl.BeanNewFuncFactory;
import jp.co.humane.rtc.common.starter.factory.impl.EmptyDeleteFuncFactory;
import jp.co.humane.rtc.common.starter.factory.impl.NormalNewFuncFactory;
import jp.co.humane.rtc.common.starter.factory.impl.StaticFieldConfFactory;
import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.ModuleInitProc;
import jp.go.aist.rtm.RTC.RTObject_impl;
import jp.go.aist.rtm.RTC.util.Properties;

/**
 * RTC開始クラス。
 * @author terada
 *
 */
public class RtcStarter {

    /** RTCマネージャ */
    protected Manager manager = null;

    /** RTC起動処理のファクトリクラス */
    protected RtcNewFuncFactory newFuncFactory = null;

    /** RTC終了処理のファクトリクラス */
    protected RtcDeleteFuncFactory deleteFuncFactory = null;

    /** RTCプロパティ生成処理のファクトリクラス */
    protected RtcConfFactory confFactory = null;

    /** コンポーネント名 */
    protected String componentName = null;

    /** ブロッキングモード */
    protected boolean isBlocking = true;

    /**
     * デフォルトコンストラクタ。
     * init以外で生成しないようにprotectedで定義。
     */
    protected RtcStarter() {
        super();
    }

    /**
     * RtcStarterの初期化処理。
     * @param args 起動引数。
     * @return インスタンス。
     */
    public static RtcStarter init(String[] args) {
        RtcStarter rtcStarter = new RtcStarter();
        rtcStarter.setManager(Manager.init(args));
        return rtcStarter;
    }

    /**
     * Rtc起動処理のコンフィグ設定。
     * @param bean      設定情報が定義されたクラス。
     * @param addParams 追加設定情報。
     * @return インスタンス。
     */
    public RtcStarter setConfig(Object bean, String ... addParams) {

        // 指定されたBeanから設定情報を生成する。
        RtcConfFactory cfgFactory = new BeanConfFactory(bean, addParams);
        setConfFactory(cfgFactory);
        return this;
    }

    /**
     * RTCを起動する。
     * @param args  起動引数。
     * @param clazz 起動対象のRTCクラス。
     */
    public void start(final Class<? extends RTObject_impl> clazz) {

        // コンポーネント名を決定
        final String name = getComponentName(clazz);

        // 各種ファクトリクラスを取得
        final RtcNewFuncFactory newFactory = getNewFuncFactory(clazz);
        final RtcDeleteFuncFactory delFancFactory = getDeleteFuncFactory(clazz);
        final RtcConfFactory cfgFactory = getConfFactory(clazz);

        // Bean定義を使用する場合はコンポーネント名を設定する。
        if (cfgFactory instanceof BeanConfFactory) {
            ((BeanConfFactory)cfgFactory).setComponentName(name);
        }
        if (newFactory instanceof BeanNewFuncFactory) {
            ((BeanNewFuncFactory)newFactory).setComponentName(name);
        }

        // モジュール初期化処理設定する
        manager.setModuleInitProc(new ModuleInitProc() {

            @Override
            public void myModuleInit(Manager mgr) {

                // 設定情報を格納
                Properties prop = cfgFactory.create(mgr);
                mgr.registerFactory(prop, newFactory.create(mgr), delFancFactory.create(mgr));

                // Create a component
                RTObject_impl comp = mgr.createComponent(name);
                if( comp==null ) {
                    System.err.println("Component(" + name + ") create failed.");
                    System.exit(0);
                }
            }
        });

        // マネージャをアクティブにしてコンポーネントをネーミングサービスに登録
        manager.activateManager();

        // マネージャを起動
        manager.runManager(!isBlocking);
    }

    /**
     * RTC生成ファクトリクラスを返す。
     * @param 起動対象のRTCクラス。
     * @return RTC生成ファクトリクラス。
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected RtcNewFuncFactory getNewFuncFactory(Class<? extends RTObject_impl> clazz) {

        // 指定されている場合は指定されたファクトリクラスを使用する
        if (null != newFuncFactory) {
            return newFuncFactory;
        }

        // 以下の条件を満たしている場合はBeanNewFuncFactoryを使用する
        //   1. 設定情報にBeanが渡されている
        //   2. BeanがConfigBaseを継承している
        //   3. clazzがDataFlowComponentを継承している
        if (confFactory instanceof BeanConfFactory) {
            Object bean = ((BeanConfFactory) confFactory).getBean();
            if (bean instanceof ConfigBase
                    && DataFlowComponent.class.isAssignableFrom(clazz)) {

                ConfigBase configBase = (ConfigBase) bean;
                Class<? extends DataFlowComponent> cls = (Class<? extends DataFlowComponent>) clazz;
                BeanNewFuncFactory newFactory = new BeanNewFuncFactory(cls, configBase);
                return newFactory;
            }
        }

        // 上記以外はNormalNewFuncFactoryを使用する
        return new NormalNewFuncFactory(clazz);
    }

    /**
     * RTC終了ファクトリクラスを返す。
     * @param 終了対象のRTCクラス。
     * @return RTC生成ファクトリクラス。
     */
    protected RtcDeleteFuncFactory getDeleteFuncFactory(Class<? extends RTObject_impl> clazz) {

        // 指定されていない場合はデフォルトのファクトリクラスを返す
        RtcDeleteFuncFactory ret = deleteFuncFactory;
        if (null == ret) {
            ret = new EmptyDeleteFuncFactory();
        }
        return ret;
    }

    /**
     * RTC設定情報のファクトリクラスを返す。
     * @param 設定情報が定義されたRTCクラス。
     * @return RTC設定情報のファクトリクラス。
     */
    protected RtcConfFactory getConfFactory(Class<?> clazz) {

        // 指定されていない場合はデフォルトのファクトリクラスを返す
        RtcConfFactory ret = confFactory;
        if (null == ret) {
            ret = new StaticFieldConfFactory(clazz);
        }

        return ret;
    }

    /**
     * コンポーネント名を取得する。
     * @param  設定情報が定義されたRTCクラス。
     */
    protected String getComponentName(Class<?> clazz) {

        String ret = componentName;

        // 指定されていない場合はクラス名(Implは除く)をコンポーネント名に指定する
        if (null == ret) {
            String[] arr = clazz.getCanonicalName().split("\\.");
            String name = arr[arr.length - 1];
            ret = name.replace("Impl", "");
        }

        return ret;
    }


    // 以下、アクセッサ

    /**
     * RTCマネージャを取得する。
     * @return RTCマネージャ。
     */
    public Manager getManager() {
        return manager;
    }


    /**
     * RTCマネージャを設定する。
     * @param manager RTCマネージャ。
     */
    protected RtcStarter setManager(Manager manager) {
        this.manager = manager;
        return this;
    }

    /**
     * RTC起動処理のファクトリクラスを取得する。
     * @return RTC起動処理のファクトリクラス。
     */
    public RtcNewFuncFactory getNewFuncFactory() {
        return newFuncFactory;
    }

    /**
     * RTC起動処理のファクトリクラスを設定する。
     * @param newFuncFactory RTC起動処理のファクトリクラス。
     */
    public RtcStarter setNewFuncFactory(RtcNewFuncFactory newFuncFactory) {
        this.newFuncFactory = newFuncFactory;
        return this;
    }

    /**
     * RTC終了処理のファクトリクラスを取得する。
     * @return RTC終了処理のファクトリクラス。
     */
    public RtcDeleteFuncFactory getDeleteFuncFactory() {
        return deleteFuncFactory;
    }

    /**
     * RTC終了処理のファクトリクラスを設定する。
     * @param deleteFuncFactory RTC終了処理のファクトリクラス。
     */
    public RtcStarter setDeleteFuncFactory(RtcDeleteFuncFactory deleteFuncFactory) {
        this.deleteFuncFactory = deleteFuncFactory;
        return this;
    }

    /**
     * RTCプロパティ生成処理のファクトリクラスを取得する。
     * @return RTCプロパティ生成処理のファクトリクラス。
     */
    public RtcConfFactory getConfFactory() {
        return confFactory;
    }

    /**
     * RTCプロパティ生成処理のファクトリクラスを設定する。
     * @param confFactory RTCプロパティ生成処理のファクトリクラス。
     */
    public RtcStarter setConfFactory(RtcConfFactory confFactory) {
        this.confFactory = confFactory;
        return this;
    }

    /**
     * コンポーネント名を取得する。
     * @return コンポーネント名。
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * コンポーネント名を設定する。
     * @param componentName コンポーネント名。
     */
    public RtcStarter setComponentName(String componentName) {
        this.componentName = componentName;
        return this;
    }

    /**
     * ブロッキングモードの設定を取得。
     * @return ブロッキングモードの有無。
     */
    public boolean isBlocking() {
        return isBlocking;
    }

    /**
     * ブロッキングモードを設定する。
     * @param isBlocking ブロッキングモード。
     */
    public RtcStarter setBlocking(boolean isBlocking) {
        this.isBlocking = isBlocking;
        return this;
    }
}
