package infoclerk;

public class VoiceDataInfo
{
	public String classId = "";
	public String voiceData = "";
	public float score = 0;
	
	public VoiceDataInfo(String data)
	{
		parse(data);
	}
	
	public void parse(String data)
	{
		if( data == null || data.length() < 1 ){return;}
		String[] elements = data.split(":");
		if( elements == null || elements.length < 1 ){return;}
		for( String element : elements )
		{
			String[] tokens = element.split("=");
			if( tokens == null || tokens.length < 1 ){continue;}
			if( tokens[0].equals("CLASSID") ){classId = tokens[1];}
			else if( tokens[0].equals("VOICE") ){voiceData = tokens[1];}
			else if( tokens[0].equals("SCORE") )
			{
				try
				{
					score = Float.parseFloat(tokens[1]);					
				}
				catch( Exception ex )
				{
					score = 0;
				}
			}
		}
	}
}