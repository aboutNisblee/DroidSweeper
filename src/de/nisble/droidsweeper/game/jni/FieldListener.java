package de.nisble.droidsweeper.game.jni;

import de.nisble.droidsweeper.utilities.Position;

/** Interface that must be implemented by fields.
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public interface FieldListener {
	void onStatusChanged(FieldStatus status, int adjacentBombs);
	Position getPosition();
}
