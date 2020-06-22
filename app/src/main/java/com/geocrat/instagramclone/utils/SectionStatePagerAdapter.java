package com.geocrat.instagramclone.utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SectionStatePagerAdapter extends FragmentStatePagerAdapter {

    //binding fragment, fragment name, fragment index number all together
    private final List<Fragment> mFragmentList = new ArrayList<>();             //list of fragments
    private final HashMap<Fragment, Integer> mFragments = new HashMap<>();      //if we have fragments, we will get its index number
    private final HashMap<String, Integer> mFragmentNumbers = new HashMap<>();   //if we have fragment name, we will get its index number
    private final HashMap<Integer, String> mFragmentNames = new HashMap<>();     //if we have index number, we will get its name

    public SectionStatePagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String name){
        mFragmentList.add(fragment);
        mFragments.put(fragment, mFragmentList.size()-1);
        mFragmentNumbers.put(name, mFragmentList.size()-1);
        mFragmentNames.put(mFragmentList.size()-1, name);
    }

    /*
        returns the fragment index number from its name
     */
    public Integer getFragmentNumber (String fragmentName) {
        if (mFragmentNumbers.containsKey(fragmentName)) {
            return mFragmentNumbers.get(fragmentName);
        } else {
            return null;
        }
    }

    /*
        returns the fragment index number from its fragment
     */
        public Integer getFragmentNumber (Fragment fragment) {
            if (mFragmentNumbers.containsKey(fragment)) {
                return mFragmentNumbers.get(fragment);
            } else {
                return null;
            }
        }

    /*
    returns the fragment index number from its name
    */
    public String getFragmentNames (Integer fragmentNumber){
        if (mFragmentNames.containsKey(fragmentNumber)){
            return mFragmentNames.get(fragmentNumber);
        }else {
            return null;
        }
    }
}
