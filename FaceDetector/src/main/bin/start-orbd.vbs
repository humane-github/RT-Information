'orbd�N���p�X�N���v�g
'�{�X�N���v�g�͊��ϐ�TEMP���ݒ肳��Ă��邱�Ƃ�O��Ƃ��܂�
'�Ȃ��A���ϐ�TEMP�͒ʗ�OS�ɂ��f�t�H���g�Őݒ肳��Ă��܂�


'�N���p�I�u�W�F�N�g�̎擾
Set objShell = WScript.CreateObject("WScript.Shell")

strMode = objShell.Environment("Process").Item("PROCESSOR_ARCHITECTURE")

'JDK�̃��W�X�g���L�[���Z�b�g
If UCase(strMode) = "X86" Then
	regJDKkey  = "HKLM\SOFTWARE\JavaSoft\Java Development Kit"
Else
	regJDKkey  = "HKLM\SOFTWARE\Wow6432Node\JavaSoft\Java Development Kit"
End If

'���W�X�g������JDK�J�����g�o�[�W�������擾
'objShell.RegRead("HKLM\SOFTWARE\JavaSoft\Java Development Kit\CurrentVersion")

'���ꂪ�A�Ⴆ��"1.5"���Ƃ���ƁA
'HKLM\SOFTWARE\JavaSoft\Java Development Kit\1.5\JavaHome
'��JDK�J�����g�o�[�W�����̃��[�g�t�H���_���L�ڂ���Ă���
Javahome  = regJDKkey & "\" & objShell.RegRead(regJDKkey & "\CurrentVersion") & "\JavaHome"

'JDK�J�����g�o�[�W�����̃��[�g�t�H���_Javahome�̉���bin\orbd.exe���ړI�̎��s�t�@�C��
targetexe = """" & objShell.RegRead(Javahome) & "\bin\orbd.exe"""

'�ړI�̎��s�t�@�C��targetexe��K�؂ȃI�v�V���������Ď��s������
objShell.Run targetexe & " -ORBInitialPort 2809 -ORBInitialHost localhost -defaultdb ""%TEMP%""\orb.db"
'����͗Ⴆ�΁A���̂悤�Ȃ��Ƃ�����Ă�B�������A��ƃf�B���N�g�������[�U�[��temp�t�H���_�Ɏw��B
'cf:objShell.Run """C:\Program Files\Java\jdk1.5.0_14\bin\orbd.exe"" -ORBInitialPort 2809 -ORBInitialHost localhost"

'�ꉞ�I�u�W�F�N�g���J��
Set objShell = Nothing


' **********************************************************
' OS �o�[�W�����̎擾
' **********************************************************
Function GetOSVersion()

    Dim strComputer, Wmi, colTarget, strWork, objRow, aData

    strComputer = "."
    Set Wmi = GetObject("winmgmts:{impersonationLevel=impersonate}!\\" & strComputer & "\root\cimv2")
    Set colTarget = Wmi.ExecQuery( "select Version from Win32_OperatingSystem" )

    For Each objRow in colTarget
        strWork = objRow.Version
        Next

        aData = Split( strWork, "." )
        strWork = aData(0) & "." & aData(1)

        GetOSVersion = CDbl( strWork )

End Function