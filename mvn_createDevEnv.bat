@echo off

@rem 開発環境構築用バッチファイル
@rem gitでファイルをダウンロード後に本バッチを起動することで開発できる環境にする。
@rem mvnの社内リポジトリとしてNAS(\\LS-WSXL973)を参照しているので、接続できる状態で実行すること。

@rem targetディレクトリを削除
echo ####### clean処理 #######
call mvn clean

@rem UserMasterは他プロジェクトから参照されるのでビルドしておく
echo ####### 事前ビルド処理 #######
call mvn -f UserMaster\pom.xml        dependency:copy-dependencies -DoutputDirectory=src/main/lib
if not "%ERRORLEVEL%" == "0" exit /b
call mvn -f UserMaster\pom.xml package
if not "%ERRORLEVEL%" == "0" exit /b

@rem 依存するjarファイルをダウンロード
echo ####### jarファイルダウンロード処理 #######
call mvn -f CameraDevice\pom.xml      dependency:copy-dependencies -DoutputDirectory=src/main/lib
if not "%ERRORLEVEL%" == "0" exit /b
call mvn -f FaceDetector\pom.xml      dependency:copy-dependencies -DoutputDirectory=src/main/lib
if not "%ERRORLEVEL%" == "0" exit /b
call mvn -f InfoClerkManager\pom.xml  dependency:copy-dependencies -DoutputDirectory=src/main/lib
if not "%ERRORLEVEL%" == "0" exit /b
call mvn -f JuliusClient\pom.xml      dependency:copy-dependencies -DoutputDirectory=src/main/lib
if not "%ERRORLEVEL%" == "0" exit /b
call mvn -f MotionDetector\pom.xml    dependency:copy-dependencies -DoutputDirectory=src/main/lib
if not "%ERRORLEVEL%" == "0" exit /b
call mvn -f str2wstr\pom.xml          dependency:copy-dependencies -DoutputDirectory=src/main/lib
if not "%ERRORLEVEL%" == "0" exit /b
call mvn -f UserMaster\pom.xml        dependency:copy-dependencies -DoutputDirectory=src/main/lib
if not "%ERRORLEVEL%" == "0" exit /b


@rem eclipseからビルド可能にするための処理
@rem テストクラスを一旦作れば不要
if not exist CameraDevice\src\test\java mkdir ConfigLib\src\test\java
if not exist FaceDetector\src\test\java mkdir DBUtility\src\test\java
if not exist InfoClerkManager\src\test\java mkdir ExceptionLib\src\test\java
if not exist JuliusClient\src\test\java mkdir FileOperatorUtil\src\test\java
if not exist MotionDetector\src\test\java mkdir HCharEncoder\src\test\java
if not exist str2wstr\src\test\java mkdir Logger\src\test\java
if not exist UserMaster\src\test\java mkdir MessageLib\src\test\java


echo ####### 正常終了しました #######
pause
exit /b
