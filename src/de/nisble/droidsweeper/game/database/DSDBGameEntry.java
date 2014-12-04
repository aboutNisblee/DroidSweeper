package de.nisble.droidsweeper.game.database;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import de.nisble.droidsweeper.config.Level;
import de.nisble.droidsweeper.game.replay.TimeStep;

/** Container class for holding database entries.<br>
 * This class is used to handle database entries. It deserializes the given
 * byte[] from the database to a list of {@link TimeStep}.
 * <ul>
 * <li>Immutable: Members are public final and capitalized.</li>
 * </ul>
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public final class DSDBGameEntry {
	// private static final String CLASSNAME =
	// DSDBGameEntry.class.getSimpleName();

	/** The ID of the game in the database. */
	public final long GAMEID;
	/** The level the game was played on. */
	public final Level LEVEL;
	/** The name of the player. */
	public final String NAME;
	/** The playtime. */
	public final long PLAYTIME;
	/** The date as epoch time. */
	public final long EPOCHTIME;
	/** The replay as list of changes of the game at a specific time. */
	public final List<TimeStep> TIMESTEPS;

	/** Initialize a game entry with the data from the database.
	 * @param id The ID of the game.
	 * @param difficulty The difficulty level.
	 * @param username The name of the player.
	 * @param playTime The reached time in milliseconds.
	 * @param epochTime The data as epoch time.
	 * @param serializedTimeSteps The serialized replay from the database.
	 * @throws ClassNotFoundException On error in deserialisation of the replay.
	 * @throws IOException On error in deserialisation of the replay. */
	public DSDBGameEntry(long id, Level difficulty, String username, long playTime, long epochTime,
			byte[] serializedTimeSteps) throws ClassNotFoundException, IOException {
		GAMEID = id;
		LEVEL = difficulty;
		NAME = username;
		PLAYTIME = playTime;
		EPOCHTIME = epochTime;
		if (serializedTimeSteps != null)
			TIMESTEPS = deserializeTimeSteps(serializedTimeSteps);
		else
			TIMESTEPS = new ArrayList<TimeStep>();
	}

	@SuppressWarnings("unchecked")
	private static List<TimeStep> deserializeTimeSteps(byte[] data) throws ClassNotFoundException, IOException {
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
		return (List<TimeStep>) ois.readObject();
	}
}
