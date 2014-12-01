package de.nisble.droidsweeper.game.replay;

import java.util.ArrayList;
import java.util.List;

import de.nisble.droidsweeper.config.GameConfig;
import de.nisble.droidsweeper.game.Field;
import de.nisble.droidsweeper.game.Position;
import de.nisble.droidsweeper.game.jni.FieldStatus;
import de.nisble.droidsweeper.game.jni.GameStatus;
import de.nisble.droidsweeper.game.jni.MatrixObserver;
import de.nisble.droidsweeper.utilities.LogDog;

public class Recorder implements MatrixObserver {
	private final String CLASSNAME = Recorder.class.getSimpleName();

	boolean mRecordFlag = false;

	private Replay mReplay = new Replay();

	private List<Field> mStepBuffer = new ArrayList<Field>();
	private GameStatus mGameStatus = GameStatus.READY;
	private int mRemainingBombs = 0;

	public void newRecord(GameConfig c) {
		mRecordFlag = true;

		mReplay = new Replay();
		mReplay.setGameConfig(c);
		mReplay.setEpochTime(System.currentTimeMillis());

		/* Reset internal state */
		mStepBuffer.clear();
		mGameStatus = GameStatus.READY;
		mRemainingBombs = c.BOMBS;
	}

	public void finalizeStep(long milliseconds) {
		finalizeStep(milliseconds, mGameStatus, mRemainingBombs);
	}

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

			mReplay.addTimeStep(s);
			mStepBuffer.clear();

			if (GameStatus.WON == gameStatus || GameStatus.LOST == gameStatus) {
				mReplay.setPlayTime(milliseconds);
				mRecordFlag = false;
			}
		} else {
			LogDog.w(CLASSNAME, "Not recording");
		}
	}

	public Replay getReplayCopy() {
		return new Replay(mReplay);
	}

	public Replay getReplay() {
		return mReplay;
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

	public boolean isRecording() {
		return mRecordFlag;
	}
}
