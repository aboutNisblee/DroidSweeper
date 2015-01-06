package de.nisble.droidsweeper.config;

import de.nisble.droidsweeper.utilities.LogDog;
import static de.nisble.droidsweeper.config.Constants.*;

/** Available difficulty levels.
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public enum Level {
	EASY(EASY_X, EASY_Y, EASY_BOMBS), NORMAL(NORMAL_X, NORMAL_Y, NORMAL_BOMBS), HARD(HARD_X, HARD_Y, HARD_BOMBS), CUSTOM(
			0, 0, 0);

	private static final String CLASSNAME = Level.class.getSimpleName();

	private Level(int x, int y, int b) {
		X = x;
		Y = y;
		BOMBS = b;
	}

	/** The horizontal size of the game grid. */
	public final int X;
	/** The vertical size of the game grid. */
	public final int Y;
	/** The count of bombs. */
	public final int BOMBS;

	/** Get the difficulty level for the corresponding integer.
	 * @note Defaults to CUSTOM (3) when index out of bounds.
	 * @param i primitive integer (int)
	 * @return The difficulty level */
	public static Level fromInt(int i) {
		Level temp;
		try {
			temp = values()[i];
		} catch (Exception e) {
			LogDog.e(CLASSNAME, "Unknown difficulty. Choosing CUSTOM: " + e.getMessage(), e);
			temp = values()[3];
		}
		return temp;
	}
}
