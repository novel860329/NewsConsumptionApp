package com.example.accessibility_detect;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ResultListViewAdapter extends BaseAdapter {
    Context mContext;
    List<String> DateList, Response;
    LayoutInflater inflater;

    public ResultListViewAdapter(Context mContext, List<String> DateList, List<String> Response) {
        this.mContext = mContext;
        this.DateList = DateList;
        this.Response = Response;
        inflater = (LayoutInflater.from(mContext));
    }

    @Override
    public int getCount() {
        return DateList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.result_textview, null);
        TextView date_tv = (TextView) view.findViewById(R.id.date_result);
        TextView result_tv = (TextView) view.findViewById(R.id.textview_result);
        String result = "已回答/總共: " + DateList.get(i) + "/" + Response.get(i);
        Log.d("ViewAdapter", result);
        date_tv.setText(DateList.get(i));
        result_tv.setText(Response.get(i));
        return view;
    }
}
