package de.nisble.droidsweeper.gui.grid;

import de.nisble.droidsweeper.R;
import de.nisble.droidsweeper.game.jni.FieldStatus;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

/** Class that holds images for the {@link FieldView}.
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public class FieldDrawables {
	// private static final String CLASSNAME =
	// FieldDrawables.class.getSimpleName();

	private static final Drawable[] mImages = new Drawable[13];

	/** Load standard {@link FieldView} theme.
	 * @param context Application context. */
	public static void loadGrayback(Context context) {
		Resources res = context.getResources();

		mImages[0] = res.getDrawable(R.drawable.empty_grayback_round_1);
		mImages[1] = res.getDrawable(R.drawable.n1_grayback_round_1);
		mImages[2] = res.getDrawable(R.drawable.n2_grayback_round_1);
		mImages[3] = res.getDrawable(R.drawable.n3_grayback_round_1);
		mImages[4] = res.getDrawable(R.drawable.n4_grayback_round_1);
		mImages[5] = res.getDrawable(R.drawable.n5_grayback_round_1);
		mImages[6] = res.getDrawable(R.drawable.n6_grayback_round_1);
		mImages[7] = res.getDrawable(R.drawable.n7_grayback_round_1);
		mImages[8] = res.getDrawable(R.drawable.n8_grayback_round_1);

		mImages[9] = res.getDrawable(R.drawable.unpushed_grayback_round_1);
		mImages[10] = res.getDrawable(R.drawable.bang_grayback_round_1);
		mImages[11] = res.getDrawable(R.drawable.query_grayback_round_1);
		mImages[12] = res.getDrawable(R.drawable.pushedbomb_grayback_round_1);
	}

	/** Get an image corresponding to a specific {@link FieldStatus}.
	 * If {@link FieldStatus#UNHIDDEN} is passed, the adjacentBombs argument
	 * specifies the number shown on the revealed field.
	 * @param status The field status.
	 * @param adjacentBombs The count of adjacent bombs.
	 * @return A Drawable. */
	public static Drawable getDrawable(FieldStatus status, int adjacentBombs) {
		switch (status) {
		case HIDDEN:
			return mImages[9];
		case UNHIDDEN:
			return mImages[adjacentBombs];
		case MARKED:
			return mImages[10];
		case QUERIED:
			return mImages[11];
		case BOMB:
			return mImages[12];
		default:
			return mImages[9];
		}
	}
}
