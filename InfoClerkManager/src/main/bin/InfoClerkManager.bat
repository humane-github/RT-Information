@rem Namingサービスを起動
@rem start start-orbd.vbs

@rem InfoClerkManagerを起動
cd ..
set CLASSPATH=%CLASSPATH%;classes
set CLASSPATH=%CLASSPATH%;lib\commons-cli-1.1.jar
set CLASSPATH=%CLASSPATH%;lib\commons-dbutils-1.5.jar
set CLASSPATH=%CLASSPATH%;lib\ConfigLib-1.0.jar
set CLASSPATH=%CLASSPATH%;lib\DBUtility-1.0.jar
set CLASSPATH=%CLASSPATH%;lib\ExceptionLib-1.0.jar
set CLASSPATH=%CLASSPATH%;lib\HCharEncoder-1.0.jar
set CLASSPATH=%CLASSPATH%;lib\Logger-1.0.jar
set CLASSPATH=%CLASSPATH%;lib\MessageLib-1.0.jar
set CLASSPATH=%CLASSPATH%;lib\MorphemeEngineLib-1.0.jar
set CLASSPATH=%CLASSPATH%;lib\opencv-249.jar
set CLASSPATH=%CLASSPATH%;lib\OpenCVLib-1.0.jar
set CLASSPATH=%CLASSPATH%;lib\OpenRTM-aist-1.1.0.jar
set CLASSPATH=%CLASSPATH%;lib\StateMachineLib-1.0.jar
set CLASSPATH=%CLASSPATH%;lib\UserMaster-1.0.jar
set CLASSPATH=%CLASSPATH%;lib\XMLLib-1.0.jar

java -classpath %CLASSPATH% InfoClerkManagerComp -f conf/rtc.conf
pause

