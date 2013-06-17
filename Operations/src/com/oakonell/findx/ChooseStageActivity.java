package com.oakonell.findx;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.oakonell.findx.custom.CustomStageActivity;
import com.oakonell.findx.data.DataBaseHelper;
import com.oakonell.findx.model.Levels;
import com.oakonell.findx.model.Puzzle;
import com.oakonell.findx.model.Stage;
import com.oakonell.utils.activity.AppLaunchUtils;

public class ChooseStageActivity extends Activity {
    private ArrayAdapter<Stage> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkForPendingPuzzle()) {
            return;
        }

        setContentView(R.layout.choose_stage);

        GridView stageSelect = (GridView) findViewById(R.id.stage_select);

        adapter = new ArrayAdapter<Stage>(getApplication(),
                R.layout.stage_select_grid_item, Levels.getStages()) {

            @Override
            public View getView(int position, View inputRow, ViewGroup parent) {
                View row = inputRow;
                if (row == null) {
                    row = getLayoutInflater().inflate(R.layout.stage_select_grid_item,
                            parent, false);
                }

                final Stage stage = getItem(position);
                TextView id = (TextView) row.findViewById(R.id.level_id);
                id.setText(stage.getId());

                Button stageButton = (Button) row.findViewById(R.id.level_name);
                stageButton.setText(stage.getTitleId());

                stageButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startStage(stage.getId());
                    }
                });

                ImageView lock = (ImageView) row.findViewById(R.id.lock);

                if (stage.isUnlocked()) {
                    stageButton.setEnabled(true);
                    lock.setVisibility(View.INVISIBLE);
                } else {
                    stageButton.setEnabled(false);
                    lock.setVisibility(View.VISIBLE);
                }
                return row;
            }

        };

        stageSelect.setAdapter(adapter);

        Button buildLevel = (Button) findViewById(R.id.custom);
        buildLevel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent levelIntent = new Intent(ChooseStageActivity.this, CustomStageActivity.class);
                startActivity(levelIntent);
            }
        });
        AppLaunchUtils.appLaunched(this);
        BackgroundMusicHelper.onActivtyCreate(this, R.raw.prelude_no_8_in_e_flat_minor_loop);
    }

    private void startStage(final String stageId) {
        Intent levelIntent = new Intent(ChooseStageActivity.this, StageActivity.class);
        levelIntent.putExtra(StageActivity.STAGE_ID, stageId);
        startActivity(levelIntent);
    }

    private boolean checkForPendingPuzzle() {
        DataBaseHelper helper = new DataBaseHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        String id = Puzzle.readPendingLevel(db);
        db.close();

        if (id != null) {
            startPuzzle(id);
            finish();
            return true;
        }
        return false;
    }

    private void startPuzzle(final String levelId) {
        Intent levelIntent = new Intent(ChooseStageActivity.this, PuzzleActivity.class);
        levelIntent.putExtra(PuzzleActivity.PUZZLE_ID, levelId);
        startActivity(levelIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return MenuHelper.onCreateOptionsMenu(this, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return MenuHelper.onOptionsItemSelected(this, item);
    }

    @Override
    protected void onPause() {
        BackgroundMusicHelper.onActivityPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        BackgroundMusicHelper.onActivityResume(this, R.raw.prelude_no_8_in_e_flat_minor_loop);
        adapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        BackgroundMusicHelper.onActivityDestroy();
        super.onDestroy();
    }

}
