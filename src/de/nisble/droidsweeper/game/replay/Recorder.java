package de.nisble.droidsweeper.game.replay;

import java.util.ArrayList;
import java.util.List;

import de.nisble.droidsweeper.config.GameConfig;
import de.nisble.droidsweeper.game.Field;
import de.nisble.droidsweeper.game.Position;
import de.nisble.droidsweeper.game.jni.FieldStatus;
import de.nisble.droidsweeper.game.jni.GameStatus;
import de.nisble.droidsweeper.game.jni.MatrixObserver;
import de.nisble.droidsweeper.game.jni.MineSweeperMatrix;
import de.nisble.droidsweeper.utilities.LogDog;

/** {@link Replay} recorder.<br>
 * This class abstracts the recording of games. Because only real changes of
 * fields should be recorded, the procedure of recording a game needs a little
 * explanation.<br>
 * <ol>
 * <li>An instance of this class should be registered as observer to
 * {@link MineSweeperMatrix} by passing it to
 * {@link MineSweeperMatrix#addMatrixObserver(MatrixObserver)}.</li>
 * <li>A new recording is started by calling {@link #newRecord(GameConfig)}.</li>
 * <li>When the player now clicks a field this class receives events over
 * changings in the of the game via the {@link MatrixObserver} interface. Each
 * click may cause multiple {@link Field fields} to change its state. So multiple
 * events are received and multiple changes are recorded for a single click (see
 * {@link Replay} for an explanation of the structure of a replay). Because for
 * this class its impossible to know which event is the last one for a click
 * (i.e. a single {@link TimeStep}) the help of the controlling instance is
 * needed.</li>
 * <li>So after a click is finished (all events are received) the controlling
 * instance must call {@link #finalizeStep(long)} to finish the record step. The
 * events are than stored inside a {@link Replay}.</li>
 * <li>After the game is finished and the last click is finished the
 * {@link Replay} can be requested by calling {@link #getReplay()}.</li>
 * </ol>
 * @author Moritz Nisblé moritz.nisble@gmx.de
 * @see {@link Player} for how to replay a recorded {@link Replay}. */
public class Recorder implements MatrixObserver {
	private final String CLASSNAME = Recorder.class.getSimpleName();

	private boolean mRecordFlag = false;

	private Replay mCurrent = new Replay();
	private Replay mComplete = new Replay();

	private List<Field> mStepBuffer = new ArrayList<Field>();
	private GameStatus mGameStatus = GameStatus.READY;
	private int mRemainingBombs = 0;

	/** Start a new record.
	 * @param c The {@link GameConfig configuration}. */
	public void newRecord(GameConfig c) {
		mRecordFlag = true;

		mCurrent = new Replay();
		mCurrent.setGameConfig(c);
		mCurrent.setEpochTime(System.currentTimeMillis());

		/* Reset internal state */
		mStepBuffer.clear();
		mGameStatus = GameStatus.READY;
		mRemainingBombs = c.BOMBS;
	}

	/** Finalize a step.
	 * @param milliseconds The current play time in milliseconds. */
	public void finalizeStep(long milliseconds) {
		finalizeStep(milliseconds, mGameStatus, mRemainingBombs);
	}

	/** Finalize step.<br>
	 * <b>Note:</b> Probably better use {@link #finalizeStep(long)}. This
	 * version is normally not needed since the gameStatus and
	 * remainingBombs for the current step are already known because passed to
	 * {@link MatrixObserver} by the game logic. This version overwrites that
	 * information.
	 * @param milliseconds The current play time in milliseconds.
	 * @param gameStatus The current {@link GameStatus}.
	 * @param remainingBombs The count of remaining bombs on the game grid. */
	public void finalizeStep(long milliseconds, GameStatus gameStatus, int remainingBombs) {
		if (mRecordFlag) {
			TimeStep s = new TimeStep(milliseconds, gameStatus.ordinal(), remainingBombs,
					mStepBuffer.toArray(new Field[mStepBuffer.size()]));

			// LogDog.i(CLASSNAME, "SNAPSHOT @ " + s.TIME + "ms GameStatus: "
			// + GameStatus.fromInt(s.GAMESTATUS).toString() +
			// " RemainingBombs: " + s.BOMBS + " Fields changed: "
			// + s.STEPS.length);
			// for (Field f : s.STEPS) {
			// LogDog.i(CLASSNAME, "\t" + f.toString());
			// }

			mCurrent.addTimeStep(s);
			mStepBuffer.clear();

			if (GameStatus.WON == gameStatus || GameStatus.LOST == gameStatus) {
				mCurrent.setPlayTime(milliseconds);
				mRecordFlag = false;

				mComplete = new Replay(mCurrent);
			}
		} else {
			LogDog.w(CLASSNAME, "Not recording");
		}
	}

	/** Get a deep copy of the last completed {@link Replay}.<br>
	 * Note that its not possible to get the replay while currently recording.
	 * So the replay may be empty when the first game is running when you call
	 * this method.
	 * @return A copy of the last completed {@link Replay}. */
	public Replay getReplay() {
		return new Replay(mComplete);
	}

	@Override
	public void onGameStatusChanged(GameStatus newStatus) {
		/* Buffer the status until someone finalizes the current step */
		mGameStatus = newStatus;
	}

	@Override
	public void onRemainingBombsChanged(int remainingBombs) {
		/* Buffer the bomb count until someone finalizes the current step */
		mRemainingBombs = remainingBombs;
	}

	@Override
	public void afterFieldStatusChanged(Position p, FieldStatus fs, int adjacentBombs) {
		/* Buffer field changes until someone finalizes the current step */
		if (mRecordFlag)
			mStepBuffer.add(new Field(p, fs, adjacentBombs));
	}

	/** @return True when currently recording. */
	public boolean isRecording() {
		return mRecordFlag;
	}
}
