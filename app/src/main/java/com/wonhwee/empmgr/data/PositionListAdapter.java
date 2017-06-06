package com.wonhwee.empmgr.data;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableInt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.wonhwee.empmgr.R;
import com.wonhwee.empmgr.databinding.PositionListItemBinding;
import com.wonhwee.empmgr.model.Position;

public class PositionListAdapter extends BaseAdapter implements ListAdapter, SpinnerAdapter {
    public ObservableArrayList<Position> list;
    private ObservableInt position = new ObservableInt();
    private LayoutInflater inflater;

    public PositionListAdapter(ObservableArrayList<Position> l) {
        list = l;
    }

    public PositionListAdapter() {
        list = new ObservableArrayList<Position>();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        int id = list.get(position).posId.get();
        return id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null) {
            inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        PositionListItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.position_list_item, parent, false);
        binding.setPosition(list.get(position));
        return binding.getRoot();
    }

    public int getPosition() {
        return position.get();
    }

    public void setPosition(int position) {
        this.position.set(position);
    }
}
