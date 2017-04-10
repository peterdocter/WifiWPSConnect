package com.congdanh.wifiwpsconnect;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by CongDanh on 19/01/2015.
 */
public class Conn_Pro_Custom_adapter extends ArrayAdapter<String> {
    private ArrayList<String> SSID;
    private ArrayList<String> Password;
    private Activity activity;

    public Conn_Pro_Custom_adapter(Activity activity, ArrayList<String> SSID, ArrayList<String> Password){
        super(activity, R.layout.conn_pro_custom_listview, SSID);
        this.activity = activity;
        this.Password = Password;
        this.SSID = SSID;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup){
        LayoutInflater inflater = activity.getLayoutInflater();
        View view1 = inflater.inflate(R.layout.conn_pro_custom_listview, null, true);
        TextView SSID_tv = (TextView) view1.findViewById(R.id.tv_SSID);
        TextView Password_tv = (TextView) view1.findViewById(R.id.tv_Password);

        SSID_tv.setText(SSID.get(position));
        Password_tv.setText(Password.get(position));
        return view1;
    }
}
