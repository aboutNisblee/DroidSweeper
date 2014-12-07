package de.nisble.droidsweeper.gui.grid;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import de.nisble.droidsweeper.game.Field;
import de.nisble.droidsweeper.game.Position;
import de.nisble.droidsweeper.game.jni.FieldListener;
import de.nisble.droidsweeper.game.jni.FieldStatus;
import de.nisble.droidsweeper.utilities.LogDog;

public class FieldView extends ImageView implements FieldListener {
	private static final String CLASSNAME = FieldView.class.getSimpleName();

	private Field mField = null;

	private void init() {
		mField = new Field(new Position(), FieldStatus.HIDDEN, 0);
		setImageDrawable(FieldDrawables.getDrawable(mField.STATUS, 0));
	}

	public FieldView(Context context) {
		this(context, null);
	}

	public FieldView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FieldView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	@Override
	public Position getPosition() {
		return mField.POSITION;
	}

	public FieldStatus getFieldStatus() {
		return mField.STATUS;
	}

	public void reset(Position p) {
		mField = new Field(p, FieldStatus.HIDDEN, 0);
		setImageDrawable(FieldDrawables.getDrawable(mField.STATUS, 0));
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
