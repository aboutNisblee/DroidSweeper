package de.nisble.droidsweeper.game.jni;

import de.nisble.droidsweeper.game.Position;

/** Interface that should be implemented by field widgets.<br>
 * This interface enables the native library to directly call widgets in the
 * view and specifically update their {@link FieldStatus state}.
 *
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public interface FieldListener {
	/** Called by the native library libmsm when the widget should change its
	 * status.
	 * @param status The new status of the field.
	 * @param adjacentBombs The cound of adjacent bombs. */
	void onStatusChanged(FieldStatus status, int adjacentBombs);

	/** The widget that implements this interface should return its current
	 * Position in the grid on a call to this function.
	 * <p>
	 * <b>Note: This position must not be adapted to the orientation of the
	 * device. It must be in the bounds of the GameConfig passed to
	 * {@link GameObserver#onBuildGrid(de.nisble.droidsweeper.config.GameConfig)}
	 * before.</b>
	 * </p>
	 * @return */
	Position getPosition();
}
