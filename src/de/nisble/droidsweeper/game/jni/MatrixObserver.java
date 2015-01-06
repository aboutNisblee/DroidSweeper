package de.nisble.droidsweeper.game.jni;

import de.nisble.droidsweeper.utilities.Position;

public interface MatrixObserver {
	void onGameStatusChanged(GameStatus newStatus);
	void onRemainingBombsChanged(int remainingBombs);
	void afterFieldStatusChanged(Position p, FieldStatus fs, int adjacentBombs);
}