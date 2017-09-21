package com.example.yinqinghao.childprotect.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yinqinghao.childprotect.R;
import com.example.yinqinghao.childprotect.entity.Group;
import com.example.yinqinghao.childprotect.entity.Person;
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
    private List<Group> mGroups;
    private List<Person> mPeople;
    private String adminId;
    private boolean isRoute = false;
    private boolean isGroup = false;
    private boolean isPerson = false;

    public GridAdapter(Context mContext, List<Zone> mZones) {
        this.mContext = mContext;
        this.mZones = mZones;
    }

    public GridAdapter(Context mContext, List<Route> mRoutes, boolean isRoute) {
        this.mContext = mContext;
        this.mRoutes = mRoutes;
        this.isRoute = true;
    }

    public GridAdapter(Context mContext, boolean isGroup, List<Group> mGroups ) {
        this.mContext = mContext;
        this.mGroups = mGroups;
        this.isGroup = isGroup;
    }

    public GridAdapter(boolean isPerson, Context mContext, List<Person> mPeople, String adminId ) {
        this.mContext = mContext;
        this.mPeople = mPeople;
        this.isPerson = isPerson;
        this.adminId = adminId;
    }

    @Override
    public int getCount() {
        if (isRoute) {
            return mRoutes.size();
        }
        if (isGroup) {
            return mGroups.size();
        }
        if (isPerson) {
            return mPeople.size();
        }

        return mZones.size();
    }

    @Override
    public Object getItem(int position) {
        if (isRoute) {
            return mRoutes.get(position);
        }
        if (isGroup) {
            return mGroups.get(position);
        }
        if (isPerson) {
            return mPeople.get(position);
        }
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
            } else if (isGroup) {
                Group group = mGroups.get(position);
                textView.setText(group.getName());
                imageView.setImageResource(R.drawable.nav_group);
            } else if (isPerson) {
                Person person = mPeople.get(position);
                textView.setText(person.getFirstName() + " " + person.getLastName());
                imageView.setImageResource(adminId.equals(person.getUid())
                        ? R.drawable.admin : R.drawable.user);
            } else {
                Zone zone = mZones.get(position);
                textView.setText(zone.getDes());
                imageView.setImageResource(zone.getStatus().equals("safe") ? R.drawable.safe : R.drawable.danger);
            }
        } else {
            grid = (View) convertView;
        }

        return grid;
    }
}
