package com.cmpe277.lab2.mytube;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TabsPageAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public TabsPageAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                SearchFragment tab1 = new SearchFragment();
                return tab1;
            case 1:
                FavoriteFragment tab2 = new FavoriteFragment();
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}