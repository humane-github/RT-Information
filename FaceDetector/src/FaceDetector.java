// -*- Java -*-
/*!
 * @file FaceDetector.java
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
 * @class FaceDetector
 * @brief ${rtcParam.description}
 */
public class FaceDetector implements RtcNewFunc, RtcDeleteFunc, RegisterModuleFunc {

//  Module specification
//  <rtc-template block="module_spec">
    public static String component_conf[] = {
    	    "implementation_id", "FaceDetector",
    	    "type_name",         "FaceDetector",
    	    "description",       "${rtcParam.description}",
    	    "version",           "1.0.0",
    	    "vendor",            "Humane systems",
    	    "category",          "FaceDetector",
    	    "activity_type",     "STATIC",
    	    "max_instance",      "1",
    	    "language",          "Java",
    	    "lang_type",         "compile",
            // Configuration variables
            "conf.default.CascadePath1", "C:\\DEV\\10.opencv\\opencv2.4.5\\data\\haarcascades\\haarcascade_frontalface_alt.xml",
            "conf.default.CascadePath2", "C:\\DEV\\10.opencv\\opencv2.4.5\\data\\haarcascades\\haarcascade_frontalface_alt.xml",
            "conf.default.CascadePath3", "C:\\DEV\\10.opencv\\opencv2.4.5\\data\\haarcascades\\haarcascade_frontalface_alt.xml",
            "conf.default.ShowPreviewDialog", "0",
            "conf.default.CascadeScale", "4",
            "conf.default.DetectThreshold", "5",
            "conf.default.WaitTime", "500",
            "conf.default.ResetTime", "4000",
            // Widget
            "conf.__widget__.CascadePath1", "text",
            "conf.__widget__.CascadePath2", "text",
            "conf.__widget__.CascadePath3", "text",
            "conf.__widget__.ShowPreviewDialog", "radio",
            "conf.__widget__.CascadeScale", "text",
            "conf.__widget__.DetectThreshold", "text",
            "conf.__widget__.WaitTime", "text",
            "conf.__widget__.ResetTime", "text",
            // Constraints
            "conf.__constraints__.ShowPreviewDialog", "(0,1)",
    	    ""
            };
//  </rtc-template>

    public RTObject_impl createRtc(Manager mgr) {
        return new FaceDetectorImpl(mgr);
    }

    public void deleteRtc(RTObject_impl rtcBase) {
        rtcBase = null;
    }
    public void registerModule() {
        Properties prop = new Properties(component_conf);
        final Manager manager = Manager.instance();
        manager.registerFactory(prop, new FaceDetector(), new FaceDetector());
    }
}
