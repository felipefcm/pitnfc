
package ffcm.pitnfc;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import ffcm.pitnfc.decision.UserInteraction;

public class LegoApp extends ApplicationAdapter 
{
	public static final int V_WIDTH = 720;
	public static final int V_HEIGHT = 1280;
	public static final float DESKTOP_SCALE = 0.7f;

	private static final String InitialInstructions = "Encoste a traseira do aparelho na etiqueta NFC, e responda após o sinal.";
	
	private SpriteBatch spriteBatch;
	private BitmapFont font;
    private OrthographicCamera camera;
    private FitViewport viewport;

    private Texture nfcTexture;
	private Texture fllTexture;
	private Texture insaTexture;
	private Texture botsTexture;
    private Texture modeButtonOnTexture;
	private Texture modeButtonOffTexture;

	private Sprite nfcSprite;
	private Sprite fllSprite;
	private Sprite insaSprite;
	private Sprite botsSprite;
	private Sprite modeButtonOnSprite;
    private Sprite modeButtonOffSprite;
	
	private TTSInterface ttsInterface;
	private SpeechRecognitionInterface speechRecognitionInterface;

	public IAnalyticsManager analyticsManager;

    private boolean interactiveMode;
    private boolean modeButtonVisible;

	private UserInteraction userInteraction;

	private boolean mainLauncherIntent;
	
	public LegoApp(PlatformInterfaceData data)
	{
		ttsInterface = data.ttsInterface;
		speechRecognitionInterface = data.speechRecognitionInterface;
		mainLauncherIntent = data.mainLauncherIntent;
        analyticsManager = data.analyticsManager;
	}
	
	@Override
	public void create() 
	{
		spriteBatch = new SpriteBatch(100);
		font = new BitmapFont();

		camera = new OrthographicCamera();
		viewport = new FitViewport(V_WIDTH, V_HEIGHT, camera);
		
		nfcTexture = new Texture(Gdx.files.internal("nfc.png"));
		fllTexture = new Texture(Gdx.files.internal("fll.png"));
		insaTexture = new Texture(Gdx.files.internal("insa.png"));
		botsTexture = new Texture(Gdx.files.internal("insabots.png"));
		modeButtonOnTexture = new Texture(Gdx.files.internal("modeButtonOn.png"));
        modeButtonOffTexture = new Texture(Gdx.files.internal("modeButtonOff.png"));

        interactiveMode = true;
        modeButtonVisible = true;

		userInteraction = new UserInteraction(ttsInterface, speechRecognitionInterface);
		userInteraction.Init();
		
		nfcTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		fllTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		insaTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		botsTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        modeButtonOnTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        modeButtonOffTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		nfcSprite = new Sprite(nfcTexture);
		fllSprite = new Sprite(fllTexture);
		insaSprite = new Sprite(insaTexture);
		botsSprite = new Sprite(botsTexture);
		modeButtonOnSprite = new Sprite(modeButtonOnTexture);
        modeButtonOffSprite = new Sprite(modeButtonOffTexture);
		
		insaSprite.setOriginCenter();
		fllSprite.setOriginCenter();
		
		nfcSprite.setPosition((V_WIDTH - nfcSprite.getWidth()) / 2.0f, (V_HEIGHT - nfcSprite.getHeight()) / 2.0f);
		botsSprite.setPosition((V_WIDTH - botsSprite.getWidth()) / 2.0f, V_HEIGHT * 0.15f);
		insaSprite.setPosition(V_WIDTH * 0.1f, V_HEIGHT * 0.68f);
		fllSprite.setPosition(V_WIDTH * 0.55f, V_HEIGHT * 0.68f);
		modeButtonOnSprite.setPosition((V_WIDTH - modeButtonOnSprite.getWidth()) / 2.0f, V_HEIGHT * 0.07f);
        modeButtonOffSprite.setPosition((V_WIDTH - modeButtonOffSprite.getWidth()) / 2.0f, V_HEIGHT * 0.07f);
		
		Gdx.gl.glClearColor(0.32f, 0.6f, 0.78f, 1.0f);

        GestureDetector gestureProcessor = new GestureDetector
        (
            new GestureDetector.GestureAdapter()
            {
                @Override
                public boolean tap(float x, float y, int count, int button)
                {
                    SpeakInitialInstructions(false);
                    return true;
                }

                @Override
                public boolean longPress(float x, float y)
                {
                    if(modeButtonVisible)
                    {
                        interactiveMode = !interactiveMode;

                        if(ttsInterface != null)
                        {
                            if(interactiveMode)
                                ttsInterface.Speak("Modo interativo ativado");
                            else
                                ttsInterface.Speak("Modo interativo desativado");
                        }
                    }

                    //simulate tag text to test
                    //ReceivedTagText("equilatero");

                    return true;
                }
            }
        );

        gestureProcessor.setLongPressSeconds(0.5f);

        InputMultiplexer inputMultiplexer = new InputMultiplexer(gestureProcessor);
        Gdx.input.setInputProcessor(inputMultiplexer);

        if(mainLauncherIntent)
		    SpeakInitialInstructions(false);

		Gdx.app.postRunnable
		(
		    new Runnable()
		    {
                @Override
                public void run()
                {
                    if(analyticsManager != null)
                        analyticsManager.StartedApplication();
                }
            }
        );
	}

