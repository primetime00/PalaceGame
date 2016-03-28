package com.kegelapps.palace.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.kegelapps.palace.Palace;

public class DesktopLauncher {

	public enum DisplayType {
		TAB_A,
		NEXUS_5,
		DESKTOP,
		NEXUS_7
	}

	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config = setupConfig(DisplayType.TAB_A);
		new LwjglApplication(new Palace(), config);
	}

	private static LwjglApplicationConfiguration setupConfig(DisplayType disp) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		switch (disp) {
			case TAB_A:
				config.width = 1024;
				config.height = 768;
				config.overrideDensity = 160;
				break;
			case DESKTOP:
				config.width = 1280;
				config.height = 720;
				break;
			case NEXUS_5:
				config.width = 1920;
				config.height = 1080;
				config.overrideDensity = 441;
				break;
			case NEXUS_7:
				config.width = 1024;
				config.height = 768;
				config.overrideDensity = 323;
				break;
		}
		return config;
	}
}
