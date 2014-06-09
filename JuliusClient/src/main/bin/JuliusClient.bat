@rem Namingサービスを起動
@rem start start-orbd.vbs

@rem JuliusClientを起動
cd ..
set CLASSPATH=%CLASSPATH%;classes
set CLASSPATH=%CLASSPATH%;lib\commons-cli-1.1.jar
set CLASSPATH=%CLASSPATH%;lib\OpenRTM-aist-1.1.0.jar
set CLASSPATH=%CLASSPATH%;lib\ExceptionLib-1.0.jar
set CLASSPATH=%CLASSPATH%;lib\Logger-1.0.jar
set CLASSPATH=%CLASSPATH%;lib\XMLLib-1.0.jar

java -classpath %CLASSPATH% JuliusClientComp -f conf/rtc.conf
pause

