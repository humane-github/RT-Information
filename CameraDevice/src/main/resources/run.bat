@rem CameraDeviceComp���N��
@echo off

@rem jar�t�@�C���̖��O�iartifactId�j
set JAR_NAME=camera-device.jar
@rem conf�t�@�C���̖��O
set CONF_NAME=rtc.conf
@rem opencv���C�u�����p�X
set OPENCV_LIB=D:\work\dev\opencv\opencv\build\java\x64
@rem ���C���N���X
set MAIN_CLASS=jp.co.humane.rtc.cameradevice.CameraDeviceImpl

@rem �t�@�C���̑��݂���f�B���N�g���̃p�X�Ɉړ�
set CURRENT_DIR=%~dp0
cd %CURRENT_DIR%

@rem �N���X�p�X��ݒ�
set CLASSPATH=%CLASSPATH%;"%JAR_NAME%"

@rem RTC�N��
java -classpath %CLASSPATH% -Djava.library.path=%OPENCV_LIB% %MAIN_CLASS% -f "%CONF_NAME%"

pause
