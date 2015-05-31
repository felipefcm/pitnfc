
package ffcm.pitnfc;

public interface TTSInterface
{
	public void Speak(String text); //cancel all speeches in progress to perform request
	public void SpeakBlocker(String text); //start speaking and returns only after completion
	public String GetLanguage();
	public boolean IsSpeaking();
}
