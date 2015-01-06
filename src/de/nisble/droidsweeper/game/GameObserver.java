package de.nisble.droidsweeper.game;

import de.nisble.droidsweeper.config.GameConfig;

public interface GameObserver {
	void onBuildGrid(GameConfig config);

	void onTimeUpdate(long milliseconds);

	void onRemainingBombsChanged(int bombCount);

	void onWon(long milliseconds, boolean highscore);

	void onLost(long milliseconds);

}