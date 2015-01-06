package de.nisble.droidsweeper.game.replay;

import static de.nisble.droidsweeper.config.Constants.TIMER_PERIOD;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import de.nisble.droidsweeper.game.Game;
import de.nisble.droidsweeper.game.database.DSDBAdapter;
import de.nisble.droidsweeper.game.jni.FieldListener;
import de.nisble.droidsweeper.utilities.Field;
import de.nisble.droidsweeper.utilities.LogDog;
import de.nisble.droidsweeper.utilities.Timer;
import de.nisble.droidsweeper.utilities.Timer.TimerObserver;

public class Player implements TimerObserver {
	private final String CLASSNAME = Game.class.getSimpleName();

	private static final int STOPPED = 0;
	private static final int PLAYING = 1;
	private static final int PAUSED = 2;
	private int mState = STOPPED;

	private List<PlayerObserver> mObservers = new ArrayList<PlayerObserver>(1);

	private Timer mTimer = new Timer();

	private Replay mReplay = new Replay();

	private ListIterator<TimeStep> mTsIter;
	private FieldListener[][] mFlMatrix;

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

	public void addFieldListener(FieldListener l) throws ArrayIndexOutOfBoundsException {
		mFlMatrix[l.getPosition().X][l.getPosition().Y] = l;
	}

	public void load(long gameID) throws Exception {
		// Stop the current replay
		mState = STOPPED;
		mTimer.stop();

		mReplay = new Replay(DSDBAdapter.INSTANCE.getReplay(gameID));

		mFlMatrix = new FieldListener[mReplay.getGameConfig().X][mReplay.getGameConfig().Y];
	}

	public void load(Replay replay) {
		// Stop the current replay
		mState = STOPPED;
		mTimer.stop();

		mReplay = new Replay(replay);

		mFlMatrix = new FieldListener[mReplay.getGameConfig().X][mReplay.getGameConfig().Y];
	}

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
			mTimer.start(TIMER_PERIOD, 1000);
		}
	}

	public void stop() {
		mState = STOPPED;
		mTimer.stop();
	}

	public void pause() {
		if (PLAYING == mState) {
			mState = PAUSED;
			mTimer.pause();
		}
	}

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

	public boolean isLoaded() {
		return !mReplay.getTimeSteps().isEmpty();
	}

	public boolean isStopped() {
		return (STOPPED == mState);
	}

	public boolean isPlaying() {
		return PLAYING == mState;
	}

	public boolean isPaused() {
		return PAUSED == mState;
	}
}
