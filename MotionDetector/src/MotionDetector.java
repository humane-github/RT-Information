// -*- Java -*-
/*!
 * @file MotionDetector.java
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
 * @class MotionDetector
 * @brief MotionDetector
 */
public class MotionDetector implements RtcNewFunc, RtcDeleteFunc, RegisterModuleFunc {

//  Module specification
//  <rtc-template block="module_spec">
    public static String component_conf[] = {
    	    "implementation_id", "MotionDetector",
    	    "type_name",         "MotionDetector",
    	    "description",       "MotionDetector",
    	    "version",           "1.0.0",
    	    "vendor",            "Humane systems",
    	    "category",          "MotionDetector",
    	    "activity_type",     "STATIC",
    	    "max_instance",      "1",
    	    "language",          "Java",
    	    "lang_type",         "compile",
            // Configuration variables
            "conf.default.FEATURES_MAX_CORNERS", "80",
            "conf.default.FEATURES_QUALITY_LEVEL", "0.01",
            "conf.default.FEATURES_MIN_DISTANCE", "5",
            "conf.default.DETECT_THRESHOLD", "10",
            "conf.default.ShowPreviewDialog", "0",
            // Widget
            "conf.__widget__.FEATURES_MAX_CORNERS", "text",
            "conf.__widget__.FEATURES_QUALITY_LEVEL", "text",
            "conf.__widget__.FEATURES_MIN_DISTANCE", "text",
            "conf.__widget__.DETECT_THRESHOLD", "text",
            "conf.__widget__.ShowPreviewDialog", "radio",
            // Constraints
            "conf.__constraints__.ShowPreviewDialog", "(0,1)",
    	    ""
            };
//  </rtc-template>

    public RTObject_impl createRtc(Manager mgr) {
        return new MotionDetectorImpl(mgr);
    }

    public void deleteRtc(RTObject_impl rtcBase) {
        rtcBase = null;
    }
    public void registerModule() {
        Properties prop = new Properties(component_conf);
        final Manager manager = Manager.instance();
        manager.registerFactory(prop, new MotionDetector(), new MotionDetector());
    }
}
