package de.nisble.droidsweeper.game.jni;

import de.nisble.droidsweeper.game.Position;

/** Interface that is used to observe changes in the matrix of the native
 * library.
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public interface MatrixObserver {
	/** Called from native code via {@link MineSweeperMatrix} when the internal
	 * {@link GameStatus
	 * status} of the
	 * game has changed.
	 * @param newStatus The new status. */
	void onGameStatusChanged(GameStatus newStatus);

	/** Called from native code via {@link MineSweeperMatrix} when the count of
	 * the remaining bombs has changed because the user has marked some fields.
	 * @param remainingBombs The new count of remaining bombs. */
	void onRemainingBombsChanged(int remainingBombs);

	/** Called from native code via {@link MineSweeperMatrix} after the
	 * {@link FieldStatus status} of a field has changed.<br>
	 * In contrast to {@link FieldListener#onStatusChanged(FieldStatus, int)}
	 * this function is commonly called for status changes of fields.
	 * <p>
	 * <b>Note:</b> This function is called after
	 * {@link FieldListener#onStatusChanged(FieldStatus, int)}.
	 * </p>
	 * @param p The {@link Position} of the field.
	 * @param fs The new {@link FieldStatus status}.
	 * @param adjacentBombs The count of adjacent bombs. */
	void afterFieldStatusChanged(Position p, FieldStatus fs, int adjacentBombs);
}