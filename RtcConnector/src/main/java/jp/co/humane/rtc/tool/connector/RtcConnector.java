package jp.co.humane.rtc.tool.connector;

import java.util.Map;
import java.util.TreeMap;

import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingIteratorHolder;
import org.omg.CosNaming.BindingListHolder;
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import RTC.RTObject;
import RTC.RTObjectHelper;
import jp.co.humane.rtc.tool.connector.bean.ObjRefHolder;
import jp.go.aist.rtm.RTC.CorbaNaming;
import jp.go.aist.rtm.RTC.util.ORBUtil;

/**
 * 設定ファイルをもとにRTCの接続を行う。
 * @author terada
 *
 */
public class RtcConnector {

    /** オブジェクト参照の最大値 */
    private static final int MAX_BINDING_SIZE = 100;

    /** RTCのIDL */
    private static final String RTC_IDL = "IDL:omg.org/RTC/RTObject:1.0";

    /** orb */
    private ORB orb = null;

    /** ネーミングサービス */
    private CorbaNaming corbaNaming = null;

    /** オブジェクト参照のマップ */
    private Map<String, ObjRefHolder> allObjMap = null;

    /**
     * コンストラクタ。
     * @throws Exception 接続に失敗した場合に発生。
     */
    public RtcConnector(String server, String port, String ... corbaArgs) throws Exception {
        orb = ORBUtil.getOrb(corbaArgs);
        corbaNaming = new CorbaNaming(orb, server + ":" + port);
    }

    // http://www.wakhok.ac.jp/~tatsuo/kougi98/20shuu/PrintBinding.java.html


