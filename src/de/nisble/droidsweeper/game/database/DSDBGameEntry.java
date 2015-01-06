package de.nisble.droidsweeper.game.database;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import de.nisble.droidsweeper.config.Level;
import de.nisble.droidsweeper.game.replay.TimeStep;

/** Container class for holding database entries.
 * This class is used to handle database entries.
 * It deserializes the given byte[] from the database to a list of
 * {@link TimeStep snapshots}.
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public final class DSDBGameEntry {
	// private static final String CLASSNAME =
	// DSDBGameEntry.class.getSimpleName();

	public final long GAMEID;
	public final Level LEVEL;
	public final String NAME;
	public final long PLAYTIME;
	public final long EPOCHTIME;
	public final List<TimeStep> TIMESTEPS;

	public DSDBGameEntry(long id, Level difficulty, String username, long playTime, long epochTime,
			byte[] serializedTimeSteps) throws Exception {
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

	private static <T> List<T> deserializeTimeSteps(byte[] data) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bais);
		return readObject(ois);
	}

	@SuppressWarnings("unchecked")
	private static <T> T readObject(java.io.ObjectInputStream in) throws java.io.IOException,
			java.lang.ClassNotFoundException {
		return (T) in.readObject();
	}
}
