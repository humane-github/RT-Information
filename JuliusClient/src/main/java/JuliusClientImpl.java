// -*- Java -*-
/*!
 * @file  JuliusClientImpl.java
 * @brief JuliusClient
 * @date  $Date$
 *
 * $Id$
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

import jp.go.aist.rtm.RTC.DataFlowComponentBase;
import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.port.ConnectorBase.ConnectorInfo;
import jp.go.aist.rtm.RTC.port.ConnectorDataListenerT;
import jp.go.aist.rtm.RTC.port.ConnectorDataListenerType;
import jp.go.aist.rtm.RTC.port.InPort;
import jp.go.aist.rtm.RTC.port.OutPort;
import jp.go.aist.rtm.RTC.util.DataRef;
import jp.go.aist.rtm.RTC.util.FloatHolder;
import jp.go.aist.rtm.RTC.util.IntegerHolder;
import jp.go.aist.rtm.RTC.util.StringHolder;
import RTC.ReturnCode_t;
import RTC.Time;
import RTC.TimedOctetSeq;
import RTC.TimedWString;

/*!
 * @class JuliusClientImpl
 * @brief JuliusClient
 *
 */
public class JuliusClientImpl extends DataFlowComponentBase implements JuliusListener
{

	private Socket audioSocket = null;
	private Thread juliusThread = null;
	private JuliusConnector juliusConnector = null;
	
  /*!
   * @brief constructor
   * @param manager Maneger Object
   */
	public JuliusClientImpl(Manager manager) {  
        super(manager);
        // <rtc-template block="initializer">
        m_voiceData_val = new TimedOctetSeq();
        m_voiceData = new DataRef<TimedOctetSeq>(m_voiceData_val);
        m_voiceDataIn = new InPort<TimedOctetSeq>("voiceData", m_voiceData);
        m_result_val = new TimedWString(new Time(0,0),"");
        m_result = new DataRef<TimedWString>(m_result_val);
        m_resultOut = new OutPort<TimedWString>("result", m_result);
        m_status_val = new TimedWString(new Time(0,0),"");
        m_status = new DataRef<TimedWString>(m_status_val);
        m_statusOut = new OutPort<TimedWString>("status", m_status);
        // </rtc-template>
    }

