package de.nisble.droidsweeper.gui.grid;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import de.nisble.droidsweeper.game.jni.FieldListener;
import de.nisble.droidsweeper.game.jni.FieldStatus;
import de.nisble.droidsweeper.utilities.Field;
import de.nisble.droidsweeper.utilities.LogDog;
import de.nisble.droidsweeper.utilities.Position;

public class FieldView extends ImageView implements FieldListener {
	private static final String CLASSNAME = FieldView.class.getSimpleName();

	private Field mField = null;

	private void init() {
		mField = new Field(new Position(), FieldStatus.HIDDEN, 0);
		setImageDrawable(FieldDrawables.getDrawable(mField.STATUS, 0));
	}

	public FieldView(Context context) {
		super(context);
		init();
	}

	public FieldView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		init();
	}

	public FieldView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void setPosition(Position p) {
		mField = new Field(p, mField.STATUS, mField.ADJACENT_BOMBS);
	}

	@Override
	public Position getPosition() {
		return mField.POSITION;
	}

	public FieldStatus getFieldStatus() {
		return mField.STATUS;
	}

	/* (non-Javadoc)
	 * @see
	 * de.nisble.droidsweeper.binding.FieldListener#onStatusChanged(de.nisble
	 * .droidsweeper.binding.FieldStatus, int)
	 * This method is directly called from native code. */
	@Override
	public void onStatusChanged(FieldStatus status, int adjacentBombs) {
		LogDog.d(CLASSNAME, "Changing status of " + mField.POSITION.toString() + " to " + status.toString());
		mField = new Field(mField.POSITION, status, adjacentBombs);
		setImageDrawable(FieldDrawables.getDrawable(status, adjacentBombs));
	}
}
