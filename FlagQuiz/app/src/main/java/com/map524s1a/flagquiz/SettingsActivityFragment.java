package com.map524s1a.flagquiz;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsActivityFragment extends PreferenceFragment {

    // creates preferences GUI from preferences.xml file in res/xml
    @Override
    public void onCreate(Bundle bundle) {
        System.out.println("Beginning of onCreate method in SettingsActivityFragment Class");
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences); // load from XML
        System.out.println("End of onCreate method in SettingsActivityFragment Class");
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        System.out.println("Beginning of onCreateView method in SettingsActivityFragment Class");
//
//        System.out.println("End of onCreateView method in SettingsActivityFragment Class");
//        return inflater.inflate(R.layout.fragment_settings, container, false);
//    }
}

// =================================================================================================

//package com.map524s1a.flagquiz;
//
//import android.support.v4.app.Fragment;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
///**
// * A placeholder fragment containing a simple view.
// */
//public class SettingsActivityFragment extends Fragment {
//
//    public SettingsActivityFragment() {
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_settings, container, false);
//    }
//}
