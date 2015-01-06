package de.nisble.droidsweeper.gui.grid;

import de.nisble.droidsweeper.R;
import de.nisble.droidsweeper.game.jni.FieldStatus;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public class FieldDrawables {
	// private static final String CLASSNAME =
	// FieldDrawables.class.getSimpleName();

	private static final Drawable[] mImages = new Drawable[13];

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
