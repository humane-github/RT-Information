@rem install rtc
@echo off

@rem 英語表記に変更(maven installの文字化け対策)
chcp 437

@rem jarファイル名（artifactId）
set JAR_NAME=infoclerk-manager
@rem confファイル名
set CONF_NAME=InfoClerkManager.conf

@rem batの存在ディレクトリ及びプロジェクトルートのディレクトリを取得
set CONF_DIR=%~dp0
pushd "%CONF_DIR%"
cd ..\..\..
set PRJ_ROOT=%CD%
cd ..

@rem targetディレクトリの存在確認
if not exist "%PRJ_ROOT%\target" (
  echo [install.bat] Not exist target directory.Do install.bat after compile.
  exit
)

@rem jarファイルの存在確認
if not exist "%PRJ_ROOT%\target\%JAR_NAME%.jar" (
  echo [install.bat] Not exist %JAR_NAME%.jar file. Do install.bat after compile.
  exit
)

@rem binディレクトリがなければ作成
if not exist "bin" (
  mkdir bin
)

@rem JAR_NAMEのディレクトリがなければ作成
if not exist "bin\%JAR_NAME%" (
  mkdir bin\%JAR_NAME%
)

@rem 配置先ディレクトリを取得
cd bin\%JAR_NAME%
set TARGET_DIR=%CD%

@rem 各種ファイルをコピー
copy /Y "%CONF_DIR%run.bat"                "%TARGET_DIR%\run.bat"
copy /Y "%CONF_DIR%rtc.conf"               "%TARGET_DIR%\rtc.conf"
copy /Y "%CONF_DIR%%CONF_NAME%"            "%TARGET_DIR%\%CONF_NAME%"
copy /Y "%PRJ_ROOT%\target\%JAR_NAME%.jar" "%TARGET_DIR%\%JAR_NAME%.jar"
copy /Y "%CONF_DIR%mapConfig.csv"          "%TARGET_DIR%\mapConfig.csv"
xcopy /Y /E "%CONF_DIR%image"              "%TARGET_DIR%\image\"

@rem 終了コメント
popd
echo [install.bat] install success.
