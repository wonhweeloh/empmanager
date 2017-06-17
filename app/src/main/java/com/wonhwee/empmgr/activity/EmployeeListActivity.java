package com.wonhwee.empmgr.activity;

import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import com.wonhwee.empmgr.data.PositionListAdapter;
import com.wonhwee.empmgr.R;
import com.wonhwee.empmgr.data.EmployeeCursorAdapter;
import com.wonhwee.empmgr.dao.EmpMgrContract;
import com.wonhwee.empmgr.dao.EmpMgrContract.EmployeeEntry;
import com.wonhwee.empmgr.dao.EmpMgrAsyncQueryHandler;
import com.wonhwee.empmgr.model.Position;
import com.wonhwee.empmgr.model.PositionList;
import com.wonhwee.empmgr.model.Employee;

public class EmployeeListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    static final int ALL_ROWS = -1;
    static final int ALL_POSITIONS = -1;

    private static final int URL_LOADER = 0;
    Cursor cursor;
    EmployeeCursorAdapter empCursorAdapter;
    Spinner spinPositions;
    PositionList positionList = new PositionList();
    PositionListAdapter positionListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);

        //*** Setting up Position Spinner ***//
        spinPositions = (Spinner) findViewById(R.id.spinPositions);

        getLoaderManager().initLoader(URL_LOADER, null, this);

        setupPositionsSpinner();

        spinPositions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position >= 0) {
                    getLoaderManager().restartLoader(URL_LOADER, null, EmployeeListActivity.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //*** Setting up Employee List ***//
        final ListView lvEmployee = (ListView) findViewById(R.id.lvEmployees);
        empCursorAdapter = new EmployeeCursorAdapter(this, cursor, false);
        lvEmployee.setAdapter(empCursorAdapter);

        lvEmployee.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {

                cursor = (Cursor) adapterView.getItemAtPosition(pos);

                int empId = cursor.getInt(cursor.getColumnIndex(EmpMgrContract.EmployeeEntry._ID));
                String empText = cursor.getString(cursor.getColumnIndex(EmployeeEntry.COLUMN_TEXT));
                String empExpireDate = cursor.getString(cursor.getColumnIndex(EmployeeEntry.COLUMN_END_DATE));
                int empDone = cursor.getInt(cursor.getColumnIndex(EmpMgrContract.EmployeeEntry.COLUMN_DONE));
                String empCreated = cursor.getString(cursor.getColumnIndex(EmployeeEntry.COLUMN_START_DATE));
                String empCategory = cursor.getString(cursor.getColumnIndex(EmployeeEntry.COLUMN_POSITIONID));
                String email = cursor.getString(cursor.getColumnIndex(EmployeeEntry.COLUMN_EMAIL));

                //create the object that will be passed to the todoActivity
                boolean boolDone = (empDone == 1);
                Employee employee = new Employee(empId, empText, empCreated, empExpireDate, boolDone, empCategory, email);
                Intent intent = new Intent(EmployeeListActivity.this, EmployeeEditActivity.class);
                //pass the ID to the todoActivity
                intent.putExtra("employee", employee);
                intent.putExtra("positions", positionList);
                startActivity(intent);
            }
        });

        //*** Setting up Toolbar ***//
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //*** Setting up Floating Action Button ***//
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newEmployee();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
/*            case R.id.action_edit_pos:
                Intent intent = new Intent(EmployeeListActivity.this, PositionEditActivity.class);
                startActivity(intent);
                break;*/
/*            case R.id.action_delete_all_employees:
                deleteEmployee(ALL_ROWS);
                break;*/
            case R.id.action_add_employee:
                newEmployee();
                break;
            default:
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {EmployeeEntry.COLUMN_TEXT,
                EmployeeEntry.TABLE_NAME + "." + EmployeeEntry._ID,
                EmpMgrContract.EmployeeEntry.COLUMN_START_DATE,
                EmployeeEntry.COLUMN_END_DATE,
                EmployeeEntry.COLUMN_DONE,
                EmployeeEntry.COLUMN_EMAIL,
                EmpMgrContract.EmployeeEntry.COLUMN_POSITIONID,
                EmpMgrContract.PositionEntry.TABLE_NAME + "." + EmpMgrContract.PositionEntry.COLUMN_DESCRIPTION,
                EmpMgrContract.PositionEntry.TABLE_NAME + "." + EmpMgrContract.PositionEntry.COLUMN_CODE};

        String selection;

        String[] arguments = new String[1];

        if (spinPositions.getSelectedItemId() < 0) {
            selection = null;
            arguments = null;
        }
        else {
            selection = EmployeeEntry.COLUMN_POSITIONID + "=?";
            arguments[0] = String.valueOf(spinPositions.getSelectedItemId());
        }

        return new CursorLoader(this, EmployeeEntry.CONTENT_URI, projection, selection, arguments, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        empCursorAdapter.swapCursor(data);

        if (positionListAdapter == null){
            positionListAdapter = new PositionListAdapter(positionList.ItemList);
            spinPositions.setAdapter(positionListAdapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        empCursorAdapter.swapCursor(null);
    }

    private void setupPositionsSpinner() {
        final EmpMgrAsyncQueryHandler asyncQryHandler = new EmpMgrAsyncQueryHandler(this.getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                try {
                    if ((cursor != null)) {
                        int i = 0;
                        positionList.ItemList.clear();
                        positionList.ItemList.add(i, new Position(ALL_POSITIONS, "", "All Position"));
                        i++;
                        while (cursor.moveToNext()) {
                            positionList.ItemList.add(i, new Position(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
                            i++;
                        }
                    }
                } finally {
                    // Do nothing.
                }
            }
        };

        asyncQryHandler.startQuery(1, null, EmpMgrContract.PositionEntry.CONTENT_URI, null, null, null, EmpMgrContract.PositionEntry.COLUMN_DESCRIPTION);
    }

    private void newEmployee() {
        Employee employee = new Employee(0,"", "", "", false, "0", "");
        Intent intent = new Intent(EmployeeListActivity.this, EmployeeEditActivity.class);
        intent.putExtra("employee", employee);
        intent.putExtra("positions", positionList);
        startActivity(intent);
    }

    private void deleteEmployee(int id) {
        String[] args = {String.valueOf(id)};

        if (id == ALL_ROWS) {
            args = null;
        }

        EmpMgrAsyncQueryHandler asyncQryHandler = new EmpMgrAsyncQueryHandler(this.getContentResolver());
        asyncQryHandler.startDelete(1, null, EmployeeEntry.CONTENT_URI, EmployeeEntry._ID + " =?", args);
    }
}
