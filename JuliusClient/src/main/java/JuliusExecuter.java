import java.io.IOException;


public class JuliusExecuter
{
	private static ProcessBuilder juliusProcessBuilder = null;
	private static Process julius = null;

	/**
	 * Julius�v���Z�X�����s����
	 * 
	 * @param	juliusPath	Julius���s�t�@�C���̃p�X
	 * @param	confPath	Conf�t�@�C���̃p�X
	 * @param	hostname	Julius���s�z�X�g��
	 * @param	audioport	AUDIO�|�[�g�ԍ�
	 * @param	moduleport	MODULE�|�[�g�ԍ�
	 * **/
	public static void execute(String juliusPath,
								String confPath,
								String hostname,int audioport,int moduleport)
	{
		String[] cmdline = new String[]
				{
				juliusPath,
				"-C",confPath,
				"-input","adinnet",
		        "-adport",String.valueOf(audioport), 
		        "-module",String.valueOf(moduleport),
		        //"-pausesegment", 
		        //"-nostrip",
		        //"-spmodel","sp",
		        //"-multipath",
		        //"-iwsp",
		        //"-iwsppenalty","-70.0",
		        //"-penalty1","5.0", 
		        //"-penalty2","20.0",
		        //"-iwcd1","max", 
		        //"-gprune","safe",
		        //"-record","c:\\users\\suzuki\\appdata\\local\\temp\\tmpuceu9z",
		        //"-forcedict"
				};
		try
    	{
    		juliusProcessBuilder = new ProcessBuilder(cmdline);
    		julius = juliusProcessBuilder.start();
    	}
    	catch( IOException e )
    	{
    		e.printStackTrace();
    	}	
	}
	
	public static void destroy()
	{
		if( julius != null )
		{
			julius.destroy();
			julius = null;
		}
	}
	
	public static Process Julius(){return julius;}
}
