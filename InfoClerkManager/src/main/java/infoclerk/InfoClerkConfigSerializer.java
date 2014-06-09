package infoclerk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import jp.co.humane.configlib.ConfigSerializer;

public class InfoClerkConfigSerializer implements ConfigSerializer
{
	private static String ELEMENT_FORMAT = "<attribute name=\"%s\">%s</attribute>\n";
	private static String COMP_FORMAT = "<component name=\"%s\">%s</component>\n";
	private static String FORMAT = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
									"<rtcml>%s</rtcml>\n";
		
	@Override
	public String serialize(Properties prop)
	{
		StringBuffer elements = new StringBuffer();
		Iterator it = prop.keySet().iterator();
		while( it.hasNext() )
		{			
			String key = (String)it.next();			
			String element = String.format(ELEMENT_FORMAT, key,prop.get(key).toString());
			elements.append(element);
		}
		String components = String.format(COMP_FORMAT, "InfoClerk",elements.toString());
		String result = String.format(FORMAT, components);
		return result;
	}
	public List<String> getAllKeys(Properties prop)
	{
		ArrayList<String> keys = new ArrayList<String>();
		while(prop.keys().hasMoreElements() )
		{
			String key = (String)prop.keys().nextElement();
			keys.add(key);
		}
		return keys;
	}

}
