package com.congdanh.wifiwpsconnect;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by CongDanh on 15/01/2015.
 */
//Create Dialog with Title + Content.
public class Tools {
    public Dialog createDialog(Activity activity, String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final TextView input = new TextView(activity);
        input.setPadding(30, 5, 25, 0);
        input.setText(content);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(input);
        builder.setTitle(title)
                .setCancelable(false)//("Are you sure you want to exit?")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }

    public Dialog createDialog_AlertPermission(Activity activity, String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final TextView input = new TextView(activity);
        input.setPadding(30, 5, 25, 0);
        input.setText(content);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(input);
        builder.setTitle(title)
                .setCancelable(false)//("Are you sure you want to exit?")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        doCloseApp();
                    }
                });
        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }

    protected void ShowMessage(Context context, String q){
        Toast.makeText(context, q, Toast.LENGTH_LONG).show();
    }

    protected void doCloseApp(){
        System.exit(0);         //Close App out leaving nothing running in the background.
    }

    //Read connections properties from wpa_supplicant file.
    public List<String> Read_Con_Pro(String dest) {
        File file = new File(dest, ConstPath.wpa_suppli_name);
        List<String> strings = new ArrayList<String>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line = null;
            String tmp = null;
            do {
                line = bufferedReader.readLine();
                if (line == null || line.isEmpty()) {
                    strings.add(tmp);
                    tmp = null;
                } else tmp += line;
            } while (line != null);

            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strings;
    }

    public Boolean CreateFolder(String dir){
        File folder = new File(dir);
        if (folder.exists())
            return true;
        else{
            try {
                folder.mkdirs();
                return true;
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
    }

    public String ReadRawTextFile(Activity activity, int resId){
        InputStream inputStream = activity.getResources().openRawResource(resId);
        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while (( line = buffreader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }

    //Copy file to temp folder in sdcard.
    public boolean Copy_RPer(String source, String dest, String name) {
        if (RootTools.remount(source, "rw")) {
            if (name != null) {
                RootTools.copyFile(source, dest + "/" + name, true, true);
            } else {
                RootTools.copyFile(source, dest, true, true);
            }
            return true;
        }
        return false;
    }

    //Check file exits?
    public boolean checkFile(String path){
        File file = new File(path);
        if (file.exists()) return true;
        else return false;
    }
}
