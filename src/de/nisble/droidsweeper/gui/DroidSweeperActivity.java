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
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableRow;
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
import de.nisble.droidsweeper.gui.grid.GameGridLayout;
import de.nisble.droidsweeper.utilities.LogDog;
import de.nisble.droidsweeper.utilities.Position;

/** Main-Activity
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public class DroidSweeperActivity extends Activity {
	private static final String CLASSNAME = DroidSweeperActivity.class.getSimpleName();

	private TextView mOverlayText;
	private RelativeLayout mOverlayLayout;

	private Button mBtNewGame;
	private GameGridLayout mGameGrid;
	private TextView mTimeLabel;
	private TextView mBombCounter;

	private Vibrator mVibrator;

	private Dialog mWinDialog;
	private static final int WIN_DIALOG = 0;
	private static final int REPLAY_DIALOG = 1;
	private static final int REPLAY_AGAIN_DIALOG = 2;
	private static final int FIRSTSTART_DIALOG = 3;

	private Player mPlayer = new Player();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		mOverlayText = (TextView) findViewById(R.id.overlayText);
		mOverlayLayout = (RelativeLayout) findViewById(R.id.overlayLayout);

		mBtNewGame = (Button) findViewById(R.id.btNewGame);
		mBtNewGame.setText(R.string.newGame);
		mBtNewGame.setOnClickListener(onNewGameClicked);

		mGameGrid = (GameGridLayout) findViewById(R.id.gameGrid);
		mGameGrid.setShrinkAllColumns(true);

		mTimeLabel = (TextView) findViewById(R.id.tvTime);
		mBombCounter = (TextView) findViewById(R.id.tvBombs);

		FieldDrawables.loadGrayback(this);

		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		
		// Open database
		DSDBAdapter.INSTANCE.open(this);
		// Load application config
		ApplicationConfig.INSTANCE.init(this).load();
		LogDog.i(CLASSNAME, "ApplicationConfig loaded: " + ApplicationConfig.INSTANCE.toString());

		Game.INSTANCE.addListener(mGameObserver);

		if (ApplicationConfig.INSTANCE.isShowInstructions() == true)
			showDialog(FIRSTSTART_DIALOG);

		mPlayer.addObserver(mReplayObserver);

		// Start game with the GameConfig loaded from persistent memory
		Game.INSTANCE.start(ApplicationConfig.INSTANCE.getGameConfig());
	}

	// @Override
	// protected void onRestoreInstanceState(Bundle savedInstanceState) {
	// super.onRestoreInstanceState(savedInstanceState);
	// }

	@Override
	protected void onResume() {
		mPlayer.resume();
		Game.INSTANCE.resume();
		super.onResume();
	}

	// @Override
	// protected void onSaveInstanceState(Bundle outState) {
	// super.onSaveInstanceState(outState);
	// }

	@Override
	protected void onPause() {
		mPlayer.pause();
		Game.INSTANCE.pause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		ApplicationConfig.INSTANCE.store(Game.INSTANCE.getGameConfig());
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

	/* TODO: Add a GridView and move this code into it.
	 * Use an interface "FieldConnector" and pass it into
	 * this method. Implement one FieldConnector for connecting
	 * the Player and one for a real game. */
	private void buildGrid(GameConfig c, boolean replay) {
		mOverlayLayout.setVisibility(View.INVISIBLE);

		// XXX: Get a orientation corrected version of the config
		GameConfig cv = c.adjustOrientation(this);

		mGameGrid.removeAllViews();
		mGameGrid.setDimensions(cv.X, cv.Y);

		for (int y = 0; y < cv.Y; y++) {
			TableRow row = new TableRow(getApplicationContext());

			// XXX: ???
			row.setId(100 + y);

			row.setLayoutParams(new GameGridLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 0));
			mGameGrid.addView(row);

			for (int x = 0; x < cv.X; x++) {

				FieldView field = new FieldView(getApplicationContext());
				field.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				field.setAdjustViewBounds(true);
				field.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

				/* NOTE: Game and Replay only know one layout.
				 * The view corrects the orientation.
				 * The Positions in the Fields remain in the PORTRAIT layout.
				 * Cause the position of the fields is queried via
				 * FieldListener.getPosition()
				 * mapping to libmsmS and PlayerS matrix is no problem.
				 * This is the only place where we have to transpose the
				 * matrix! */
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
					field.setPosition(new Position(y, c.Y - 1 - x));
				else
					field.setPosition(new Position(x, y));

				field.setOnClickListener(onFieldClicked);

				field.setLongClickable(true);
				field.setOnLongClickListener(onLongFiledClicked);

				row.addView(field);

				/* This should be moved out.
				 * Call it via a passed FieldConnector interface. */
				try {
					if (replay)
						mPlayer.addFieldListener(field);
					else
						Game.INSTANCE.setFieldListener(field);
				} catch (Exception e) {
					LogDog.e(CLASSNAME, e.getMessage(), e);
				}
			}
		}
	}

	private final GameObserver mGameObserver = new GameObserver() {
		@Override
		public void onBuildGrid(GameConfig config) {
			buildGrid(config, false);
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
					showOverlayMsg(getString(R.string.wonMsg) + "\n" + getString(R.string.badTimeMsg));

					if (ApplicationConfig.INSTANCE.isReplayOnLost()) {
						// Load the current game into the player
						mPlayer.load(Game.INSTANCE.getReplay());
						showDialog(REPLAY_DIALOG);
					}
				}
			} else {
				showOverlayMsg(getString(R.string.wonMsg) + "\n" + getString(R.string.customGameMsg));
			}
		}

		@Override
		public void onLost(long milliseconds) {
			showOverlayMsg(getString(R.string.lostMsg));

			if (ApplicationConfig.INSTANCE.isReplayOnLost()) {
				// Load the current game into the player
				mPlayer.load(Game.INSTANCE.getReplay());
				showDialog(REPLAY_DIALOG);
			}
		}
	};

	private final PlayerObserver mReplayObserver = new PlayerObserver() {
		@Override
		public void onBuildGrid(GameConfig config) {
			buildGrid(config, true);
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

	private void showOverlayMsg(String str) {
		mOverlayText.setText(str);
		mOverlayLayout.setVisibility(View.VISIBLE);
	}

	private final OnClickListener onFieldClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Game.INSTANCE.revealField(((FieldView) v).getPosition());
		}
	};

	private final OnLongClickListener onLongFiledClicked = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			// TODO: Make vibration (and time) configurable
			// TODO: Mark with double click 
			mVibrator.vibrate(100);
			Game.INSTANCE.cycleMark(((FieldView) v).getPosition());
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