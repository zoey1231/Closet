package com.example.frontend;


import java.util.List;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import android.widget.TextView;

public class CalendarAdapter extends ArrayAdapter<Event> {
    private static final String TAG ="CalendarFragment";
    private List<Event> list;
    private LayoutInflater mInflater;

    public CalendarAdapter(Context context, List<Event> list) {
        super(context, R.layout.activity_calendar_event, list);
        this.mInflater = LayoutInflater.from(context);
        this.list = list;
    }

    static class ViewHolder {
        TextView text;

    }

    public void addItems(List<Event> list) {
        this.list.clear();
        this.list.addAll(list);
        notifyDataSetChanged();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View returnView = convertView;
        ViewHolder viewHolder;

        if (returnView == null) {

            returnView = mInflater.inflate(R.layout.activity_calendar_event, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.text = (TextView) returnView.findViewById(R.id.label);

            returnView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) returnView.getTag();
        }

        viewHolder.text.setText(list.get(position).getSummary());

        return returnView;
    }
}