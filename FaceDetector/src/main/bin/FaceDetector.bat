@rem FaceDetector���N��

@rem �t�@�C���̑��݂���f�B���N�g���̃p�X���擾
set CURRENT_DIR=%~dp0
cd "%CURRENT_DIR%"

@rem �N���X�p�X��ݒ�
set CLASSPATH=%CLASSPATH%;"%CURRENT_DIR%face-detector.jar"

@rem RTC�N��
java -classpath %CLASSPATH% FaceDetectorComp -f "%CURRENT_DIR%rtc.conf"

pause
