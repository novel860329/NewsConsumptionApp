package com.example.accessibility_detect.questions.adapters;



import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;


public class ViewPagerAdapter_diary extends FragmentPagerAdapter
{
    private final ArrayList<Fragment> fragments;

    public ViewPagerAdapter_diary(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);

        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }


    @Override
    public int getCount() {
        return this.fragments.size();
    }
}
