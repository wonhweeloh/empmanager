package com.wonhwee.empmgr.dao;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wonhwee.empmgr.client.HttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import static com.wonhwee.empmgr.dao.EmpMgrContract.CONTENT_AUTHORITY;
import static com.wonhwee.empmgr.dao.EmpMgrContract.PositionEntry;
import static com.wonhwee.empmgr.dao.EmpMgrContract.PATH_POSITION;
import static com.wonhwee.empmgr.dao.EmpMgrContract.PATH_EMPLOYEE;
import static com.wonhwee.empmgr.dao.EmpMgrContract.EmployeeEntry;

public class EmpMgrProvider extends ContentProvider{

    public static final String module = EmpMgrProvider.class.getSimpleName();

    //constants for the operation
    private static final int EMPLOYEES = 1;
    private static final int EMPLOYEE_ID = 2;
    private static final int POSITIONS = 3;
    private static final int POSITION_ID = 4;
    //urimatcher
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private String packageName = "";
    private String empmgr_server_proto_ip_port = "";
    private Resources res = null;

    static {
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_EMPLOYEE, EMPLOYEES);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_EMPLOYEE + "/#", EMPLOYEE_ID);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_POSITION, POSITIONS);
        uriMatcher.addURI(CONTENT_AUTHORITY, PATH_POSITION + "/#", POSITION_ID);
    }

    private DatabaseHelper helper;
    @Override
    public boolean onCreate() {
        helper = new DatabaseHelper(getContext());
        packageName = getContext().getPackageName();
        res = getContext().getResources();
        empmgr_server_proto_ip_port = res.getString(res.getIdentifier("empmgr_server_proto_ip_port", "string", packageName));
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor;
        int match = uriMatcher.match(uri);

        String inTables = EmployeeEntry.TABLE_NAME
                + " inner join "
                + PositionEntry.TABLE_NAME
                + " on " + EmpMgrContract.EmployeeEntry.COLUMN_POSITIONID + " = "
                + PositionEntry.TABLE_NAME + "." + PositionEntry._ID;

        SQLiteQueryBuilder builder;

        switch (match) {
            case EMPLOYEES:
                builder = new SQLiteQueryBuilder();
                builder.setTables(inTables);
                cursor = builder.query(db, projection, selection, selectionArgs, null, null, orderBy);
                break;
            case EMPLOYEE_ID:
                builder = new SQLiteQueryBuilder();
                builder.setTables(inTables);
                selection = EmployeeEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = builder.query(db, projection, selection, selectionArgs, null, null, orderBy);
                break;
            case POSITIONS:
                cursor = db.query(PositionEntry.TABLE_NAME, projection, null, null, null, null, orderBy);
                break;
            case POSITION_ID:
                selection = PositionEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(PositionEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, orderBy);
                break;
            default:
                throw new IllegalArgumentException("Query unknown URI: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case EMPLOYEES:
                return insertRecord(uri, contentValues, EmployeeEntry.TABLE_NAME);
            case POSITIONS:
                return insertRecord(uri, contentValues, PositionEntry.TABLE_NAME);
            default:
                throw new IllegalArgumentException("Insert unknown URI: " + uri);
        }
    }

    private Uri insertRecord(Uri uri, ContentValues values, String table) {
        if(table.equals(EmpMgrContract.EmployeeEntry.TABLE_NAME)) {
            String email = values.get(EmployeeEntry.COLUMN_EMAIL).toString();
            String name = values.get(EmployeeEntry.COLUMN_TEXT).toString().replace(" ", "%20");
            String code = values.get(EmployeeEntry.COLUMN_POSITIONID).toString();
            String jsonStr = HttpClient.getResponse(empmgr_server_proto_ip_port + "/empmgr/updateemployee.json?email=" + email + "&name=" + name + "&code=" + code + "&id=-1");

            Log.d(module, "jsonStr:" + jsonStr);

            if (jsonStr == null || jsonStr.length() <= 0) {
                // http request is failed in this case.
                return null;
            }

            try {
                JSONObject jsonObject = new JSONObject(jsonStr);
                String id = jsonObject.getString("id");
                values.put(EmployeeEntry._ID, id);
            }catch(JSONException e){
                return null;
            }
        }

        SQLiteDatabase db = helper.getWritableDatabase();
        long id = db.insert(table, null, values);
        if (id == -1) {
            Log.e("Error", "insert error for URI " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case EMPLOYEES:
                return deleteRecord(uri, null, null, EmpMgrContract.EmployeeEntry.TABLE_NAME);
            case EMPLOYEE_ID:
                return deleteRecord(uri, selection, selectionArgs, EmpMgrContract.EmployeeEntry.TABLE_NAME);
            case POSITIONS:
                return deleteRecord(uri, null, null, PositionEntry.TABLE_NAME);
            case POSITION_ID:
                long id = ContentUris.parseId(uri);
                selection = PositionEntry._ID + "=?";
                String[] sel = new String[1];
                sel[0] = String.valueOf(id);
                return deleteRecord(uri, selection, sel, PositionEntry.TABLE_NAME);
            default:
                throw new IllegalArgumentException("Insert unknown URI: " + uri);
        }
    }

    private int deleteRecord(Uri uri, String selection, String[] selectionArgs, String tableName) {
        if(tableName.equals(EmpMgrContract.EmployeeEntry.TABLE_NAME) && selectionArgs != null && selectionArgs.length > 0) {
            String id = selectionArgs[0];
            String jsonStr = HttpClient.getResponse(empmgr_server_proto_ip_port + "/empmgr/deleteemployee.json?id=" + id);

            Log.d(module, "jsonStr:" + jsonStr);

            if(jsonStr == null || jsonStr.length() <= 0){
                // http request is failed in this case.
                return -1;
            }
        }

        SQLiteDatabase db = helper.getWritableDatabase();
        int id = db.delete(tableName, selection, selectionArgs);
        if (id == -1) {
            Log.e("Error", "delete unknown URI " + uri);
            return -1;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return id;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case EMPLOYEES:
                return updateRecord(uri, values, selection, selectionArgs, EmpMgrContract.EmployeeEntry.TABLE_NAME);
            case POSITIONS:
                return updateRecord(uri, values, selection, selectionArgs, PositionEntry.TABLE_NAME);
            default:
                throw new IllegalArgumentException("Update unknown URI: " + uri);
        }
    }

    private int updateRecord(Uri uri, ContentValues values, String selection, String[] selectionArgs, String tableName) {
        if(tableName.equals(EmpMgrContract.EmployeeEntry.TABLE_NAME) && selectionArgs != null && selectionArgs.length > 0) {
            String email = values.get(EmployeeEntry.COLUMN_EMAIL).toString();
            String name = values.get(EmployeeEntry.COLUMN_TEXT).toString().replace(" ", "%20");
            String code = values.get(EmployeeEntry.COLUMN_POSITIONID).toString();
            String id = selectionArgs[0];
            String jsonStr = HttpClient.getResponse(empmgr_server_proto_ip_port + "/empmgr/updateemployee.json?email=" + email + "&name=" + name + "&code=" + code + "&id=" + id);

            Log.d(module, "jsonStr:" + jsonStr);

            if(jsonStr == null || jsonStr.length() <= 0){
                // http request is failed in this case.
                return -1;
            }
        }

        SQLiteDatabase db = helper.getWritableDatabase();
        int id = db.update(tableName, values, selection, selectionArgs);
        if (id == 0) {
            Log.e("Error", "update error for URI " + uri);
            return -1;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return id;
    }
}
