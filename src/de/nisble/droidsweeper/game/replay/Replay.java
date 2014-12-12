package de.nisble.droidsweeper.game.replay;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.nisble.droidsweeper.config.GameConfig;
import de.nisble.droidsweeper.config.Level;
import de.nisble.droidsweeper.game.Field;
import de.nisble.droidsweeper.game.Position;
import de.nisble.droidsweeper.game.database.DSDBAdapter;
import de.nisble.droidsweeper.game.database.DSDBGameEntry;
import de.nisble.droidsweeper.game.jni.FieldStatus;
import de.nisble.droidsweeper.game.jni.GameStatus;

/** Representation of a game replay.<br>
 * A replay is defined as a series of changes of the {@link FieldStatus states}
 * of {@link Field fields} at a specific time in the game.
 * When the game should be replayed, a timer (with the same resolution of the
 * timer that was used during recording) is started. When the timer fires, the
 * time in the next step of the replay is compared to the timer. The GUI is
 * updated when the two times are compared equal.<br>
 * For this to work a replay is structured as follows:<br>
 * <ul>
 * <li>{@link Field}: Class that represents a single field. Each instance holds
 * the {@link Position}, the {@link FieldStatus state} and the count of adjacent
 * bombs of a single field.</li>
 * <li>{@link TimeStep}: Class that holds the incremental changes of the game at
 * a specific time. Beside the play time, the {@link GameStatus} and the
 * remaining bombs, this class holds an array of all {@link Field fields} that
 * have changed its state. So only the changes of the game matrix is recorded.
 * Furthermore instances of this class are only produced when there are changes
 * to record, not at each time step.</li>
 * <li>{@link Replay} (this class): This class holds a series of TimeStepS and
 * adds the date (as epoch time), the overall play time, the dimensions of the
 * game matrix (as {@link GameConfig}) and the name of the player.</li>
 * </ul>
 * The replay can than be stored in the database by
 * {@link #serializeTimeSteps() serializing} the list of TimeStepS. The
 * {@link DSDBAdapter#insertTime(Replay)} abstracts this mechanism.
 * @author Moritz Nisblé moritz.nisble@gmx.de
 * @see {@link Player} for how to play a recorded replay.
 * @see {@link Recorder} for how to record a replay. */
public class Replay {
	// private static final String CLASSNAME = Replay.class.getSimpleName();

	/* GameConfig is immutable */
	private GameConfig mGameConfig = new GameConfig(Level.EASY);
	private String mName = new String();
	private long mPlayTime = 0;
	private long mEpochTime = 0;
	/* TimeStepS are immutable */
	private List<TimeStep> mTimeSteps = new ArrayList<TimeStep>();

	/** Create an empty {@link Replay}. */
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

	/** @return The {@link GameConfig} of this replay. */
	public GameConfig getGameConfig() {
		return mGameConfig;
	}

	/** Set the {@link GameConfig} of this replay.
	 * @param gameConfig The config. */
	public void setGameConfig(GameConfig gameConfig) {
		mGameConfig = gameConfig;
	}

	/** @return The name of the game player. */
	public String getName() {
		return mName;
	}

	/** Set the name of the game player.
	 * @param name The name. */
	public void setName(String name) {
		mName = name;
	}

	/** @return The play time in milliseconds. */
	public long getPlayTime() {
		return mPlayTime;
	}

	/** Set the reached play time.
	 * @param milliseconds The play time. */
	public void setPlayTime(long milliseconds) {
		mPlayTime = milliseconds;
	}

	/** @return The date as epoch time (Unix time). */
	public long getEpochTime() {
		return mEpochTime;
	}

	/** Set the date as epoch time (Unix time).
	 * @param epochTime The date. */
	public void setEpochTime(long epochTime) {
		mEpochTime = epochTime;
	}

	/** Push {@link TimeStep steps} to the interal list.
	 * @param steps The {@link TimeStep steps} to add. */
	public void addTimeStep(TimeStep steps) {
		mTimeSteps.add(steps);
	}

	/** @return The internal list of {@link TimeStep} */
	public List<TimeStep> getTimeSteps() {
		return mTimeSteps;
	}

	/** Serialize changes in the game matrix stored as array of {@link TimeStep
	 * TimeStepS}.
	 * @return A byte array with the serialized array of {@link TimeStep}.
	 * @throws IOException Thrown on error in serialization. */
	public byte[] serializeTimeSteps() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oot = new ObjectOutputStream(baos);
		oot.writeObject(mTimeSteps);
		oot.close();
		return baos.toByteArray();
	}
}
