package com.example.yinqinghao.childprotect.asyncTask;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * Created by yinqinghao on 27/8/17.
 */

public class LocationTask extends GoogleApiClient {
    private Location location;

    public LocationTask() {
    }

    public Location getLocation() {
        try {
            location = LocationServices.FusedLocationApi.getLastLocation(this);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public boolean hasConnectedApi(@NonNull Api<?> api) {
        return false;
    }

    @NonNull
    @Override
    public ConnectionResult getConnectionResult(@NonNull Api<?> api) {
        return null;
    }

    @Override
    public void connect() {

    }

    @Override
    public ConnectionResult blockingConnect() {
        return null;
    }

    @Override
    public ConnectionResult blockingConnect(long l, @NonNull TimeUnit timeUnit) {
        return null;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void reconnect() {

    }

    @Override
    public PendingResult<Status> clearDefaultAccountAndReconnect() {
        return null;
    }

    @Override
    public void stopAutoManage(@NonNull FragmentActivity fragmentActivity) {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean isConnecting() {
        return false;
    }

    @Override
    public void registerConnectionCallbacks(@NonNull ConnectionCallbacks connectionCallbacks) {

    }

    @Override
    public boolean isConnectionCallbacksRegistered(@NonNull ConnectionCallbacks connectionCallbacks) {
        return false;
    }

    @Override
    public void unregisterConnectionCallbacks(@NonNull ConnectionCallbacks connectionCallbacks) {

    }

    @Override
    public void registerConnectionFailedListener(@NonNull OnConnectionFailedListener onConnectionFailedListener) {

    }

    @Override
    public boolean isConnectionFailedListenerRegistered(@NonNull OnConnectionFailedListener onConnectionFailedListener) {
        return false;
    }

    @Override
    public void unregisterConnectionFailedListener(@NonNull OnConnectionFailedListener onConnectionFailedListener) {

    }

    @Override
    public void dump(String s, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strings) {

    }
}
