package com.congdanh.wifiwpsconnect;

import android.os.Environment;

import java.io.File;

/**
 * Created by CongDanh on 23/01/2015.
 */
public class ConstPath {
    public static final File Data = Environment.getDataDirectory();
    public static final File SD_Scard = Environment.getExternalStorageDirectory();

    public static final String wpa_suppli_name = "wpa_supplicant.conf";
    public static final String wpa_suppli_bkname = "wpa_supplicant.bk";

    public static final String wifi_dir = Data + "/misc/wifi/";
    public static final String wpa_supplicant_file_dir = wifi_dir + wpa_suppli_name;
    public static final String MyFolder_dir = SD_Scard + "WifiWPSConnect";
    public static final String MyTemp_dir = SD_Scard + "/WifiWPSConnect/temp/";
    public static final String MyBackup_dir = SD_Scard + "/WifiWPSConnect/backup/";
}
