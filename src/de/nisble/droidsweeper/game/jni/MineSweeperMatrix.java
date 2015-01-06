package de.nisble.droidsweeper.game.jni;

import java.util.ArrayList;
import java.util.List;

import de.nisble.droidsweeper.config.GameConfig;
import de.nisble.droidsweeper.utilities.Position;

public final class MineSweeperMatrix {
	private static final String CLASSNAME = MineSweeperMatrix.class.getSimpleName();

	public static final MineSweeperMatrix INSTANCE = new MineSweeperMatrix();

	private List<MatrixObserver> observer = new ArrayList<MatrixObserver>();

	private MineSweeperMatrix() {
		System.loadLibrary("msm");
		init();
	}

	// TODO: Find another solution!
	@Override
	protected void finalize() throws Throwable {
		free();
		super.finalize();
	}

	/* Native methods. */

	private native void init();

	private native void free();

	private native void nativeSetFieldListener(Object obj, int x, int y) throws Exception;

	private native void nativeCreate(int size_x, int size_y, int bombs);

	private native int nativeGameStatus();

	private native int nativeRemainingBombs();

	private native int nativeReveal(int x, int y) throws IndexOutOfBoundsException;

	private native void nativeCycleMark(int x, int y) throws IndexOutOfBoundsException;

	// Called from native code after game status has changed
	private void onGameStatusChanged(GameStatus gs) {
		for (MatrixObserver l : observer) {
			l.onGameStatusChanged(gs);
		}
	}

	// Called from native code when remaining bomb count has changed
	private void onRemainingBombsChanged(int remainingBombs) {
		for (MatrixObserver l : observer) {
			l.onRemainingBombsChanged(remainingBombs);
		}
	}

	// Called from native code after a field was altered
	private void afterFieldStatusChanged(int x, int y, int adjacentBombs, FieldStatus fs) {
		for (MatrixObserver l : observer) {
			l.afterFieldStatusChanged(new Position(x, y), fs, adjacentBombs);
		}
	}

	/* Public interface */

	/** Set the field callback for to a given widget.
	 * @param l A field implementing the FieldListener interface. */
	public void setFieldListener(FieldListener l) throws Exception {
		nativeSetFieldListener((Object) l, l.getPosition().X, l.getPosition().Y);
	}

	/** Add a matrix observer.
	 * @param l A matrix observer. */
	public void addMatrixObserver(MatrixObserver l) {
		if (!observer.contains(l))
			observer.add(l);
	}

	/** Remove a matrix observer.
	 * @param l The observer to remove. */
	public void removeObserver(MatrixObserver l) {
		observer.remove(l);
	}

	/** Create a new game with the given config.
	 * @param prefs */
	public void create(GameConfig c) {
		nativeCreate(c.X, c.Y, c.BOMBS);
	}

	/** Get the current game status.
	 * @return The current GameStatus. */
	public GameStatus gameStatus() {
		return GameStatus.fromInt(nativeGameStatus());
	}

	/** Is libmsm initialized and ready for a new game.
	 * @return true on ready. */
	public boolean isReady() {
		return GameStatus.READY == GameStatus.fromInt(nativeGameStatus());
	}

	/** Is currently a game running?
	 * @return true on running game. */
	public boolean isRunning() {
		return GameStatus.RUNNING == GameStatus.fromInt(nativeGameStatus());
	}

	/** Game lost?
	 * @return true on lost. */
	public boolean isLost() {
		return GameStatus.LOST == GameStatus.fromInt(nativeGameStatus());
	}

	/** Game won?
	 * @return true on won. */
	public boolean isWon() {
		return GameStatus.WON == GameStatus.fromInt(nativeGameStatus());
	}

	/** Get the count of the remaining bombs on the grid.
	 * This number can also be negative when the player has marked more field
	 * than bombs are present.
	 * @return The count of remaining bombs. */
	public int remainingBombs() {
		return nativeRemainingBombs();
	}

	/** Reveal a field.
	 * @param p The position.
	 * @return The count of the adjacent bombs.
	 * @throws NullPointerException on coordinates of the position out of the
	 *             bound of the configured game matrix. */
	public int reveal(Position p) throws IndexOutOfBoundsException {
		return nativeReveal(p.X, p.Y);
	}

	/** Cycle through the sequence of marks of a field.
	 * Marked (not revealable), Queried (revealable), Hidden (revealable)
	 * @param p The position.
	 * @throws NullPointerException on coordinates of the position out of the
	 *             bound of the configured game matrix. */
	public void cycleMark(Position p) throws IndexOutOfBoundsException {
		nativeCycleMark(p.X, p.Y);
	}
}
