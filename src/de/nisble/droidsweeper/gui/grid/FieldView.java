package de.nisble.droidsweeper.gui.grid;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import de.nisble.droidsweeper.game.Field;
import de.nisble.droidsweeper.game.Position;
import de.nisble.droidsweeper.game.jni.FieldListener;
import de.nisble.droidsweeper.game.jni.FieldStatus;
import de.nisble.droidsweeper.game.jni.MineSweeperMatrix;
import de.nisble.droidsweeper.utilities.LogDog;

/** A FieldView is an ImageView responsible for showing the {@link FieldStatus
 * status} of a single field on the {@link GameGridView game grid}. It implements
 * the {@link FieldListener} interface and can therefore be registered in
 * {@link MineSweeperMatrix#setFieldListener(FieldListener)}. The
 * {@link FieldListener#onStatusChanged(FieldStatus, int)} is than directly
 * called from native code, causing this widget to change its appearance.
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public class FieldView extends ImageView implements FieldListener {
	private static final String CLASSNAME = FieldView.class.getSimpleName();

	private Field mField = null;

	private void init() {
		mField = new Field(new Position(), FieldStatus.HIDDEN, 0);
		setImageDrawable(FieldDrawables.getDrawable(mField.STATUS, 0));
	}

	/** Constructor. */
	public FieldView(Context context) {
		this(context, null);
	}

	/** Constructor. */
	public FieldView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/** Constructor. */
	public FieldView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	@Override
	public Position getPosition() {
		return mField.POSITION;
	}

	/** @return The current {@link FieldStatus}. */
	public FieldStatus getFieldStatus() {
		return mField.STATUS;
	}

	/** Reset the internal {@link FieldStatus} to {@link FieldStatus#HIDDEN
	 * HIDDEN}, load the corresponding {@link FieldDrawables image} and the
	 * {@link Position} to the given one.
	 * @param p The new {@link Position}. */
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
