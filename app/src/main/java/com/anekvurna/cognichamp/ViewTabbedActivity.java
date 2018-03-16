package com.anekvurna.cognichamp;

import android.content.Intent;
import android.support.design.widget.TabLayout;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;

import java.util.List;

import static com.anekvurna.cognichamp.SanathUtilities.*;

public class ViewTabbedActivity extends DrawerActivity {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tabbed);

        // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new ViewTabbedActivity.SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        initializeSharedPrefs(this);

        initializeCurrentUser();
    }





    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.recreate();
    }

    public void onEditBasic(View view)
    {
        Intent intent = new Intent(this, ProfileBasicActivity.class);
        intent.putExtra(getString(R.string.is_editing), true);
        intent.putExtra(getString(R.string.only_editing), true);
        startActivity(intent);
    }

    public void onEditAddress(View view)
    {
        Intent intent = new Intent(this, ProfileManualAddressActivity.class);
        intent.putExtra(getString(R.string.is_editing), true);
        intent.putExtra(getString(R.string.only_editing), true);
        startActivity(intent);
    }
    


   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_tabbed, menu);
        return true;
    }*/





    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new BasicProfileFragment();

                case 1:
                    return new AddressProfileFragment();

                default:
                    return null;

            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            switch (position)
            {
                case 0: return "BASIC";
                case 1: return "ADDRESS";
                default:return null;
            }
        }
    }
}

