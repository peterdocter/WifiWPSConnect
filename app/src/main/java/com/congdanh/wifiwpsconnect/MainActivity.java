package com.congdanh.wifiwpsconnect;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Created by CongDanh on 19/01/2015.
 */

public class MainActivity extends  ActionBarActivity{

    ListView MyLview;
    Button btScan, btClear;
    Spinner spinner;
    final ArrayList<ScanResult> todoItems = new ArrayList<ScanResult>();    //List Scan Result with Wifi type(WPA/ WPS/ WEP...)
    Custom_adapter custom_adapter;
    ArrayList<String> arr_SSID = new ArrayList<String>();
    ArrayList<String> arr_BSSID = new ArrayList<String>();
    List<Integer> arr_icon_wifi_signal = new ArrayList<Integer>();
    ArrayAdapter<String> adapter_nettype;
    List<ScanResult> ScanList;      //List All ScanResult.
    WifiManager wifiManager;

    Tools tools = new Tools();

    ScanResult Itemlongclick;  // Item long click in MyList Wifi.
    Resources res;

    //SharePreferences Setting
    boolean autowifion = true;
    boolean autowifioff = false;
    boolean flag_wifion = false;

    //StartApp Advs.
//    private StartAppAd startAppAd = new StartAppAd(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //StartApp Advs
//        StartAppSDK.init(this, "102661022", "202351651", true);

        setContentView(R.layout.activity_main);

        res = getResources();

        MyLview = (ListView) findViewById(R.id.wifilist);
        registerForContextMenu(MyLview);
        btScan = (Button) findViewById(R.id.bt_scan);
        btClear = (Button) findViewById(R.id.bt_clear);
        spinner = (Spinner) findViewById(R.id.net_type);

        custom_adapter = new Custom_adapter(this, arr_SSID, arr_BSSID, arr_icon_wifi_signal);
        MyLview.setAdapter(custom_adapter);