    /*!
     *
     * The initialize action (on CREATED->ALIVE transition)
     * formaer rtc_init_entry() 
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
    @Override
    protected ReturnCode_t onInitialize() {
        // Registration: InPort/OutPort/Service
        // <rtc-template block="registration">
        // Set InPort buffers
        addInPort("voiceData", m_voiceDataIn);
        
        // Set OutPort buffer
        addOutPort("result", m_resultOut);
        addOutPort("status", m_statusOut);
        // </rtc-template>
        bindParameter("audioPort", m_audioPort, "23972");
        bindParameter("modulePort", m_modulePort, "23973");
        bindParameter("juliusHostname", m_juliusHostname, "localhost");
        bindParameter("juliusPath", m_juliusPath, "C:\\DEV\\17.InformationClerk\\20.bin\\julius-4.3-win32bin\\bin\\julius");
        bindParameter("juliusConfPath", m_juliusConfPath, "C:\\DEV\\17.InformationClerk\\20.bin\\julius-4.3-win32bin\\gram\\conf.jconf");
        bindParameter("availableScore",m_availableScore,"0.5");
        bindParameter("targetClassid",m_targetClassid,"0|2|3");
        
        m_voiceDataIn.addConnectorDataListener(ConnectorDataListenerType.ON_BUFFER_WRITE,
        		new ConnectorDataListenerT<TimedOctetSeq>(TimedOctetSeq.class)
        		{
        			public void operator(ConnectorInfo info,TimedOctetSeq data)
        			{
        				onVoiceData(info,data);
        			}
        		});
        return super.onInitialize();
    }
    
    /***
     *
     * The finalize action (on ALIVE->END transition)
     * formaer rtc_exiting_entry()
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
//    @Override
//    protected ReturnCode_t onFinalize() {
//        return super.onFinalize();
//    }

    /***
     *
     * The startup action when ExecutionContext startup
     * former rtc_starting_entry()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
//    @Override
//    protected ReturnCode_t onStartup(int ec_id) {
//        return super.onStartup(ec_id);
//    }

    /***
     *
     * The shutdown action when ExecutionContext stop
     * former rtc_stopping_entry()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
//    @Override
//    protected ReturnCode_t onShutdown(int ec_id) {
//        return super.onShutdown(ec_id);
//    }

    /***
     *
     * The activated action (Active state entry action)
     * former rtc_active_entry()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
    @Override
    protected ReturnCode_t onActivated(int ec_id)
    {
    	System.out.println(String.format("onActivated ec_id=%d",ec_id));
       	
    	//処理対象とするクラスIDを取得
    	String[] targetClassId = new String[]{};
    	String tmpClassid = m_targetClassid.value;
    	if( tmpClassid != null && tmpClassid.indexOf("|") < 0 )
    	{
    		targetClassId = new String[]{tmpClassid};
    	}
    	else
    	{
    		String[] tokens = tmpClassid.split("|");
    		if( tokens != null && tokens.length > 0 )
    		{
    			targetClassId = new String[tokens.length];
    			for(int i=0;i<tokens.length;i++)
    			{
    				targetClassId[i] = tokens[i];
    			}
    		}
    	}
    	
    	//Juliusの実行
    	JuliusExecuter.execute(m_juliusPath.value,
    							m_juliusConfPath.value,
    							m_juliusHostname.value,
    							m_audioPort.value,
    							m_modulePort.value);
   	
    	//Juliusから結果取得用クライアント
		System.out.println("### Initialize julius connector");
    	juliusConnector = new JuliusConnector(JuliusExecuter.Julius(),
    									m_juliusHostname.value,
    									m_modulePort.value,
    									m_availableScore.value);
    	juliusConnector.addListener(this);
    	juliusConnector.setTargetClassId(targetClassId);
    	juliusConnector.initialize();
    	//JuliusClientを実行
		System.out.println("### Start julius connector");
    	juliusConnector.start();
    	
    	// open socket
		System.out.println("### Open audio socket");
    	try
    	{
        	audioSocket = openAudioSocket(m_juliusHostname.value,
        									m_audioPort.value);
    	}
    	catch( IOException e)
    	{
    		System.out.println("Failed Create socket");
    		//return super.onActivated(ec_id);
    	}
    	
    	// Juliusから取得した音声認識結果を監視
    	juliusThread = new Thread()
    	{
    		public void run()
    		{
    			if( JuliusExecuter.Julius() != null )
    	    	{
            		InputStream is = JuliusExecuter.Julius().getInputStream();
            		BufferedReader br = new BufferedReader(new InputStreamReader(is));
    				while(true)
	    			{
        	    		try
        	    		{
        	        		String line = null;	        	        		
        	        		while( (line = br.readLine()) != null )
        	        		{
        	        		}
        	    		}
        	    		catch( IOException e )
        	    		{
        	    			e.printStackTrace();
        	    			break;
        	    		}
        	    		try {
							this.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    			}
	        		try {
						br.close();
		        		is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    	    	}
    		}
    	};
    	if(juliusThread != null){juliusThread.start();}    	
        return super.onActivated(ec_id);
    }

    /***
     *
     * The deactivated action (Active state exit action)
     * former rtc_active_exit()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
    @Override
    protected ReturnCode_t onDeactivated(int ec_id)
    {
    	System.out.println(String.format("onDeactivated ec_id=%d",ec_id));
    	try
    	{
        	closeSocket(audioSocket);
        	juliusConnector.stop();
    	}
    	catch( Exception e )
    	{
    		e.printStackTrace();
    	}
    	JuliusExecuter.destroy();
        return super.onDeactivated(ec_id);
    }

    /***
     *
     * The execution action that is invoked periodically
     * former rtc_active_do()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
    @Override
    protected ReturnCode_t onExecute(int ec_id) {
        return super.onExecute(ec_id);
    }

    /***
     *
     * The aborting action when main logic error occurred.
     * former rtc_aborting_entry()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
//  @Override
//  public ReturnCode_t onAborting(int ec_id) {
//      return super.onAborting(ec_id);
//  }

    /***
     *
     * The error action in ERROR state
     * former rtc_error_do()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
//    @Override
//    public ReturnCode_t onError(int ec_id) {
//        return super.onError(ec_id);
//    }

    /***
     *
     * The reset action that is invoked resetting
     * This is same but different the former rtc_init_entry()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
//    @Override
//    protected ReturnCode_t onReset(int ec_id) {
//        return super.onReset(ec_id);
//    }

    /***
     *
     * The state update action that is invoked after onExecute() action
     * no corresponding operation exists in OpenRTm-aist-0.2.0
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
//    @Override
//    protected ReturnCode_t onStateUpdate(int ec_id) {
//        return super.onStateUpdate(ec_id);
//    }

    /***
     *
     * The action that is invoked when execution context's rate is changed
     * no corresponding operation exists in OpenRTm-aist-0.2.0
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
//    @Override
//    protected ReturnCode_t onRateChanged(int ec_id) {
//        return super.onRateChanged(ec_id);
//    }
//
    
    /**
     * 音声データ入力時に発生するイベント
     * **/
    protected void onVoiceData(ConnectorInfo info,TimedOctetSeq data)
    {
    	if( audioSocket != null && (audioSocket.isConnected() || !audioSocket.isClosed()) )
    	{
    		try
    		{
        		OutputStream out = audioSocket.getOutputStream();
        		byte[] octets = data.data;

        		out.write(int2octet(octets.length));
        		out.write(octets);
        		out.flush();
    		}
    		catch( IOException e)
    		{
    			//e.printStackTrace();
    		}
    	}
    }
    
