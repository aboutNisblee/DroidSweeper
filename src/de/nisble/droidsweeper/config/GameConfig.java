package de.nisble.droidsweeper.config;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Parcel;
import android.os.Parcelable;

/** Representation of the configuration of a game.
 * This class wraps a Level and adds the ability to handle custom difficulty
 * levels. If instantiated from a standard level the grid sizes are set
 * accordingly.
 * It is also able to adapt the dimensions of the game grid to the
 * current orientation of the device. This is simply done by switching the sides
 * of the game grid. The class is immutable and parcelable!
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public final class GameConfig implements Parcelable {
	public final Level LEVEL;
	public final int X;
	public final int Y;
	public final int BOMBS;

	/** Instantiate from Level.
	 * <b>Do not call this constructor with Level.CUSTOM</b>
	 * @param l A Level. */
	public GameConfig(Level l) {
		LEVEL = l;
		X = l.X;
		Y = l.Y;
		BOMBS = l.BOMBS;
	}

	/** Instantiate with custom grid size.
	 * <b>The internal Level is set to Level.CUSTOM</b>
	 * @param x The width of the grid.
	 * @param y The height of the grid.
	 * @param bombs The bomb count. */
	public GameConfig(int x, int y, int bombs) {
		LEVEL = Level.CUSTOM;
		X = x;
		Y = y;
		BOMBS = bombs;
	}

	private GameConfig(Level l, int x, int y, int bombs) {
		LEVEL = l;
		X = x;
		Y = y;
		BOMBS = bombs;
	}

	/** Get an orientation adjusted version.
	 * @param c The application context.
	 * @return An orientation corrected copy of the GameConfig. */
	public GameConfig adjustOrientation(Context c) {
		return adjustOrientation(c.getResources().getConfiguration());
	}

	/** Get an orientation adjusted version.
	 * @param config The current configuration.
	 * @return An orientation corrected copy of the GameConfig. */
	public GameConfig adjustOrientation(Configuration config) {
		if (Configuration.ORIENTATION_LANDSCAPE == config.orientation)
			return toLandscape();
		else
			return toPortrait();
	}

	/** Get the portrait version.
	 * @return A copy of the GameConfig that fits best to portrait orientation. */
	public GameConfig toPortrait() {
		int max = Math.max(X, Y);
		int min = (max == X) ? Y : X;
		return new GameConfig(LEVEL, min, max, BOMBS);
	}

	/** Get the landscape version.
	 * @return A copy of the GameConfig that fits best to landscape orientation. */
	public GameConfig toLandscape() {
		int max = Math.max(X, Y);
		int min = (max == X) ? Y : X;
		return new GameConfig(LEVEL, max, min, BOMBS);
	}

	@Override
	public String toString() {
		return "GameConfig [LEVEL=" + LEVEL.toString() + ", X=" + X + ", Y=" + Y + ", BOMBS=" + BOMBS + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + BOMBS;
		result = prime * result + ((LEVEL == null) ? 0 : LEVEL.hashCode());
		result = prime * result + X;
		result = prime * result + Y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof GameConfig))
			return false;
		GameConfig other = (GameConfig) obj;
		if (BOMBS != other.BOMBS)
			return false;
		if (LEVEL != other.LEVEL)
			return false;
		if (X != other.X)
			return false;
		if (Y != other.Y)
			return false;
		return true;
	}

	public GameConfig(Parcel in) {
		int[] data = new int[4];

		in.readIntArray(data);
		LEVEL = Level.fromInt(data[0]);
		X = data[1];
		Y = data[2];
		BOMBS = data[3];
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeIntArray(new int[] { LEVEL.ordinal(), X, Y, BOMBS });
	}

	public static final Creator<GameConfig> CREATOR = new Creator<GameConfig>() {
		public GameConfig createFromParcel(Parcel in) {
			return new GameConfig(in);
		}

		public GameConfig[] newArray(int size) {
			return new GameConfig[size];
		}
	};
}
