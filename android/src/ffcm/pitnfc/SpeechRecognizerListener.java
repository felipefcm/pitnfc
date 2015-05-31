
package ffcm.pitnfc;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

public class SpeechRecognizerListener implements RecognitionListener
{
	private LegoApp app;
	private AndroidLauncher launcher;
	
	public void SetApp(LegoApp app, AndroidLauncher launcher)
	{
		this.app = app;
		this.launcher = launcher;
	}
	
	@Override
	public void onReadyForSpeech(Bundle params)
	{
        Log.d("PIT_NFC", "onReadyForSpeech");
	}

	@Override
	public void onBeginningOfSpeech()
	{
        Log.d("PIT_NFC", "onBeginningOfSpeech");
	}

	@Override
	public void onRmsChanged(float rmsdB)
	{
	}

	@Override
	public void onBufferReceived(byte[] buffer)
	{
        Log.d("PIT_NFC", "onBufferReceived");
	}

	@Override
	public void onEndOfSpeech()
	{
        Log.d("PIT_NFC", "onEndOfSpeech");
	}

	@Override
	public void onError(int error)
	{
        Log.d("PIT_NFC", "onError: " + error);

        if(error != SpeechRecognizer.ERROR_CLIENT)
            launcher.androidSpeechRecognition.OnError();
	}

	@Override
	public void onResults(Bundle results)
	{
        Log.d("PIT_NFC", "onResults");

        ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if(data == null)
        {
            Log.d("PIT_NFC", "Speech result data is null");
            return;
        }

        String[] result = new String[data.size()];

        for(int i = 0; i < data.size(); ++i)
        {
        	Log.d("PIT_NFC", "Speech recognition result[" + i + "]: " + data.get(i));
        	result[i] = data.get(i);
        }

        if(app == null)
        {
            Log.d("PIT_NFC", "Will not call App because reference is null");
            return;
        }

        if(data.size() > 0)
            app.ReceivedSpeechRecognition(result);
	}

	@Override
	public void onPartialResults(Bundle partialResults)
	{
        Log.d("PIT_NFC", "onPartialResults");
	}

	@Override
	public void onEvent(int eventType, Bundle params)
	{
        Log.d("PIT_NFC", "onEvent");
	}	
}
