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
import de.nisble.droidsweeper.utilities.Timer.TimerObserver;
import static de.nisble.droidsweeper.config.Constants.*;

/** This class coordinates a game.<br>
 * It completely abstracts the game procedure and has no dependencies to the
 * view. View classes should implement the {@link GameObserver observer
 * interface} to get updates on important changes of the internal state of the
 * game. Architectural this class sits between the view and the
 * {@link MineSweeperMatrix interface to the native library libmsm}. It maintains
 * a timer that counts the elapsed playtime and records each game. A view class
 * is responsible for loading and showing a replay.
 * <ul>
 * <li>Singleton: Use the public final INSTANCE member.</li>
 * </ul>
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public class Game implements MatrixObserver, TimerObserver {
	private final String CLASSNAME = Game.class.getSimpleName();

	/** The instance. */
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
	public void addObserver(GameObserver l) {
		// Avoid multiple entries of the same object.
		if (!mObservers.contains(l))
			mObservers.add(l);
	}

	/** Remove an observer.
	 * @param l Observer. */
	public void removeObserver(GameObserver l) {
		mObservers.remove(l);
	}

	/** @return The currently loaded {@link GameConfig}. */
	public GameConfig getGameConfig() {
		return mConfig;
	}

	/** This function only return true when a new game was created but not
	 * started (i.e. no click was made).
	 * @return True if the game is in {@link GameStatus#RUNNING} state. */
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

	/** Cycle through marks of fields.<br>
	 * <b>Cycle sequence:</b> {@link FieldStatus#HIDDEN HIDDEN},
	 * {@link FieldStatus#MARKED MARKED} , {@link FieldStatus#QUERIED QUERIED}<br>
	 * <b>Note:</b> Its not possible to {@link #revealField(Position)
	 * reveal} a field with the status {@link FieldStatus#MARKED MARKED} while
	 * its possible in both other states. This is prevented by the native
	 * library.
	 * @param p The position. */
	public void cycleMark(Position p) {
		GameStatus oldStatus = MineSweeperMatrix.INSTANCE.gameStatus();
		if (isClickAcceptable(oldStatus)) {
			beforeClick();
			MineSweeperMatrix.INSTANCE.cycleMark(p);
			afterClick(oldStatus, p);
		}
	}

	/** Start a new game with old {@link GameConfig}.
	 * @see #start(GameConfig) */
	public void start() {
		start(mConfig);
	}

	/** Start a new game with the given CameConfig.<br>
	 * <b>Note:</b> There are some things the calling view class should take
	 * into account when calling this method. After resetting the internal state
	 * of this class (timer and replay recorder) and instructing the native
	 * library to create a new game matrix,
	 * {@link GameObserver#onBuildGrid(GameConfig)} is called on each observer.
	 * The view should create the game grid from inside that callback. Each
	 * field widget that is created in that process should be registered by a
	 * call to {@link #setFieldListener(FieldListener)} for the native library
	 * to ba able to inform the widget over changes in its state.
	 * @param c A {@link GameConfig}. */
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

	/** Stop the current game. */
	public void stop() {
		mTimer.stop();
	}

	/** Pause the current game.<br>
	 * It can be resumed later by calling {@link #resume()}. */
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

	/** @return Get a copy of the {@link Replay} of the last completed game. */
	public Replay getReplay() {
		return mRecorder.getReplay();
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
