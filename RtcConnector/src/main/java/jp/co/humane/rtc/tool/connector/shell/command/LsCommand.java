/**
 *
 */
package jp.co.humane.rtc.tool.connector.shell.command;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import jp.co.humane.rtc.tool.connector.dto.RtcInfo;
import jp.co.humane.rtc.tool.connector.shell.common.TableBuilderUtil;
import jp.co.humane.rtc.tool.connector.shell.tablebean.LsBean;

/**
 * lsコマンドの処理を定義。
 * @author terada.
 *
 */
@Component
public class LsCommand implements CommandMarker {

    /** ネーミングサーバーDAO */
    @Autowired
    private NamingServerDao dao = null;

    @CliCommand(value = "ls", help = "Print a simple hello world message")
    public String simple(
            @CliOption(key = { "message" }, mandatory = true, help = "The hello world message") final File message
            ) {

        dao.reflesh();
        List<RtcInfo> rtcList = dao.getRtcList();
        return getLsResponse(rtcList);
    }

    /**
     * RTCのリストからlsコマンドの結果を取得する。
     * @param rtcList RTC情報一覧。
     * @return lsコマンドの結果。
     */
    private String getLsResponse(List<RtcInfo> rtcList) {

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
