package com.wonhwee.empmgr.model;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;

import java.io.Serializable;

public class Employee implements Serializable {
    public final ObservableInt Id = new ObservableInt();
    public final ObservableField<String> text = new ObservableField<String>();
    public final ObservableField<String> start_date = new ObservableField<String>();
    public final ObservableField<String> end_date = new ObservableField<String>();
    public final ObservableBoolean done = new ObservableBoolean();
    public final ObservableField<String> positionid = new ObservableField<String>();
    public final ObservableField<String> email = new ObservableField<String>();

    public Employee(int id, String text, String start_date, String end_date, boolean done, String positionid, String email) {
        this.Id.set(id);
        this.text.set(text);
        this.start_date.set(start_date);
        this.end_date.set(end_date);
        this.done.set(done);
        this.positionid.set(positionid);
        this.email.set(email);
    }

}

