package de.nisble.droidsweeper.game.replay;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.nisble.droidsweeper.config.GameConfig;
import de.nisble.droidsweeper.config.Level;
import de.nisble.droidsweeper.game.database.DSDBGameEntry;

public class Replay {
	// private static final String CLASSNAME = Replay.class.getSimpleName();

	/* GameConfig is immutable */
	private GameConfig mGameConfig = new GameConfig(Level.EASY);
	private String mName = new String();
	private long mPlayTime = 0;
	private long mEpochTime = 0;
	/* TimeStapes are immutable */
	private List<TimeStep> mTimeSteps = new ArrayList<TimeStep>();

	public Replay() {
	}

	/** Copy constructor.
	 * @param r An instance to copy. */
	public Replay(Replay r) {
		mGameConfig = r.mGameConfig;
		mName = r.mName;
		mPlayTime = r.mPlayTime;
		mEpochTime = r.mEpochTime;
		mTimeSteps = new ArrayList<TimeStep>(r.mTimeSteps);
	}

	/** Initialize a replay from a database entry.
	 * This leads always to a standard difficulty level.
	 * @param ge A database entry. */
	public Replay(DSDBGameEntry ge) {
		mGameConfig = new GameConfig(ge.LEVEL);
		mName = ge.NAME;
		mPlayTime = ge.PLAYTIME;
		mEpochTime = ge.EPOCHTIME;
		mTimeSteps = ge.TIMESTEPS;
	}

	public GameConfig getGameConfig() {
		return mGameConfig;
	}

	public void setGameConfig(GameConfig gameConfig) {
		mGameConfig = gameConfig;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public long getPlayTime() {
		return mPlayTime;
	}

	public void setPlayTime(long milliseconds) {
		mPlayTime = milliseconds;
	}

	public long getEpochTime() {
		return mEpochTime;
	}

	public void setEpochTime(long epochTime) {
		mEpochTime = epochTime;
	}

	public void addTimeStep(TimeStep snapshots) {
		mTimeSteps.add(snapshots);
	}

	public List<TimeStep> getTimeSteps() {
		return mTimeSteps;
	}

	public byte[] serializeTimeSteps() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oot = new ObjectOutputStream(baos);
		oot.writeObject(mTimeSteps);
		oot.close();
		return baos.toByteArray();
	}
}
