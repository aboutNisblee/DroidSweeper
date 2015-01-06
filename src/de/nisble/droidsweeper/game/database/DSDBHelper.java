package de.nisble.droidsweeper.game.database;

import de.nisble.droidsweeper.utilities.LogDog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import static de.nisble.droidsweeper.game.database.DSDBContract.*;

final class DSDBHelper extends SQLiteOpenHelper {
	private static final String CLASSNAME = DSDBHelper.class.getSimpleName();

	DSDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		LogDog.i(CLASSNAME, "Creating new database: " + DB_NAME + " Version " + DB_VERSION);
		// Create tables
		db.execSQL(LevelTable.CREATE_TABLE);
		db.execSQL(PlayerTable.CREATE_TABLE);
		db.execSQL(GameTable.CREATE_TABLE);
		// Create views
		db.execSQL(Level4GameView.CREATE_VIEW);
		db.execSQL(Player4GameView.CREATE_VIEW);
		db.execSQL(GamesView.CREATE_VIEW);
		// Insert standard levels
		db.execSQL(LevelTable.INSERT_EASY);
		db.execSQL(LevelTable.INSERT_NORMAL);
		db.execSQL(LevelTable.INSERT_HARD);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		LogDog.i(CLASSNAME, "Updating database from " + oldVersion + " to " + newVersion);
		// Drop tables
		db.execSQL(GameTable.DROP_TABLE);
		db.execSQL(PlayerTable.DROP_TABLE);
		db.execSQL(LevelTable.DROP_TABLE);
		// Drop views
		db.execSQL(Level4GameView.DROP_VIEW);
		db.execSQL(Player4GameView.DROP_VIEW);
		db.execSQL(GamesView.DROP_VIEW);
		// Create new one
		onCreate(db);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		db.execSQL("PRAGMA foreign_keys=ON;");
	}
}
