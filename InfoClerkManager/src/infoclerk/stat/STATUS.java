package infoclerk.stat;
import jp.co.humane.statemachine.BaseStatus;

public enum STATUS implements BaseStatus
{
	IDLE,
	FACE_DETECT,
	SELECT_USER_WAIT,
	SELECT_USER,
	CONFIRM_SELECT_USER_WAIT,
	CONFIRM_SELECT_USER,
	RESPONSE_WAIT,
	SPEECH,
	ERROR
}
