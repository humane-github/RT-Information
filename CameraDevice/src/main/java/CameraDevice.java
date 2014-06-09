// -*- Java -*-
/*!
 * @file CameraDevice.java
 * @date $Date$
 *
 * $Id$
 */

import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.RTObject_impl;
import jp.go.aist.rtm.RTC.RtcDeleteFunc;
import jp.go.aist.rtm.RTC.RtcNewFunc;
import jp.go.aist.rtm.RTC.RegisterModuleFunc;
import jp.go.aist.rtm.RTC.util.Properties;

/*!
 * @class CameraDevice
 * @brief CameraDevice
 */
public class CameraDevice implements RtcNewFunc, RtcDeleteFunc, RegisterModuleFunc {

//  Module specification
//  <rtc-template block="module_spec">
    public static String component_conf[] = {
    	    "implementation_id", "CameraDevice",
    	    "type_name",         "CameraDevice",
    	    "description",       "CameraDevice",
    	    "version",           "1.0.0",
    	    "vendor",            "Humane systems",
    	    "category",          "Category",
    	    "activity_type",     "STATIC",
    	    "max_instance",      "1",
    	    "language",          "Java",
    	    "lang_type",         "compile",
            // Configuration variables
            "conf.default.deviceid", "1",
            "conf.default.waittime", "100",
            // Widget
            "conf.__widget__.deviceid", "text",
            "conf.__widget__.waittime", "text",
            // Constraints
    	    ""
            };
//  </rtc-template>

    public RTObject_impl createRtc(Manager mgr) {
        return new CameraDeviceImpl(mgr);
    }

    public void deleteRtc(RTObject_impl rtcBase) {
        rtcBase = null;
    }
    public void registerModule() {
        Properties prop = new Properties(component_conf);
        final Manager manager = Manager.instance();
        manager.registerFactory(prop, new CameraDevice(), new CameraDevice());
    }
}
