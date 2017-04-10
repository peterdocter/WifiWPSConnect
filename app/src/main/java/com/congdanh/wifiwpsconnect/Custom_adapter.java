package com.congdanh.wifiwpsconnect;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CongDanh on 19/01/2015.
 */
public class Custom_adapter extends ArrayAdapter<String> {
    private ArrayList<String> SSID;
    private ArrayList<String> BSSID;
    private List<Integer> level;
    private Activity activity;

    public Custom_adapter(Activity activity, ArrayList<String> SSID, ArrayList<String> BSSID, List<Integer> level){
        super(activity, R.layout.custom_listview, SSID);
        this.activity = activity;
        this.BSSID = BSSID;
        this.SSID = SSID;
        this.level = level;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup){
        LayoutInflater inflater = activity.getLayoutInflater();
        View view1 = inflater.inflate(R.layout.custom_listview, null, true);
        TextView SSID_tv = (TextView) view1.findViewById(R.id.tv_SSID);
        TextView BSSID_tv = (TextView) view1.findViewById(R.id.tv_BSSID);
        ImageView wifi_signal = (ImageView) view1.findViewById(R.id.icon_wifi_signal);

        SSID_tv.setText(SSID.get(position));
        BSSID_tv.setText(BSSID.get(position));
        wifi_signal.setImageResource(level.get(position));
        return view1;
    }
}
