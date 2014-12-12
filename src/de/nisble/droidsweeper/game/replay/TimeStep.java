package de.nisble.droidsweeper.game.replay;

import java.io.Serializable;

import de.nisble.droidsweeper.game.Field;
import de.nisble.droidsweeper.game.jni.FieldStatus;
import de.nisble.droidsweeper.game.jni.GameStatus;

/** This class holds changes of field states that occur during one
 * time step (typically one click).
 * <ul>
 * <li>Immutable: Members are public final and capitalized.</li>
 * <li>Serializable: Do not change this class!</li>
 * </ul>
 * @author Moritz Nisblé moritz.nisble@gmx.de
 * @see {@link Replay} for a detailed explanation of how a replay is structured.
 * @see {@link Recorder} for an explanation of how a replay is recorded. */
public final class TimeStep implements Serializable {
	private static final long serialVersionUID = 1L;

	/** The play time of this step in milliseconds. */
	public final long TIME;
	/** An integer corresponding to the current {@link GameStatus}. */
	public final int GAMESTATUS;
	/** The count of remaining bombs. */
	public final int BOMBS;
	/** An array of all {@link Field fields} that have changed its
	 * {@link FieldStatus state} during the time step. */
	public final Field[] STEPS;

	/** Create a new step.
	 * @param time The current play time.
	 * @param gameStatus The {@link GameStatus}.
	 * @param remainingBombs The count of remaining bombs.
	 * @param steps An array of all {@link Field} that have changed its state. */
	public TimeStep(long time, int gameStatus, int remainingBombs, Field[] steps) {
		TIME = time;
		GAMESTATUS = gameStatus;
		BOMBS = remainingBombs;
		STEPS = steps;
	}
}
