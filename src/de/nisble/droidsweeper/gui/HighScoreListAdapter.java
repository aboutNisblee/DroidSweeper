package de.nisble.droidsweeper.gui;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.nisble.droidsweeper.R;
import de.nisble.droidsweeper.game.database.DSDBGameEntry;

/** High score list adapter.
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public class HighScoreListAdapter extends BaseAdapter {
	// private static final String CLASSNAME =
	// HighScoreListAdapter.class.getSimpleName();

	private ArrayList<DSDBGameEntry> mEntries;

	private final LayoutInflater mLayoutInflater;
	private final java.text.DateFormat mDateFormat;
	private final java.text.DateFormat mTimeFormat;

	/** Create a new list adapter for the given highscores.
	 * @param c The application context.
	 * @param highscores The highscores as ArrayList of {@link DSDBGameEntry
	 *            database entries}. */
	public HighScoreListAdapter(Context c, ArrayList<DSDBGameEntry> highscores) {
		mEntries = highscores;

		mLayoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDateFormat = android.text.format.DateFormat.getDateFormat(c);
		mTimeFormat = android.text.format.DateFormat.getTimeFormat(c);
	}

	@Override
	public int getCount() {
		return mEntries.size();
	}

	@Override
	public Object getItem(int position) {
		return mEntries.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout view;

		if (convertView != null) {
			view = (LinearLayout) convertView;
		} else {
			view = (LinearLayout) mLayoutInflater.inflate(R.layout.layout_timelist_item, parent, false);
		}

		// TODO: Remove redundant Position item!
		((TextView) view.findViewById(R.id.tvTimeListItem_Position)).setText(String.valueOf(position + 1));
		((TextView) view.findViewById(R.id.tvTimeListItem_Name)).setText(mEntries.get(position).NAME);
		((TextView) view.findViewById(R.id.tvTimeListItem_Time)).setText(DateFormat.format("mm:ss",
				mEntries.get(position).PLAYTIME).toString());

		// Localize epoch time and display date and time
		Date date = new Date(mEntries.get(position).EPOCHTIME);
		((TextView) view.findViewById(R.id.tvTimeListItem_Date)).setText(mDateFormat.format(date) + " "
				+ mTimeFormat.format(date));

		return view;
	}
}
