package de.nisble.droidsweeper.game.replay;

import static de.nisble.droidsweeper.config.Constants.TIMER_PERIOD;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import de.nisble.droidsweeper.config.GameConfig;
import de.nisble.droidsweeper.game.Field;
import de.nisble.droidsweeper.game.Game;
import de.nisble.droidsweeper.game.database.DSDBAdapter;
import de.nisble.droidsweeper.game.jni.FieldListener;
import de.nisble.droidsweeper.game.jni.FieldStatus;
import de.nisble.droidsweeper.utilities.LogDog;
import de.nisble.droidsweeper.utilities.Timer;
import de.nisble.droidsweeper.utilities.Timer.TimerObserver;

/** Replay player.<br>
 * This class is used to show a game {@link Replay} that was loaded from the
 * database or {@link Recorder recorded} right before. The key feature of this
 * program to update the field widgets directly from native code makes showing a
 * replay a bit tricky.<br>
 * To update the {@link FieldStatus status} of the field widgets this
 * class must know the widgets. Therefore this class holds a matrix of
 * {@link FieldListener interfaces} to the field widgets.<br>
 * It is mandatory for the view to distinguish between a real game and a replay.
 * If its a game the field widgets must be registered in
 * {@link Game#setFieldListener(FieldListener)} else in this class by calling
 * {@link Player#setFieldListener(FieldListener)}.<br>
 * The view should register a {@link PlayerObserver} in this class. After
 * loading a {@link Replay} and while a call to {@link Player#play()} the
 * interface method {@link PlayerObserver#onBuildGrid(GameConfig)} is invoked.
 * Inside this method the view should create the game grid just as if the call
 * comes from {@link Game}. But rather than registering the field widgets in the
 * Game, the widgets should be registered here.
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public class Player implements TimerObserver {
	private final String CLASSNAME = Player.class.getSimpleName();

	private static final int STOPPED = 0;
	private static final int PLAYING = 1;
	private static final int PAUSED = 2;
	private int mState = STOPPED;

	private List<PlayerObserver> mObservers = new ArrayList<PlayerObserver>(1);

	private Timer mTimer = new Timer();

	private Replay mReplay = new Replay();

	private ListIterator<TimeStep> mTsIter;
	private FieldListener[][] mFlMatrix;

	/** Instantiate and initialize a new replay player. */
	public Player() {
		mTimer.addListener(this);
	}

	/** Add an observer.
	 * @param l Observer. */
	public void addObserver(PlayerObserver l) {
		// Avoid multiple entries of the same object.
		if (!mObservers.contains(l))
			mObservers.add(l);
	}

	/** Remove an observer.
	 * @param l Observer. */
	public void removeObserver(PlayerObserver l) {
		mObservers.remove(l);
	}

	/** Register a field widget for the player to be able to update the status of
	 * the widgets.
	 * @param l A field widget that implements the {@link FieldListener}
	 *            interface.
	 * @throws ArrayIndexOutOfBoundsException If a coordinate that the widget
	 *             returns in its {@link FieldListener#getPosition()} method is
	 *             out of the bounds of the current {@link GameConfig}. */
	public void setFieldListener(FieldListener l) throws ArrayIndexOutOfBoundsException {
		mFlMatrix[l.getPosition().X][l.getPosition().Y] = l;
	}

	/** Load a {@link Replay} by its game ID directly from the database.
	 * @param gameID The ID of the game in the database.
	 * @throws Exception Multiple exceptions are possible due to errors while
	 *             loading from the database of deserialisation of the replay. */
	public void load(long gameID) throws Exception {
		// Stop the current replay
		mState = STOPPED;
		mTimer.stop();

		mReplay = new Replay(DSDBAdapter.INSTANCE.getReplay(gameID));

		mFlMatrix = new FieldListener[mReplay.getGameConfig().X][mReplay.getGameConfig().Y];
	}

	/** Load the given {@link Replay}.
	 * @param replay A Replay. */
	public void load(Replay replay) {
		// Stop the current replay
		mState = STOPPED;
		mTimer.stop();

		mReplay = new Replay(replay);

		mFlMatrix = new FieldListener[mReplay.getGameConfig().X][mReplay.getGameConfig().Y];
	}

	/** Play a previously loaded {@link Replay}.<br>
	 * This invokes {@link PlayerObserver#onBuildGrid(GameConfig)} and passes
	 * the {@link GameConfig} of the Replay. The view should build the game grid
	 * and register the field widgets by passing them to
	 * {@link #setFieldListener(FieldListener)} */
	public void play() {
		if (!mReplay.getTimeSteps().isEmpty()) {
			mState = PLAYING;
			mTimer.stop();

			mTsIter = mReplay.getTimeSteps().listIterator(0);

			for (PlayerObserver l : mObservers) {
				l.onTimeUpdate(0);
				l.onRemainingBombsChanged(mReplay.getGameConfig().BOMBS);

				/* Let the view build the game grid. */
				l.onBuildGrid(mReplay.getGameConfig());
			}
			mTimer.start(TIMER_PERIOD);
		}
	}

	/** Stop a {@link Replay}. */
	public void stop() {
		mState = STOPPED;
		mTimer.stop();
	}

	/** Pause a currently running {@link Replay}. The Replay can be resumed by
	 * calling {@link #resume()}. */
	public void pause() {
		if (PLAYING == mState) {
			mState = PAUSED;
			mTimer.pause();
		}
	}

	/** Resume a {@link #pause() paused} {@link Replay}. */
	public void resume() {
		if (PAUSED == mState) {
			mState = PLAYING;
			mTimer.resume();
		}
	}

	@Override
	public void onTick(long milliseconds) {
		if (mTsIter.hasNext()) {
			// Get next time step
			TimeStep ts = mTsIter.next();
			if (ts.TIME <= milliseconds) {
				// LogDog.d(CLASSNAME, "HIT @ TimeStep " + ts.TIME + "ms <= " +
				// milliseconds + "ms");

				for (PlayerObserver l : mObservers) {
					l.onRemainingBombsChanged(ts.BOMBS);
				}

				// Get field changes in this time step
				for (Field f : ts.STEPS) {
					// LogDog.v(CLASSNAME, "Changed field: X:" + f.POSITION.X +
					// " Y:" + f.POSITION.Y + " ("
					// + f.ADJACENT_BOMBS + ") to " + f.STATUS.toString());

					try {
						mFlMatrix[f.POSITION.X][f.POSITION.Y].onStatusChanged(f.STATUS, f.ADJACENT_BOMBS);
					} catch (ArrayIndexOutOfBoundsException e) {
						LogDog.e(CLASSNAME, "Out of bounds of FieldListener matrix @ X:" + f.POSITION.X + " Y:"
								+ f.POSITION.Y, e);
					}
				}
			} else {
				mTsIter.previous();
			}
		} else {
			mState = STOPPED;
			mTimer.stop();

			for (PlayerObserver l : mObservers) {
				l.onReplayEnded();
			}
		}
	}

	@Override
	public void onSecond(long seconds) {
		for (PlayerObserver l : mObservers) {
			l.onTimeUpdate(seconds * 1000);
		}
	}

	/** @return True if a {@link Replay} is currently {@link #load(long) loaded}. */
	public boolean isLoaded() {
		return !mReplay.getTimeSteps().isEmpty();
	}

	/** @return True if the the {@link Player} is stopped. */
	public boolean isStopped() {
		return (STOPPED == mState);
	}

	/** @return True if the {@link Player} is currently playing. */
	public boolean isPlaying() {
		return PLAYING == mState;
	}

	/** @return True if the {@link Player} is paused. */
	public boolean isPaused() {
		return PAUSED == mState;
	}
}
