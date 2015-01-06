package de.nisble.droidsweeper.game;

import java.util.ArrayList;
import java.util.List;

import de.nisble.droidsweeper.config.GameConfig;
import de.nisble.droidsweeper.config.Level;
import de.nisble.droidsweeper.game.database.DSDBAdapter;
import de.nisble.droidsweeper.game.jni.FieldListener;
import de.nisble.droidsweeper.game.jni.FieldStatus;
import de.nisble.droidsweeper.game.jni.GameStatus;
import de.nisble.droidsweeper.game.jni.MatrixObserver;
import de.nisble.droidsweeper.game.jni.MineSweeperMatrix;
import de.nisble.droidsweeper.game.replay.Recorder;
import de.nisble.droidsweeper.game.replay.Replay;
import de.nisble.droidsweeper.utilities.Timer;
import de.nisble.droidsweeper.utilities.LogDog;
import de.nisble.droidsweeper.utilities.Position;
import de.nisble.droidsweeper.utilities.Timer.TimerObserver;
import static de.nisble.droidsweeper.config.Constants.*;

public class Game implements MatrixObserver, TimerObserver {
	private final String CLASSNAME = Game.class.getSimpleName();

	public static final Game INSTANCE = new Game();

	private GameConfig mConfig = new GameConfig(Level.EASY);

	private Timer mTimer = new Timer();

	private Recorder mRecorder = new Recorder();

	private List<GameObserver> mObservers = new ArrayList<GameObserver>(2);

	private Game() {
		MineSweeperMatrix.INSTANCE.addMatrixObserver(this);
		// Add recorder as MatrixObserver
		MineSweeperMatrix.INSTANCE.addMatrixObserver(mRecorder);

		mTimer.addListener(this);
	}

	/** Add an observer.
	 * @param l Observer. */
	public void addListener(GameObserver l) {
		// Avoid multiple entries of the same object.
		if (!mObservers.contains(l))
			mObservers.add(l);
	}

	/** Remove an observer.
	 * @param l Observer. */
	public void removeObserver(GameObserver l) {
		mObservers.remove(l);
	}

	public GameConfig getGameConfig() {
		return mConfig;
	}

	@Deprecated
	public boolean isOrientationChangeable() {
		return (MineSweeperMatrix.INSTANCE.isReady());
	}

	/** Register a FieldListener.
	 * @note This connects a FieldListener directly to a field object
	 *       in the native libmsm.
	 * @param l Typically a widget that is able to represent a single field.
	 * @throws Exception An IndexOutOfBoundsException when the coordinates
	 *             that the given FieldListener returns from its getPosition()
	 *             method is out of the bounds of the matrix in the GameConfig
	 *             passed to start() before. */
	public void setFieldListener(FieldListener l) throws Exception {
		MineSweeperMatrix.INSTANCE.setFieldListener(l);
	}

	/* A click on the grid is only acceptable after initialization
	 * of a new game or while running. */
	private boolean isClickAcceptable(GameStatus gs) {
		return (gs != GameStatus.LOST && gs != GameStatus.WON);
	}

	/* Actions to perform before control is passed to libmsm. */
	private void beforeClick() {
		if (!mTimer.isRunning()) {
			mTimer.start(TIMER_PERIOD);
		}
	}

	/* Actions to perform after control is passed back and all actions
	 * for a click are performed by libmsm. The old status is used to
	 * detect changes of the internal status. */
	private void afterClick(GameStatus old, Position p) {
		GameStatus newStatus = MineSweeperMatrix.INSTANCE.gameStatus();

		mRecorder.finalizeStep(mTimer.getMilliseconds());

		if (newStatus != old) {
			// LogDog.i(CLASSNAME, "GAMESTATUS changed to " +
			// newStatus.toString());

			switch (newStatus) {
			case RUNNING:
				break;
			case LOST:
				mTimer.stop();
				for (GameObserver l : mObservers) {
					l.onLost(mTimer.getMilliseconds());
				}
				break;
			case READY:
				break;
			case WON:
				mTimer.stop();

				boolean highscore = false;
				try {
					if (mConfig.LEVEL != Level.CUSTOM) {
						highscore = DSDBAdapter.INSTANCE.isHighScore(mConfig.LEVEL, mTimer.getMilliseconds());
					}

					// LogDog.i(CLASSNAME, "Highscore: " + highscore);
				} catch (Exception e) {
					LogDog.e(CLASSNAME, e.getMessage(), e);
				}

				for (GameObserver l : mObservers) {
					l.onWon(mTimer.getMilliseconds(), highscore);
				}
				break;
			default:
				break;
			}
		}
	}

	/** Reveal a field fields.
	 * @param p The position. */
	public void revealField(Position p) {
		GameStatus oldStatus = MineSweeperMatrix.INSTANCE.gameStatus();
		if (isClickAcceptable(oldStatus)) {
			beforeClick();
			MineSweeperMatrix.INSTANCE.reveal(p);
			afterClick(oldStatus, p);
		}
	}

	/** Cycle through marks of fields.
	 * @param p The position. */
	public void cycleMark(Position p) {
		GameStatus oldStatus = MineSweeperMatrix.INSTANCE.gameStatus();
		if (isClickAcceptable(oldStatus)) {
			beforeClick();
			MineSweeperMatrix.INSTANCE.cycleMark(p);
			afterClick(oldStatus, p);
		}
	}

	public void start() {
		start(mConfig);
	}

	/** Start a new game with the given CameConfig.
	 * @param c A GameConfig. */
	public void start(GameConfig c) {
		// Make an internal copy of the GameConfig.
		mConfig = c;

		mTimer.stop();
		mRecorder.newRecord(c);

		// Create a new matrix
		MineSweeperMatrix.INSTANCE.create(mConfig);

		// Update observers
		for (GameObserver l : mObservers) {
			l.onBuildGrid(mConfig);
			l.onTimeUpdate(0);
			l.onRemainingBombsChanged(mConfig.BOMBS);
		}
	}

	public void stop() {
		mTimer.stop();
	}

	public void pause() {
		if (MineSweeperMatrix.INSTANCE.isRunning() && mTimer.isRunning()) {
			mTimer.pause();
		}
	}

	/** Try to resume a running game or a replay.
	 * @return True if there is a game or replay to resume, else false. */
	public boolean resume() {
		if (MineSweeperMatrix.INSTANCE.isRunning() && mTimer.isPaused()) {
			mTimer.resume();
			return true;
		}
		return false;
	}

	public Replay getReplay() {
		return mRecorder.getReplayCopy();
	}

	@Override
	public void onGameStatusChanged(GameStatus newStatus) {
		/* Don't use this callback!
		 * Makes things more complicated and can cause a high call stack! */
	}

	@Override
	public void onRemainingBombsChanged(int remainingBombs) {
		for (GameObserver l : mObservers) {
			l.onRemainingBombsChanged(remainingBombs);
		}
	}

	@Override
	public void afterFieldStatusChanged(Position p, FieldStatus fs, int adjacentBombs) {
	}

	@Override
	public void onTick(long milliseconds) {
	}

	@Override
	public void onSecond(long seconds) {
		for (GameObserver l : mObservers) {
			l.onTimeUpdate(seconds * 1000);
		}
	}
}
