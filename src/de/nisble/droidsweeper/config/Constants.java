package de.nisble.droidsweeper.config;

import android.app.Activity;

public final class Constants {
	public static final long TIMER_PERIOD = 50;

	public static final int MAX_HIGHSCORES = 10;

	public static final int EASY_X = 6;
	public static final int EASY_Y = 8;
	public static final int EASY_BOMBS = 6;

	public static final int NORMAL_X = 8;
	public static final int NORMAL_Y = 12;
	public static final int NORMAL_BOMBS = 15;

	public static final int HARD_X = 10;
	public static final int HARD_Y = 18;
	public static final int HARD_BOMBS = 37;

	public static final int INTENTREQUEST_SETTINGS = 0;
	public static final int INTENTREQUEST_HIGHSCORELIST = 1;

	public static final int INTENTRESULT_PLAY_REPLAY = Activity.RESULT_FIRST_USER + 0;
	public static final int INTENTRESULT_CHANGE_GAMECONFIG = Activity.RESULT_FIRST_USER + 1;
}