	@Override
	public void render() 
	{
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        modeButtonVisible = userInteraction.GetState() == UserInteraction.InteractionState.NotStarted;
		
		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.begin();
		{
			nfcSprite.draw(spriteBatch);
			fllSprite.draw(spriteBatch);
			insaSprite.draw(spriteBatch);
			botsSprite.draw(spriteBatch);

            if(modeButtonVisible)
            {
                if(interactiveMode)
                    modeButtonOnSprite.draw(spriteBatch);
                else
                    modeButtonOffSprite.draw(spriteBatch);
            }
		}
		spriteBatch.end();
	}
	
	@Override
	public void resize(int width, int height)
	{
		super.resize(width, height);
		
		viewport.update(width, height, true);
	}
	
	public void ReceivedTagText(String text)
	{
	    if(interactiveMode)
        {
            userInteraction.ReceivedTag(text);
        }
        else
        {
            if(ttsInterface != null)
            {
                if(text.equalsIgnoreCase("equilatero"))
                    ttsInterface.SpeakBlocker("Isto é um triângulo equilátero. Os três lados possuem o mesmo comprimento");
                else if(text.equalsIgnoreCase("isosceles"))
                    ttsInterface.SpeakBlocker("Isto é um triângulo isósceles. De seus 3 lados, dois possuem o mesmo comprimento");
                else if(text.equalsIgnoreCase("quadrado"))
                    ttsInterface.SpeakBlocker("Isto é um quadrado. Seus quatro lados são iguais");
                else if(text.equalsIgnoreCase("retangulo"))
                    ttsInterface.SpeakBlocker("Isto é um retângulo: lados opostos iguais e paralelos");
                else
                {
                    ttsInterface.SpeakBlocker("Etiqueta desconhecida");
                    return;
                }
            }
        }
	}
	
	public void ReceivedSpeechRecognition(String[] text)
	{
        if(interactiveMode)
            userInteraction.ReceivedSpeech(text);
	}

	private void SpeakApplicationRunning()
    {
        if(ttsInterface == null)
            return;

        if(ttsInterface.GetLanguage().toLowerCase().contains("en"))
        {
            ttsInterface.Speak("Application running");
        }
        else if(ttsInterface.GetLanguage().toLowerCase().contains("por") ||
                ttsInterface.GetLanguage().toLowerCase().contains("pt"))
        {
            ttsInterface.Speak("Aplicação em execução");
        }
    }

    private void SpeakInitialInstructions(boolean blocker)
    {
        if(ttsInterface == null)
            return;

        if(blocker)
            ttsInterface.SpeakBlocker(InitialInstructions);
        else
            ttsInterface.Speak(InitialInstructions);
    }
	
	@Override
	public void dispose()
	{
		super.dispose();
		
		nfcTexture.dispose();
		fllTexture.dispose();
		insaTexture.dispose();
		botsTexture.dispose();
		modeButtonOnTexture.dispose();
		modeButtonOffTexture.dispose();
		
		spriteBatch.dispose();
		font.dispose();
	}
}
