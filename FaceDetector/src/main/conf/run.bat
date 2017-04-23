@rem CameraDeviceComp���N��
@echo off

@rem �R�}���h�v�����v�g�^�C�g���ύX
title CameraDevice

@rem jar�t�@�C���̖��O�iartifactId�j
set JAR_NAME=face-detector.jar
@rem ���C���N���X
set MAIN_CLASS=jp.co.humane.rtc.facedetector.FaceDetectorImpl
@rem opencv���C�u�����p�X
set OPENCV_LIB=\\ls-wsxl973\p-���c\���\�z\opencv\x64
@rem conf�t�@�C���̖��O
set CONF_NAME=rtc.conf

@rem �t�@�C���̑��݂���f�B���N�g���̃p�X�Ɉړ�
set CURRENT_DIR=%~dp0
cd %CURRENT_DIR%

@rem �N���X�p�X��ݒ�
set CLASSPATH=%CLASSPATH%;"%JAR_NAME%"

@rem RTC�N��
java -classpath %CLASSPATH% -Djava.library.path=%OPENCV_LIB% %MAIN_CLASS% -f "%CONF_NAME%"

pause
