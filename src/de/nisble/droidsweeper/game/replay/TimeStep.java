package de.nisble.droidsweeper.game.replay;

import java.io.Serializable;

import de.nisble.droidsweeper.game.Field;

/** This class holds changes of field states that occur during one
 * time step (typically one click).
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public final class TimeStep implements Serializable {
	private static final long serialVersionUID = 1L;

	public final long TIME;
	public final int GAMESTATUS;
	public final int BOMBS;
	public final Field[] STEPS;

	public TimeStep(long time, int gameStatus, int remainingBombs, Field[] steps) {
		TIME = time;
		GAMESTATUS = gameStatus;
		BOMBS = remainingBombs;
		STEPS = steps;
	}
}
