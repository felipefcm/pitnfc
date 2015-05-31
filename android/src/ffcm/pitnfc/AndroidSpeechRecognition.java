
package ffcm.pitnfc;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

public class AndroidSpeechRecognition implements SpeechRecognitionInterface
{
    private static final int MaxResults = 5;

    //Message values
    public static final int StartListening = 0;
    public static final int StopListening = 1;
	
	private SpeechRecognizer speechRecognizer;
    private MessageHandler messageHandler;

    private AndroidLauncher launcher;

    private boolean isRunning;
	
	public AndroidSpeechRecognition(SpeechRecognizer speechRecognizer, MessageHandler handler, AndroidLauncher launcher)
	{
		this.speechRecognizer = speechRecognizer;
        this.messageHandler = handler;
        this.launcher = launcher;

        isRunning = false;
	}

	@Override
	public void StartListening()
	{
        messageHandler.sendEmptyMessage(StartListening);
	}

	@Override
	public void StopListening()
	{
        messageHandler.sendEmptyMessage(StopListening);
	}

    public void StartSpeechListening()
    {
        if(IsRunning())
            StopSpeechListening();

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt_BR");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MaxResults); //android seems to ignore this setting
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "ffcm.pitnfc");

        isRunning = true;
        Log.d("PIT_NFC", "SpeechRecognition will start listening");

        speechRecognizer.startListening(intent);
    }

    public void StopSpeechListening()
    {
        speechRecognizer.cancel();
        speechRecognizer.stopListening();

        isRunning = false;
        Log.d("PIT_NFC", "SpeechRecognition stopped listening");
    }

    public void OnError()
    {
        //launcher.InitSpeechRecognizer();
        //launcher.androidTTS.SpeakBlocker("Repita novamente, por favor");
        StartListening();
    }

    @Override
    public boolean IsRunning()
    {
        return isRunning;
    }
}
