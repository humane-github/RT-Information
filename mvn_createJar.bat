@echo off

@rem ビルドを行い、生成されたjarファイルを「_target」ディレクトリに集める
echo ####### ビルド処理 #######
call mvn package

echo ####### 正常に終了しました #######
pause

exit /b