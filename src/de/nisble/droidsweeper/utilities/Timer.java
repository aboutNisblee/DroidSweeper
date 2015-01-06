package de.nisble.droidsweeper.utilities;

import java.util.ArrayList;

import android.os.Handler;

/** An Android style timer that ticks with configured frequency and fires events
 * on each tick and each second.
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public class Timer {
	/** Interface for getting updates on timer events.
	 * @author Moritz Nisblé moritz.nisble@gmx.de */
	public interface TimerObserver {
		/** Called on each timer tick.
		 * @param milliseconds Milliseconds elapsed since timer start. */
		void onTick(long milliseconds);

		/** Called each second.
		 * @param seconds Seconds elapsed since timer start. */
		void onSecond(long seconds);
	}

	private enum STATUS {
		STOPPED, RUNNING, PAUSED
	}

	/* Member */

	private static final String CLASSNAME = Timer.class.getSimpleName();

	private long mPeriod = 20;
	private long mTicks = 0;
	private long mMilliseconds = 0;
	private long mSeconds = 0;
	private STATUS mStatus = STATUS.STOPPED;
	private ArrayList<TimerObserver> mListeners = new ArrayList<Timer.TimerObserver>(5);

	private Handler handler = new Handler();
	private Runnable run = new Runnable() {
		@Override
		public void run() {
			mTicks++;
			mMilliseconds = mTicks * mPeriod;

			handler.postDelayed(this, mPeriod);

			LogDog.v(CLASSNAME, "Ticks: " + mTicks + " -> Milliseconds: " + mMilliseconds);

			// Inform listeners
			for (TimerObserver l : mListeners) {
				l.onTick(mMilliseconds);
			}
			if ((mTicks % (1000 / mPeriod)) == 0) {
				mSeconds++;

				LogDog.v(CLASSNAME, "Seconds: " + mSeconds);

				for (TimerObserver l : mListeners) {
					l.onSecond(mSeconds);
				}
			}
		}
	};

	/* Interface */

	/** Add a listener.
	 * @param l A listener. */
	public void addListener(TimerObserver l) {
		// Avoid multiple entries of the same object.
		if (!mListeners.contains(l))
			mListeners.add(l);
	}

	/** Remove a listener.
	 * @param l A listener. */
	public void removeListener(TimerObserver l) {
		mListeners.remove(l);
	}

	/** Start the timer.
	 * @param period The tick period in ms.
	 * @param delay A delay for the first tick in ms. */
	public void start(long period, long delay) {
		if (STATUS.STOPPED == mStatus) {
			mTicks = 0;
			mMilliseconds = 0;
			mSeconds = 0;

			// Ensure a minimum period time of 10ms
			this.mPeriod = (period >= 10) ? period : 10;

			mStatus = STATUS.RUNNING;
			handler.postDelayed(run, this.mPeriod + delay);

			LogDog.d(CLASSNAME, "Timer started: period=" + this.mPeriod + " delay=" + delay);
		} else {
			LogDog.d(CLASSNAME, "Timer already running");
		}
	}

	/** Start timer with given period.
	 * @param period The timer period in ms. */
	public void start(long period) {
		start(period, 0);
	}

	/** Start timer with currently configured period.
	 * @note Initial period is 20ms. */
	public void start() {
		start(mPeriod, 0);
	}

	/** Stop the timer.
	 * @note Tick count and seconds are valid until next start. */
	public void stop() {
		mStatus = STATUS.STOPPED;
		handler.removeCallbacksAndMessages(null);
		// handler.removeCallbacks(run);

		LogDog.d(CLASSNAME, "Timer stopped");
	}

	/** Pause the timer. */
	public void pause() {
		mStatus = STATUS.PAUSED;
		handler.removeCallbacksAndMessages(null);
		// handler.removeCallbacks(run);

		LogDog.d(CLASSNAME, "Timer paused");
	}

	/** Resume timer. */
	public void resume() {
		if (STATUS.PAUSED == mStatus) {
			mStatus = STATUS.RUNNING;
			handler.postDelayed(run, this.mPeriod);

			LogDog.d(CLASSNAME, "Resuming timer");
		} else {
			LogDog.d(CLASSNAME, "Unable to resume timer: status=" + mStatus);
		}
	}

	public boolean isStopped() {
		return mStatus == STATUS.STOPPED;
	}

	public boolean isRunning() {
		return mStatus == STATUS.RUNNING;
	}

	public boolean isPaused() {
		return mStatus == STATUS.PAUSED;
	}

	/** Get current tick count.
	 * @return Tick count. */
	@Deprecated
	public long getTicks() {
		return mTicks;
	}

	/** Get elapsed milliseconds.
	 * @return Elapsed milliseconds. */
	public long getMilliseconds() {
		return mMilliseconds;
	}

	/** Get elapsed seconds.
	 * @return Elapsed seconds. */
	public long getSeconds() {
		return mSeconds;
	}
}
