package de.nisble.droidsweeper.gui.grid;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import de.nisble.droidsweeper.R;
import de.nisble.droidsweeper.config.GameConfig;
import de.nisble.droidsweeper.game.Position;
import de.nisble.droidsweeper.utilities.LogDog;

public class GameGridView extends RelativeLayout {
	private static final String CLASSNAME = GameGridView.class.getSimpleName();

	public interface FieldWidgetConnector {
		// http://steve-yegge.blogspot.de/2006/03/execution-in-kingdom-of-nouns.html
		void connect(FieldView field);
	}

	public interface FieldClickListener {
		void onClick(FieldView field);

		/** @return true if the callback consumed the long click, false
		 *         otherwise. */
		boolean onLongClick(FieldView field);
	}

	private class GridLayout extends TableLayout {
		public GridLayout(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int widthSize = MeasureSpec.getSize(widthMeasureSpec);
			int heightSize = MeasureSpec.getSize(heightMeasureSpec);

			int fieldWidth = widthSize / mHeight;
			int fieldHeight = heightSize / mWidth;

			int newMeasureWidth;
			int newMeasureHeight;

			if (fieldWidth <= fieldHeight) {
				newMeasureWidth = MeasureSpec.makeMeasureSpec(fieldWidth * mHeight, MeasureSpec.UNSPECIFIED);
				newMeasureHeight = MeasureSpec.makeMeasureSpec(fieldWidth * mWidth, MeasureSpec.UNSPECIFIED);
			} else {
				newMeasureWidth = MeasureSpec.makeMeasureSpec(fieldHeight * mHeight, MeasureSpec.UNSPECIFIED);
				newMeasureHeight = MeasureSpec.makeMeasureSpec(fieldHeight * mWidth, MeasureSpec.UNSPECIFIED);
			}

			super.onMeasure(newMeasureWidth, newMeasureHeight);
		}
	}

	private final GridLayout mGrid;

	private TextView mOverlayText = null;

	private FieldClickListener mFieldClickListener = null;

	private int mHeight = 1;
	private int mWidth = 1;

	public GameGridView(Context context) {
		this(context, null);
	}

	public GameGridView(Context context, AttributeSet attrs) {
		super(context, attrs);

		RelativeLayout.LayoutParams gridLayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		gridLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

		mGrid = new GridLayout(context, attrs);
		mGrid.setLayoutParams(gridLayoutParams);
		mGrid.setShrinkAllColumns(true);

		// getParent().
		addView(mGrid);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.GameGridView, 0, 0);

		try {
			if (a.getBoolean(R.styleable.GameGridView_hasOverlay, false)) {
				mOverlayText = new TextView(context);

				RelativeLayout.LayoutParams overlayLayoutParams = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
				overlayLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

				mOverlayText.setLayoutParams(overlayLayoutParams);

				mOverlayText.setClickable(false);
				mOverlayText.setGravity(Gravity.CENTER);
				mOverlayText.setTextSize(a.getDimension(R.styleable.GameGridView_overlayTextSize, 26));
				mOverlayText.setTextColor(a.getColor(R.styleable.GameGridView_overlayTextColor, Color.RED));
				mOverlayText.setBackgroundColor(a.getColor(R.styleable.GameGridView_overlayBackgroundColor,
						Color.LTGRAY));

				mOverlayText.setVisibility(INVISIBLE);
				addView(mOverlayText);
			}
		} finally {
			a.recycle();
		}

		bringChildToFront(mGrid);
	}

	public void setFieldClickListener(FieldClickListener l) {
		mFieldClickListener = l;
	}

	public void newGrid(GameConfig config, FieldWidgetConnector connector) {
		// Get an orientation corrected version of the config
		GameConfig cv = config.adjustOrientation(getContext());

		/* TODO: Recycle Fields!!
		 * But how?? Maintain matrix with fields? */
		// getChildAt(index)

		mGrid.removeAllViews();

		mHeight = cv.X;
		mWidth = cv.Y;

		for (int y = 0; y < cv.Y; y++) {
			TableRow row = new TableRow(getContext());

			row.setLayoutParams(new GameGridView.LayoutParams(LayoutParams.WRAP_CONTENT, 0));

			// row.findViewById(id)
			// row.findViewWithTag(tag)

			mGrid.addView(row);

			for (int x = 0; x < cv.X; x++) {

				FieldView field = new FieldView(getContext());
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
					field.setPosition(new Position(y, config.Y - 1 - x));
				else
					field.setPosition(new Position(x, y));

				field.setLongClickable(true);

				field.setOnClickListener(mOnFieldClick);
				field.setOnLongClickListener(mOnFieldLongClick);

				// field.setTag(tag);

				// field.bringToFront();

				row.addView(field);

				// Let the caller connect the field
				connector.connect(field);
			}
		}

		invalidate();
		requestLayout();
	}

	public void showOverlay(String text) {
		if (mOverlayText != null) {
			mOverlayText.setText(text);
			mOverlayText.setVisibility(VISIBLE);

			mOverlayText.bringToFront();

			invalidate();
			requestLayout();
		}
	}

	public void hideOverlay() {
		if (mOverlayText != null) {
			mOverlayText.setVisibility(INVISIBLE);

			mGrid.bringToFront();

			invalidate();
			requestLayout();
		}
	}

	private final OnClickListener mOnFieldClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mFieldClickListener != null) {
				mFieldClickListener.onClick((FieldView) v);
			} else {
				LogDog.w(CLASSNAME, "Register a FieldClickListener to get informed about clicks on FieldViewS");
			}
		}
	};

	private final OnLongClickListener mOnFieldLongClick = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			if (mFieldClickListener != null) {
				return mFieldClickListener.onLongClick((FieldView) v);
			} else {
				LogDog.w(CLASSNAME, "Register a FieldClickListener to get informed about clicks on FieldViewS");
				return false;
			}
		}
	};
}
