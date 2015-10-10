package com.cmpe277.lab2.mytube;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by knbarve on 10/9/15.
 */

public class TabsPageAdapter extends FragmentPagerAdapter {

    public TabsPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                // Top Rated fragment activity
                return new SearchFragment();
            case 1:
                // Games fragment activity
                return new FavoriteFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
