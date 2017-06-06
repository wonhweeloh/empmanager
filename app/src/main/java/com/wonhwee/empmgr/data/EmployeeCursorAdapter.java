package com.wonhwee.empmgr.data;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.wonhwee.empmgr.R;
import com.wonhwee.empmgr.dao.EmpMgrContract;


public class EmployeeCursorAdapter extends CursorAdapter {
    public EmployeeCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.employee_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView empTextView = (TextView) view.findViewById(R.id.tvText);
        int textColumn = cursor.getColumnIndex(EmpMgrContract.EmployeeEntry.COLUMN_TEXT);
        String text = cursor.getString(textColumn);
        int codeColumn = cursor.getColumnIndex(EmpMgrContract.PositionEntry.COLUMN_CODE);
        String posCode = cursor.getString(codeColumn);
        empTextView.setText(posCode + " " + text);
    }
}
