package hci.itba.edu.ar.tpe2;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;
import hci.itba.edu.ar.tpe2.backend.data.PersistentData;

public class FlightDetailMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FlightDetailsFragment.OnFragmentInteractionListener {

    public static final String PARAM_STATUS = "hci.itba.edu.ar.tpe2.FlightDetailMainActivity.STATUS";
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    FlightStatus flightStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_detail_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        //Get info from the specified flight
        Intent callerIntent = getIntent();
        if (!callerIntent.hasExtra(PARAM_STATUS)) {
            throw new IllegalStateException("Flight details activity started without " + PARAM_STATUS + " parameter in Intent");
        }
        flightStatus = (FlightStatus) callerIntent.getSerializableExtra(PARAM_STATUS);
        setTitle(flightStatus.getAirline().getID() + "#" + flightStatus.getFlight().getNumber());


         /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
         ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
         this, drawer, toolbar, R.string.navigation_drawer_open, R.string.drawer_close);

         NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
         navigationView.setNavigationItemSelectedListener(this);*/
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new FlightDetailsFragment(), "Detalles");
        adapter.addFragment(new FlightReviewsFragment(), "Comentarios");

        viewPager.setAdapter(adapter);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.flight_detail_main, menu);

      //  Flight flight = flightStatus.getFlight();

        int id = R.id.action_follow;
        if (new PersistentData(this).getWatchedStatuses().contains(flightStatus)) {
            toolbar.getMenu().findItem(id).setIcon(R.drawable.ic_star_white_on_24dp);
        } else {
            toolbar.getMenu().findItem(id).setIcon(R.drawable.ic_star_white_off_24dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

       // Flight flight = flightStatus.getFlight();

        int id = item.getItemId();


        if (id == R.id.action_follow) {
            PersistentData persistentData = new PersistentData(this);

            if (persistentData.getWatchedStatuses().contains(flightStatus)) {
                persistentData.stopWatchingStatus(flightStatus,this);
                toolbar.getMenu().findItem(id).setIcon(R.drawable.ic_star_white_off_24dp);
            } else {
                persistentData.watchStatus(flightStatus, this);
                toolbar.getMenu().findItem(id).setIcon(R.drawable.ic_star_white_on_24dp);
            }
            return true;
        }
        if (id == R.id.action_review) {
            Intent reviewIntent = new Intent(this, MakeReviewActivity.class);
            reviewIntent.putExtra(FlightDetailMainActivity.PARAM_STATUS, flightStatus);
            reviewIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            startActivity(reviewIntent);
            return true;
        }
        //Esto de aca abajo lo copie y pgue y me olvide, pero creo que servia para uqe se cambie el toolbar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.invalidateOptionsMenu();
        }


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent i = null;
        if (id == R.id.drawer_flights) {
            i = new Intent(this, FlightsActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else if (id == R.id.drawer_search) {
            i = new Intent(this, SearchActivity.class);
        } else if (id == R.id.drawer_map) {
            i = new Intent(this, DealsMapActivity.class);
        } else if (id == R.id.drawer_settings) {
            i = new Intent(this, SettingsActivity.class);
        } else if (id == R.id.drawer_help) {

        }

        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
        }
        //else, unrecognized option selected, close drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public FlightStatus getFlightStatus() {
        return flightStatus;
    }
}
