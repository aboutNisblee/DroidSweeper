package de.nisble.droidsweeper.config;

import android.app.Activity;

/** Global constants.
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public final class Constants {
	/** Timer resolution. */
	public static final long TIMER_PERIOD = 50;

	/** Maximum count of highscores in the database per {@link Level difficulty
	 * level}. */
	public static final int MAX_HIGHSCORES = 10;

	/** Horizontal dimension of a game at {@link Level#EASY}. */
	public static final int EASY_X = 6;
	/** Vertical dimension of a game at {@link Level#EASY}. */
	public static final int EASY_Y = 8;
	/** Bomb count of a game at {@link Level#EASY}. */
	public static final int EASY_BOMBS = 6;

	/** Horizontal dimension of a game at {@link Level#NORMAL}. */
	public static final int NORMAL_X = 8;
	/** Vertical dimension of a game at {@link Level#NORMAL}. */
	public static final int NORMAL_Y = 12;
	/** Bomb count of a game at {@link Level#NORMAL}. */
	public static final int NORMAL_BOMBS = 15;

	/** Horizontal dimension of a game at {@link Level#HARD}. */
	public static final int HARD_X = 10;
	/** Vertical dimension of a game at {@link Level#HARD}. */
	public static final int HARD_Y = 18;
	/** Bomb count of a game at {@link Level#HARD}. */
	public static final int HARD_BOMBS = 37;

	public static final int INTENTREQUEST_SETTINGS = 0;
	public static final int INTENTREQUEST_HIGHSCORELIST = 1;

	public static final int INTENTRESULT_PLAY_REPLAY = Activity.RESULT_FIRST_USER + 0;
	public static final int INTENTRESULT_CHANGE_GAMECONFIG = Activity.RESULT_FIRST_USER + 1;
}
