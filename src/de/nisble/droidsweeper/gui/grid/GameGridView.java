package de.nisble.droidsweeper.gui.grid;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
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
			/* Ensure that table layout has the minimum size (wraps its
			 * children). */

			// Get count of children
			int rowCount = getChildCount();
			int columnCount = (rowCount > 0) ? ((TableRow) getChildAt(0)).getChildCount() : 0;

			/* Calculate the maximum allowed side length of the fields by
			 * dividing the maximum allowed size passed by parent for both
			 * directions by the count of widgets in the corresponding
			 * direction.
			 * Finally take the minimum of both sizes to ensure that the
			 * complete matrix fits to into the bounds passed by parent while
			 * single views stays quadratic. */
			int maxSideLength = Math.min(MeasureSpec.getSize(heightMeasureSpec) / ((rowCount > 0) ? rowCount : 1),
					MeasureSpec.getSize(widthMeasureSpec) / ((columnCount > 0) ? columnCount : 1));

			/* Take this maximum allowed side length for each field, multiply it
			 * with the count of fields in each direction and pass it back to
			 * parent. */
			super.onMeasure(MeasureSpec.makeMeasureSpec(maxSideLength * ((columnCount > 0) ? columnCount : 1),
					MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(maxSideLength
					* ((rowCount > 0) ? rowCount : 1), MeasureSpec.UNSPECIFIED));
		}
	}

	private final GridLayout mGrid;

	private TextView mOverlayText = null;

	private FieldClickListener mFieldClickListener = null;

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

	public void update(GameConfig config, FieldWidgetConnector connector) {
		// Get an orientation corrected version of the config
		GameConfig cv = config.adjustOrientation(getContext());

		// Adjust row count while recycling rows already present
		if (mGrid.getChildCount() < cv.Y) {
			LogDog.d(CLASSNAME, "Adding FieldView rows: " + mGrid.getChildCount() + " -> " + cv.Y);

			// Add left rows
			while (mGrid.getChildCount() < cv.Y) {
				TableRow row = new TableRow(getContext());
				row.setLayoutParams(new GameGridView.LayoutParams(LayoutParams.WRAP_CONTENT, 0));
				mGrid.addView(row);
			}
		} else if (mGrid.getChildCount() > cv.Y) {
			LogDog.d(CLASSNAME, "Removing FieldView rows: " + mGrid.getChildCount() + " -> " + cv.Y);

			// Remove redundant rows
			mGrid.removeViews(cv.Y, mGrid.getChildCount() - cv.Y);
		}

		// Adjusting column count (FieldViewS) in each row while recycling
		// already present FieldViewS
		for (int y = 0; y < mGrid.getChildCount(); ++y) {
			TableRow row = (TableRow) mGrid.getChildAt(y);

			if (row.getChildCount() < cv.X) {
				LogDog.d(CLASSNAME, "Adding FieldViewS: " + row.getChildCount() + " -> " + cv.X);

				// Add left columns to each row
				while (row.getChildCount() < cv.X) {
					FieldView field = new FieldView(getContext());
					field.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT));
					field.setAdjustViewBounds(true);
					field.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

					field.setLongClickable(true);

					field.setOnClickListener(mOnFieldClick);
					field.setOnLongClickListener(mOnFieldLongClick);
					row.addView(field);
				}
			} else if (row.getChildCount() > cv.X) {
				LogDog.d(CLASSNAME, "Removing FieldViewS: " + row.getChildCount() + " -> " + cv.X);

				// Remove redundant columns from each row
				row.removeViews(cv.X, row.getChildCount() - cv.X);
			}
		}

		/* Iterate over all FieldViewS in the TableLayout, inform the views
		 * about its Position in the Matrix and let the caller connect the view
		 * to any module that controls the status of the view. */
		for (int y = 0; y < cv.Y; ++y) {
			TableRow row = (TableRow) mGrid.getChildAt(y);

			for (int x = 0; x < cv.X; ++x) {
				FieldView field = (FieldView) row.getChildAt(x);

				/* NOTE: Game/libmsm and Replay only know the PORTRAIT layout.
				 * Only the view adapts to the orientation of the device!
				 * So if the device is in LANDSCAPE mode, the internal Position
				 * of the FieldViewS differs from its position in the
				 * TableLayout. Therefore we have to correct this Position back
				 * to PORTRAIT, if the Grind was built for LANDSCAPE.
				 *
				 * Because the game logic gets the coordinates of each field by
				 * calling FieldListener.getPosition(), this is the only place
				 * where we have to consider this. */
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
					/* Transpose or rotate?
					 * This is the code for rotate right by 90 degree:
					 * field.reset(new Position(y, config.Y - 1 - x)); */
					field.reset(new Position(y, x));
				else
					field.reset(new Position(x, y));

				/* Let the caller connect the field to any module that is able
				 * to control the FieldStatus. */
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
