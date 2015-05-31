
package ffcm.pitnfc;

import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class AndroidLauncher extends AndroidApplication 
{
	private LegoApp app;
	
	private NfcAdapter nfcAdapter;
	
	public TextToSpeech tts;
	public AndroidTTS androidTTS;
	
	public SpeechRecognizer speechRecognizer;
	public AndroidSpeechRecognition androidSpeechRecognition;
    private SpeechRecognizerListener speechListener;
    private AnalyticsManager analyticsManager;

    private MessageHandler messageHandler;

    public AlertDialog.Builder alertDlgBuilder;

    private static boolean fgDispatchEnabled;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useWakelock = true;
		config.useAccelerometer = false;
		config.useCompass = false;

		fgDispatchEnabled = false;

		alertDlgBuilder = new AlertDialog.Builder(this);

        InitNfcAdapter();
        InitTTS();
        InitSpeechRecognizer();
        AnalyticsManager.launcher = this;

        messageHandler = new MessageHandler();

		androidTTS = new AndroidTTS(tts);
		tts.setOnUtteranceCompletedListener(androidTTS);

		androidSpeechRecognition = new AndroidSpeechRecognition(speechRecognizer, messageHandler, this);

        messageHandler.SetSR(androidSpeechRecognition);

        analyticsManager = AnalyticsManager.GetInstance();
        analyticsManager.Init();

        PlatformInterfaceData data = new PlatformInterfaceData();
        data.ttsInterface = androidTTS;
        data.speechRecognitionInterface = androidSpeechRecognition;
        data.analyticsManager = analyticsManager;
        data.mainLauncherIntent = getIntent().getAction().equals(Intent.ACTION_MAIN);

        Log.d("PIT_NFC", "Setting 'startedByMainLaucherIntent' to " + Boolean.toString(data.mainLauncherIntent) + " because intent action is: " + getIntent().getAction());

        app = new LegoApp(data);
        speechListener.SetApp(app, this);
		initialize(app, config);

		HandleIntent(getIntent());
	}

    @Override
    protected void onResume() 
	{
        super.onResume();
         
        SetupForegroundDispatch(this, nfcAdapter);
    }
	
	@Override
    protected void onPause() 
	{
        speechRecognizer.stopListening();

        StopForegroundDispatch(this, nfcAdapter);
         
        super.onPause();
    }
	
	@Override
	protected void onDestroy()
	{
		tts.shutdown();
        speechRecognizer.stopListening();
        speechRecognizer.destroy();
		
		super.onDestroy();
	}
	
	@Override
    protected void onNewIntent(Intent intent) 
	{ 
        HandleIntent(intent);
    }
	
	private void InitNfcAdapter()
	{
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		
		if(nfcAdapter == null)
		{
		    Log.d("PIT_NFC", "Failed to get NfcAdapter");
		}
		else
		{
			if(!nfcAdapter.isEnabled())
			{
                Log.d("PIT_NFC", "NFC is disabled");
			}
			else
			{
                Log.d("PIT_NFC", "NFC adapter found");
			}
		}
	}
	
	private void InitTTS()
	{
		tts = new TextToSpeech
		(
			getApplicationContext(),
			new TextToSpeech.OnInitListener()
			{
				@Override
				public void onInit(int status)
				{
				    if(status == TextToSpeech.ERROR)
                    {
                        alertDlgBuilder.setTitle("Erro");
                        alertDlgBuilder.setMessage("Nenhuma interface de voz encontrada");
                        alertDlgBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    finish();
                                }
                            });
                        alertDlgBuilder.setCancelable(false);
                        alertDlgBuilder.create().show();
                    }

					Locale locale = new Locale("pt_BR");
					
					int langAvail = tts.isLanguageAvailable(locale);
					
					if(langAvail == TextToSpeech.LANG_MISSING_DATA || langAvail == TextToSpeech.LANG_NOT_SUPPORTED)
					{
						locale = Locale.getDefault();
						Log.d("PIT_NFC", "pt_BR language check returned " + langAvail);
						Log.d("PIT_NFC", "pt_BR not found, setting default");
					}
					else
					{
						Log.d("PIT_NFC", "pt_BR found");
					}
					
					tts.setLanguage(locale);

					Log.d("PIT_NFC", "TTS started successfully");
				}
			}
		);

		SystemClock.sleep(350);
	}
	
	public void InitSpeechRecognizer()
	{
		if(!SpeechRecognizer.isRecognitionAvailable(this))
		{
            Log.d("PIT_NFC", "Speech recognition is not available");
			return;
		}
		else
		{
			Log.d("PIT_NFC", "Speech recognition is available");
		}
		
		speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
		speechRecognizer.setRecognitionListener(speechListener = new SpeechRecognizerListener());
    }
	
	private void HandleIntent(Intent intent)
	{
		String action = intent.getAction();

        Log.d("PIT_NFC", "HandleIntent() called with action: " + action);
		
	    if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action))
	    {
	        String type = intent.getType();
	        
	        if(type.equals("text/plain"))
	        {
	            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                Log.d("PIT_NFC", "Calling NdefReaderTask for NDEF compatible tag");
	            new NdefReaderTask(app).execute(tag);     
	        } 
	        else 
	        {
	            Log.d("PIT_NFC", "Wrong MIME type: " + type);
	        }
	    } 
	    else if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) 
    	{
	        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	        String[] techList = tag.getTechList();
	        String searchedTech = Ndef.class.getName();
	         
	        for (String tech : techList) 
	        {
	            if (searchedTech.equals(tech)) 
	            {
                    Log.d("PIT_NFC", "Calling NdefReaderTask with tech: " + searchedTech);
	                new NdefReaderTask(app).execute(tag);
	                break;
	            }
	        }
    	}
	}
	
	public static void SetupForegroundDispatch(final Activity activity, NfcAdapter adapter) 
	{
        Log.d("PIT_NFC", "Setting up foreground dispatch");

		if(adapter == null)
        {
            Log.d("PIT_NFC", "Adapter is null on SetupForegroundDispatch");
			return;
        }

        if(fgDispatchEnabled)
        {
            Log.d("PIT_NFC", "SetupForegroundDispatch is already enabled, skipping");
            return;
        }

        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 
        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);
 
        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};
 
        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        
        try 
        {
            filters[0].addDataType("text/plain");
        } 
        catch(MalformedMimeTypeException e) 
        {
            throw new RuntimeException("Check your mime type.");
        }
         
        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);

        fgDispatchEnabled = true;
    }
	
	public static void StopForegroundDispatch(final Activity activity, NfcAdapter adapter) 
	{
        Log.d("PIT_NFC", "Stopping foreground dispatch");

		if(adapter == null)
        {
            Log.d("PIT_NFC", "Adapter is null on stopping foreground dispatch");
			return;
        }

        adapter.disableForegroundDispatch(activity);

        fgDispatchEnabled = false;
    }
}
