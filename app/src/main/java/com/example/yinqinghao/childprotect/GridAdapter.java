package com.example.yinqinghao.childprotect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yinqinghao.childprotect.entity.Zone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yinqinghao on 18/8/17.
 */

public class GridAdapter extends BaseAdapter {
    private Context mContext;
    private List<Zone> mZones;

    public GridAdapter(Context mContext, List<Zone> mZones) {
        this.mContext = mContext;
        this.mZones = mZones;
    }

    @Override
    public int getCount() {
        return mZones.size();
    }

    @Override
    public Object getItem(int position) {
        return mZones.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            grid = new View(mContext);
            grid = inflater.inflate(R.layout.grid_single, null);
            TextView textView = (TextView) grid.findViewById(R.id.grid_text);
            ImageView imageView = (ImageView)grid.findViewById(R.id.grid_image);
            Zone zone = mZones.get(position);
            textView.setText(zone.getDes());
            imageView.setImageResource(zone.getStatus().equals("safe") ? R.drawable.ic_security_24dp : R.drawable.ic_pan_tool_24dp);
        } else {
            grid = (View) convertView;
        }

        return grid;
    }
}
