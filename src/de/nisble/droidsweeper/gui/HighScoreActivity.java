package de.nisble.droidsweeper.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.WrapperListAdapter;
import de.nisble.droidsweeper.R;
import static de.nisble.droidsweeper.config.Constants.*;
import de.nisble.droidsweeper.config.Level;
import de.nisble.droidsweeper.game.database.DSDBAdapter;
import de.nisble.droidsweeper.game.database.DSDBGameEntry;
import de.nisble.droidsweeper.utilities.LogDog;

/** MultiListActivity for showing the highscores.
 * @author Moritz Nisblé moritz.nisble@gmx.de */
public class HighScoreActivity extends Activity {
	private static final String CLASSNAME = HighScoreActivity.class.getSimpleName();

	private static final String[] POSTFIX = { "_easy", "_normal", "_hard" };
	private static final Level[] LEVELS = { Level.EASY, Level.NORMAL, Level.HARD };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_timelist);

		try {
			for (int i = 0; i < 3; ++i) {
				ListView l = (ListView) findViewById(getResources().getIdentifier("lvTimeList" + POSTFIX[i], "id",
						getPackageName()));
				l.setEmptyView((TextView) findViewById(getResources().getIdentifier("tvTimeList_empty" + POSTFIX[i],
						"id", getPackageName())));

				/* Add header view */
				l.addHeaderView(getLayoutInflater().inflate(R.layout.layout_timelist_header, l, false), null, false);
				l.setHeaderDividersEnabled(true);

				/* Fetch list entries from database and put it into
				 * HighScoreListAdapter. */
				l.setAdapter(new HighScoreListAdapter(this, DSDBAdapter.INSTANCE.getGames(LEVELS[i])));

				l.setOnItemClickListener(onClickListener);
			}
		} catch (Exception e) {
			LogDog.e(CLASSNAME, "Unable to create HighScoreActivity", e);
		}
	}

	private AdapterView.OnItemClickListener onClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

			/* Get the HighScoreListAdapter back from parent view.
			 * The Adapter is wrapped, so we need to cast it back.
			 * Use the id to get the corresponding data base entry from the
			 * adapter. */
			DSDBGameEntry entry = (DSDBGameEntry) ((HighScoreListAdapter) ((WrapperListAdapter) ((ListView) parent)
					.getAdapter()).getWrappedAdapter()).getItem((int) id);

			Intent intent = getIntent();
			intent.putExtra("GAMEID", entry.GAMEID);

			setResult(INTENTRESULT_PLAY_REPLAY, intent);

			finish();
		}
	};
}
