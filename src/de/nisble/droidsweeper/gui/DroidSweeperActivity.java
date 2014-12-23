package de.nisble.droidsweeper.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.nisble.droidsweeper.R;
import static de.nisble.droidsweeper.config.Constants.*;
import de.nisble.droidsweeper.config.GameConfig;
import de.nisble.droidsweeper.config.Level;
import de.nisble.droidsweeper.config.ApplicationConfig;
import de.nisble.droidsweeper.game.Game;
import de.nisble.droidsweeper.game.GameObserver;
import de.nisble.droidsweeper.game.database.DSDBAdapter;
import de.nisble.droidsweeper.game.replay.Player;
import de.nisble.droidsweeper.game.replay.PlayerObserver;
import de.nisble.droidsweeper.game.replay.Replay;
import de.nisble.droidsweeper.gui.grid.FieldDrawables;
import de.nisble.droidsweeper.gui.grid.FieldView;
import de.nisble.droidsweeper.gui.grid.GameGridView;
import de.nisble.droidsweeper.utilities.LogDog;

/** Main-Activity
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public class DroidSweeperActivity extends Activity {
	private static final String CLASSNAME = DroidSweeperActivity.class.getSimpleName();

	private Button mBtNewGame;
	private GameGridView mGameGrid;
	private TextView mTimeLabel;
	private TextView mBombCounter;

	private Vibrator mVibrator;

	private Dialog mWinDialog;
	private static final int WIN_DIALOG = 0;
	private static final int REPLAY_DIALOG = 1;
	private static final int REPLAY_AGAIN_DIALOG = 2;
	private static final int FIRSTSTART_DIALOG = 3;

	// Replay player
	private Player mPlayer = new Player();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		mBtNewGame = (Button) findViewById(R.id.btNewGame);
		mBtNewGame.setText(R.string.newGame);
		mBtNewGame.setOnClickListener(onNewGameClicked);

		mGameGrid = (GameGridView) findViewById(R.id.gameGrid);
		mGameGrid.setFieldClickListener(mFieldClickListener);

		mTimeLabel = (TextView) findViewById(R.id.tvTime);
		mBombCounter = (TextView) findViewById(R.id.tvBombs);

		FieldDrawables.loadGrayback(this);

		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

		// Open database
		DSDBAdapter.INSTANCE.open(this);
		// Load config from disk
		GameConfig c = ApplicationConfig.INSTANCE.init(this).load();
		LogDog.i(CLASSNAME, "ApplicationConfig loaded: " + ApplicationConfig.INSTANCE.toString());

		Game.INSTANCE.addObserver(mGameObserver);

		if (ApplicationConfig.INSTANCE.isShowInstructions() == true)
			showDialog(FIRSTSTART_DIALOG);

		mPlayer.addObserver(mReplayObserver);

		// Start game with the GameConfig loaded from persistent memory
		Game.INSTANCE.start(c);
	}

	@Override
	protected void onResume() {
		mPlayer.resume();
		Game.INSTANCE.resume();
		super.onResume();
	}

	@Override
	protected void onPause() {
		mPlayer.pause();
		Game.INSTANCE.pause();

		ApplicationConfig.INSTANCE.store(Game.INSTANCE.getGameConfig());

		super.onPause();
	}

	@Override
	protected void onDestroy() {
		DSDBAdapter.INSTANCE.close();
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		invalidateOptionsMenu();

		if (Game.INSTANCE.isOrientationChangeable() && !mPlayer.isPlaying()) {
			Game.INSTANCE.start();
		}
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/* TODO: Implement a Button in the menu to reshow the first startup
		 * message! */
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.mainmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		/* WORAROUND für http://code.google.com/p/android/issues/detail?id=20493 */
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			DisplayMetrics dm = getResources().getDisplayMetrics();
			int screenWidthDIP = (int) Math.round(((double) dm.widthPixels) / dm.density);
			boolean reallyWide = screenWidthDIP > 450;
			int menuItemState = reallyWide ? MenuItem.SHOW_AS_ACTION_ALWAYS
					: (MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
			int size = menu.size();
			for (int i = 0; i < size; i++) {
				menu.getItem(i).setShowAsAction(menuItemState);
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.mm_settings:
			intent = new Intent(getApplicationContext(), SettingsActivity.class);
			// TODO: Make Constant from string literal
			intent.putExtra("GameConfig", Game.INSTANCE.getGameConfig());

			startActivityForResult(intent, INTENTREQUEST_SETTINGS);
			break;
		case R.id.mm_highscores:
			intent = new Intent(getApplicationContext(), HighScoreActivity.class);

			startActivityForResult(intent, INTENTREQUEST_HIGHSCORELIST);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/* Callback that is passed to GameGridView.update() to connect the field
	 * views to the native library. */
	private GameGridView.FieldWidgetConnector mGameConnector = new GameGridView.FieldWidgetConnector() {
		@Override
		public void connect(FieldView field) {
			try {
				Game.INSTANCE.setFieldListener(field);
			} catch (Exception e) {
				LogDog.e(CLASSNAME, e.getMessage(), e);
			}
		}
	};

	/* Callback that is passed to GameGridView.update() to connect the field
	 * views to the replay player. */
	private GameGridView.FieldWidgetConnector mReplayConnector = new GameGridView.FieldWidgetConnector() {
		@Override
		public void connect(FieldView field) {
			try {
				mPlayer.setFieldListener(field);
			} catch (Exception e) {
				LogDog.e(CLASSNAME, e.getMessage(), e);
			}
		}
	};

	private final GameObserver mGameObserver = new GameObserver() {
		@Override
		public void onBuildGrid(GameConfig config) {
			mGameGrid.hideOverlay();
			mGameGrid.update(config, mGameConnector);
		}

		@Override
		public void onTimeUpdate(long milliseconds) {
			mTimeLabel.setText(DateFormat.format("mm:ss", milliseconds).toString());
		}

		@Override
		public void onRemainingBombsChanged(int bombCount) {
			mBombCounter.setText(String.valueOf(bombCount));
		}

		@Override
		public void onWon(long milliseconds, boolean highscore) {
			if (Game.INSTANCE.getGameConfig().LEVEL != Level.CUSTOM) {
				if (highscore) {
					showDialog(WIN_DIALOG);
				} else {
					mGameGrid.showOverlay(getString(R.string.wonMsg) + "\n" + getString(R.string.badTimeMsg));

					if (ApplicationConfig.INSTANCE.isReplayOnLost()) {
						// Load the current game into the player
						mPlayer.load(Game.INSTANCE.getReplay());
						showDialog(REPLAY_DIALOG);
					}
				}
			} else {
				mGameGrid.showOverlay(getString(R.string.wonMsg) + "\n" + getString(R.string.customGameMsg));
			}
		}

		@Override
		public boolean onLost(long milliseconds) {
			mGameGrid.showOverlay(getString(R.string.lostMsg));

			if (ApplicationConfig.INSTANCE.isReplayOnLost()) {
				// Load the current game into the player
				mPlayer.load(Game.INSTANCE.getReplay());
				showDialog(REPLAY_DIALOG);
			}

			// Return true to reveal all fields
			return true;
		}
	};

	private final PlayerObserver mReplayObserver = new PlayerObserver() {
		@Override
		public void onBuildGrid(GameConfig config) {
			mGameGrid.hideOverlay();
			mGameGrid.update(config, mReplayConnector);
		}

		@Override
		public void onTimeUpdate(long milliseconds) {
			mTimeLabel.setText(DateFormat.format("mm:ss", milliseconds).toString());
		}

		@Override
		public void onRemainingBombsChanged(int bombCount) {
			mBombCounter.setText(String.valueOf(bombCount));
		}

		@Override
		public void onReplayEnded() {
			showDialog(REPLAY_AGAIN_DIALOG);
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		AlertDialog.Builder builder;

		switch (id) {
		case WIN_DIALOG:

			/* TODO: Use a normal Dialog! */

			mWinDialog = new Dialog(this);
			mWinDialog.setContentView(R.layout.layout_name_dialog);
			mWinDialog.setTitle(getString(R.string.congratulations));
			mWinDialog.setCanceledOnTouchOutside(false);
			Button b = (Button) mWinDialog.findViewById(R.id.bt_nameDialog_OK);
			b.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					EditText name = (EditText) mWinDialog.findViewById(R.id.editText_nameDialog_name);
					if (name.getText().length() >= 3) {

						Replay replay = Game.INSTANCE.getReplay();
						replay.setName(name.getText().toString());
						try {
							DSDBAdapter.INSTANCE.insertTime(replay);
						} catch (Exception e) {
							LogDog.e(CLASSNAME, e.getMessage(), e);
						}

						mWinDialog.dismiss();

						// Load the game into the player
						mPlayer.load(replay);
						showDialog(REPLAY_DIALOG);

					} else {
						Toast.makeText(getApplicationContext(), R.string.hint_short_name, Toast.LENGTH_SHORT).show();
						name.requestFocus();
					}
				}
			});

			dialog = mWinDialog;
			break;
		case REPLAY_DIALOG:
		case REPLAY_AGAIN_DIALOG:
			builder = new AlertDialog.Builder(this);
			builder.setMessage((REPLAY_DIALOG == id) ? getString(R.string.playReplay) : getString(R.string.playAgain));
			builder.setCancelable(false);
			builder.setPositiveButton(getString(R.string.Yes), onReplayYESButtonClicked);
			builder.setNegativeButton(getString(R.string.No), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			dialog = builder.create();
			break;
		case FIRSTSTART_DIALOG:
			builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.firstStartMsg));
			builder.setCancelable(false);
			builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			dialog = builder.create();
			break;
		default:
			dialog = null;
			break;
		}

		return dialog;
	}

	/* This is called before onResume!
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent) */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		/* Result from SettingsActivity */
		case INTENTREQUEST_SETTINGS:
			switch (resultCode) {

			/* Configuration changed */
			case INTENTRESULT_CHANGE_GAMECONFIG:

				/* Stop a maybe currently paused game */
				Game.INSTANCE.stop();
				mPlayer.stop();

				/* Get the new GameConfig from the Intent */
				GameConfig newConfig = data.getParcelableExtra("GameConfig");
				if (newConfig != null) {
					ApplicationConfig.INSTANCE.store(newConfig);
					Game.INSTANCE.start(newConfig);
				} else
					LogDog.e(CLASSNAME, "Unable to unpack GameConfig from intent");

				break;

			/* Nothing changed */
			case RESULT_CANCELED:
				break;

			default:
				LogDog.e(CLASSNAME, "Got unknown intent result code from SettingsActivity! Code:" + resultCode);
				break;
			}
			break;

		/* Result from HighScoreListActivity */
		case INTENTREQUEST_HIGHSCORELIST:
			switch (resultCode) {

			/* Replay selected for play */
			case INTENTRESULT_PLAY_REPLAY:

				/* Stop a maybe currently paused game */
				Game.INSTANCE.stop();
				mPlayer.stop();

				/* Get the GameID (RowID from DB) from the Intent */
				long gameID = data.getLongExtra("GAMEID", -1);
				if (-1 != gameID) {
					try {
						/* Let the player load the Replay from the DB */
						mPlayer.load(gameID);
						mPlayer.play();
					} catch (Exception e) {
						LogDog.e(CLASSNAME, e.getMessage(), e);
					}
				} else {
					LogDog.e(CLASSNAME, "Invalid GameID " + gameID);
				}
				break;

			/* Nothing selected */
			case RESULT_CANCELED:
				break;

			default:
				LogDog.e(CLASSNAME, "Got unknown intent result code from HighScoreActivity! Code:" + resultCode);
				break;
			}
			break;
		}
	}

	private final GameGridView.FieldClickListener mFieldClickListener = new GameGridView.FieldClickListener() {
		@Override
		public void onClick(FieldView field) {
			Game.INSTANCE.revealField(field.getPosition());
		}

		@Override
		public boolean onLongClick(FieldView field) {
			// TODO: Make vibration (and time) configurable
			// TODO: Mark with double click
			mVibrator.vibrate(100);
			Game.INSTANCE.cycleMark(field.getPosition());
			return true;
		}
	};

	private final OnClickListener onNewGameClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mPlayer.stop();
			Game.INSTANCE.start();
		}
	};

	private final DialogInterface.OnClickListener onReplayYESButtonClicked = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			mPlayer.play();
		}
	};
}