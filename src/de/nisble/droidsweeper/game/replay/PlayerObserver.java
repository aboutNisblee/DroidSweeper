package de.nisble.droidsweeper.game.replay;

import de.nisble.droidsweeper.config.GameConfig;

public interface PlayerObserver {
	void onBuildGrid(GameConfig config);

	void onTimeUpdate(long milliseconds);

	void onRemainingBombsChanged(int bombCount);

	void onReplayEnded();
}
