@rem InfoClerkManager���N��

@rem �t�@�C���̑��݂���f�B���N�g���̃p�X���擾
set CURRENT_DIR=%~dp0
cd "%CURRENT_DIR%"

@rem �N���X�p�X��ݒ�
set CLASSPATH=%CLASSPATH%;"%CURRENT_DIR%infoclerk-manager.jar"

@rem RTC�N��
java -classpath %CLASSPATH% CameraDeviceComp -f "%CURRENT_DIR%rtc.conf"

pause
