package de.nisble.droidsweeper.gui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.NumberPicker.OnValueChangeListener;
import de.nisble.droidsweeper.R;
import static de.nisble.droidsweeper.config.Constants.*;
import de.nisble.droidsweeper.config.GameConfig;
import de.nisble.droidsweeper.config.Level;
import de.nisble.droidsweeper.config.ApplicationConfig;
import de.nisble.droidsweeper.utilities.LogDog;

public class SettingsActivity extends Activity {
	private static final String CLASSNAME = SettingsActivity.class.getSimpleName();

	private ScrollView mScrollGameSettings;
	private Spinner mSpinDifficulty;
	private NumberPicker mNpWidth;
	private NumberPicker mNpHeight;
	private NumberPicker mNpBombs;
	private CheckBox mCbShowReplayOnLost;

	private GameConfig mPassedConfig = null;
	private GameConfig mCurrentConfig = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPassedConfig = getIntent().getParcelableExtra("GameConfig");
		if (null == mPassedConfig) {
			LogDog.e(CLASSNAME, "Unable to unpack GameConfig from intent");
			mPassedConfig = new GameConfig(Level.EASY);
		}

		mCurrentConfig = mPassedConfig;

		LogDog.i(CLASSNAME, "Passed GameConfig: " + mPassedConfig.toString());

		setContentView(R.layout.layout_gamesettings);

		mScrollGameSettings = (ScrollView) findViewById(R.id.scrollGameSettings);
		mSpinDifficulty = (Spinner) findViewById(R.id.spinner_difficulty);
		mNpWidth = (NumberPicker) findViewById(R.id.npWidth);
		mNpHeight = (NumberPicker) findViewById(R.id.npHeight);
		mNpBombs = (NumberPicker) findViewById(R.id.npBombs);
		mCbShowReplayOnLost = (CheckBox) findViewById(R.id.cbShowReplayOnLost);

		mSpinDifficulty.setSelection(mPassedConfig.LEVEL.ordinal());

		// TODO: Get values from Constants!
		mNpWidth.setMinValue(1);
		mNpWidth.setMaxValue(20);
		mNpHeight.setMinValue(1);
		mNpHeight.setMaxValue(20);
		mNpBombs.setMinValue(1);
		mNpBombs.setMaxValue((int) (mNpWidth.getValue() * mNpHeight.getValue() * 0.9));

		mCbShowReplayOnLost.setChecked(ApplicationConfig.INSTANCE.isReplayOnLost());
		mCbShowReplayOnLost.setOnCheckedChangeListener(onShowOnLostCheckedChanged);

		mNpWidth.setOnTouchListener(onNumberPickerTouchListener);
		mNpHeight.setOnTouchListener(onNumberPickerTouchListener);
		mNpBombs.setOnTouchListener(onNumberPickerTouchListener);

		mSpinDifficulty.setOnItemSelectedListener(onSpinnerItemSelected);
		mNpWidth.setOnValueChangedListener(onValueChanged);
		mNpHeight.setOnValueChangedListener(onValueChanged);
	}

	@Override
	public void onBackPressed() {
		Level level = Level.fromInt((int) mSpinDifficulty.getSelectedItemId());

		if (Level.CUSTOM == level) {
			mCurrentConfig = new GameConfig(mNpWidth.getValue(), mNpHeight.getValue(), mNpBombs.getValue());
		} else {
			mCurrentConfig = new GameConfig(level);
		}

		if (mCurrentConfig.equals(mPassedConfig)) {
			setResult(RESULT_CANCELED);
		} else {
			// Bundle b = new Bundle();
			// b.putParcelable("GameConfig", mCurrentConfig);
			// getIntent().replaceExtras(b);

			getIntent().removeExtra("GameConfig");
			getIntent().putExtra("GameConfig", mCurrentConfig);

			LogDog.i(CLASSNAME, "GameConfig changed: " + mCurrentConfig.toString());

			setResult(INTENTRESULT_CHANGE_GAMECONFIG, getIntent());
		}

		finish();
	}

	/* This is directly called after onCreate for the selection made for the
	 * mPassedConfig */
	private AdapterView.OnItemSelectedListener onSpinnerItemSelected = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			// Get the selected level
			Level level = Level.fromInt((int) id);
			if (Level.CUSTOM == level) {
				/* Custom level */
				mNpWidth.setEnabled(true);
				mNpHeight.setEnabled(true);
				mNpBombs.setEnabled(true);

				mCurrentConfig = new GameConfig(mCurrentConfig.X, mCurrentConfig.Y, mCurrentConfig.BOMBS);
			} else {
				/* Standard level */
				mNpWidth.setEnabled(false);
				mNpHeight.setEnabled(false);
				mNpBombs.setEnabled(false);

				mCurrentConfig = new GameConfig(level);
			}

			mNpWidth.setValue(mCurrentConfig.X);
			mNpHeight.setValue(mCurrentConfig.Y);

			mNpBombs.setMaxValue((int) (mCurrentConfig.X * mCurrentConfig.Y * 0.9));
			mNpBombs.setValue(mCurrentConfig.BOMBS);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	};

	private OnValueChangeListener onValueChanged = new OnValueChangeListener() {
		@Override
		public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
			mNpBombs.setMaxValue((int) (mNpWidth.getValue() * mNpHeight.getValue() * 0.9));
		}
	};

	private View.OnTouchListener onNumberPickerTouchListener = new View.OnTouchListener() {
		// TODO: Check this warning!
		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_MOVE) {
				/* This prevents scrolling of the underling scroll view,
				 * when a move is detected on the NumberPicker! */
				mScrollGameSettings.requestDisallowInterceptTouchEvent(true);
			}
			return false;
		}
	};

	protected OnCheckedChangeListener onShowOnLostCheckedChanged = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			ApplicationConfig.INSTANCE.setShowReplayOnLost(isChecked);
		}
	};
}
