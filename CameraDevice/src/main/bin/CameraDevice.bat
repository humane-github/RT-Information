@rem Naming�T�[�r�X���N��
@rem start start-orbd.vbs

@rem CameraDeviceComp���N��
cd ..
set CLASSPATH=%CLASSPATH%;classes
set CLASSPATH=%CLASSPATH%;lib\MessageLib-1.0.jar
set CLASSPATH=%CLASSPATH%;lib\opencv-249.jar
set CLASSPATH=%CLASSPATH%;lib\OpenCVLib-1.0.jar
set CLASSPATH=%CLASSPATH%;lib\OpenRTM-aist-1.1.0.jar
set CLASSPATH=%CLASSPATH%;lib\XMLLib-1.0.jar
set CLASSPATH=%CLASSPATH%;lib\commons-cli-1.1.jar

java -classpath %CLASSPATH% CameraDeviceComp -f conf/rtc.conf
pause