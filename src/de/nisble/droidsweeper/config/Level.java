package de.nisble.droidsweeper.config;

import de.nisble.droidsweeper.utilities.LogDog;
import static de.nisble.droidsweeper.config.Constants.*;

/** Available difficulty levels.
 * <ul>
 * <li>Immutable: Members are public final and capitalized.</li>
 * <li>Serializable: Do not change this class!</li>
 * </ul>
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public enum Level {
	/** Difficulty level easy.
	 * Dimensions and bombs are defined by:
	 * <ul>
	 * <li>{@link Constants#EASY_X}</li>
	 * <li>{@link Constants#EASY_Y}</li>
	 * <li>{@link Constants#EASY_BOMBS}</li>
	 * </ul> */
	EASY(EASY_X, EASY_Y, EASY_BOMBS),
	/** Difficulty level easy.
	 * Dimensions and bombs are defined by:
	 * <ul>
	 * <li>{@link Constants#NORMAL_X}</li>
	 * <li>{@link Constants#NORMAL_Y}</li>
	 * <li>{@link Constants#NORMAL_BOMBS}</li>
	 * </ul> */
	NORMAL(NORMAL_X, NORMAL_Y, NORMAL_BOMBS),
	/** Difficulty level easy.
	 * Dimensions and bombs are defined by:
	 * <ul>
	 * <li>{@link Constants#HARD_X}</li>
	 * <li>{@link Constants#HARD_Y}</li>
	 * <li>{@link Constants#HARD_BOMBS}</li>
	 * </ul> */
	HARD(HARD_X, HARD_Y, HARD_BOMBS),
	/** Difficulty level easy.
	 * Dimensions and bombs are set to 0. */
	CUSTOM(0, 0, 0);

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
