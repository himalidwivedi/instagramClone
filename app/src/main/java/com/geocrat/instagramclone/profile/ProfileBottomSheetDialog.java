package com.geocrat.instagramclone.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.geocrat.instagramclone.AccountSettingActivity;
import com.geocrat.instagramclone.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class ProfileBottomSheetDialog extends BottomSheetDialogFragment {
    private static final String TAG = "ProfileBottomSheetDialo";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.snippet_profile_bottom_nav_bar, container, false);

        final ProfileBottomSheetListView listView = v.findViewById(R.id.lvAccountSettings);
        ArrayList options = new ArrayList();
        options.add("Edit Profile");
        options.add("Login to another account");
        ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, options);
        listView.setAdapter(adapter);

   listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
       @Override
       public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
           Log.d(TAG, "onItemClick: navigating to fragments : " + position);
           Intent intent = new Intent(getContext(), AccountSettingActivity.class);
           startActivity(intent);
       }
   });
   return v;
    }
}
