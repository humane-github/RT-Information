@rem CameraDeviceComp���N��
@echo off

@rem jar�t�@�C���̖��O�iartifactId�j
set JAR_NAME=julius-client.jar
@rem ���C���N���X
set MAIN_CLASS=jp.co.humane.rtc.juliusclient.JuliusClientImpl
@rem conf�t�@�C���̖��O
set CONF_NAME=rtc.conf

@rem �t�@�C���̑��݂���f�B���N�g���̃p�X�Ɉړ�
set CURRENT_DIR=%~dp0
cd %CURRENT_DIR%

@rem �N���X�p�X��ݒ�
set CLASSPATH=%CLASSPATH%;"%JAR_NAME%"

@rem utf8�ŃR���\�[���\��
chcp 65001

@rem RTC�N��
java -classpath %CLASSPATH% -Dfile.encoding=UTF-8 %MAIN_CLASS% -f "%CONF_NAME%"

pause
