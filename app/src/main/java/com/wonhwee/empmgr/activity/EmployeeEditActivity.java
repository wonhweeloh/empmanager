package com.wonhwee.empmgr.activity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Spinner;

import com.wonhwee.empmgr.data.PositionListAdapter;
import com.wonhwee.empmgr.R;
import com.wonhwee.empmgr.dao.EmpMgrContract;
import com.wonhwee.empmgr.dao.EmpMgrAsyncQueryHandler;
import com.wonhwee.empmgr.databinding.ActivityEmployeeEditBinding;
import com.wonhwee.empmgr.model.Position;
import com.wonhwee.empmgr.model.PositionList;
import com.wonhwee.empmgr.model.Employee;

public class EmployeeEditActivity extends AppCompatActivity {
    Employee employee;
    EmpMgrAsyncQueryHandler empMgrAsyncQueryHandler;
    Spinner spinPositions;
    PositionList positionList;
    PositionListAdapter positionListAdapter;
    private boolean mIsCancelling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int position = 0;

        empMgrAsyncQueryHandler =  new EmpMgrAsyncQueryHandler(getContentResolver());
        ActivityEmployeeEditBinding activityEmployeeEditBinding = DataBindingUtil.setContentView(this, R.layout.activity_employee_edit);

        Intent intent = getIntent();
        employee = (Employee)intent.getSerializableExtra("employee");
        positionList = (PositionList) intent.getSerializableExtra("positions");

        //*** Populate position data on UI ***//
        positionListAdapter = new PositionListAdapter(positionList.ItemList);
        spinPositions =(Spinner) findViewById(R.id.spPositions);
        spinPositions.setAdapter(positionListAdapter);

        //*** Populate employee data on UI ***//
        activityEmployeeEditBinding.setEmployee(employee);

        if (Integer.valueOf(employee.positionid.get()) == 0) {
            position = 1;
            spinPositions.setSelection(position);
        }
        else {
            for (Position cat : positionList.ItemList) {
                if (Integer.valueOf(cat.posId.get()) == Integer.valueOf(employee.positionid.get())) {
                    break;
                }
                position++;
            }
            spinPositions.setSelection(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_todo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_todo) {
            new AlertDialog.Builder(EmployeeEditActivity.this)
                    .setTitle(getString(R.string.delete_todo_dialog_title))
                    .setMessage(getString(R.string.delete_todo_dialog))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            //delete
                            Uri uri =  Uri.withAppendedPath(EmpMgrContract.EmployeeEntry.CONTENT_URI, String.valueOf(employee.Id.get()));

                            String selection = EmpMgrContract.EmployeeEntry._ID + "=?";
                            String[] arguments = new String[1];
                            arguments[0] = String.valueOf(employee.Id.get());

                            empMgrAsyncQueryHandler.startDelete(1, null, uri, selection, arguments);
                            Intent intent = new Intent(EmployeeEditActivity.this, EmployeeListActivity.class);
                            startActivity(intent);
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        }
        else if(id == R.id.action_send_email){
            sendEmail();
        }else if (id == R.id.action_cancel) {
            mIsCancelling = true;
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        String [] args = new String[1];
        EmpMgrAsyncQueryHandler empMgrAsyncQueryHandler =  new EmpMgrAsyncQueryHandler(getContentResolver());

        Position position = (Position) spinPositions.getSelectedItem();
        int posId = position.posId.get();

        //*** Validation (start) ***//
        if(employee == null){
            return;
        }

        if(employee != null && (employee.text.get().trim().length() <= 0 || employee.email.get().trim().length() <= 0 )){
            return;
        }

        if(mIsCancelling){
            return;
        }
        //*** Validation (end) ***//

        ContentValues values = new ContentValues();
        values.put(EmpMgrContract.EmployeeEntry.COLUMN_TEXT, employee.text.get());
        values.put(EmpMgrContract.EmployeeEntry.COLUMN_POSITIONID, posId);
        values.put(EmpMgrContract.EmployeeEntry.COLUMN_DONE, employee.done.get());
        values.put(EmpMgrContract.EmployeeEntry.COLUMN_EMAIL, employee.email.get());
        values.put(EmpMgrContract.EmployeeEntry.COLUMN_END_DATE, employee.end_date.get());
        if(employee != null && employee.Id.get() != 0) {
            args[0] = String.valueOf(employee.Id.get());
            empMgrAsyncQueryHandler.startUpdate(1,null, EmpMgrContract.EmployeeEntry.CONTENT_URI, values, EmpMgrContract.EmployeeEntry._ID + "=?", args);
        }
        else if(employee != null && employee.Id.get() == 0) {
            empMgrAsyncQueryHandler.startInsert(1,null, EmpMgrContract.EmployeeEntry.CONTENT_URI, values);
        }
    }

    private void sendEmail() {
        //*** Validation (start) ***//
        if(employee == null){
            return;
        }

        if(employee != null && (employee.text.get().trim().length() <= 0 || employee.email.get().trim().length() <= 0 )){
            return;
        }
        //*** Validation (end) ***//

        Position position = (Position) spinPositions.getSelectedItem();
        String subject = employee.text.get();
        String text = "Important notification for \"" + position.description.get() + "\" " + employee.text.get();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");

        String[] recepients = new String[1];
        recepients[0] = employee.email.get();

        intent.putExtra(Intent.EXTRA_EMAIL, recepients);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

}
