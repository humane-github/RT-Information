// -*- C++ -*-
/*!
 * @file  wstr2str.cpp
 * @brief wstr2str
 * @date $Date$
 *
 * $Id$
 */

#include "wstr2str.h"

// Module specification
// <rtc-template block="module_spec">
static const char* wstr2str_spec[] =
  {
    "implementation_id", "wstr2str",
    "type_name",         "wstr2str",
    "description",       "wstr2str",
    "version",           "1.0.0",
    "vendor",            "Humane systems",
    "category",          "Category",
    "activity_type",     "PERIODIC",
    "kind",              "DataFlowComponent",
    "max_instance",      "1",
    "language",          "C++",
    "lang_type",         "compile",
    ""
  };
// </rtc-template>

BOOL convUTF16toUTF8( BYTE* buffUtf16, BYTE* pDist, int* pSize )
{
	*pSize = 0;


	const int nSizeUtf8 = ::WideCharToMultiByte( CP_UTF8, 0, (LPCWSTR)buffUtf16, -1, NULL, 0, NULL, NULL );
	if ( !pDist ) {
		*pSize = nSizeUtf8;
		return TRUE;
	}

	BYTE* buffUtf8 = new BYTE[ nSizeUtf8 * 2 ];
	ZeroMemory( buffUtf8, nSizeUtf8 * 2 );
	::WideCharToMultiByte( CP_UTF8, 0, (LPCWSTR)buffUtf16, -1, (LPSTR)buffUtf8, nSizeUtf8, NULL, NULL );

	*pSize = lstrlen( (char*)buffUtf8 );
	memcpy( pDist, buffUtf8, *pSize );

	delete buffUtf8;

	return TRUE;
}


BOOL UTF16utf8(BYTE* source, BYTE** dest) {

	int size = 0;
	convUTF16toUTF8( source, NULL, &size );


	*dest = new BYTE[ size + 1 ];
	ZeroMemory( *dest, size + 1 );
	convUTF16toUTF8( source, *dest, &size );

	return TRUE;
}
/*!
 * @brief constructor
 * @param manager Maneger Object
 */
wstr2str::wstr2str(RTC::Manager* manager)
    // <rtc-template block="initializer">
  : RTC::DataFlowComponentBase(manager),
    m_wstrIn("wstr", m_wstr),
    m_strOut("str", m_str)

    // </rtc-template>
{
}

/*!
 * @brief destructor
 */
wstr2str::~wstr2str()
{
}



RTC::ReturnCode_t wstr2str::onInitialize()
{
  // Registration: InPort/OutPort/Service
  // <rtc-template block="registration">
  // Set InPort buffers
  addInPort("wstr", m_wstrIn);
  
  // Set OutPort buffer
  addOutPort("str", m_strOut);
  
  // Set service provider to Ports
  
  // Set service consumers to Ports
  
  // Set CORBA Service Ports
  
  // </rtc-template>

  // <rtc-template block="bind_config">
  // </rtc-template>
  
  return RTC::RTC_OK;
}

/*
RTC::ReturnCode_t wstr2str::onFinalize()
{
  return RTC::RTC_OK;
}
*/

/*
RTC::ReturnCode_t wstr2str::onStartup(RTC::UniqueId ec_id)
{
  return RTC::RTC_OK;
}
*/

/*
RTC::ReturnCode_t wstr2str::onShutdown(RTC::UniqueId ec_id)
{
  return RTC::RTC_OK;
}
*/


RTC::ReturnCode_t wstr2str::onActivated(RTC::UniqueId ec_id)
{
  return RTC::RTC_OK;
}


RTC::ReturnCode_t wstr2str::onDeactivated(RTC::UniqueId ec_id)
{
  return RTC::RTC_OK;
}


RTC::ReturnCode_t wstr2str::onExecute(RTC::UniqueId ec_id)
{
	if( m_wstrIn.isNew())
	{
		m_wstrIn.read();
		std::wstring str = m_wstr.data;
		std::wcout << str << std::endl;

		BYTE* strUtf8;
		UTF16utf8((BYTE*)str.c_str(), &strUtf8);
		m_str.data = (char*)strUtf8;
		m_strOut.write();

	}
  return RTC::RTC_OK;
}


/*
RTC::ReturnCode_t wstr2str::onAborting(RTC::UniqueId ec_id)
{
  return RTC::RTC_OK;
}
*/

/*
RTC::ReturnCode_t wstr2str::onError(RTC::UniqueId ec_id)
{
  return RTC::RTC_OK;
}
*/

/*
RTC::ReturnCode_t wstr2str::onReset(RTC::UniqueId ec_id)
{
  return RTC::RTC_OK;
}
*/

/*
RTC::ReturnCode_t wstr2str::onStateUpdate(RTC::UniqueId ec_id)
{
  return RTC::RTC_OK;
}
*/

/*
RTC::ReturnCode_t wstr2str::onRateChanged(RTC::UniqueId ec_id)
{
  return RTC::RTC_OK;
}
*/



extern "C"
{
 
  void wstr2strInit(RTC::Manager* manager)
  {
    coil::Properties profile(wstr2str_spec);
    manager->registerFactory(profile,
                             RTC::Create<wstr2str>,
                             RTC::Delete<wstr2str>);
  }
  
};


