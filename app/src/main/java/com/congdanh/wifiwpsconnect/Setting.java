package com.congdanh.wifiwpsconnect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.DisplayMetrics;
import android.widget.Toast;

import java.util.Locale;

public class Setting extends PreferenceActivity {
    private Locale myLocale;

    ListPreference language;
    CheckBoxPreference cb_autowifion;
    CheckBoxPreference cb_autowifioff;
    Preference createshortcut;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    Resources res;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);

        res = getResources();

        preferences = getSharedPreferences("settings", Context.MODE_WORLD_READABLE);
        editor = preferences.edit();

        language = (ListPreference) this.findPreference("language");
        cb_autowifion = (CheckBoxPreference) this.findPreference("autowifion");
        cb_autowifioff = (CheckBoxPreference) this.findPreference("autowifioff");
        createshortcut = (Preference) this.findPreference("createshortcut");

        //List Preference
        language.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.equals("vi")) {
                    //vietnamese
                    setLocale("vi");
                    editor.putInt("lang", 1);
                } else if (newValue.equals("en")) {
                    //english
                    setLocale("en");
                    editor.putInt("lang", 0);
                }
                return true;
            }
        });

        //Check Box autowifion
        cb_autowifion.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (cb_autowifion.isChecked()) {
                    editor.putBoolean("autowifion", true);
                    return true;
                } else {
                    editor.putBoolean("autowifion", false);
                    return false;
                }
            }
        });

        //Check Box cb_autowifioff
        cb_autowifioff.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (cb_autowifioff.isChecked()) {
                    editor.putBoolean("autowifioff", true);
                    return true;
                } else {
                    editor.putBoolean("autowifioff", false);
                    return false;
                }
            }
        });

        //Preference createshortcut
        createshortcut.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (addShortcut()) Toast.makeText(getBaseContext(), res.getString(R.string.create_shortcut_succ), Toast.LENGTH_SHORT).show();
                else Toast.makeText(getBaseContext(), res.getString(R.string.create_shortcut_fail), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    protected void onResume(){
        super.onResume();

    }

    protected void onPause() {
        super.onPause();

        editor.commit();
    }

    private void setLocale(String lang) {
        myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        restartActivity();
    }

    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private Boolean addShortcut() {
        //Adding shortcut on Home screen
        try {
            Intent shortcutIntent = new Intent(getApplicationContext(), MainActivity.class);
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Intent addIntent = new Intent();
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, res.getString(R.string.app_name));
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.wifiwpsconnect));
            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            getApplicationContext().sendBroadcast(addIntent);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}