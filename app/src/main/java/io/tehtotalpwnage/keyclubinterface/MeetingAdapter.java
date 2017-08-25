package io.tehtotalpwnage.keyclubinterface;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by tehtotalpwnage on 8/24/17.
 */

public class MeetingAdapter extends BaseAdapter {
    private Context mContext;
    private String[][] values;

    MeetingAdapter(Context context, String[][] values) {
        this.mContext = context;
        this.values = values;
    }

    @Override
    public int getCount() {
        return values.length;
    }

    @Override
    public Object getItem(int position) {
        return values[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.meeting_spinner, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.dateTime = (TextView) view.findViewById(R.id.textView3);
        view.setTag(holder);
        holder.dateTime.setText(values[position][1]);

        return view;
    }

    private static class ViewHolder {
        TextView dateTime;
    }
}


