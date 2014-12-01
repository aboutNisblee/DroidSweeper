package de.nisble.droidsweeper.game.jni;

import de.nisble.droidsweeper.utilities.LogDog;

/** Mapping of msm::GAMESTATUS
 * <ul>
 * <li>Serializable: Do not change this class!</li>
 * </ul>
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public enum GameStatus {
	/** Created but not started (i.e. no click was made) */
	READY(0),
	/** Game is currently running. */
	RUNNING(1),
	/** Game is ended and won. */
	WON(2),
	/** Game is ended and lost. */
	LOST(3);

	final int value;

	private static final String CLASSNAME = GameStatus.class.getSimpleName();

	private GameStatus(int v) {
		value = v;
	}

	/** Get the GameStatus for the corresponding integer.<br>
	 * The value defaults to READY when the given number is invalid.
	 * @param i The value.
	 * @return The corresponding status. */
	public static GameStatus fromInt(int i) {
		GameStatus temp;
		try {
			temp = values()[i];
		} catch (Exception e) {
			LogDog.e(CLASSNAME, "IndexOutOfBoundsException: " + e.getMessage(), e);
			temp = values()[0];
		}
		return temp;
	}
}
