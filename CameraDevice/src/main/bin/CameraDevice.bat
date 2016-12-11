@rem CameraDeviceCompを起動

@rem ファイルの存在するディレクトリのパスに移動
set CURRENT_DIR=%~dp0
cd "%CURRENT_DIR%"

@rem クラスパスを設定
set CLASSPATH=%CLASSPATH%;"%CURRENT_DIR%camera-device.jar"

@rem RTC起動
java -classpath %CLASSPATH% CameraDeviceComp -f "%CURRENT_DIR%rtc.conf"

pause
