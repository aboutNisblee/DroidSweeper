package de.nisble.droidsweeper.game.jni;

import de.nisble.droidsweeper.utilities.LogDog;

/** Mapping of msm::GAMESTATUS
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public enum GameStatus {
	READY(0), RUNNING(1), WON(2), LOST(3);
	final int value;

	private static final String CLASSNAME = GameStatus.class.getSimpleName();

	private GameStatus(int v) {
		value = v;
	}

	/** Get the GameStatus for the corresponding integer.
	 * @param i
	 *            primitive integer (int)
	 * @return The difficulty level */
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
