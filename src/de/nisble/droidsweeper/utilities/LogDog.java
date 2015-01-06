package de.nisble.droidsweeper.utilities;

import de.nisble.droidsweeper.BuildConfig;
import android.util.Log;

/** LogDog: The natural enemy of LogCat.
 * <p>
 * <b>Note:</b> Use the following ProGuard statement to completely remove all
 * calls to Log:
 *
 * <pre>
 * -assumenosideeffects class android.util.Log {
 * 		public static *** v(...);
 * 		public static *** d(...);
 * 		public static *** i(...);
 * 		public static *** w(...);
 * }
 * </pre>
 *
 * </p>
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public final class LogDog {
	// private static final String CLASSNAME = LogDog.class.getSimpleName();

	// public static void check() {
	// Log.println(Log.ASSERT, CLASSNAME, "BuildConfig.DEBUG=" +
	// BuildConfig.DEBUG);
	// Log.println(Log.ASSERT, CLASSNAME, "Log.isLoggable(..., Log.VERBOSE)=" +
	// Log.isLoggable(CLASSNAME, Log.VERBOSE));
	// Log.println(Log.ASSERT, CLASSNAME, "Log.isLoggable(..., Log.DEBUG)=" +
	// Log.isLoggable(CLASSNAME, Log.DEBUG));
	// Log.println(Log.ASSERT, CLASSNAME, "Log.isLoggable(..., Log.INFO)=" +
	// Log.isLoggable(CLASSNAME, Log.INFO));
	// Log.println(Log.ASSERT, CLASSNAME, "Log.isLoggable(..., Log.WARN)=" +
	// Log.isLoggable(CLASSNAME, Log.WARN));
	// Log.println(Log.ASSERT, CLASSNAME, "Log.isLoggable(..., Log.ERROR)=" +
	// Log.isLoggable(CLASSNAME, Log.ERROR));
	// }

	/** Send an {@link Log#VERBOSE} log message.
	 * @param tag Used to identify the source of a log message. It usually
	 *            identifies
	 *            the class or activity where the log call occurs.
	 * @param msg The message you would like logged. */
	public static void v(String tag, String msg) {
		// Removed from release when BuildConfig.DEBUG is false.
		if (BuildConfig.DEBUG && Log.isLoggable(tag, Log.VERBOSE)) {
			Log.v(tag, msg);
		}
	}

	/** Send an {@link Log#VERBOSE} log message and log the exception.
	 * @param tag Used to identify the source of a log message. It usually
	 *            identifies
	 *            the class or activity where the log call occurs.
	 * @param msg The message you would like logged. */
	public static void v(String tag, String msg, Throwable tr) {
		// Removed from release when BuildConfig.DEBUG is false.
		if (BuildConfig.DEBUG && Log.isLoggable(tag, Log.VERBOSE)) {
			Log.v(tag, msg, tr);
		}
	}

	/** Send an {@link Log#DEBUG} log message.
	 * @param tag Used to identify the source of a log message. It usually
	 *            identifies
	 *            the class or activity where the log call occurs.
	 * @param msg The message you would like logged. */
	public static void d(String tag, String msg) {
		// Removed from release when BuildConfig.DEBUG is false.
		if (BuildConfig.DEBUG && Log.isLoggable(tag, Log.DEBUG)) {
			Log.d(tag, msg);
		}
	}

	/** Send an {@link Log#DEBUG} log message and log the exception.
	 * @param tag Used to identify the source of a log message. It usually
	 *            identifies
	 *            the class or activity where the log call occurs.
	 * @param msg The message you would like logged. */
	public static void d(String tag, String msg, Throwable tr) {
		// Removed from release when BuildConfig.DEBUG is false.
		if (BuildConfig.DEBUG && Log.isLoggable(tag, Log.DEBUG)) {
			Log.d(tag, msg, tr);
		}
	}

	/** Send an {@link Log#INFO} log message.
	 * @param tag Used to identify the source of a log message. It usually
	 *            identifies
	 *            the class or activity where the log call occurs.
	 * @param msg The message you would like logged. */
	public static void i(String tag, String msg) {
		if (Log.isLoggable(tag, Log.INFO)) {
			Log.i(tag, msg);
		}
	}

	/** Send an {@link Log#INFO} log message and log the exception.
	 * @param tag Used to identify the source of a log message. It usually
	 *            identifies
	 *            the class or activity where the log call occurs.
	 * @param msg The message you would like logged. */
	public static void i(String tag, String msg, Throwable tr) {
		if (Log.isLoggable(tag, Log.INFO)) {
			Log.i(tag, msg, tr);
		}
	}

	/** Send an {@link Log#WARN} log message.
	 * @param tag Used to identify the source of a log message. It usually
	 *            identifies
	 *            the class or activity where the log call occurs.
	 * @param msg The message you would like logged. */
	public static void w(String tag, String msg) {
		if (Log.isLoggable(tag, Log.WARN)) {
			Log.w(tag, msg);
		}
	}

	/** Send an {@link Log#WARN} log message and log the exception.
	 * @param tag Used to identify the source of a log message. It usually
	 *            identifies
	 *            the class or activity where the log call occurs.
	 * @param msg The message you would like logged. */
	public static void w(String tag, String msg, Throwable tr) {
		if (Log.isLoggable(tag, Log.WARN)) {
			Log.w(tag, msg, tr);
		}
	}

	/** Send an {@link Log#ERROR} log message.
	 * @param tag Used to identify the source of a log message. It usually
	 *            identifies
	 *            the class or activity where the log call occurs.
	 * @param msg The message you would like logged. */
	public static void e(String tag, String msg) {
		if (Log.isLoggable(tag, Log.ERROR)) {
			Log.e(tag, msg);
		}
	}

	/** Send an {@link Log#ERROR} log message and log the exception.
	 * @param tag Used to identify the source of a log message. It usually
	 *            identifies
	 *            the class or activity where the log call occurs.
	 * @param msg The message you would like logged. */
	public static void e(String tag, String msg, Throwable tr) {
		if (Log.isLoggable(tag, Log.ERROR)) {
			Log.e(tag, msg, tr);
		}
	}
}