@rem Namingサービスを起動
@rem start start-orbd.vbs

@rem FaceDetectorを起動
cd ..
set CLASSPATH=%CLASSPATH%;classes
set CLASSPATH=%CLASSPATH%;lib\commons-cli-1.1.jar
set CLASSPATH=%CLASSPATH%;lib\opencv-249.jar
set CLASSPATH=%CLASSPATH%;lib\OpenRTM-aist-1.1.0.jar
set CLASSPATH=%CLASSPATH%;lib\ConfigLib-1.0.jar
set CLASSPATH=%CLASSPATH%;lib\ExceptionLib-1.0.jar
set CLASSPATH=%CLASSPATH%;lib\OpenCVLib-1.0.jar
set CLASSPATH=%CLASSPATH%;lib\XMLLib-1.0.jar

java -classpath %CLASSPATH% FaceDetectorComp -f conf/rtc.conf
pause

