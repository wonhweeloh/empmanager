package com.wonhwee.empmgr.activity;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.wonhwee.empmgr.data.PositionListAdapter;
import com.wonhwee.empmgr.R;
import com.wonhwee.empmgr.dao.EmpMgrContract;
import com.wonhwee.empmgr.dao.EmpMgrAsyncQueryHandler;
import com.wonhwee.empmgr.databinding.ActivityPositionEditBinding;
import com.wonhwee.empmgr.model.Position;
import com.wonhwee.empmgr.model.PositionList;

public class PositionEditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final int URL_LOADER = 0;
    protected Position position;
    protected PositionList positionList;
    ObservableArrayList<Position> positionListObs;
    Cursor cursor;
    PositionListAdapter posListAdapter;
    ActivityPositionEditBinding activityPositionEditBinding;
    EmpMgrAsyncQueryHandler empMgrAsyncQueryHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityPositionEditBinding = DataBindingUtil.setContentView(this, R.layout.activity_position_edit);
        getLoaderManager().initLoader(URL_LOADER, null, this);

        empMgrAsyncQueryHandler =  new EmpMgrAsyncQueryHandler(getContentResolver());

        ListView lvPositions = (ListView)findViewById(R.id.lvPositions);

        lvPositions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            PositionEditActivity.this.position = positionList.ItemList.get(position);
            activityPositionEditBinding.setPosition(PositionEditActivity.this.position);
          }
        });


        final Button btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                position = new Position();
                activityPositionEditBinding.setPosition(position);
            }
        });

        final Button btnDelete = (Button) findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(PositionEditActivity.this)
                        .setTitle(getString(R.string.delete_categories_dialog_title))
                        .setMessage(getString(R.string.delete_categories_dialog))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                //delete
                                positionList.ItemList.remove(position);
                                String [] args = new String[1];
                                Uri uri =  Uri.withAppendedPath(EmpMgrContract.PositionEntry.CONTENT_URI, String.valueOf(position.posId.get()));
                                empMgrAsyncQueryHandler.startDelete(1, null, uri, null, null);
                                position = null;
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

        final Button btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(position != null && position.posId.get() != null) {
                    //update existing positions
                    Log.d("Save Click", "update");
                    String [] args = new String[1];
                    ContentValues values = new ContentValues();
                    values.put(EmpMgrContract.PositionEntry.COLUMN_DESCRIPTION, position.description.get());
                    values.put(EmpMgrContract.PositionEntry.COLUMN_CODE, position.code.get());
                    args[0] = position.posId.get().toString();
                    empMgrAsyncQueryHandler.startUpdate(1, null, EmpMgrContract.PositionEntry.CONTENT_URI, values, EmpMgrContract.PositionEntry._ID + "=?", args);
                }
                else if(position != null && position.posId.get() == null) {
                    //add new positions
                    ContentValues values = new ContentValues();
                    values.put(EmpMgrContract.PositionEntry.COLUMN_DESCRIPTION, position.description.get());
                    values.put(EmpMgrContract.PositionEntry.COLUMN_CODE, position.code.get());
                    empMgrAsyncQueryHandler.startInsert(1, null, EmpMgrContract.PositionEntry.CONTENT_URI, values);
                }
            }
        });
    }

    @Override
    public void onResume(){
        getLoaderManager().restartLoader(URL_LOADER, null, this);
        super.onResume();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {EmpMgrContract.PositionEntry.TABLE_NAME + "." + EmpMgrContract.PositionEntry._ID,
                EmpMgrContract.PositionEntry.COLUMN_CODE,
                EmpMgrContract.PositionEntry.COLUMN_DESCRIPTION};

        return new CursorLoader(this, EmpMgrContract.PositionEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        final ListView lvPositions = (ListView) findViewById(R.id.lvPositions);
        positionListObs = new ObservableArrayList<>();
        int i=0;

        // Move cursor before first to iterate after config change
        //data.moveToPosition(-1);

        while (cursor.moveToNext()){
            positionListObs.add(i, new Position(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
            i++;
        }

        posListAdapter = new PositionListAdapter(positionListObs);
        lvPositions.setAdapter(posListAdapter);

        position = new Position();
        positionList = new PositionList(positionListObs);
        activityPositionEditBinding.setPositionList(positionList);
        activityPositionEditBinding.setPosition(position);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        posListAdapter.list = null;
    }
}
