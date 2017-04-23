@rem JuliusClientを起動
@echo off

@rem コマンドプロンプトタイトル変更
title JuliusClient

@rem jarファイルの名前（artifactId）
set JAR_NAME=julius-client.jar
@rem メインクラス
set MAIN_CLASS=jp.co.humane.rtc.juliusclient.JuliusClientImpl
@rem confファイルの名前
set CONF_NAME=rtc.conf

@rem ファイルの存在するディレクトリのパスに移動
set CURRENT_DIR=%~dp0
cd %CURRENT_DIR%

@rem クラスパスを設定
set CLASSPATH=%CLASSPATH%;"%JAR_NAME%"

@rem utf8でコンソール表示
chcp 65001

@rem RTC起動
java -classpath %CLASSPATH% -Dfile.encoding=UTF-8 %MAIN_CLASS% -f "%CONF_NAME%"

pause
