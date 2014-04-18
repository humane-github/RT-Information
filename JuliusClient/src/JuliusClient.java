// -*- Java -*-
/*!
 * @file JuliusClient.java
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
 * @class JuliusClient
 * @brief JuliusClient
 */
public class JuliusClient implements RtcNewFunc, RtcDeleteFunc, RegisterModuleFunc {

//  Module specification
//  <rtc-template block="module_spec">
    public static String component_conf[] = {
    	    "implementation_id", "JuliusClient",
    	    "type_name",         "JuliusClient",
    	    "description",       "JuliusClient",
    	    "version",           "1.0.0",
    	    "vendor",            "Humane systems",
    	    "category",          "Category",
    	    "activity_type",     "STATIC",
    	    "max_instance",      "1",
    	    "language",          "Java",
    	    "lang_type",         "compile",
            // Configuration variables
            "conf.default.audioPort", "23972",
            "conf.default.modulePort", "23973",
            "conf.default.juliusHostname", "localhost",
            "conf.default.juliusPath", "C:\\DEV\\17.InformationClerk\\20.bin\\julius-4.3-win32bin\\bin\\julius",
            "conf.default.juliusConfPath", "C:\\DEV\\17.InformationClerk\\20.bin\\julius-4.3-win32bin\\gram\\conf.jconf",
            "conf.default.availableScore", "0.5",
            "conf.default.targetClassid", "0|2|3",
            // Widget
            "conf.__widget__.audioPort", "text",
            "conf.__widget__.modulePort", "text",
            "conf.__widget__.juliusHostname", "text",
            "conf.__widget__.juliusPath", "text",
            "conf.__widget__.juliusConfPath", "text",
            "conf.__widget__.availableScore", "text",
            "conf.__widget__.targetClassid", "text",
            // Constraints
    	    ""
            };
//  </rtc-template>

    public RTObject_impl createRtc(Manager mgr) {
        return new JuliusClientImpl(mgr);
    }

    public void deleteRtc(RTObject_impl rtcBase) {
        rtcBase = null;
    }
    public void registerModule() {
        Properties prop = new Properties(component_conf);
        final Manager manager = Manager.instance();
        manager.registerFactory(prop, new JuliusClient(), new JuliusClient());
    }
}
