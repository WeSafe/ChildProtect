package com.example.yinqinghao.childprotect.clusterMarker;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by yinqinghao on 23/8/17.
 */

public class MarkerRender extends DefaultClusterRenderer<MarkerItem> {
    public MarkerRender(Context context, GoogleMap map, ClusterManager<MarkerItem> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(MarkerItem item, MarkerOptions markerOptions) {
        markerOptions.title(item.getmTitle());
        if (!item.getmSnippet().equals(""))
            markerOptions.snippet(item.getmSnippet());
        BitmapDescriptor icon = item.getmIcon();
        if ( icon != null) {
            markerOptions.icon(icon);
        }
        markerOptions.anchor(0.0f, 1.0f);
        super.onBeforeClusterItemRendered(item,markerOptions);
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<MarkerItem> cluster, MarkerOptions markerOptions) {
        String title = "";
        for (MarkerItem item : cluster.getItems()) {
            title += item.getmTitle() + ",";
        }
        title = title.substring(0,title.length()-1);
        title += " Location";
        markerOptions.title(title);
        super.onBeforeClusterRendered(cluster,markerOptions);
    }

    @Override
    protected void onClusterItemRendered(MarkerItem clusterItem, Marker marker) {
        marker.showInfoWindow();
        super.onClusterItemRendered(clusterItem,marker);
    }

    @Override
    protected void onClusterRendered(Cluster<MarkerItem> cluster, Marker marker) {
        marker.showInfoWindow();
        super.onClusterRendered(cluster, marker);
    }
}
