@rem CameraDeviceComp���N��
@echo off

@rem jar�t�@�C���̖��O�iartifactId�j
set JAR_NAME=camera-device.jar
@rem conf�t�@�C���̖��O
set CONF_NAME=rtc.conf
@rem ���C���N���X
set MAIN_CLASS=CameraDeviceComp

@rem �t�@�C���̑��݂���f�B���N�g���̃p�X�Ɉړ�
set CURRENT_DIR=%~dp0
cd %CURRENT_DIR%

@rem �N���X�p�X��ݒ�
set CLASSPATH=%CLASSPATH%;"%JAR_NAME%"

@rem RTC�N��
java -classpath %CLASSPATH% %MAIN_CLASS% -f "%CONF_NAME%"

pause