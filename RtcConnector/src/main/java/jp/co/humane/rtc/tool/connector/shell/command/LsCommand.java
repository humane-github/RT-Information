/**
 *
 */
package jp.co.humane.rtc.tool.connector.shell.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BeanListTableModel;
import org.springframework.shell.table.BorderSpecification;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.Table;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;
import org.springframework.shell.table.Tables;
import org.springframework.stereotype.Component;

import jp.co.humane.rtc.tool.connector.dao.NamingServerDao;
import jp.co.humane.rtc.tool.connector.dto.PortInfo;
import jp.co.humane.rtc.tool.connector.dto.RtcInfo;
import jp.co.humane.rtc.tool.connector.shell.command.arg.RtcParam;
import jp.co.humane.rtc.tool.connector.shell.command.table.LsBean;
import jp.co.humane.rtc.tool.connector.shell.command.table.LsPortBean;
import jp.co.humane.rtc.tool.connector.shell.common.TableBuilderUtil;

/**
 * lsコマンドの処理を定義。
 * @author terada.
 *
 */
@Component
public class LsCommand implements CommandMarker {

    /** リストの種類：RTC */
    private static final int TYPE_RTC = 0;

    /** リストの種類：ポート */
    private static final int TYPE_PORT = 1;

    /** ネーミングサーバーDAO */
    @Autowired
    private NamingServerDao dao = null;

    @CliCommand(value = "ls", help = "Print a simple hello world message")
    public String simple(
            @CliOption(key = { "port" }, mandatory = false, help = "Show RTC port list", specifiedDefaultValue = "") final RtcParam rtcParam
            ) {

        dao.reflesh();
        String message = null;
        int type = decideListType(rtcParam);
        switch (type) {
        case TYPE_RTC:
            message = getLsResponse();
            break;

        case TYPE_PORT:
            message = getLsPortResponse(rtcParam.getName());

        }
        return message;

    }


    private int decideListType(RtcParam portParam) {
        if (null == portParam) {
            return TYPE_RTC;
        }
        return TYPE_PORT;

    }

    /**
     * RTCのリストからlsコマンドの結果を取得する。
     * @return lsコマンドの結果。
     */
    private String getLsResponse() {

        List<RtcInfo> rtcList = dao.getRtcList();

        // lsコマンド出力リストを取得
        List<LsBean> beanList = new ArrayList<>();
        for (RtcInfo rtc : rtcList) {
            LsBean bean = new LsBean();
            bean.setDirectory(rtc.getDirectory());
            bean.setId(rtc.getId());
            bean.setKind(rtc.getKind());
            bean.setState(rtc.getState().name());
            beanList.add(bean);
        }

        // リストをテーブル表現の文字列にして返却
        return TableBuilderUtil.render(beanList, LsBean.HEADER);
    }

    /**
     * ls --portのコマンド結果を取得する。
     * @param name 出力対象のRTC名。
     * @return ls --portコマンドの結果。
     */
    private String getLsPortResponse(String name) {

        List<LsPortBean> beanList = new ArrayList<>();

        // ポート一覧からコマンド結果のBeanリストを生成
        Map<String, List<PortInfo>> portMap = dao.getPortMap();
        for (String rtcName: portMap.keySet()) {

            // RTC名の指定があり、処理対象と一致しない場合は次にスキップ
            if (0 != name.length() && !name.equals(rtcName)) {
                continue;
            }

            for (PortInfo port : portMap.get(rtcName)) {
                LsPortBean bean = new LsPortBean();
                bean.setRtcName(rtcName);
                bean.setName(port.getName());
                bean.setPortType(port.isInPort() ? "InPort" : "OutPort");
                bean.setConnectNum(String.valueOf(dao.getConnectNum(rtcName, port.getName())));
                bean.setDataType(port.getDataType());
                bean.setSubscriptionTypeSet(joinSet(port.getSubscriptionTypeSet()));
                bean.setDataFlowSet(joinSet(port.getDataFlowSet()));
                bean.setInterfaceType(port.getInterfaceType());
                beanList.add(bean);
            }
        }

        // Beanリストをテーブル形式に変換
        return TableBuilderUtil.render(beanList, LsPortBean.HEADER);

    }