    public void connect() {

        String[] args = new String[0];

        try {
            //CORBA ORBオブジェクトを生成
            orb = ORBUtil.getOrb(args);
            corbaNaming = new CorbaNaming(orb, "localhost:2809");

            getAllObjMap();


            // 登録されているオブジェクトの一覧を取得
            BindingListHolder bindings = new BindingListHolder();
            BindingIteratorHolder itr = new BindingIteratorHolder();
            corbaNaming.list(corbaNaming.getRootContext(), 30, bindings, itr);

            String id = null;
            String kind = null;
            for(Binding b : bindings.value) {

                NameComponent comp = b.binding_name[0];
                id = comp.id;
                kind = comp.kind;
                System.out.println(id + "." + kind);

                if (BindingType.ncontext == b.binding_type) {

                    org.omg.CORBA.Object obj = corbaNaming.resolve(b.binding_name);
                    NamingContext namingContext = NamingContextHelper.narrow(obj);

                    BindingListHolder bindings2 = new BindingListHolder();
                    BindingIteratorHolder itr2 = new BindingIteratorHolder();
                    namingContext.list(30, bindings2, itr2);

                    for (Binding b2 : bindings2.value) {
                        System.out.println(b2.binding_name[0].id + "." + b2.binding_name[0].kind);
                    }

                }

            }

            org.omg.CORBA.Object object = corbaNaming.resolve(".host_cxt/SendString0.rtc");
            org.omg.CORBA.Object object2 = corbaNaming.resolve(".host_cxt/MotionDetector0.rtc");

            // 登録有無を確認
            object._non_existent();

            // narrow前に確認
            object._is_a("IDL:omg.org/RTC/RTObject:1.0");

            RTObject rto = RTObjectHelper.narrow(object);
//            RTObject rto2 = RTObjectHelper.narrow(object2);

            // つなげない場合
//            corbaNaming.unbind(".host_cxt/CameraDevice0.rtc");

//            org.omg.CORBA.Object object = corbaNaming.resolve(id + "|" + kind);
//            RTObject rtObject = RTObjectHelper.narrow(object);
//            rtObject.get_owned_contexts()[0].con


            orb.destroy();

//            org.omg.CORBA.Object object = corbaNaming.resolve("siva.local.host_cxt/ConsoleOutput0.rtc");
//            CorbaConsumer<RTObject> periodicConsoleOut0 = new CorbaConsumer<RTObject>(RTObject.class);
//            periodicConsoleOut0.setObject(object);
//
//            RTC.ExecutionContext[] executionContextList = periodicConsoleOut0._ptr().get_owned_contexts();
//            executionContextList[0].activate_component(periodicConsoleOut0._ptr());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void activate(String name) {

        RTObject obj = getRTObject(name);
        if (null == obj) {
            System.out.println(name + "は存在しません");
            return;
        }

        obj.get_owned_contexts()[0].activate_component(obj);
    }

    /**
     * オブジェクト参照のマップを更新する。
     */
    public void updateObjMap() {

        // 取得済みの情報があれば解放する
        if (null != allObjMap) {
            for (ObjRefHolder orh : allObjMap.values()) {
                orh.getObject()._release();
            }
        }

        // 最新のマップを取得する
        allObjMap = getAllObjMap();
    }

    /**
     * すべてのオブジェクト参照をマップとして取得する。
     * @return オブジェクト参照マップ。
     */
    protected Map<String, ObjRefHolder> getAllObjMap() {

        Map<String, ObjRefHolder> map = new TreeMap<>();

        // 登録されているオブジェクトの一覧を取得
        for (Binding b: getBindings(corbaNaming)) {
            recursiveGetObjRef("", b, map);
        }
        return map;
    }

    /**
     * 再帰的にオブジェクト参照を取得し、マップに登録する。
     * @param directory ディレクトリ。
     * @param binding   バインド情報。
     * @param map       マップ。
     */
    protected void recursiveGetObjRef(String directory, Binding binding, Map<String, ObjRefHolder> map) {

        // 対象としているオブジェクト参照のidとkindからパスを取得
        NameComponent nc = binding.binding_name[0];
        String path = directory + nc.id + "." + nc.kind;

        // オブジェクト参照を取得
        org.omg.CORBA.Object obj = null;
        try {
            obj = corbaNaming.resolve(path);
        } catch (InvalidName | SystemException | NotFound | CannotProceed ex) {
            return;
        }

        // ネーミングコンテキストの場合は配下の情報を取得
        if (BindingType.ncontext == binding.binding_type) {

            NamingContext context = NamingContextHelper.narrow(obj);
            for (Binding b : getBindings(context)) {
                recursiveGetObjRef(path + "/", b, map);
            }
            return;
        }

        // RTCとして管理対象の場合はリストに追加する
        if (isValid(obj, path)) {
            RTObject rtc = RTObjectHelper.narrow(obj);
            map.put(nc.id, new ObjRefHolder(directory, rtc, nc.id, nc.kind));
        }

        // 対象外の場合は解放
        obj._release();
    }

    /**
     * RTCの管理対象として有効か否かを判定する。
     * 過去の削除し忘れのオブジェクトは削除する。
     *
     * @param obj  オブジェクト参照。
     * @param path パス。
     * @return 判定結果。
     */
    protected boolean isValid(org.omg.CORBA.Object obj, String path) {

        try {
            // 前回の削除漏れの場合はfalseを返す
            if (obj._non_existent()) {
                corbaNaming.unbind(path);
                return false;
            }

            // RTCではない場合はfalseを返す
            if (!obj._is_a(RTC_IDL)) {
                return false;
            }
        } catch (Exception ex) {
            // 接続失敗時はfalseを返す
            return false;
        }
        return true;
    }

    /**
     * CorbaNamingからバインド情報を取得する。
     * @param naming CorbaNamin.
     * @return バインド情報。
     */
    protected Binding[] getBindings(CorbaNaming naming) {
        BindingListHolder bindings = new BindingListHolder();
        BindingIteratorHolder itr = new BindingIteratorHolder();
        naming.list(naming.getRootContext(), MAX_BINDING_SIZE, bindings, itr);
        return bindings.value;
    }

    /**
     * NamingContextgからバインド情報を取得する。
     * @param naming NamingContext.
     * @return バインド情報。
     */
    protected Binding[] getBindings(NamingContext naming) {
        BindingListHolder bindings = new BindingListHolder();
        BindingIteratorHolder itr = new BindingIteratorHolder();
        naming.list(MAX_BINDING_SIZE, bindings, itr);
        return bindings.value;
    }


    /**
     * 名前(id)に対応するオブジェクト参照を返す。
     * @param name 名前(id)
     * @return オブジェクト参照。
     */
    protected RTObject getRTObject(String name) {

        // マップが無いまたはマップに名前が存在しない場合は更新を行う
        if (null == allObjMap || allObjMap.containsKey(name)) {
            updateObjMap();
        }

        // マップから対応するオブジェクト参照を返す
        ObjRefHolder orh = allObjMap.get(name);
        if (null != orh) {
            return orh.getObject();
        }

        // 存在しない場合はnullを返す
        return null;
    }

    public static void main(String[] args) throws Exception {
        new RtcConnector("localhost", "2809").activate("SendString");
    }

}
