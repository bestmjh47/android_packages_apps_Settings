/*
 * Copyright (C) 2012 The CyanogenMod Project
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

import android.content.ContentResolver;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.widget.AlphaSeekBar;

public class StatusBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String STATUS_BAR_CLOCK_CATEGORY = "category_status_bar_clock";
    private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
    private static final String STATUS_BAR_BATTERY = "status_bar_battery";
    private static final String STATUS_BAR_AUTO_HIDE = "status_bar_auto_hide";
    private static final String STATUS_BAR_SIGNAL = "status_bar_signal";
    private static final String STATUS_BAR_CATEGORY_GENERAL = "status_bar_general";
    private static final String FREF_FULLSCREEN_STATUSBAR_TIMEOUT = "fullscreen_statusbar_timeout";

    private ListPreference mStatusBarAmPm;
    private ListPreference mStatusBarBattery;
    private ListPreference mStatusBarCmSignal;

    private CheckBoxPreference mStatusBarClock;
    private CheckBoxPreference mStatusBarAutoHide;
    ListPreference mFullScreenStatusBarTimeout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mStatusBarAmPm = (ListPreference) prefSet.findPreference(STATUS_BAR_AM_PM);
        mStatusBarBattery = (ListPreference) prefSet.findPreference(STATUS_BAR_BATTERY);
        mStatusBarCmSignal = (ListPreference) prefSet.findPreference(STATUS_BAR_SIGNAL);

        mFullScreenStatusBarTimeout = (ListPreference) findPreference(FREF_FULLSCREEN_STATUSBAR_TIMEOUT);
        mFullScreenStatusBarTimeout.setOnPreferenceChangeListener(this);
        mFullScreenStatusBarTimeout.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.FULLSCREEN_STATUSBAR_TIMEOUT, 10000) + "");

        if (DateFormat.is24HourFormat(getActivity())) {
            ((PreferenceCategory) prefSet.findPreference(STATUS_BAR_CLOCK_CATEGORY))
                    .removePreference(prefSet.findPreference(STATUS_BAR_AM_PM));
        } else {
            mStatusBarAmPm = (ListPreference) prefSet.findPreference(STATUS_BAR_AM_PM);
            int statusBarAmPm = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_AM_PM, 2);

            mStatusBarAmPm.setValue(String.valueOf(statusBarAmPm));
            mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntry());
            mStatusBarAmPm.setOnPreferenceChangeListener(this);
        }

        CheckBoxPreference statusBarBrightnessControl = (CheckBoxPreference)
                prefSet.findPreference(Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL);

        mStatusBarAutoHide = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_AUTO_HIDE);
        mStatusBarAutoHide.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.AUTO_HIDE_STATUSBAR, 0) == 1));

        try {
            if (Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
                    == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                statusBarBrightnessControl.setEnabled(false);
                statusBarBrightnessControl.setSummary(R.string.status_bar_toggle_info);
            }
        } catch (SettingNotFoundException e) {
        }

        int statusBarBattery = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_BATTERY, 0);
        mStatusBarBattery.setValue(String.valueOf(statusBarBattery));
        mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
        mStatusBarBattery.setOnPreferenceChangeListener(this);

        int signalStyle = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_SIGNAL_TEXT, 0);
        mStatusBarCmSignal.setValue(String.valueOf(signalStyle));
        mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntry());
        mStatusBarCmSignal.setOnPreferenceChangeListener(this);

        PreferenceCategory generalCategory =
                (PreferenceCategory) findPreference(STATUS_BAR_CATEGORY_GENERAL);

        if (Utils.isWifiOnly(getActivity())) {
            generalCategory.removePreference(mStatusBarCmSignal);
        }

        if (Utils.isTablet(getActivity())) {
            generalCategory.removePreference(statusBarBrightnessControl);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (mStatusBarAmPm != null && preference == mStatusBarAmPm) {
            int statusBarAmPm = Integer.valueOf((String) newValue);
            int index = mStatusBarAmPm.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_AM_PM, statusBarAmPm);
            mStatusBarAmPm.setSummary(mStatusBarAmPm.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarBattery) {
            int statusBarBattery = Integer.valueOf((String) newValue);
            int index = mStatusBarBattery.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_BATTERY, statusBarBattery);
            mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);
            return true;
        } else if (preference == mStatusBarCmSignal) {
            int signalStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarCmSignal.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_SIGNAL_TEXT, signalStyle);
            mStatusBarCmSignal.setSummary(mStatusBarCmSignal.getEntries()[index]);
            return true;
        } else if (preference == mFullScreenStatusBarTimeout) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.FULLSCREEN_STATUSBAR_TIMEOUT, val);
            return true;
        }
        return false;
    }

    private void openTransparencyDialog() {
        getFragmentManager().beginTransaction().add(new AdvancedTransparencyDialog(), null)
                .commit();
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;
            if (preference.getKey().equals("transparency_dialog")) {
            // getFragmentManager().beginTransaction().add(new
            // TransparencyDialog(), null).commit();
            openTransparencyDialog();
            return true;
        } else if (preference == mStatusBarAutoHide) {
            value = mStatusBarAutoHide.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.AUTO_HIDE_STATUSBAR, value ? 1 : 0);
        }

        return false;
    }

    public static class AdvancedTransparencyDialog extends DialogFragment {

        private static final int KEYGUARD_ALPHA = 255;

        private static final int STATUSBAR_ALPHA = 0;
        private static final int STATUSBAR_KG_ALPHA = 1;
        private static final int NAVBAR_ALPHA = 2;
        private static final int NAVBAR_KG_ALPHA = 3;

        ViewGroup mNavigationBarGroup;

        AlphaSeekBar mSeekBars[] = new AlphaSeekBar[4];

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setShowsDialog(true);
            setRetainInstance(true);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View layout = View.inflate(getActivity(), R.layout.dialog_transparency, null);

            mNavigationBarGroup = (ViewGroup) layout.findViewById(R.id.navbar_layout);
            mSeekBars[STATUSBAR_ALPHA] = (AlphaSeekBar) layout.findViewById(R.id.statusbar_alpha);
            mSeekBars[STATUSBAR_KG_ALPHA] = (AlphaSeekBar) layout
                    .findViewById(R.id.statusbar_keyguard_alpha);
            mSeekBars[NAVBAR_ALPHA] = (AlphaSeekBar) layout.findViewById(R.id.navbar_alpha);
            mSeekBars[NAVBAR_KG_ALPHA] = (AlphaSeekBar) layout
                    .findViewById(R.id.navbar_keyguard_alpha);

            try {
                // restore any saved settings
                int alphas[] = new int[2];
                final String sbConfig = Settings.System.getString(getActivity()
                        .getContentResolver(),
                        Settings.System.STATUS_BAR_ALPHA_CONFIG);
                if (sbConfig != null) {
                    String split[] = sbConfig.split(";");
                    alphas[0] = Integer.parseInt(split[0]);
                    alphas[1] = Integer.parseInt(split[1]);

                    mSeekBars[STATUSBAR_ALPHA].setCurrentAlpha(alphas[0]);
                    mSeekBars[STATUSBAR_KG_ALPHA].setCurrentAlpha(alphas[1]);

                        final String navConfig = Settings.System.getString(getActivity()
                                .getContentResolver(),
                                Settings.System.NAVIGATION_BAR_ALPHA_CONFIG);
                        if (navConfig != null) {
                            split = navConfig.split(";");
                            alphas[0] = Integer.parseInt(split[0]);
                            alphas[1] = Integer.parseInt(split[1]);
                            mSeekBars[NAVBAR_ALPHA].setCurrentAlpha(alphas[0]);
                            mSeekBars[NAVBAR_KG_ALPHA].setCurrentAlpha(alphas[1]);
                        }
                    }
            } catch (Exception e) {
                resetSettings();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(layout);
            builder.setTitle(getString(R.string.transparency_dialog_title));
            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                        String sbConfig = mSeekBars[STATUSBAR_ALPHA].getCurrentAlpha() + ";" +
                                mSeekBars[STATUSBAR_KG_ALPHA].getCurrentAlpha();
                        Settings.System.putString(getActivity().getContentResolver(),
                                Settings.System.STATUS_BAR_ALPHA_CONFIG, sbConfig);

                        String nbConfig = mSeekBars[NAVBAR_ALPHA].getCurrentAlpha() + ";" +
                                mSeekBars[NAVBAR_KG_ALPHA].getCurrentAlpha();
                        Settings.System.putString(getActivity().getContentResolver(),
                                Settings.System.NAVIGATION_BAR_ALPHA_CONFIG, nbConfig);
                }
            });

            return builder.create();
        }

        private void resetSettings() {
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_ALPHA_CONFIG, null);
            Settings.System.putString(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_ALPHA_CONFIG, null);
        }

        @Override
        public void onDestroyView() {
            if (getDialog() != null && getRetainInstance())
                getDialog().setDismissMessage(null);
            super.onDestroyView();
        }

        private boolean getSavedLinkedState() {
            return getActivity().getSharedPreferences("transparency", Context.MODE_PRIVATE)
                    .getBoolean("link", true);
        }

        private void saveSavedLinkedState(boolean v) {
            getActivity().getSharedPreferences("transparency", Context.MODE_PRIVATE).edit()
                    .putBoolean("link", v).commit();
        }
    }
}
