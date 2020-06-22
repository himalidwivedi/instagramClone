package com.geocrat.instagramclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.geocrat.instagramclone.profile.EditProfileFragment;
import com.geocrat.instagramclone.profile.LogOut;
import com.geocrat.instagramclone.utils.FirebaseMethods;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AccountSettingActivity extends AppCompatActivity {

    private static final String TAG = "AccountSettingActivity";
    Context mContext;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    MenuItem previousItem;
    FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);

        mContext  = AccountSettingActivity.this;

//        getIncommingIntent();

        setUpFragments();
        openEditProfile();

        if (getIntent().hasExtra(getString(R.string.selected_bitmap))){
            firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), null, 0 ,null, (Bitmap) getIntent().getParcelableExtra(getString(R.string.selected_bitmap)));
        }

    }

    private void setUpFragments(){
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                if (previousItem != null) {
                    previousItem.setChecked(false);
                }
                menuItem.setCheckable(true);
                menuItem.setChecked(true);
                previousItem = menuItem;

                switch (menuItem.getItemId()) {
                    case R.id.editProfile:
                        openEditProfile();
                        drawerLayout.closeDrawers();
                        break;

                    case R.id.logOut:
//                        Toast.makeText(AccountSettingActivity.this, "User Clicked", Toast.LENGTH_SHORT).show();
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame, new LogOut()).commit();
                        drawerLayout.closeDrawers();
                        break;
                }
                return true;
            }
        });
    }


    public void openEditProfile(){
        EditProfileFragment frag = new EditProfileFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame, frag).commit();
        navigationView.setCheckedItem(R.id.editProfile);
        drawerLayout.closeDrawers();
    }
}