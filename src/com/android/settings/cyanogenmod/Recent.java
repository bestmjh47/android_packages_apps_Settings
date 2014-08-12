/*
 * Copyright (C) 2014 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cyanogenmod;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import com.android.settings.util.Helpers;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class Recent extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String CUSTOM_RECENT_MODE = "custom_recent_mode";
    private static final String RECENT_PANEL_EXPANDED_MODE = "recent_panel_expanded_mode";
    private static final String RECENT_PANEL_LEFTY_MODE = "recent_panel_lefty_mode";
    private static final String RECENT_PANEL_SCALE = "recent_panel_scale";
    private static final String RECENT_PANEL_BG_COLOR = "recent_panel_bg_color";

    private CheckBoxPreference mRecentsCustom;
    private ListPreference mRecentPanelExpandedMode;
    private CheckBoxPreference mRecentPanelLeftyMode;
    private ListPreference mRecentPanelScale;
    private ColorPickerPreference mRecentPanelBgColor;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DEFAULT_BACKGROUND_COLOR = 0x00ffffff;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.recent_settings);

        ContentResolver resolver = getActivity().getContentResolver();

        mRecentsCustom = (CheckBoxPreference) findPreference(CUSTOM_RECENT_MODE);
        final boolean enableRecentsCustom = Settings.System.getBoolean(resolver,
                Settings.System.CUSTOM_RECENT_TOGGLE, true);
        mRecentsCustom.setChecked(enableRecentsCustom);
        mRecentsCustom.setOnPreferenceChangeListener(this);

        mRecentPanelExpandedMode = (ListPreference) findPreference(RECENT_PANEL_EXPANDED_MODE);
        final int recentPanelExpandedMode = Settings.System.getInt(resolver,
                Settings.System.RECENT_PANEL_EXPANDED_MODE, 0);
        mRecentPanelExpandedMode.setValue(recentPanelExpandedMode + "");
        mRecentPanelExpandedMode.setOnPreferenceChangeListener(this);

        mRecentPanelLeftyMode = (CheckBoxPreference) findPreference(RECENT_PANEL_LEFTY_MODE);
        final boolean recentLeftyMode = Settings.System.getInt(getContentResolver(),
                Settings.System.RECENT_PANEL_GRAVITY, Gravity.RIGHT) == Gravity.LEFT;
        mRecentPanelLeftyMode.setChecked(recentLeftyMode);
        mRecentPanelLeftyMode.setOnPreferenceChangeListener(this);

        mRecentPanelScale = (ListPreference) findPreference(RECENT_PANEL_SCALE);
        final int recentPanelScale = Settings.System.getInt(resolver,
                Settings.System.RECENT_PANEL_SCALE_FACTOR, 100);
        mRecentPanelScale.setValue(recentPanelScale + "");
        mRecentPanelScale.setOnPreferenceChangeListener(this);

        // Recent panel background color
        mRecentPanelBgColor =
                (ColorPickerPreference) findPreference(RECENT_PANEL_BG_COLOR);
        mRecentPanelBgColor.setOnPreferenceChangeListener(this);
        final int intColor = Settings.System.getInt(getContentResolver(),
                Settings.System.RECENT_PANEL_BG_COLOR, 0x00ffffff);
        String hexColor = String.format("#%08x", (0x00ffffff & intColor));
        mRecentPanelBgColor.setSummary(hexColor);
        mRecentPanelBgColor.setNewPreviewColor(intColor);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mRecentsCustom) {
            Settings.System.putBoolean(resolver,
                    Settings.System.CUSTOM_RECENT_TOGGLE, (Boolean) newValue);
            Helpers.restartSystemUI();
            return true;
        } else if (preference == mRecentPanelScale) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.RECENT_PANEL_SCALE_FACTOR, value);
            return true;
        } else if (preference == mRecentPanelLeftyMode) {
            Settings.System.putInt(resolver,
                    Settings.System.RECENT_PANEL_GRAVITY,
                    ((Boolean) newValue) ? Gravity.LEFT : Gravity.RIGHT);
            return true;
        } else if (preference == mRecentPanelExpandedMode) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.RECENT_PANEL_EXPANDED_MODE, value);
            return true;
        } else if (preference == mRecentPanelBgColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.RECENT_PANEL_BG_COLOR,
                    intHex);
            return true;
        }

        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.menu_restore)
                .setIcon(R.drawable.ic_settings_backup)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.shortcut_action_reset);
        alertDialog.setMessage(R.string.recent_panel_reset_message);
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetValues();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void resetValues() {
        Settings.System.putInt(getContentResolver(),
                Settings.System.RECENT_PANEL_BG_COLOR, DEFAULT_BACKGROUND_COLOR);
        mRecentPanelBgColor.setNewPreviewColor(DEFAULT_BACKGROUND_COLOR);
    }
}
