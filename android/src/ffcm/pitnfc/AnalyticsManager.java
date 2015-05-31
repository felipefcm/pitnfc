
package ffcm.pitnfc;

import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class AnalyticsManager implements IAnalyticsManager
{
    private static AnalyticsManager instance = null;

    public static AnalyticsManager GetInstance()
    {
        if(instance == null)
            instance = new AnalyticsManager();

        return instance;
    }

    public static AndroidLauncher launcher;

    private Tracker tracker;

    public AnalyticsManager()
    {
    }

    public void Init()
    {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(launcher);

        if(analytics == null)
        {
            Log.d("PIT_NFC", "GoogleAnalytics instance is null");
            return;
        }

        try
        {
            tracker = analytics.newTracker(R.xml.app_tracker);
        }
        catch(Exception e)
        {
            Log.d("PIT_NFC", "GoogleAnalytics new tracker failed with exception: " + e.getMessage());
            return;
        }

        if(tracker == null)
        {
            Log.d("PIT_NFC", "GoogleAnalytics new tracker failed");
            return;
        }

        Log.d("PIT_NFC", "GoogleAnalytics successfully started");
    }

    @Override
    public void OnReceivedTagText(String tagText)
    {
        if(tracker == null)
            return;

        tracker.send
        (
            new HitBuilders.EventBuilder()
            .setCategory("ReceivedTagText")
            .setAction("Received")
            .setLabel(tagText)
            .setValue(1)
            .build()
        );
    }

    @Override
    public void StartedApplication()
    {
        if(tracker == null)
            return;

        tracker.setScreenName("AndroidLauncher");
        tracker.send
        (
            new HitBuilders.ScreenViewBuilder()
            .build()
        );
    }
}