        String[] net_type = {"All", "WPS", "WPA/WPA2", "WEP", "Opens"};
        adapter_nettype = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_nettype, net_type);
        adapter_nettype.setDropDownViewResource(R.layout.spinner_nettype_dropdown);
        spinner.setAdapter(adapter_nettype);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                //0 - All, 1 - WPS, 2 - WPA/WPA2, 3 - WEP, 4 - Opens.
                switch (position) {
                    case 0:
                        doScan(wifiManager, 0);
                        break;
                    case 1:
                        doScan(wifiManager, 1);
                        break;
                    case 2:
                        doScan(wifiManager, 2);
                        break;
                    case 3:
                        doScan(wifiManager, 3);
                        break;
                    case 4:
                        doScan(wifiManager, 4);
                        break;
                    default:
                        return;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Scan wifi, update to listview.
                doScan(wifiManager, spinner.getSelectedItemPosition());
            }
        });

        btClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doClearMLView();
            }
        });

        MyLview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ScanResult SR = todoItems.get(position);
                createDialog(SR.SSID, SR.BSSID, SR.capabilities, CheckChannel(SR.frequency), SR.level).show();
            }
        });

        MyLview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Itemlongclick = todoItems.get(position);
                return false;
            }
        });

        checkFirstRun();
    }

    protected void onResume(){
        super.onResume();
        //StartApp Advs.
//        startAppAd.onResume();

        SharedPreferences preferences = getSharedPreferences("settings", 0);
        autowifion = preferences.getBoolean("autowifion", true);
        autowifioff = preferences.getBoolean("autowifioff", false);

        //Turn on Wi-fi when start or resume App.
        if (autowifion && !wifiManager.isWifiEnabled()) {
            Toast.makeText(getBaseContext(), res.getString(R.string.wifi_on_status), Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
            flag_wifion = true;
        }
    }

    protected void onPause(){
        super.onPause();
        //StartApp Advs.
//        startAppAd.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        //Turn off Wi-fi when exit App.
        if (autowifioff && flag_wifion && wifiManager.isWifiEnabled()) {
            Toast.makeText(getBaseContext(), res.getString(R.string.wifi_off_status), Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(false);
        }
    }

    //Show permission dialog first run app.
    public void checkFirstRun() {
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun || !RootTools.isRootAvailable()){
            if (RootTools.isRootAvailable()) {
                tools.createDialog(this, "Permission", tools.ReadRawTextFile(this, R.raw.alertpermission)).show();
            }else {
                tools.createDialog_AlertPermission(this, "Permission", tools.ReadRawTextFile(this, R.raw.alert_denied_permission)).show();
            }
            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply();
        }
    }

    //Channel?
    private int CheckChannel(int frequency){
        switch (frequency){
            case 2412:
                return 1;
            case 2417:
                return 2;
            case 2422:
                return 3;
            case 2427:
                return 4;
            case 2432:
                return 5;
            case 2437:
                return 6;
            case 2442:
                return 7;
            case 2447:
                return 8;
            case 2452:
                return 9;
            case 2457:
                return 10;
            case 2462:
                return 11;
            case 2467:
                return 12;
            case 2472:
                return 13;
            case 2484:
                return 14;
            default:
                return 0;
        }
    }

    //Scan wireless.
    protected void doScan(final WifiManager wifiManager, final int net_type){
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {         //Default reload after about 3 seconds.
                ScanList = wifiManager.getScanResults();
                doClearMLView();
                for (int i = 0; i < ScanList.size(); i++){
                    switch (net_type){
                        //0 - All, 1 - WPS, 2 - WPA/WPA2, 3 - WEP, 4 - Opens.
                        case 0:
                            AddItem(i);
                            break;
                        case 1:
                            if (ScanList.get(i).capabilities.contains(Constants.WPS)){
                                AddItem(i);
                            }
                            break;
                        case 2:
                            if (ScanList.get(i).capabilities.contains(Constants.WPA) || ScanList.get(i).capabilities.contains(Constants.WPA2)){
                                AddItem(i);
                            }
                            break;
                        case 3:
                            if (ScanList.get(i).capabilities.contains(Constants.WEP)){
                                AddItem(i);
                            }
                            break;
                        case 4:
                            if (!ScanList.get(i).capabilities.contains(Constants.WEP) && !ScanList.get(i).capabilities.contains(Constants.WPA)
                                    && !ScanList.get(i).capabilities.contains(Constants.WPA2)){
                                AddItem(i);
                            }
                            break;
                        default:
                            return;
                    }
                }
                custom_adapter.notifyDataSetChanged();
            }
        },filter);
        wifiManager.startScan();
    }

    //Add Items
    private void AddItem(int i){
        arr_SSID.add(ScanList.get(i).SSID);
        arr_BSSID.add(ScanList.get(i).BSSID);
        int lv = WifiManager.calculateSignalLevel(ScanList.get(i).level,5);
        switch (lv){
            case 1:
                arr_icon_wifi_signal.add(R.drawable.wifi_signal_1);
                break;
            case 2:
                arr_icon_wifi_signal.add(R.drawable.wifi_signal_2);
                break;
            case 3:
                arr_icon_wifi_signal.add(R.drawable.wifi_signal_3);
                break;
            case 4:
                arr_icon_wifi_signal.add(R.drawable.wifi_signal_4);
                break;
            default:
                arr_icon_wifi_signal.add(R.drawable.wifi_signal_disable);
                break;
        }
        todoItems.add(ScanList.get(i));
    }

    //Clear List View.
    protected void doClearMLView(){
        arr_SSID.clear();
        arr_BSSID.clear();
        arr_icon_wifi_signal.clear();
        custom_adapter.clear();
        todoItems.clear();
        custom_adapter.notifyDataSetChanged();
    }

    //Get 6 end char in MAC.
    private String GetEndMac(String bssid){
        String EndMac = null;
        if (bssid.length() == 17){
            EndMac = bssid.substring(9,11) + bssid.substring(12,14) + bssid.substring(15,17);
        }
        return EndMac;
    }

    //General WPS default.
    protected String GeneralWPSDefault(String bssid) {
        int WPSNum = 0;
        int WPSNum_end = 0;
        if (bssid.length() == 6) {
            WPSNum = Integer.parseInt(bssid, 16) % 10000000;
            WPSNum_end = wps_pin_checksum(WPSNum);
            WPSNum = (WPSNum * 10) + WPSNum_end;
        }
        return check_WPSNum(WPSNum);
    }

    private String check_WPSNum(int WPSNum){
        if (WPSNum < 10) return "0000000".concat(Integer.toString(WPSNum));
        else if (WPSNum < 100) return "000000".concat(Integer.toString(WPSNum));
        else if (WPSNum < 1000) return "00000".concat(Integer.toString(WPSNum));
        else if (WPSNum < 10000) return "0000".concat(Integer.toString(WPSNum));
        else if (WPSNum < 100000) return "000".concat(Integer.toString(WPSNum));
        else if (WPSNum < 1000000) return "00".concat(Integer.toString(WPSNum));
        else if (WPSNum < 10000000) return "0".concat(Integer.toString(WPSNum));
//        else if (WPSNum < 100000000) return Integer.toString(WPSNum);
        return Integer.toString(WPSNum);
    }

