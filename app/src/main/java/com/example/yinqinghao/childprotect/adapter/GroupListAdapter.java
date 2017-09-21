package com.example.yinqinghao.childprotect.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yinqinghao.childprotect.R;

import java.util.List;

/**
 * Created by yinqinghao on 19/9/17.
 */

public class GroupListAdapter extends BaseAdapter {
    //Listview data
    private List<String> mData;
    private Context currentContext;

    //UI reference
    private TextView titleView;
    private ImageView img;


    /**
     * Constructor
     *
     * @param context The context where the View associated with this MenuAdapter is running
     * @param data    A List of Maps. Each entry in the List corresponds to one row in the list. The
     *                Maps contain the data for each row, and should include all the entries specified in
     *                "from"
     */
    public GroupListAdapter(Context context, List<String> data) {
        mData = data;
        currentContext = context;
    }

    /**
     * @see android.widget.Adapter#getCount()
     */
    public int getCount() {
        return mData.size();
    }

    /**
     * @see android.widget.Adapter#getItem(int)
     */
    public Object getItem(int position) {
        return mData.get(position);
    }

    /**
     * @see android.widget.Adapter#getItemId(int)
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * @see android.widget.Adapter#getView(int, View, ViewGroup)
     */
    public View getView(final int position, View convertView, final ViewGroup parent) {
        // Check if view already exists. If not inflate it
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)
                    currentContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // Create a list item based off layout definition
            convertView = inflater.inflate(R.layout.group_list_item, null);
        }

        //find views from the layout
//        img = (ImageView) convertView.findViewById(R.id.list_img);
        titleView = (TextView) convertView.findViewById(R.id.list_text);

        //get the data of current row
        String row = mData.get(position);
        titleView.setText(row);
        return convertView;
    }
}
