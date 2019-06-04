package com.maze.simplemaze;

import android.content.Context;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * @author: chasen
 * @date: 2019/6/2
 */
public class SettingFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.preference);
        getPreferenceManager().setSharedPreferencesName("setting");
        if(getContext().getSharedPreferences("setting", Context.MODE_PRIVATE).getBoolean("bg",true)){
            SwitchPreference switchPreference = (SwitchPreference)findPreference("bg");
            switchPreference.setChecked(true);
        }
        String name = getContext().getSharedPreferences("setting",Context.MODE_PRIVATE).getString("name","anonymous");
        findPreference("show").setSummary(name);
        findPreference("name").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                findPreference("show").setSummary(o.toString());
                return true;
            }
        });
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if(preference.getKey().equals("bg")){
            SwitchPreference switchPreference = (SwitchPreference)preference;
            if(switchPreference.isChecked()){
               MainActivity.mp.start();
            }
            else {
                MainActivity.mp.pause();
            }
        }
        return super.onPreferenceTreeClick(preference);
    }
}
