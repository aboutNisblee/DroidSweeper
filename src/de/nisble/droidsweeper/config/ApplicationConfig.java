package de.nisble.droidsweeper.config;

import de.nisble.droidsweeper.utilities.LogDog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/** Class that is responsible for holding, loading and saving of the application
 * preferences.
 * <ul>
 * <li>Singleton: Use the public final INSTANCE member.</li>
 * </ul>
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public final class ApplicationConfig {
	private static final String CLASSNAME = ApplicationConfig.class.getSimpleName();

	/** The one and only instance. */
	public static final ApplicationConfig INSTANCE = new ApplicationConfig();

	private SharedPreferences mShPrefs = null;

	private boolean mReplayOnLost = false;
	private boolean mShowInstructions = true;

	private ApplicationConfig() {
	}

	/** Initialization.
	 * @param context The application context. */
	public ApplicationConfig init(Context context) {
		mShPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		return this;
	}

	/** Is a replay after a lost game desired?
	 * @return True if replay on lost option is enabled. */
	public boolean isReplayOnLost() {
		return mReplayOnLost;
	}

	/** Enable or disable replay on lost.
	 * @param replayOnLost True to enable replay after lost. */
	public void setShowReplayOnLost(boolean replayOnLost) {
		mReplayOnLost = replayOnLost;
	}

	/** Show instruction on start?
	 * @return True if instruction should be shown after start. */
	public boolean isShowInstructions() {
		return mShowInstructions;
	}

	/** Enable/disable instruction dialog after start.
	 * @param showInstructions True to enable. */
	public void setShowInstructions(boolean showInstructions) {
		mShowInstructions = showInstructions;
	}

	/** Store given {@link GameConfig} and application settings on disk.
	 * @param c The {@link GameConfig} to store. */
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
	 * If no config file available the settings for {@link Level#EASY} are
	 * returned.
	 * @return The stored {@link GameConfig}. */
	public GameConfig load() {
		mReplayOnLost = mShPrefs.getBoolean("ReplayOnLost", mReplayOnLost);
		mShowInstructions = mShPrefs.getBoolean("ShowInstructions", mShowInstructions);

		GameConfig c;
		Level l = Level.fromInt(mShPrefs.getInt("Level", Level.EASY.ordinal()));
		if (l.equals(Level.CUSTOM)) {
			int x = mShPrefs.getInt("CustomX", Level.EASY.X);
			int y = mShPrefs.getInt("CustomY", Level.EASY.Y);
			int bombs = mShPrefs.getInt("count_bombs", Level.EASY.BOMBS);
			c = new GameConfig(x, y, bombs);
		} else {
			c = new GameConfig(l);
		}

		LogDog.d(CLASSNAME, "GameConfig loaded: " + c.toString());

		return c;
	}

	@Override
	public String toString() {
		return "ApplicationConfig [mReplayOnLost=" + mReplayOnLost + ", mShowInstructions=" + mShowInstructions + "]";
	}
}
