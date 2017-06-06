package com.wonhwee.empmgr.dao;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;


public class EmpMgrAsyncQueryHandler extends AsyncQueryHandler {
    public EmpMgrAsyncQueryHandler(ContentResolver cr) {
        super(cr);
    }
}
