package com.geocrat.instagramclone.share;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.geocrat.instagramclone.R;
import com.geocrat.instagramclone.home.SectionPagerAdapter;
import com.geocrat.instagramclone.utils.BottomNavigationViewHelper;
import com.geocrat.instagramclone.utils.Permissions;
import com.google.android.material.tabs.TabLayout;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class ShareActivity extends AppCompatActivity {

    private static final String TAG = "ShareActivity";
    private static final int VERIFY_PERMISSION_REQUEST = 1;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        Log.d(TAG, "onCreate: started.");
        if (checkPermissionsArray(Permissions.PERMISSIONS)){

        }else{
            verifyPermissions(Permissions.PERMISSIONS);
        }

        setUpViewPager();
//        setUpBottomNavigationView();
    }

    /*
    return the current tab number
    0 = Gallery Fragment
    1 = Photo Fragment
     */
    public int getCurrentTabNumber(){
        return mViewPager.getCurrentItem();
    }

    private void setUpViewPager(){
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GalleryFragment());
        adapter.addFragment(new PhotoFragment());
        mViewPager = (ViewPager) findViewById(R.id.viewpager_container);
        mViewPager.setAdapter(adapter);
        TabLayout tabLayout = findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setText("Gallery");
        tabLayout.getTabAt(1).setText("Photo");
    }

    public int getTask(){
        Log.d(TAG, "getTask: " +getIntent().getFlags());
        return getIntent().getFlags();
    }

    public boolean checkPermissionsArray(String permissions[]){
        Log.d(TAG, "checkPermissionsArray: checking permissions array");
        for(String permission : permissions){
            if (!checkPermissions(permission)){
                return false;
            }
        }
        return true;
    }

    public boolean checkPermissions(String permission){
        Log.d(TAG, "checkPermissions: checking permission : " + permission);
        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this, permission);
        if (permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermissions: Permission was not granted for " + permission);
            return false;
        }else{
            Log.d(TAG, "checkPermissions: Permission was not granted for " + permission);
            return true;
        }
    }

    public void verifyPermissions(String permissions[]){
        Log.d(TAG, "verifyPermissions: verifying permissions");
        ActivityCompat.requestPermissions(ShareActivity.this, permissions, VERIFY_PERMISSION_REQUEST);
    }

    /*
        bottom navigation view setup
     */
    private void setUpBottomNavigationView(){
        Log.d(TAG, "setUpBottomNavigationView: setting up bottom navigation view");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setUpBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(ShareActivity.this, this, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(2);        //this will highlight the item at index 2 of the bot nav bar when this activity is clicked
        menuItem.setChecked(true);
    }
}
