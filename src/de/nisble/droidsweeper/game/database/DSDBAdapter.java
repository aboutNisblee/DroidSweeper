package de.nisble.droidsweeper.game.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import de.nisble.droidsweeper.config.Constants;
import de.nisble.droidsweeper.config.Level;
import de.nisble.droidsweeper.game.database.DSDBContract.GameTable;
import de.nisble.droidsweeper.game.replay.Replay;
import de.nisble.droidsweeper.utilities.LogDog;
import static de.nisble.droidsweeper.game.database.DSDBContract.*;

/** Adapter to ease the access to the database.
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public final class DSDBAdapter {
	private static final String CLASSNAME = DSDBAdapter.class.getSimpleName();

	public static final DSDBAdapter INSTANCE = new DSDBAdapter();

	private DSDBHelper mHelper = null;

	private DSDBAdapter() {
	}

	/** Initialization.
	 * @note Method isn't thread-safe.
	 * @param context Androids application context. */
	public DSDBAdapter open(Context context) {
		if (mHelper == null) {
			mHelper = new DSDBHelper(context);
		}
		return this;
	}

	public void close() {
		if (mHelper != null)
			mHelper.close();
	}

	/** Get all stored games for a specific difficulty without the replay data.
	 * @param difficulty The difficulty.
	 * @return A with all entries for the given difficulty.
	 * @throws Exception On unknown difficulty. */
	public ArrayList<DSDBGameEntry> getGames(Level level) throws Exception {
		if (Level.CUSTOM == level) {
			throw new Exception("No database entries for custom difficulty levels");
		}

		ArrayList<DSDBGameEntry> result = new ArrayList<DSDBGameEntry>();
		SQLiteDatabase db = mHelper.getReadableDatabase();

		Cursor c = db.rawQuery("SELECT * FROM " + GamesView.VIEW_NAME + " WHERE " + GamesView.CN_LEVEL + "=? "
				+ " ORDER BY " + GamesView.CN_TIME + ", " + GamesView.CN_DATE,
				new String[] { String.valueOf(level.ordinal()) });
		try {
			if (c.moveToFirst()) {
				while (!c.isAfterLast()) {
					DSDBGameEntry entry = new DSDBGameEntry(c.getLong(c.getColumnIndexOrThrow(GamesView._ID)), level,
							c.getString(c.getColumnIndexOrThrow(GamesView.CN_NAME)), c.getLong(c
									.getColumnIndexOrThrow(GamesView.CN_TIME)), c.getLong(c
									.getColumnIndexOrThrow(GamesView.CN_DATE)), null);

					result.add(entry);
					c.moveToNext();
				}
			} else {
				LogDog.d(CLASSNAME, "No entries available for level: " + level.toString());
			}
		} finally {
			c.close();
		}

		return result;
	}

	public DSDBGameEntry getReplay(long gameID) throws Exception {
		DSDBGameEntry entry = null;
		SQLiteDatabase db = mHelper.getReadableDatabase();

		Cursor c = db.rawQuery("SELECT * FROM " + GamesView.VIEW_NAME + " WHERE " + GamesView._ID + "=? ",
				new String[] { String.valueOf(gameID) });

		try {
			if (c.moveToFirst()) {
				// Create an entry object with the information from GameView
				entry = new DSDBGameEntry(c.getLong(c.getColumnIndexOrThrow(GamesView._ID)), Level.fromInt(c.getInt(c
						.getColumnIndexOrThrow(GamesView.CN_LEVEL))), c.getString(c
						.getColumnIndexOrThrow(GamesView.CN_NAME)), c.getLong(c
						.getColumnIndexOrThrow(GamesView.CN_TIME)), c.getLong(c
						.getColumnIndexOrThrow(GamesView.CN_DATE)), null);
			} else {
				throw new Exception("Unable to query replay for ID " + gameID);
			}
		} finally {
			c.close();
		}

		// Use getReplay(DSDBGameEntry entry) to query the replay
		return getReplay(entry);
	}

	/** Get the replay for a given DBEntry.
	 * @note The returned entry is not the same as the one passed in.
	 *       Not thread-safe!
	 * @param entry The entry to query the replay for.
	 * @return A new entry including the replay data. */
	public DSDBGameEntry getReplay(DSDBGameEntry entry) throws Exception {
		if (Level.CUSTOM == entry.LEVEL) {
			throw new Exception("No database entries for custom difficulty levels");
		}

		DSDBGameEntry result = null;
		SQLiteDatabase db = mHelper.getReadableDatabase();

		/* ID is sufficient for where clause. */
		Cursor c = db.query(GameTable.TABLE_NAME, new String[] { GameTable._ID, GameTable.CN_REPLAY }, GameTable._ID
				+ "=?", new String[] { String.valueOf(entry.GAMEID) }, null, null, null);
		try {
			if (c.moveToFirst()) {
				/* Create a new entry from the given one and add the replay */
				result = new DSDBGameEntry(entry.GAMEID, entry.LEVEL, entry.NAME, entry.PLAYTIME, entry.EPOCHTIME,
						c.getBlob(c.getColumnIndex(GameTable.CN_REPLAY)));
			} else {
				throw new Exception("Unable to query replay for ID " + entry.GAMEID);
			}
		} finally {
			c.close();
		}

		return result;
	}

	/** Check if a time would be inserted or not. The database isn't changed.
	 * @param difficulty The difficulty.
	 * @param milliseconds The time in ms.
	 * @return True on insert, else false. */
	public boolean isHighScore(Level level, long milliseconds) throws Exception {
		if (Level.CUSTOM == level) {
			throw new Exception("No database entries for custom difficulty levels");
		}

		boolean result = false;
		SQLiteDatabase db = mHelper.getReadableDatabase();

		Cursor c = db.rawQuery("SELECT " + Level4GameView.CN_TIME + " FROM " + Level4GameView.VIEW_NAME + " WHERE "
				+ Level4GameView.CN_LEVEL + "=? " + " ORDER BY " + Level4GameView.CN_TIME + ", "
				+ Level4GameView.CN_DATE, new String[] { String.valueOf(level.ordinal()) });

		try {
			if (!c.moveToLast() || (c.getCount() < Constants.MAX_HIGHSCORES)
					|| milliseconds < c.getLong(c.getColumnIndexOrThrow(Level4GameView.CN_TIME))) {
				/* Highscore when: Less than 10 entries. Move to last is false
				 * (means there are no entries! If it is true we are now on the
				 * last entry.) The new time is faster (<) than the last
				 * one. (That means: No highscore on equal times. That should be
				 * the expected behavior.) */
				result = true;
			}
		} finally {
			c.close();
		}

		return result;
	}

	private void deleteExcessGames(Level level) {
		SQLiteDatabase db = mHelper.getWritableDatabase();

		/* Statement for getting all games for the given level.
		 * The ID is the one from the game table. */
		String sqlGames = "SELECT " + Level4GameView._ID + ", " + Level4GameView.CN_GAMEPLAYER + " FROM "
				+ Level4GameView.VIEW_NAME + " WHERE " + Level4GameView.CN_LEVEL + "="
				+ String.valueOf(level.ordinal()) + " ORDER BY " + Level4GameView.CN_TIME + ", "
				+ Level4GameView.CN_DATE;

		Cursor c = null;
		try {
			/* Query in loop while the cursor isn't empty and the count of games
			 * is bigger than the configured maximum */
			while ((c = db.rawQuery(sqlGames, null)).moveToLast() && c.getCount() > Constants.MAX_HIGHSCORES) {
				int result = 0;

				long gameID = c.getLong(c.getColumnIndexOrThrow(Level4GameView._ID));
				/* Save the playerID foreign key */
				long playerID = c.getLong(c.getColumnIndexOrThrow(Level4GameView.CN_GAMEPLAYER));

				/* Delete the game */
				result = db.delete(GameTable.TABLE_NAME, GameTable._ID + "=" + gameID, null);
				if (result > 0)
					LogDog.i(CLASSNAME, "Game " + gameID + " dropped.");

				/* Try to delete the player. DBMS restricts this when there are
				 * other games referencing this player! */
				try {
					result = db.delete(PlayerTable.TABLE_NAME, PlayerTable._ID + "=" + playerID, null);
					if (result > 0)
						LogDog.i(CLASSNAME, "Player " + playerID + " dropped.");
				} catch (SQLiteConstraintException e) {
					LogDog.i(CLASSNAME, "Unable to drop player " + playerID + ". Player is still referenced by other games.");
				}
			}
		} finally {
			if (c != null)
				c.close();
		}

	}

	/** Insert a new entry.
	 * @note Database removes the slowest time by trigger when more than 10
	 *       entries are made. Use isHighscore() to check if the time would be
	 *       inserted before a call to this function.
	 * @param entry An entry.
	 * @throws Exception On unknown difficulty.
	 * @return The ID of the inserted game. */
	public long insertTime(Replay replay) throws Exception {
		if (Level.CUSTOM == replay.getGameConfig().LEVEL) {
			throw new Exception("No database entries for custom difficulty levels");
		} else if (0 == replay.getName().length()) {
			throw new Exception("No player name set!");
		}

		ContentValues values = new ContentValues();
		long playerID = -1;
		long gameID = -1;
		SQLiteDatabase db = mHelper.getWritableDatabase();

		// Query player name
		Cursor c = db.query(PlayerTable.TABLE_NAME, new String[] { PlayerTable._ID, PlayerTable.CN_NAME },
				PlayerTable.CN_NAME + "=?", new String[] { replay.getName() }, null, null, null);

		/* Use a transaction to not pollute the player table with names
		 * that are not linked with a game, if something goes wrong and the
		 * game insert fails. */
		db.beginTransaction();
		try {
			/* Check whether the cursor is empty */
			if (!c.moveToFirst()) {
				/* Empty! Player name not found. Insert new one. */
				values.put(PlayerTable.CN_NAME, replay.getName());
				playerID = db.insertOrThrow(PlayerTable.TABLE_NAME, null, values);
				values.clear();
			} else if (c.getCount() > 1) {
				throw new SQLException("Player table inconsistant. Broken or manipulated database.");
			} else {
				/* Player name found. Store row id. */
				playerID = c.getLong(c.getColumnIndexOrThrow(PlayerTable._ID));
			}

			/* A query for the level is redundant. The level number is the
			 * foreign key! */
			values.put(GameTable.CN_GAMELEVEL, replay.getGameConfig().LEVEL.ordinal());
			values.put(GameTable.CN_GAMEPLAYER, playerID);
			values.put(GameTable.CN_TIME, replay.getPlayTime());
			values.put(GameTable.CN_DATE, replay.getEpochTime());
			values.put(GameTable.CN_REPLAY, replay.serializeTimeSteps());

			gameID = db.insertOrThrow(GameTable.TABLE_NAME, null, values);

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			c.close();
		}

		deleteExcessGames(replay.getGameConfig().LEVEL);

		return gameID;
	}
}
