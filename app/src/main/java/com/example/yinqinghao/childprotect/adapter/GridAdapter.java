package com.example.yinqinghao.childprotect.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yinqinghao.childprotect.R;
import com.example.yinqinghao.childprotect.entity.Route;
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
    private List<Route> mRoutes;
    private boolean isRoute = false;

    public GridAdapter(Context mContext, List<Zone> mZones) {
        this.mContext = mContext;
        this.mZones = mZones;
    }

    public GridAdapter(Context mContext, List<Route> mRoutes, boolean isRoute) {
        this.mContext = mContext;
        this.mRoutes = mRoutes;
        this.isRoute = true;
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
            if (isRoute) {
                Route route = mRoutes.get(position);
                textView.setText(route.getDes());
            } else {
                Zone zone = mZones.get(position);
                textView.setText(zone.getDes());
                imageView.setImageResource(zone.getStatus().equals("safe") ? R.drawable.sz : R.drawable.da);
            }
        } else {
            grid = (View) convertView;
        }

        return grid;
    }
}
