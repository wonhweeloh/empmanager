package com.wonhwee.empmgr;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wonhwee.empmgr.dao.DatabaseHelper;
import com.wonhwee.empmgr.dao.EmpMgrContract;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertTrue;

public class DatabaseHelperTest {
    Context context;
    DatabaseHelper helper;
    SQLiteDatabase db;
    @Before
    public void Setup(){
        context = getTargetContext();
        helper = new DatabaseHelper(context);
        db = helper.getWritableDatabase();
    }
    @After
    public void TearDown(){
        db.close();
    }
    @Test
    public void TestOnCreate() {
        assertTrue("Database could not open", db.isOpen());

    }
    @Test
    public void TestStartData() {
        db.execSQL("DROP TABLE IF EXISTS " + EmpMgrContract.EmployeeEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EmpMgrContract.PositionEntry.TABLE_NAME);
        helper.onCreate(db);
        Cursor c = db.rawQuery("select * from " +
                EmpMgrContract.PositionEntry.TABLE_NAME, null);
        assertTrue(c.getCount() == 4);
        assertTrue(c.getColumnCount() == 3);
    }
}
