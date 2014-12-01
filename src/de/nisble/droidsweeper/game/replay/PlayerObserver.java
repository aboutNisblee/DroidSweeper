package de.nisble.droidsweeper.game.replay;

import de.nisble.droidsweeper.config.GameConfig;

/** Interface for observing a {@link Player replay player}.
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public interface PlayerObserver {
	/** Called when its time to build a new game grid for showing a
	 * {@link Replay}.
	 * @param config The {@link GameConfig configuration} for the new game grid. */
	void onBuildGrid(GameConfig config);

	/** Called when its time for the view to update the time label.
	 * Currently called each second.
	 * @param milliseconds The elapsed play time in milliseconds. */
	void onTimeUpdate(long milliseconds);

	/** Called when its time for the view to update the remaining bombs shown to
	 * the user. This value is negative, when the user has marked more fields as
	 * bombs than present in the game.
	 * @param bombCount The count of remaining bombs in the game. */
	void onRemainingBombsChanged(int bombCount);

	/** Called when the {@link Replay} is finished. */
	void onReplayEnded();
}
