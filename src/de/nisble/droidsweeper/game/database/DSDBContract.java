package de.nisble.droidsweeper.game.database;

import de.nisble.droidsweeper.config.Level;
import android.provider.BaseColumns;

final class DSDBContract {
	// private static final String CLASSNAME =
	// DSDBContract.class.getSimpleName();

	static final int DB_VERSION = 4;
	static final String DB_NAME = "droidsweeper.sqlite";

	DSDBContract() {
	}

	static abstract class LevelTable implements BaseColumns {
		static final String TABLE_NAME = "level";
		static final String CN_LEVEL = "level";
		static final String CN_X = "x";
		static final String CN_Y = "y";
		static final String CN_BOMBS = "bombs";

		//@formatter:off
		static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
				+ _ID + " INTEGER PRIMARY KEY, "
				+ CN_LEVEL + " INTEGER UNIQUE NOT NULL, "
				+ CN_X + " INTEGER NOT NULL, "
				+ CN_Y + " INTEGER NOT NULL, "
				+ CN_BOMBS + " INTEGER NOT NULL)";
		static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
		//@formatter:on

		// Helper that creates standard insert strings
		private static String getInsertString(Level l) {
			//@formatter:off
			return new String("INSERT INTO " + TABLE_NAME
					+ " ("
					+ CN_LEVEL + ", " + CN_X + ", " + CN_Y + ", " + CN_BOMBS
					+ ") VALUES ("
					+ l.ordinal() + ", " + l.X + ", " + l.Y + ", " + l.BOMBS
					+ ")");
			//@formatter:on
		}

		static final String INSERT_EASY = getInsertString(Level.EASY);
		static final String INSERT_NORMAL = getInsertString(Level.NORMAL);
		static final String INSERT_HARD = getInsertString(Level.HARD);

		static String q(String column) {
			return new String(TABLE_NAME + "." + column);
		}
	}

	static abstract class PlayerTable implements BaseColumns {
		static final String TABLE_NAME = "player";
		static final String CN_NAME = "name";

		//@formatter:off
		static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
				+ _ID + " INTEGER PRIMARY KEY, "
				+ CN_NAME + " TEXT NOT NULL)";
		//@formatter:on
		static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

		static String q(String column) {
			return new String(TABLE_NAME + "." + column);
		}
	}

	static abstract class GameTable implements BaseColumns {
		static final String TABLE_NAME = "game";
		static final String CN_GAMEPLAYER = "gameplayer";
		static final String CN_GAMELEVEL = "gamelevel";
		static final String CN_TIME = "time";
		static final String CN_DATE = "date";
		static final String CN_REPLAY = "replay";

		//@formatter:off
		static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
				+ _ID + " INTEGER PRIMARY KEY, "
				+ CN_GAMEPLAYER + " REFERENCES " + PlayerTable.TABLE_NAME + "(" + PlayerTable._ID + "), "
				+ CN_GAMELEVEL + " REFERENCES " + LevelTable.TABLE_NAME + "(" + LevelTable.CN_LEVEL + "), "
				+ CN_TIME + " INTEGER NOT NULL, "
				+ CN_DATE + " DATETIME NOT NULL, "
				+ CN_REPLAY + " BLOB)";
		//@formatter:on
		static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

		static String q(String column) {
			return new String(TABLE_NAME + "." + column);
		}
	}

	static abstract class Level4GameView {
		static final String VIEW_NAME = "level4game";
		static final String _ID = GameTable._ID;
		static final String CN_LEVEL = LevelTable.CN_LEVEL;
		static final String CN_TIME = GameTable.CN_TIME;
		static final String CN_DATE = GameTable.CN_DATE;
		static final String CN_GAMEPLAYER = GameTable.CN_GAMEPLAYER;

		//@formatter:off
		static final String CREATE_VIEW = "CREATE VIEW IF NOT EXISTS " + VIEW_NAME
				+ " AS SELECT "
				+ GameTable.q(_ID) + ", "
				+ CN_LEVEL + ", "
				+ CN_TIME + ", "
				+ CN_DATE + ", "
				+ CN_GAMEPLAYER
				+ " FROM " + GameTable.TABLE_NAME + " JOIN " + LevelTable.TABLE_NAME
				+ " ON " + LevelTable.CN_LEVEL + "=" + GameTable.CN_GAMELEVEL;
		//@formatter:on
		static final String DROP_VIEW = "DROP VIEW IF EXISTS " + VIEW_NAME;
	}

	static abstract class Player4GameView {
		static final String VIEW_NAME = "player4game";
		static final String _ID = GameTable._ID;
		static final String CN_NAME = PlayerTable.CN_NAME;

		//@formatter:off
		static final String CREATE_VIEW = "CREATE VIEW IF NOT EXISTS " + VIEW_NAME
				+" AS SELECT "
				+ GameTable.q(_ID) + ", "
				+ CN_NAME
				+ " FROM " + GameTable.TABLE_NAME + " JOIN " + PlayerTable.TABLE_NAME
				+ " ON " + GameTable.CN_GAMEPLAYER + "=" + PlayerTable.q(_ID);
		//@formatter:on
		static final String DROP_VIEW = "DROP VIEW IF EXISTS " + VIEW_NAME;
	}

	static abstract class GamesView {
		static final String VIEW_NAME = "games";
		static final String _ID = GameTable._ID;
		static final String CN_LEVEL = LevelTable.CN_LEVEL;
		static final String CN_NAME = PlayerTable.CN_NAME;
		static final String CN_TIME = GameTable.CN_TIME;
		static final String CN_DATE = GameTable.CN_DATE;

		// CREATE VIEW IF NOT EXISTS games AS SELECT l4g._id AS _id, level,
		// name, time, date FROM level4game l4g JOIN player4game p4g ON
		// l4g._id=p4g._id;

		//@formatter:off
		static final String CREATE_VIEW = "CREATE VIEW IF NOT EXISTS " + VIEW_NAME
				+" AS SELECT "
				+ "l4g." + _ID + " AS " + _ID + ", "
				+ CN_LEVEL + ", "
				+ CN_NAME + ", "
				+ CN_TIME + ", "
				+ CN_DATE
				+ " FROM " + Level4GameView.VIEW_NAME + " l4g JOIN " + Player4GameView.VIEW_NAME + " p4g"
				+ " ON l4g." + _ID + "=p4g." + _ID;
		//@formatter:on
		static final String DROP_VIEW = "DROP VIEW IF EXISTS " + VIEW_NAME;
	}

//	static final String ORDER_TIME_DATE_ASC = GameTable.CN_TIME + ", " + GameTable.CN_DATE;
}
