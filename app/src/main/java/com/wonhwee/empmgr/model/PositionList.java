package com.wonhwee.empmgr.model;

import android.databinding.ObservableArrayList;

import java.io.Serializable;

public class PositionList implements Serializable {
    public final ObservableArrayList<Position> ItemList;

    public PositionList() {
        ItemList = new ObservableArrayList<>();
    }

    public PositionList(ObservableArrayList<Position> il) {
        ItemList = il;
    }
}