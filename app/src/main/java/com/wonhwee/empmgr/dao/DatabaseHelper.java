package com.wonhwee.empmgr.dao;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wonhwee.empmgr.client.HttpClient;
import com.wonhwee.empmgr.dao.EmpMgrContract.PositionEntry;
import com.wonhwee.empmgr.dao.EmpMgrContract.EmployeeEntry;

public class DatabaseHelper extends SQLiteOpenHelper{
    public static final String module = DatabaseHelper.class.getSimpleName();

    private final Resources res;
    private final String packageName;
    private static final String DATABASE_NAME = "empmgrapp.sqLiteDatabase";
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase sqLiteDatabase = null;

    private static final String TABLE_CATEGORIES_CREATE=
            "CREATE TABLE " + PositionEntry.TABLE_NAME + " (" +
                    PositionEntry._ID + " INTEGER PRIMARY KEY, " +
                    PositionEntry.COLUMN_CODE + " TEXT, " +
                    PositionEntry.COLUMN_DESCRIPTION + " TEXT " +
                    ")";
    private static final String TABLE_TODOS_CREATE =
            "CREATE TABLE " + EmployeeEntry.TABLE_NAME + " (" +
                    EmployeeEntry._ID + " INTEGER PRIMARY KEY, " +
                    EmpMgrContract.EmployeeEntry.COLUMN_TEXT + " TEXT, " +
                    EmpMgrContract.EmployeeEntry.COLUMN_START_DATE + " TEXT default CURRENT_TIMESTAMP, " +
                    EmployeeEntry.COLUMN_END_DATE + " TEXT, " +
                    EmployeeEntry.COLUMN_EMAIL + " TEXT, " +
                    EmployeeEntry.COLUMN_DONE + " INTEGER, " +
                    EmployeeEntry.COLUMN_POSITIONID + " INTEGER, " +
                    " FOREIGN KEY("+ EmployeeEntry.COLUMN_POSITIONID + ") REFERENCES " +
                    PositionEntry.TABLE_NAME +
                    "(" + PositionEntry._ID +") " + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        res = context.getResources();
        packageName = context.getPackageName();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.sqLiteDatabase = db;
        this.sqLiteDatabase.execSQL(TABLE_CATEGORIES_CREATE);
        this.sqLiteDatabase.execSQL(TABLE_TODOS_CREATE);

        //*** Setting up sample position and employee data from strings.xml (start) ***/
        String[] position_sample_array = res.getStringArray(res.getIdentifier("position_sample_array", "array", packageName));
        JSONObject jobj;
        ContentValues values = new ContentValues();
        long idPosition = -1;

        for(int i = 0; i < position_sample_array.length; i++){
            String jsonStr = position_sample_array[i];

            try {
                jobj = new JSONObject(jsonStr);
                String id = jobj.getString("id");
                String desc = jobj.getString("desc");
                String code = jobj.getString("code");

                values.put(PositionEntry._ID, id);
                values.put(PositionEntry.COLUMN_DESCRIPTION, desc);
                values.put(PositionEntry.COLUMN_CODE, code);
                idPosition = this.sqLiteDatabase.insert(PositionEntry.TABLE_NAME, null, values);
                values.clear();
            }catch(JSONException e){
                Log.d(e.getMessage(), "Failed to parse:" + jsonStr);
                values.clear();
                break;
            }
        }

/*        if(idPosition > 0) {
            String employee_sample_array = res.getString(res.getIdentifier("employee_sample", "string", packageName));
            try{
                jobj = new JSONObject(employee_sample_array);
                String name = jobj.getString("name");
                String email = jobj.getString("email");

                values.put(EmployeeEntry.COLUMN_POSITIONID, String.valueOf(idPosition));
                values.put(EmployeeEntry.COLUMN_TEXT, name);
                values.put(EmployeeEntry.COLUMN_EMAIL, email);
                long idEmployee = sqLiteDatabase.insert(EmployeeEntry.TABLE_NAME, null, values);
            }catch(JSONException e){
                Log.d(e.getMessage(), "Failed to parse:" + employee_sample_array);
                values.clear();
            }
        }*/
        //*** Setting up sample position and employee data from strings.xml (end) ***/

        refreshEmployees();
    }

    protected void refreshEmployees() {
        String empmgr_server_proto_ip_port = res.getString(res.getIdentifier("empmgr_server_proto_ip_port", "string", packageName));
        String jsonStr = HttpClient.getResponse(empmgr_server_proto_ip_port + "/employees.json");
        ContentValues values = new ContentValues();

        if (jsonStr != null && jsonStr.length() > 0) {

            try {
                JSONArray jsonArray = new JSONArray(jsonStr);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    values.clear();

                    long id = jsonObject.getLong("id");
                    String text = jsonObject.getString("text");
                    String email = jsonObject.getString("email");
                    long positionId = jsonObject.getLong("positionId");

                    values.put(EmployeeEntry._ID, id);
                    values.put(EmployeeEntry.COLUMN_TEXT, text);
                    values.put(EmployeeEntry.COLUMN_EMAIL, email);
                    values.put(EmployeeEntry.COLUMN_POSITIONID, positionId);
                    long idEmployee = this.sqLiteDatabase.insert(EmployeeEntry.TABLE_NAME, null, values);
                }
            } catch (JSONException e) {
                Log.e(module, e.getMessage());
                values.clear();
            }
        }
        Log.d(module, "Parsing http JSON request is done:" + jsonStr);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + EmployeeEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PositionEntry.TABLE_NAME);
        onCreate(db);
    }
}
