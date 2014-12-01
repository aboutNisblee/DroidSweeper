package de.nisble.droidsweeper.game.jni;

import de.nisble.droidsweeper.utilities.LogDog;

/** Mapping of msm::FIELDSTATUS
 * <ul>
 * <li>Serializable: Do not change this class!</li>
 * </ul>
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public enum FieldStatus {
	/** Field is hidden (i.e. not clicked). */
	HIDDEN,
	/** Field is revealed. */
	UNHIDDEN,
	/** Field is marked as bomb. */
	MARKED,
	/** Field is marked a queried. */
	QUERIED,
	/** Field is revealed and a bomb -> Game is lost. */
	BOMB;

	private static final String CLASSNAME = FieldStatus.class.getSimpleName();

	/** Transform a integer to its corresponding status value.<br>
	 * The status defaults to HIDDEN when the given number is invalid.
	 * @param i The value.
	 * @return The corresponding status. */
	public static FieldStatus fromInt(int i) {
		FieldStatus temp;
		try {
			temp = values()[i];
		} catch (Exception e) {
			LogDog.e(CLASSNAME, "IndexOutOfBoundsException: " + e.getMessage(), e);
			temp = values()[0];
		}
		return temp;
	}
}
