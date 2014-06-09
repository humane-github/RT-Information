@echo off

@rem �J�����\�z�p�o�b�`�t�@�C��
@rem git�Ńt�@�C�����_�E�����[�h��ɖ{�o�b�`���N�����邱�ƂŊJ���ł�����ɂ���B
@rem mvn�̎Г����|�W�g���Ƃ���NAS(\\LS-WSXL973)���Q�Ƃ��Ă���̂ŁA�ڑ��ł����ԂŎ��s���邱�ƁB

@rem target�f�B���N�g�����폜
echo ####### clean���� #######
call mvn clean

@rem UserMaster�͑��v���W�F�N�g����Q�Ƃ����̂Ńr���h���Ă���
echo ####### ���O�r���h���� #######
call mvn -f UserMaster\pom.xml        dependency:copy-dependencies -DoutputDirectory=src/main/lib
if not "%ERRORLEVEL%" == "0" exit /b
call mvn -f UserMaster\pom.xml package
if not "%ERRORLEVEL%" == "0" exit /b

@rem �ˑ�����jar�t�@�C�����_�E�����[�h
echo ####### jar�t�@�C���_�E�����[�h���� #######
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


@rem eclipse����r���h�\�ɂ��邽�߂̏���
@rem �e�X�g�N���X����U���Εs�v
if not exist CameraDevice\src\test\java mkdir ConfigLib\src\test\java
if not exist FaceDetector\src\test\java mkdir DBUtility\src\test\java
if not exist InfoClerkManager\src\test\java mkdir ExceptionLib\src\test\java
if not exist JuliusClient\src\test\java mkdir FileOperatorUtil\src\test\java
if not exist MotionDetector\src\test\java mkdir HCharEncoder\src\test\java
if not exist str2wstr\src\test\java mkdir Logger\src\test\java
if not exist UserMaster\src\test\java mkdir MessageLib\src\test\java


echo ####### ����I�����܂��� #######
pause
exit /b
