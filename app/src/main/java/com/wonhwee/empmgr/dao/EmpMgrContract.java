package com.wonhwee.empmgr.dao;

import android.net.Uri;
import android.provider.BaseColumns;

public final class EmpMgrContract {
    //URI section
    public static final String CONTENT_AUTHORITY = "com.wonhwee.empmgr.empmgrprovider";
    public static final String PATH_EMPLOYEE="employee";
    public static final String PATH_POSITION="positions";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public String concatContent(String path){
        return "content://" + path;
    }

    public static final class EmployeeEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_EMPLOYEE);

        // Table name
        public static final String TABLE_NAME = "employee";
        //column (field) names
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_TEXT = "text";
        public static final String COLUMN_START_DATE = "start_date";
        public static final String COLUMN_END_DATE = "end_date";
        public static final String COLUMN_DONE = "done";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_POSITIONID = "positionid";
    }

    public static final class PositionEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_POSITION);

        // Table name
        public static final String TABLE_NAME = "positions";
        //column names
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_DESCRIPTION = "description";
    }
}
