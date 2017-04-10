package com.congdanh.wifiwpsconnect;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CongDanh on 22/01/2015.
 */
public class ConnectionProperties extends Activity{

    ListView conn_pro;
    TextView conn_info;
    Conn_Pro_Custom_adapter adapter;
    ArrayList<String> arr_SSID = new ArrayList<String>();
    ArrayList<String> arr_Password = new ArrayList<String>();

    Tools tools = new Tools();

    Resources res;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_properties);

        res = getResources();

        conn_info = (TextView) findViewById(R.id.conn_info);
        conn_pro = (ListView) findViewById(R.id.conn_pro);
        registerForContextMenu(conn_pro);

        adapter = new Conn_Pro_Custom_adapter(this, arr_SSID, arr_Password);
        conn_pro.setAdapter(adapter);

        ShowAllPw();

        int pw = 0;
        for (String Password : arr_Password){
            if (Password.equals("No Password")) pw++;
        }
        conn_info.append(arr_SSID.size() + " " + res.getString(R.string.saved_net_found));
        conn_info.append("\n");
        conn_info.append((arr_Password.size() - pw) + " " + res.getString(R.string.prot_net_found) );

        conn_pro.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText(arr_SSID.get(position), arr_Password.get(position));
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getBaseContext(), res.getString(R.string.cp_pw_of) + " " + arr_SSID.get(position), Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void ShowAllPw() {

        //Copy file wpa_supplicant to temp folder in sdcard.
        String source = ConstPath.wpa_supplicant_file_dir;
        String dest = ConstPath.MyTemp_dir;
//        String dest = "/sdcard/WifiWPSConnect/temp/";
        if (tools.CreateFolder(ConstPath.MyFolder_dir) && tools.CreateFolder(ConstPath.MyTemp_dir) && tools.Copy_RPer(source, dest, null)) {

            //Read connections properties from wpa_supplicant file.
            List<String> strings = tools.Read_Con_Pro(dest);

            //Read information every connection properties and get SSID and Password from this.
            Clear_Conn_pro_list();
            String[][] info = new String[strings.size()][];
            for (int i = 1; i < strings.size(); i++) {
                if (!strings.get(i).isEmpty()) {
                    info[i] = strings.get(i).split("\"");
                    if (info[i][1] != null && info[i].length > 3) {
                        AddItem(info[i][1], info[i][3]);
                    } else if (info[i][1] != null && info[i].length <= 3) {
                        AddItem(info[i][1], "No Password");
                    }
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    //Add items to Array.
    private void AddItem(String SSID, String Password){
        arr_SSID.add(SSID);
        arr_Password.add(Password);
    }

    private void Clear_Conn_pro_list(){
        adapter.clear();
        arr_Password.clear();
        arr_SSID.clear();
        adapter.notifyDataSetChanged();
    }

    //Backup wpa_supplicant.conf
    private boolean doBackup(){
        String source = ConstPath.wpa_supplicant_file_dir;
        String dest = ConstPath.MyBackup_dir;
//        String dest = "/sdcard/WifiWPSConnect/backup/";
        if (tools.CreateFolder(ConstPath.MyFolder_dir) && tools.CreateFolder(ConstPath.MyBackup_dir)) {
            return (tools.Copy_RPer(source, dest, ConstPath.wpa_suppli_bkname));
        }
        return false;
    }

    //Restore wpa_supplicant.conf
    private boolean doRestore(){
        String source = ConstPath.MyBackup_dir + ConstPath.wpa_suppli_bkname;
//        String source = "/sdcard/WifiWPSConnect/backup/" + ConstPath.wpa_suppli_bkname;
        String dest = ConstPath.wpa_supplicant_file_dir;
        return  (tools.Copy_RPer(source, dest, null));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cp_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.backup:
                if (doBackup()) tools.createDialog(this, res.getString(R.string.backup), res.getString(R.string.backup_decri) + " " + ConstPath.MyBackup_dir).show();
                else tools.createDialog(this, res.getString(R.string.backup), res.getString(R.string.backup_fail)).show();
                return true;
            case R.id.restore:
                if (tools.checkFile(ConstPath.MyBackup_dir + ConstPath.wpa_suppli_bkname)) {
                    if (doRestore()) tools.createDialog(this, res.getString(R.string.restore), res.getString(R.string.restore_completed)).show();
                    else tools.createDialog(this, res.getString(R.string.restore), res.getString(R.string.restore_fail)).show();
                }else tools.createDialog(this, res.getString(R.string.restore), res.getString(R.string.not_found_bkf)).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
