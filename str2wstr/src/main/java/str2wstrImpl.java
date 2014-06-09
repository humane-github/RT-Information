// -*- Java -*-
/*!
 * @file  str2wstrImpl.java
 * @brief str2wstr
 * @date  $Date$
 *
 * $Id$
 */

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import jp.co.humane.hcharencoder.HCharEncoder;
import jp.go.aist.rtm.RTC.DataFlowComponentBase;
import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.port.InPort;
import jp.go.aist.rtm.RTC.port.OutPort;
import jp.go.aist.rtm.RTC.util.DataRef;
import RTC.ReturnCode_t;
import RTC.Time;
import RTC.TimedString;
import RTC.TimedWString;

/*!
 * @class str2wstrImpl
 * @brief str2wstr
 *
 */
public class str2wstrImpl extends DataFlowComponentBase {

  /*!
   * @brief constructor
   * @param manager Maneger Object
   */
	public str2wstrImpl(Manager manager) {  
        super(manager);
        // <rtc-template block="initializer">
        m_timedString_val = new TimedString(new Time(0,0),"");
        m_timedString = new DataRef<TimedString>(m_timedString_val);
        m_timedStringIn = new InPort<TimedString>("timedString", m_timedString);
        m_timedWstring_val = new TimedWString(new Time(0,0),"");
        m_timedWstring = new DataRef<TimedWString>(m_timedWstring_val);
        m_timedWstringOut = new OutPort<TimedWString>("timedWstring", m_timedWstring);
        // </rtc-template>

    }

    /**
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
        addInPort("timedString", m_timedStringIn);
        
        // Set OutPort buffer
        addOutPort("timedWstring", m_timedWstringOut);
        // </rtc-template>
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
    protected ReturnCode_t onActivated(int ec_id) {
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
    protected ReturnCode_t onDeactivated(int ec_id) {
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
    protected ReturnCode_t onExecute(int ec_id)
    {
    	if( m_timedStringIn.isNew() )
    	{
    		m_timedStringIn.read();
    		String val = m_timedString.v.data;
    		System.out.println("recv data:"+val);
    		
    		ArrayList<String> recvdata = new ArrayList<String>(); 
    		StringBuffer buff = new StringBuffer();
    		for( int i=0; i<val.length();i+=4)
    		{
    			String token = val.substring(i,i+4);
    			buff.append(token.charAt(2)).append(token.charAt(3)).append(token.charAt(0)).append(token.charAt(1));
    			recvdata.add(buff.toString());
    			buff.delete(0, buff.length());
    		}
    		String[] recvdataArray = new String[recvdata.size()];
    		recvdataArray = (String[])recvdata.toArray(recvdataArray);
    		for( String s : recvdataArray )
    		{
    			System.out.println("convert data:"+s);
    		}
    		    		
			String convdata = HCharEncoder.toString(recvdataArray);
    		System.out.println("convert data:"+convdata);
    		m_timedWstring_val.data = convdata;
    		m_timedWstringOut.write();
    	}
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
    // DataInPort declaration
    // <rtc-template block="inport_declare">
    protected TimedString m_timedString_val;
    protected DataRef<TimedString> m_timedString;
    /*!
     */
    protected InPort<TimedString> m_timedStringIn;

    
    // </rtc-template>

    // DataOutPort declaration
    // <rtc-template block="outport_declare">
    protected TimedWString m_timedWstring_val;
    protected DataRef<TimedWString> m_timedWstring;
    /*!
     */
    protected OutPort<TimedWString> m_timedWstringOut;

    
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
