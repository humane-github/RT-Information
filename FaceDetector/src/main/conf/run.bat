@rem CameraDeviceCompを起動
@echo off

@rem コマンドプロンプトタイトル変更
title CameraDevice

@rem jarファイルの名前（artifactId）
set JAR_NAME=face-detector.jar
@rem メインクラス
set MAIN_CLASS=jp.co.humane.rtc.facedetector.FaceDetectorImpl
@rem opencvライブラリパス
set OPENCV_LIB=\\ls-wsxl973\p-寺田\環境構築\opencv\x64
@rem confファイルの名前
set CONF_NAME=rtc.conf

@rem ファイルの存在するディレクトリのパスに移動
set CURRENT_DIR=%~dp0
cd %CURRENT_DIR%

@rem クラスパスを設定
set CLASSPATH=%CLASSPATH%;"%JAR_NAME%"

@rem RTC起動
java -classpath %CLASSPATH% -Djava.library.path=%OPENCV_LIB% %MAIN_CLASS% -f "%CONF_NAME%"

pause