    protected byte[] int2octet(int length)
    {
    	byte[] data = new byte[4];
    	for (int i = 0; i < 4; i++) {
    		data[i] = (byte)(length & 0xFF);
    		length = length >> 8;
    	}
    	return data;
    }
    
    /**
     * JuliusのAUDIOソケットと接続する
     * 
     * @param	host	Julius実行ホスト名
     * @param	port	接続ポート番号
     * **/
    private Socket openAudioSocket(String host,int port) throws IOException
    {
    	Socket s = null;
    	try
    	{
        	s = new Socket();
        	s.connect(new InetSocketAddress(host,port));
    	}
    	catch( ConnectException e1 )
    	{
    		System.out.println("Julius audio socket is closed");
    		s = null;
    		e1.printStackTrace();
    	}
    	return s;
    }
    
    /**
     * ポートを閉じる
     * **/
    private void closeSocket(Socket socket)
    {
    	try
    	{
        	if( socket != null && (socket.isConnected() || !socket.isClosed()) ) {socket.close();}
    	}
    	catch( Exception e )
    	{
    		e.printStackTrace();
    	}    	
    }

    /**
     * JuliusConnectorからJuliusの音声認識結果を受け取る
     * 
     * @param	data	音声認識結果
     * **/
    public void onVoiceData(String data,String status)
    {
		m_result.v.data = data;
		m_resultOut.write();
		m_status.v.data = status;
		m_statusOut.write();
    }
    
	// Configuration variable declaration
	// <rtc-template block="config_declare">
    /*!
     * 
     * - Name:  audioPort
     * - DefaultValue: 23972
     */
    protected IntegerHolder m_audioPort = new IntegerHolder();
    /*!
     * 
     * - Name:  modulePort
     * - DefaultValue: 23973
     */
    protected IntegerHolder m_modulePort = new IntegerHolder();
    /*!
     * 
     * - Name:  juliusHostname
     * - DefaultValue: localhost
     */
    protected StringHolder m_juliusHostname = new StringHolder();
    protected StringHolder m_juliusPath = new StringHolder();
    protected StringHolder m_juliusConfPath = new StringHolder();
    protected FloatHolder m_availableScore = new FloatHolder();
    protected StringHolder m_targetClassid = new StringHolder();
	// </rtc-template>

    // DataInPort declaration
    // <rtc-template block="inport_declare">
    protected TimedOctetSeq m_voiceData_val;
    protected DataRef<TimedOctetSeq> m_voiceData;
    /*!
     */
    protected InPort<TimedOctetSeq> m_voiceDataIn;

    
    // </rtc-template>

    // DataOutPort declaration
    // <rtc-template block="outport_declare">
    protected TimedWString m_result_val;
    protected DataRef<TimedWString> m_result;
    protected OutPort<TimedWString> m_resultOut;

    protected TimedWString m_status_val;
    protected DataRef<TimedWString> m_status;
    protected OutPort<TimedWString> m_statusOut;
    
    // </rtc-template>

    // CORBA Port declaration
    // <rtc-template block="corbaport_declare">
    
    // </rtc-template>

    // Service declaration
    // <rtc-template block="service_declare">
    
    // </rtc-template>

    // Consumer declaration
    // <rtc-template block="consumer_declare">
    
    // </rtc-template>


}
