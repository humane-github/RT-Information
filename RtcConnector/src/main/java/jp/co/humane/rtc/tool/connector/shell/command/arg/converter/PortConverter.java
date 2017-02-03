/**
 *
 */
package jp.co.humane.rtc.tool.connector.shell.command.arg.converter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.Completion;
import org.springframework.shell.core.Converter;
import org.springframework.shell.core.MethodTarget;
import org.springframework.stereotype.Component;

import jp.co.humane.rtc.tool.connector.dao.NamingServerDao;
import jp.co.humane.rtc.tool.connector.dto.PortInfo;
import jp.co.humane.rtc.tool.connector.shell.command.arg.PortParam;

/**
 * ポートパラメータのコンバータ。
 * @author terada.
 *
 */
@Component
public class PortConverter implements Converter<PortParam> {

    /** ネーミングサーバDao */
    @Autowired
    private NamingServerDao dao = null;

    /**
     * サポートする型を設定する。
     * @inheritDoc
     */
    @Override
    public boolean supports(Class<?> type, String optionContext) {
        return PortParam.class.isAssignableFrom(type);    }

    /**
     * 文字列からポートパラメータを生成する。
     * @inheritDoc
     */
    @Override
    public PortParam convertFromText(String value, Class<?> targetType, String optionContext) {
        PortParam portParam = new PortParam();
        portParam.setName(value);
        return portParam;
    }

    /**
     * 取り得る値を列挙する。
     * @inheritDoc
     */
    @Override
    public boolean getAllPossibleValues(List<Completion> completions, Class<?> targetType, String existingData,
            String optionContext, MethodTarget target) {

        Set<String> portNameSet = new TreeSet<>();

        // ポート名の一覧を取得
        Map<String, List<PortInfo>> portListMap = dao.getPortMap();
        for (List<PortInfo> portList : portListMap.values()) {
            for (PortInfo port : portList) {
                portNameSet.add(port.getName());
            }
        }

        // Completionにポート名を登録する
        for (String portName : portNameSet) {
            completions.add(new Completion(portName));
        }

        return false;
    }

}
