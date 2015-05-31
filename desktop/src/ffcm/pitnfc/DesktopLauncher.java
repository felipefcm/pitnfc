
package ffcm.pitnfc;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher 
{
	public static void main (String[] arg) 
	{
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		
		config.width = (int)(LegoApp.V_WIDTH * LegoApp.DESKTOP_SCALE);
		config.height = (int)(LegoApp.V_HEIGHT * LegoApp.DESKTOP_SCALE);
		
		new LwjglApplication(new LegoApp(null, null, true), config);
	}
}