    /**
     * Setの情報を"/"区切りで連結する。
     * @param set Set情報。
     * @return 連結後の文字列。
     */
    private String joinSet(Set<String> set) {
        StringBuilder sb = new StringBuilder();
        for (String s: set) {
            sb.append(s + "/");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private String msg1() {

        Map<String, String> values = new LinkedHashMap<String, String>();
        values.put("a", "b");
        values.put("long-key", "c");
        values.put("d", "long-value");
        TableModel model = new ArrayTableModel(new Object[][] {{"Thing", "Properties"}, {"Something", values}});
        TableBuilder tableBuilder = new TableBuilder(model)
                .addHeaderAndVerticalsBorders(BorderStyle.fancy_light);
        Tables.configureKeyValueRendering(tableBuilder, " = ");
        Table table = tableBuilder.build();
        String result = table.render(10);

        return result;
    }

    private String msg2() {

        StringBuilder sb = new StringBuilder();

        for (BorderStyle s : BorderStyle.values()) {

            TableModel model = new ArrayTableModel(new String[][] {{"a", "b"}, {"c", "d"}, {"aaaaaaaaaaaaaa", "b"}, {"x", "aaaaaaaaaaaaaa"}});
            TableBuilder tableBuilder = new TableBuilder(model)
                    .addHeaderAndVerticalsBorders(s);
            Table table = tableBuilder.build();
            String text = table.render(10);
            sb.append("-----" + s + "-----\n");
            sb.append(text);
            sb.append("\n\n");
        }

        return sb.toString();
    }

    private String msg3() {

        TableModel model = new ArrayTableModel(new String[][] {{"a", "b"}, {"c", "d"}, {"aaaaaaaaaaaaaa", "b"}, {"x", "aaaaaaaaaaaaaa"}});


        String text = "-----addHeaderAndVerticalsBorders-----\n" +
                new TableBuilder(model)
                .addHeaderAndVerticalsBorders(BorderStyle.fancy_light)
                .build()
                .render(50);

        text += "\n\n-----addFullBorder-----\n" +
                new TableBuilder(model)
                .addFullBorder(BorderStyle.fancy_light)
                .build()
                .render(50);

        text += "\n\n-----addHeaderBorder-----\n" +
                new TableBuilder(model)
                .addHeaderBorder(BorderStyle.fancy_light)
                .build()
                .render(50);

        text += "\n\n-----addInnerBorder-----\n" +
                new TableBuilder(model)
                .addInnerBorder(BorderStyle.fancy_light)
                .build()
                .render(50);

        text += "\n\n-----addOutlineBorder-----\n" +
                new TableBuilder(model)
                .addOutlineBorder(BorderStyle.fancy_light)
                .build()
                .render(50);

        return text;
    }

    private String msg4() {

        List<SampleDto> dataList = new LinkedList<>();
        dataList.add(new SampleDto("xxx", 123, "-"));
        dataList.add(new SampleDto("z", -12, "aaaaa"));
        dataList.add(new SampleDto("xxswerrrr", 0, "hogehoge"));
        dataList.add(new SampleDto("", null, null));

        LinkedHashMap<String, Object> header = new LinkedHashMap<>();
        header.put("name", "名前");
        header.put("old", "年齢");
        header.put("address", "住所");

        String text = new TableBuilder(new BeanListTableModel<SampleDto>(dataList, header))
                .paintBorder(BorderStyle.air, BorderSpecification.INNER_VERTICAL).fromTopLeft().toBottomRight()
                .build().render(50);
        return text;

    }

    public class SampleDto {
        private String name = null;
        private Integer old = null;
        private String address = null;
        public SampleDto(String name, Integer old, String address) {
            this.name = name;
            this.old = old;
            this.address = address;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public Integer getOld() {
            return old;
        }
        public void setOld(Integer old) {
            this.old = old;
        }
        public String getAddress() {
            return address;
        }
        public void setAddress(String address) {
            this.address = address;
        }
    }

}
