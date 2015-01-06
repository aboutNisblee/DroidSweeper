package de.nisble.droidsweeper.gui.grid;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TableLayout;

public class GameGridLayout extends TableLayout {

	public int iAnzahl_x = 1;
	public int iAnzahl_y = 1;

	public GameGridLayout(Context context) {
		super(context);
	}

	public GameGridLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setDimensions(int numX, int numY) {
		iAnzahl_x = numX;
		iAnzahl_y = numY;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int fieldWidth = widthSize / iAnzahl_x;
		int fieldHeight = heightSize / iAnzahl_y;

		int newMeasureWidth;
		int newMeasureHeight;

		if (fieldWidth <= fieldHeight) {
			newMeasureWidth = MeasureSpec.makeMeasureSpec(fieldWidth * iAnzahl_x, MeasureSpec.UNSPECIFIED);
			newMeasureHeight = MeasureSpec.makeMeasureSpec(fieldWidth * iAnzahl_y, MeasureSpec.UNSPECIFIED);
		} else {
			newMeasureWidth = MeasureSpec.makeMeasureSpec(fieldHeight * iAnzahl_x, MeasureSpec.UNSPECIFIED);
			newMeasureHeight = MeasureSpec.makeMeasureSpec(fieldHeight * iAnzahl_y, MeasureSpec.UNSPECIFIED);
		}

		super.onMeasure(newMeasureWidth, newMeasureHeight);
	}
}