//    private int wps_pin_checksum(int pin){
//        int accum = 0;
//        while(pin!=0){
//            accum += 3 * (pin % 10);
//            pin /= 10;
//            accum += pin % 10;
//            pin /= 10;
//            accum = (10 - accum % 10) % 10;
//        }
//        return accum;
//    }

    private int wps_pin_checksum(int pin){
        int accum = 0;
        pin = pin * 10;
        accum = accum + (3 * ((pin / 10000000) % 10));
        accum = accum + (1 * ((pin / 1000000) % 10));
        accum = accum + (3 * ((pin / 100000) % 10));
        accum = accum + (1 * ((pin / 10000) % 10));
        accum = accum + (3 * ((pin / 1000) % 10));
        accum = accum + (1 * ((pin / 100) % 10));
        accum = accum + (3 * ((pin / 10) % 10));
        int digit = accum % 10;
        int checksum = (10 - digit) %10;
        return  checksum;
    }

    //Connect Wifi use WPS PIN.
    private void ConnectToDeviceUsePin(String bssid, String pin) {
        String cmd = "wpa_cli wps_reg " + bssid + " " + pin;
        try {
            CommandCapture cmdCapture = new CommandCapture(0, cmd);
            RootTools.getShell(true).add(cmdCapture);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (RootDeniedException e) {
            e.printStackTrace();
        }
    }

    //Connect Wifi use Password.
    private void ConnectToDeviceUsePW(String ssid, String security ,String password){
        WifiConfiguration wfc = new WifiConfiguration();
        wfc.SSID = "\"".concat(ssid).concat("\"");
        wfc.status = WifiConfiguration.Status.DISABLED;
        wfc.priority = 40;

        if (security.contains(Constants.WEP)) {
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            if (isHexString(password)) wfc.wepKeys[0] = password;
            else wfc.wepKeys[0] = "\"".concat(password).concat("\"");
            wfc.wepTxKeyIndex = 0;
        }
        else if (security.contains(Constants.WPA) || security.contains(Constants.WPA2)){
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wfc.preSharedKey = "\"".concat(password).concat("\"");
        }
        else { //Open
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wfc.allowedAuthAlgorithms.clear();
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        }

        int networkID = wifiManager.addNetwork(wfc);
        if (networkID != -1){   //add success.
            wifiManager.enableNetwork(networkID, true); //Connect.
        }
    }

    //Check is Hex String?
    private boolean isHexString(String str){
        for (char c : str.toCharArray()){
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')))
                return false;
        }
        return true;
    }

    //Check Pin Vaild?
    private boolean CheckPin(String pin) {
        if (pin.length() != 8)
            return false;
        else {
            for (char c : pin.toCharArray()) {
                if (!Character.isDigit(c)) return false;
            }
        }
        return true;
    }

    //Get Password of a Wifi
    private void GetPassword(String SSID, String security){
        //If SSID saved in device.
        ArrayList<String> SSID_List = new ArrayList<String>();
        ArrayList<String> Password_List = new ArrayList<String>();

        String source = ConstPath.wpa_supplicant_file_dir;
        String dest = ConstPath.MyTemp_dir;
//        String dest = "/sdcard/WifiWPSConnect/temp/";

        if (tools.CreateFolder(ConstPath.MyFolder_dir) && tools.CreateFolder(ConstPath.MyTemp_dir) && tools.Copy_RPer(source, dest, null)) {
            List<String> strings = tools.Read_Con_Pro(dest);
            String[][] info = new String[strings.size()][];
            for (int i = 1; i < strings.size(); i++) {
                if (!strings.get(i).isEmpty()) {
                    info[i] = strings.get(i).split("\"");
                    if (info[i][1] != null && info[i].length > 3) {
                        SSID_List.add(info[i][1]);
                        Password_List.add(info[i][3]);
                    } else if (info[i][1] != null && info[i].length <= 3) {
                        SSID_List.add(info[i][1]);
                        Password_List.add("No Password");
                    }
                }
            }

            for (int i = 0; i < SSID_List.size(); i++) {
                if (SSID_List.get(i).equals(SSID)) {
                    tools.createDialog(this, SSID, Password_List.get(i)).show();
                    return;
                }
            }
        }

        //If not saved.
        if (security.contains(Constants.WPS)){
            tools.createDialog(this, res.getString(R.string.not_found), SSID + " " + res.getString(R.string.not_found2) + " " + SSID + " " + res.getString(R.string.not_found3)).show();
        }else {
            tools.createDialog(this, res.getString(R.string.not_found), SSID + " " + res.getString(R.string.not_found4)).show();
        }
    }

    //Alert ask before exit App.
    public void AlertExitApp(String q){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(q)
                .setCancelable(false)
                .setPositiveButton(res.getString(R.string.yes), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //StartApp Advs
//                        startAppAd.showAd(); // show the ad
//                        startAppAd.loadAd(); // load the next ad

                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton(res.getString(R.string.no), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    //Create a Dialog show wifi info.
    public Dialog createDialog(final String title, final String bssid, final String capabilities, int frequency, int level) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final TextView input = new TextView(this);
        input.setPadding(30, 5, 25, 0);
        input.append("BSSID:    " + bssid + "\n" + "Security: " + capabilities + "\n" + "Channel: " + frequency + "\n" + "Signal:   " + level + "\n");
        builder.setView(input);
        builder.setTitle(title)
                .setCancelable(false)//("Are you sure you want to exit?")
                .setPositiveButton(res.getString(R.string.try_conn), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ConnectToDeviceUsePin(bssid, GeneralWPSDefault(GetEndMac(bssid)));
                        new CheckWifiConnect_AsyncTask().execute();
                    }
                })
                .setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();

        //Enable or disable Positive button.
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (capabilities.contains(Constants.WPS)){
                    ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }else {
                    ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });
        return alertDialog;
    }

    //Create input Dialog for Enter Custom Pin.
    public AlertDialog createPinInputDialog(Activity activity, String title, String content, final String bssid) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LinearLayout linearLayout = new LinearLayout(activity);
        final TextView textView = new TextView(activity);
        final EditText editText = new EditText(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        textView.setText(content);
        textView.setPadding(20,20,20,20);
        editText.setPadding(20,20,20,20);

        linearLayout.addView(textView);
        linearLayout.addView(editText);
        builder.setView(linearLayout);
        builder.setTitle(title)
                .setCancelable(false)   //("Are you sure you want to exit?")
                .setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String inputDialog = editText.getText().toString();
                        if (CheckPin(inputDialog)) {
                            ConnectToDeviceUsePin(bssid, inputDialog);
                        } else
                            tools.ShowMessage(getBaseContext(), res.getString(R.string.pin_invalid));
                    }
                })
                .setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }

    //Create input Dialog for Enter Password.
    public AlertDialog createPwInputDialog(Activity activity, final String title, String content,final String capabilities) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LinearLayout linearLayout = new LinearLayout(activity);
        final TextView textView = new TextView(activity);
        final EditText editText = new EditText(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        textView.setText(content);
        textView.setPadding(20,20,20,20);
        editText.setPadding(20,20,20,20);

        linearLayout.addView(textView);
        linearLayout.addView(editText);
        builder.setView(linearLayout);
        builder.setTitle(title)
                .setCancelable(false)       //("Are you sure you want to exit?")
                .setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String inputDialog = editText.getText().toString();
                        ConnectToDeviceUsePW(title, capabilities, inputDialog);
                    }
                })
                .setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }


    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (event.getAction() == KeyEvent.ACTION_DOWN){
            if (keyCode == KeyEvent.KEYCODE_BACK){
                String q = res.getString(R.string.question_exit);
                AlertExitApp(q);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.showpw:
                Intent connection_properties = new Intent(this, ConnectionProperties.class);
                startActivity(connection_properties);
                return true;
            case R.id.setting:
                Intent settingsActivity = new Intent(this, Setting.class);
                startActivity(settingsActivity);
                return true;
            case R.id.help:
                tools.createDialog(this, res.getString(R.string.help), tools.ReadRawTextFile(this, R.raw.help)).show();
                return true;
            case R.id.about:
                tools.createDialog(this, res.getString(R.string.about), tools.ReadRawTextFile(this, R.raw.about)).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.generalpin:
                tools.createDialog(this, Itemlongclick.SSID, res.getString(R.string.default_pin) + " " + GeneralWPSDefault(GetEndMac(Itemlongclick.BSSID))).show();
                return true;
            case R.id.custompin:
                createPinInputDialog(this, Itemlongclick.SSID, res.getString(R.string.enter_pin), Itemlongclick.BSSID).show();
                return true;
            case R.id.custompw:
                createPwInputDialog(this, Itemlongclick.SSID, res.getString(R.string.enter_pw), Itemlongclick.capabilities).show();
                return true;
            case R.id.getpw:
                GetPassword(Itemlongclick.SSID, Itemlongclick.capabilities);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public class CheckWifiConnect_AsyncTask extends AsyncTask<Void, Integer, String[]> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(MainActivity.this);
            pd.setTitle(res.getString(R.string.try_conn_background));
            pd.setMessage(res.getString(R.string.connecting_background));
            pd.show();
            pd.setCancelable(false);
        }

        @Override
        protected String[] doInBackground(Void...voids) {
            String[] flag;
            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (networkInfo.isConnected()) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    flag = new String[]{"1", wifiInfo.getSSID()};
                    return flag;
                }
            }
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            flag = new String[]{"0", null};
            return flag;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            if (result[0].equals("1")){
                Toast.makeText(getBaseContext(), res.getString(R.string.connect_to_background) + " " + result[1], Toast.LENGTH_LONG).show();
            }else if (result[0].equals("0")){
                Toast.makeText(getBaseContext(), res.getString(R.string.fail_conn_background), Toast.LENGTH_LONG).show();
            }
            pd.dismiss();
        }
    }
}

