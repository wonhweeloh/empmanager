package com.wonhwee.empmgr.data;


import android.databinding.BindingAdapter;
import android.databinding.ObservableArrayList;
import android.widget.ListView;

import com.wonhwee.empmgr.data.PositionListAdapter;
import com.wonhwee.empmgr.model.Position;

public class PositionListBinder {

    @BindingAdapter("bind:items")
    public static void bindList(ListView view, ObservableArrayList<Position> list) {
        PositionListAdapter adapter;
        if (list == null) {
            adapter = new PositionListAdapter();
        }
        else {
            adapter = new PositionListAdapter(list);
        }
        view.setAdapter(adapter);
    }
}
