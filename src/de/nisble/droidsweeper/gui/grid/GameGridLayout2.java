package de.nisble.droidsweeper.gui.grid;

import de.nisble.droidsweeper.utilities.LogDog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.widget.TableLayout;

/**
 * 
 * @author moritz
 *
 */
class GameGridLayout2 extends TableLayout {
	private static final String CLASSNAME = GameGridLayout.class.getSimpleName();

	private float mCurrentOffsetX = 0;
	private float mCurrentOffsetY = 0;

	private float mPrimaryTouchX;
	private float mPrimaryTouchY;

	private int mActivePointerID = MotionEvent.INVALID_POINTER_ID;;

	private final ScaleGestureDetector mScaleDetector;
	private float mScaleFactor = 1.0f;

	public int iAnzahl_x = 1;
	public int iAnzahl_y = 1;

	public GameGridLayout2(Context context) {
		super(context);

		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
	}

	public GameGridLayout2(Context context, AttributeSet attrs) {
		super(context, attrs);

		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
	}

	public void setDimensions(int numX, int numY) {
		iAnzahl_x = numX;
		iAnzahl_y = numY;

		offsetLeftAndRight(0);
		offsetTopAndBottom(0);
		setScaleX(1.0f);
		setScaleY(1.0f);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int newMeasureWidth;
		int newMeasureHeight;

		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int fieldWidth = widthSize / iAnzahl_x;
		int fieldHeight = heightSize / iAnzahl_y;
		if (fieldWidth <= fieldHeight) {
			newMeasureWidth = MeasureSpec.makeMeasureSpec(fieldWidth * iAnzahl_x, MeasureSpec.UNSPECIFIED);
			newMeasureHeight = MeasureSpec.makeMeasureSpec(fieldWidth * iAnzahl_y, MeasureSpec.UNSPECIFIED);
		} else {
			newMeasureWidth = MeasureSpec.makeMeasureSpec(fieldHeight * iAnzahl_x, MeasureSpec.UNSPECIFIED);
			newMeasureHeight = MeasureSpec.makeMeasureSpec(fieldHeight * iAnzahl_y, MeasureSpec.UNSPECIFIED);
		}

		super.onMeasure(newMeasureWidth, newMeasureHeight);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Log.e(CLASSNAME, "onTouchEvent called. Action: " +
		// event.getAction());

		boolean handeled = false;

		final int action = event.getAction(); // Current action

		mScaleDetector.onTouchEvent(event); // Process gesture

		switch (action & MotionEvent.ACTION_MASK) {

		case MotionEvent.ACTION_DOWN: // Never gets called!
			LogDog.e(CLASSNAME, "onTouchEvent: Action: ACTION_DOWN");
			// if (!mScaleDetector.isInProgress())
			// handeled = super.onTouchEvent(event);
			// else
			// handeled = false;
			break;

		case MotionEvent.ACTION_MOVE: {
			// Log.e(CLASSNAME, "onTouchEvent: Action: ACTION_MOVE");

			final int pointerIndex = event.findPointerIndex(mActivePointerID);
			final float x = event.getX(pointerIndex);
			final float y = event.getY(pointerIndex);

			// if (x > (mPrimaryTouchX * 1.02) || x < (mPrimaryTouchX * 0.98) ||
			// y > (mPrimaryTouchX * 1.02) || y < (mPrimaryTouchX * 0.98)) {
			// Only move if the ScaleGestureDetector isn't processing a gesture.
			if (!mScaleDetector.isInProgress() && event.getPointerCount() == 1) {

				final float dx = x - mPrimaryTouchX;
				final float dy = y - mPrimaryTouchY;

				mCurrentOffsetX += dx;
				mCurrentOffsetY += dy;

				offsetLeftAndRight((int) mCurrentOffsetX);
				offsetTopAndBottom((int) mCurrentOffsetY);

				handeled = true;
			}
			// }
			mPrimaryTouchX = x;
			mPrimaryTouchY = y;

			break;
		}

		case MotionEvent.ACTION_UP:
			// Log.e(CLASSNAME, "onTouchEvent: Action: ACTION_UP");
			mActivePointerID = MotionEvent.INVALID_POINTER_ID;
			handeled = super.onTouchEvent(event);
			break;

		case MotionEvent.ACTION_CANCEL:
			// Log.e(CLASSNAME, "onTouchEvent: Action: ACTION_CANCEL");
			mActivePointerID = MotionEvent.INVALID_POINTER_ID;
			handeled = super.onTouchEvent(event);
			break;

		case MotionEvent.ACTION_POINTER_UP: {
			// Log.e(CLASSNAME, "onTouchEvent: Action: ACTION_POINTER_UP");
			/* Correct the situation in that the primary pointer left the screen
			 * while the second stay. The second pointer become the first. */

			// Extract the index of the pointer that left the touch sensor
			final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			final int pointerId = event.getPointerId(pointerIndex);
			if (pointerId == mActivePointerID) {
				// This was our active pointer going up. Choose a new
				// active pointer and adjust accordingly.
				final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
				mPrimaryTouchX = event.getX(newPointerIndex);
				mPrimaryTouchY = event.getY(newPointerIndex);
				mActivePointerID = event.getPointerId(newPointerIndex);
			}

			handeled = true;
			break;
		}

		case MotionEvent.ACTION_POINTER_DOWN:
			LogDog.e(CLASSNAME, "onTouchEvent: Action: ACTION_POINTER_DOWN");
			return true;

		default:
			LogDog.e(CLASSNAME, "onTouchEvent: Action: unknown/default");
			handeled = super.onTouchEvent(event);
			break;
		}

		return handeled;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// Log.e(CLASSNAME, "onInterceptTouchEvent called. Action: " +
		// ev.getAction());

		boolean handeled = false;

		final float x = ev.getX();
		final float y = ev.getY();

		switch (ev.getAction()) {

		case MotionEvent.ACTION_DOWN:
			// Log.e(CLASSNAME, "onInterceptTouchEvent: Action: ACTION_DOWN");
			mPrimaryTouchX = x;
			mPrimaryTouchY = y;

			mActivePointerID = ev.getPointerId(0); // Save primary pointer ID
			handeled = false;
			break;

		case MotionEvent.ACTION_MOVE:
			// Log.e(CLASSNAME, "onInterceptTouchEvent: Action: ACTION_MOVE");
			if (!mScaleDetector.isInProgress() && x > (mPrimaryTouchX * 1.02) || x < (mPrimaryTouchX * 0.98)
					|| y > (mPrimaryTouchX * 1.02) || y < (mPrimaryTouchX * 0.98))
				handeled = true;
			else
				handeled = false;
			break;

		case MotionEvent.ACTION_UP:
			// Log.e(CLASSNAME, "onInterceptTouchEvent: Action: ACTION_UP");
			mActivePointerID = MotionEvent.INVALID_POINTER_ID;
			handeled = false;
			break;

		case MotionEvent.ACTION_CANCEL:
			// Log.e(CLASSNAME, "onInterceptTouchEvent: Action: ACTION_CANCEL");
			mActivePointerID = MotionEvent.INVALID_POINTER_ID;
			handeled = false;
			break;

		case MotionEvent.ACTION_POINTER_DOWN:
			// Log.e(CLASSNAME,
			// "onInterceptTouchEvent: Action: ACTION_POINTER_DOWN");
			handeled = true;
			break;

		case MotionEvent.ACTION_POINTER_UP:
			// Log.e(CLASSNAME,
			// "onInterceptTouchEvent: Action: ACTION_POINTER_UP");
			handeled = true;
			break;

		default:
			handeled = true;
			break;
		}

		// mScaleDetector.onTouchEvent(ev); // Process gesture
		// if (mScaleDetector.isInProgress())
		// handeled = true;

		return handeled;
	}

	private class ScaleListener extends SimpleOnScaleGestureListener {

		// @Override
		// public boolean onScaleBegin(ScaleGestureDetector detector) {
		// Log.e(CLASSNAME, "onScaleBegin");
		// return super.onScaleBegin(detector);
		// }
		//
		// @Override
		// public void onScaleEnd(ScaleGestureDetector detector) {
		// Log.e(CLASSNAME, "onScaleEnd");
		// super.onScaleEnd(detector);
		// }

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			// Log.e(CLASSNAME, "onScale called");

			mScaleFactor *= detector.getScaleFactor();

			// Don't let the object get too small or too large.
			mScaleFactor = Math.max(0.75f, Math.min(mScaleFactor, 3.5f));

			setPivotX(mPrimaryTouchX);
			setPivotY(mPrimaryTouchY);
			setScaleX(mScaleFactor);
			setScaleY(mScaleFactor);

			return true;
		}
	}
}
