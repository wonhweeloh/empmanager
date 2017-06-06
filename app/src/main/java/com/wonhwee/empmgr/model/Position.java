package com.wonhwee.empmgr.model;

import android.databinding.ObservableField;

import java.io.Serializable;

public class Position implements Serializable {
    public final ObservableField<Integer> posId = new ObservableField<Integer>();
    public final ObservableField<String>  code = new ObservableField<String>();
    public final ObservableField<String>  description = new ObservableField<String>();

    public Position() {
    }

    public Position(int i, String c, String d) {
        posId.set(i);
        code.set(c);
        description.set(d);
    }


}
