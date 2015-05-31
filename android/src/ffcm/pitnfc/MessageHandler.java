
package ffcm.pitnfc;

import android.os.Handler;
import android.os.Message;

public class MessageHandler extends Handler
{
    private AndroidSpeechRecognition sr;

    public void SetSR(AndroidSpeechRecognition sr)
    {
        this.sr = sr;
    }

    @Override
    public void handleMessage(Message msg)
    {
        switch(msg.what)
        {
            case AndroidSpeechRecognition.StartListening:
                sr.StartSpeechListening();
            break;

            case AndroidSpeechRecognition.StopListening:
                sr.StopSpeechListening();
            break;
        }
    }
}
