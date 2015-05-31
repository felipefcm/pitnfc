
package ffcm.pitnfc;

import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.HashMap;

import ffcm.pitnfc.TTSInterface;

public class AndroidTTS implements TTSInterface, TextToSpeech.OnUtteranceCompletedListener
{
	private TextToSpeech tts;
	private boolean isSpeaking;

    private HashMap<String, String> ttsParams;
	
	public AndroidTTS(TextToSpeech tts)
	{
		this.tts = tts;
		isSpeaking = false;

        ttsParams = new HashMap<String, String>();
        ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "pitnfc_uID");
	}
	
	@Override
	public void Speak(String text)
	{
	    if(tts == null)
	        return;

	    isSpeaking = true;
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, ttsParams);
	}

    @Override
    public void SpeakBlocker(String text)
    {
        Log.d("PIT_NFC", "Starting blocker speak request, text: " + text);

        Speak(text);

        while(isSpeaking)
            SystemClock.sleep(150);
    }

    @Override
	public String GetLanguage()
	{
		return tts.getLanguage().getDisplayLanguage();
	}

    @Override
    public boolean IsSpeaking()
    {
        return isSpeaking;
    }

    @Override
    public void onUtteranceCompleted(String utteranceId)
    {
        if(utteranceId.equalsIgnoreCase("pitnfc_uID"))
            isSpeaking = false;
    }
}
