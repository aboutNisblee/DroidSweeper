package de.nisble.droidsweeper.game.jni;

import de.nisble.droidsweeper.utilities.LogDog;

/** Mapping of msm::FIELDSTATUS
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public enum FieldStatus {
	HIDDEN, UNHIDDEN, MARKED, QUERIED, BOMB;

	private static final String CLASSNAME = FieldStatus.class.getSimpleName();

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
