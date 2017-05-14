@rem CameraDeviceCompを起動
@echo off

@rem ファイルの存在するディレクトリのパスに移動
set CURRENT_DIR=%~dp0
cd %CURRENT_DIR%

@rem コマンドプロンプトタイトル変更
title %CURRENT_DIR:*bin\=%%~nx0
@rem 日本語を扱えるようにする
chcp 932

@rem jarファイルの名前（artifactId）
set JAR_NAME=camera-device.jar
@rem confファイルの名前
set CONF_NAME=rtc.conf
@rem opencvライブラリパス
set OPENCV_LIB=\\ls-wsxl973\p-寺田\環境構築\opencv\x64
@rem メインクラス
set MAIN_CLASS=jp.co.humane.rtc.cameradevice.CameraDeviceImpl

@rem クラスパスを設定
set CLASSPATH=%CLASSPATH%;"%JAR_NAME%"

@rem RTC起動
java -classpath %CLASSPATH% -Djava.library.path=%OPENCV_LIB% %MAIN_CLASS% -f "%CONF_NAME%"

pause
