@rem CameraDeviceComp���N��

@rem �t�@�C���̑��݂���f�B���N�g���̃p�X�Ɉړ�
set CURRENT_DIR=%~dp0
cd "%CURRENT_DIR%"

@rem �N���X�p�X��ݒ�
set CLASSPATH=%CLASSPATH%;"%CURRENT_DIR%camera-device.jar"

@rem RTC�N��
java -classpath %CLASSPATH% CameraDeviceComp -f "%CURRENT_DIR%rtc.conf"

pause
