package com.somshubra.ragialnotifier.intro;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.somshubra.ragialnotifier.R;

public class IntroActivity extends ActionBarActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    public static final String INTRO_FLAG = "INTRO_FLAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_intro, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putBoolean(INTRO_FLAG, true).commit();
        super.onBackPressed();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            PlaceholderFragment f = new PlaceholderFragment();
            f.id = position;

            return f;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Main Item Screen";
                case 1:
                    return "Add Item";
                case 2:
                    return "Selecting Items";
                case 3:
                    return "Vending/Buying items";
            }
            return null;
        }
    }

    public static class PlaceholderFragment extends Fragment {
        public int id = 0;

        public PlaceholderFragment() {}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = null;
            if(id < 3)
                rootView = inflater.inflate(R.layout.fragment_intro, container, false);
            else
                rootView = inflater.inflate(R.layout.fragment_intro_end, container, false);

            ImageView i = (ImageView) rootView.findViewById(R.id.intro_image);
            TextView t = (TextView) rootView.findViewById(R.id.intro_footer);

            Button b;
            if(id == 3) {
                b = (Button) rootView.findViewById(R.id.intro_end_button);

                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        sp.edit().putBoolean(INTRO_FLAG, true).commit();

                        getActivity().finish();
                    }
                });
            }

            chooseImage(i, id);
            chooseText(t, id);

            return rootView;
        }

        public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }

        public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(res, resId, options);

            // Calculate inSampleSize
            //options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inSampleSize = 2;

            // Decode bitmap with inSampleSize set
            //options.inJustDecodeBounds = false;
            return BitmapFactory.decodeResource(res, resId, options);
        }


        private void chooseImage(ImageView i, int id) {
            switch(id) {
                case 0: {
                    i.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.intro_1, i.getWidth(), i.getHeight()));
                    return;
                }
                case 1: {
                    i.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.intro_2, i.getWidth(), i.getHeight()));
                    return;
                }case 2: {
                    i.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.intro_3, i.getWidth(), i.getHeight()));
                    return;
                }case 3: {
                    i.setImageBitmap(decodeSampledBitmapFromResource(getResources(), R.drawable.intro_4, i.getWidth(), i.getHeight()));
                    return;
                }
            }
        }

        private void chooseText(TextView t, int id) {
            String text = "";
            switch(id) {
                case 0: {
                    text = "Add items or visit ragial.com";
                    break;
                }
                case 1: {
                    text = "The name has to be exact. Copy from ragial.com";
                    break;
                }case 2: {
                    text = "Select items by long cliking them";
                    break;
                }case 3: {
                    text = "Click on items to see venders";
                    break;
                }
            }

            t.setText(text);
        }
    }

}
