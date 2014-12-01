package de.nisble.droidsweeper.game;

import de.nisble.droidsweeper.config.GameConfig;

/** Interface for observing the game status.<br>
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public interface GameObserver {
	/** Called when its time to build a new game grid.
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

	/** Called when the user won the game.
	 * The playtime is internally checked for highscore against the time in the
	 * database, if the game wasn't played on the custom difficulty level.
	 * If its a new highscore the view should ask the user for its name and
	 * insert the time into the database using DSDBAdapter.insertTime().
	 * @param milliseconds The reached playtime in milliseconds.
	 * @param highscore True when the time is a new highscore. */
	void onWon(long milliseconds, boolean highscore);

	/** Called when the game was lost.
	 * @param milliseconds The playtime in milliseconds. */
	void onLost(long milliseconds);
}