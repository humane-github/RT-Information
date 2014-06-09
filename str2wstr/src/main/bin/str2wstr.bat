@rem Namingサービスを起動
@rem start start-orbd.vbs

@rem str2wstrを起動
cd ..
set CLASSPATH=%CLASSPATH%;classes
set CLASSPATH=%CLASSPATH%;lib\commons-cli-1.1.jar
set CLASSPATH=%CLASSPATH%;lib\OpenRTM-aist-1.1.0.jar
set CLASSPATH=%CLASSPATH%;lib\HCharEncoder-1.0.jar

java -classpath %CLASSPATH% str2wstrComp -f conf/rtc.conf
pause

