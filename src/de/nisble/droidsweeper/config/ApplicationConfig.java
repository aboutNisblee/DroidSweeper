package de.nisble.droidsweeper.config;

import de.nisble.droidsweeper.utilities.LogDog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/** Class that is responsible for holding, loading and saving of the application
 * preferences.
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public final class ApplicationConfig {
	private static final String CLASSNAME = ApplicationConfig.class.getSimpleName();

	public static final ApplicationConfig INSTANCE = new ApplicationConfig();

	private SharedPreferences mShPrefs = null;

	private boolean mReplayOnLost = false;
	private boolean mShowInstructions = true;

	private GameConfig mGameConfig = new GameConfig(Level.EASY);

	private ApplicationConfig() {
	}

	/** Initialization.
	 * @param context The application context. */
	public ApplicationConfig init(Context context) {
		mShPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		return this;
	}

	public boolean isReplayOnLost() {
		return mReplayOnLost;
	}

	public void setShowReplayOnLost(boolean replayOnLost) {
		mReplayOnLost = replayOnLost;
	}

	public boolean isShowInstructions() {
		return mShowInstructions;
	}

	public void setShowInstructions(boolean showInstructions) {
		mShowInstructions = showInstructions;
	}

	public GameConfig getGameConfig() {
		return mGameConfig;
	}
	
	public void setGameConfig(GameConfig gameConfig) {
		mGameConfig = gameConfig;
	}

	/** Persistently store current configuration. */
	public void store(GameConfig c) {
		LogDog.d(CLASSNAME, "Saving GameConfig: " + c.toString());
		SharedPreferences.Editor editor = mShPrefs.edit();
		editor.putInt("Level", c.LEVEL.ordinal());
		editor.putInt("CustomX", c.X);
		editor.putInt("CustomY", c.Y);
		editor.putInt("CustomBombs", c.BOMBS);
		editor.putBoolean("ReplayOnLost", mReplayOnLost);
		editor.putBoolean("ShowInstructions", false);
		editor.commit();
	}

	/** Load game settings from SD card.
	 * If no config file available the settings for Difficulty EASY are set. */
	public void load() {
		mReplayOnLost = mShPrefs.getBoolean("ReplayOnLost", mReplayOnLost);
		mShowInstructions = mShPrefs.getBoolean("ShowInstructions", mShowInstructions);

		Level l = Level.fromInt(mShPrefs.getInt("Level", Level.EASY.ordinal()));
		if (l.equals(Level.CUSTOM)) {
			int x = mShPrefs.getInt("CustomX", Level.EASY.X);
			int y = mShPrefs.getInt("CustomY", Level.EASY.Y);
			int bombs = mShPrefs.getInt("count_bombs", Level.EASY.BOMBS);
			mGameConfig = new GameConfig(x, y, bombs);
		} else {
			mGameConfig = new GameConfig(l);
		}

		LogDog.d(CLASSNAME, "GameConfig loaded: " + mGameConfig.toString());
	}

	@Override
	public String toString() {
		return "ApplicationConfig [mReplayOnLost=" + mReplayOnLost + ", mShowInstructions=" + mShowInstructions + "]";
	}
}
