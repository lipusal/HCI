package hci.itba.edu.ar.tpe2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;
import hci.itba.edu.ar.tpe2.backend.data.PersistentData;
import hci.itba.edu.ar.tpe2.backend.network.NetworkRequestCallback;
import hci.itba.edu.ar.tpe2.backend.service.NotificationScheduler;
import hci.itba.edu.ar.tpe2.fragment.FlightDetailsFragment;
import hci.itba.edu.ar.tpe2.fragment.FlightDetailsMainFragment;
import hci.itba.edu.ar.tpe2.fragment.FlightStatusListFragment;
import hci.itba.edu.ar.tpe2.fragment.StarInterface;
import hci.itba.edu.ar.tpe2.fragment.YourFlightsFragment;

public class FlightsActivity extends AppCompatActivity
        implements StarInterface, NavigationView.OnNavigationItemSelectedListener, FlightStatusListFragment.OnFragmentInteractionListener, YourFlightsFragment.OnFragmentInteractionListener, FlightDetailsFragment.OnFragmentInteractionListener, FlightDetailsMainFragment.OnFragmentInteractionListener {

    private static final String PARAM_STATUS = "FlightStatusClicked";
    private static final String PARAM_REVIEW_VISIBLE = "ReviewVisible";

    @Override
    public void onFragmentInteraction(Uri uri) {
        //Do nothing for now
    }

    private CoordinatorLayout coordinatorLayout;
    private Toolbar toolbar;
    private Menu menu;
    private PersistentData persistentData;
    private boolean reviewVisible;
    private boolean isFailDialogDisplayed;
    YourFlightsFragment yourFlightsFragment;

//    private FlightStatus lastFlightClicked;
//    public FlightStatus getLastFlightClicked(){
//        return lastFlightClicked;
//    }
//    public void setLastFlightCLicked(FlightStatus lastFlightCLicked){
//        this.lastFlightClicked=lastFlightCLicked;
//    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            setFlightStatus((FlightStatus) savedInstanceState.getSerializable(PARAM_STATUS));
            reviewVisible = savedInstanceState.getBoolean(PARAM_REVIEW_VISIBLE);
        } else {
            reviewVisible = false;
        }
//        if (getResources().getBoolean(R.bool.landscape_only)) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        }
//        lastFlightClicked=null;
        setContentView(R.layout.activity_flights);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_flights);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Go to flights search activity
                Intent i = new Intent(FlightsActivity.this, SearchActivity.class);
                startActivity(i);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);       //Set the flights option as selected TODO I don't think this is Android standard
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        persistentData = new PersistentData(this);
        if (!persistentData.isInited()) {
            final ProgressDialog progressDialog = new ProgressDialog(FlightsActivity.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setTitle(getString(R.string.downloading_data) + "...");
            progressDialog.show();
            persistentData.init(
                    new NetworkRequestCallback<Void>() {
                        @Override
                        public void execute(Context c, Void param) {
                            progressDialog.dismiss();
                            //TODO refresh Your Flights list if necessary
                            if (!NotificationScheduler.areUpdatesEnabled()) {
                                Intent i = new Intent(NotificationScheduler.ACTION_UPDATE_FREQUENCY_SETTING_CHANGED);   //Set automatic updates (NotificationScheduler will handle all the logic)
                                sendBroadcast(i);
                            }
                        }
                    },
                    new NetworkRequestCallback<String>() {
                        @Override
                        public void execute(Context c, String param) {
                            progressDialog.dismiss();
                            if (!isFailDialogDisplayed) {
                                new AlertDialog.Builder(FlightsActivity.this)
                                        .setTitle(R.string.startup_failed_title)
                                        .setMessage(R.string.startup_failed_message)
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                FlightsActivity.this.finish();
                                            }
                                        }).show();
                                isFailDialogDisplayed = true;
                            }
                        }
                    });
        }


        if (savedInstanceState == null) {

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            fm.beginTransaction();
            yourFlightsFragment = YourFlightsFragment.newInstance(coordinatorLayout);
//        Bundle arguments = new Bundle();
//        arguments.putString(PARAM_STATUS,flightStatus.toString());
//        detailsFragment.setArguments(arguments);
            ft.add(R.id.fragment_container_your_flight, yourFlightsFragment);
            ft.commit();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(PARAM_STATUS, getFlightStatus());
        outState.putBoolean(PARAM_REVIEW_VISIBLE, reviewVisible);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);       //Set the flights option as selected TODO I don't think this is Android standard
    }

    @Override
    public void onPause() {
        super.onPause();
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
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Flights Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://hci.itba.edu.ar.tpe2/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;

        View detailsFrame = this.findViewById(R.id.fragment_container_flight_details);
        boolean dualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
        if (dualPane) {
            getMenuInflater().inflate(R.menu.flight, menu);
            MenuItem item = menu.findItem(R.id.action_review);
            item.setVisible(reviewVisible);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_review) {
            Intent reviewIntent = new Intent(this, MakeReviewActivity.class);
            reviewIntent.putExtra(FlightDetailMainActivity.PARAM_STATUS, flightStatus);
            reviewIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            startActivity(reviewIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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


    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Flights Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://hci.itba.edu.ar.tpe2/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private FlightStatus flightStatus;
    @Deprecated
    private View selectedView;

    public FlightStatus getFlightStatus() {
        View detailsFrame = findViewById(R.id.fragment_container_flight_details);
        boolean isDualPaneEnabled = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
        if (isDualPaneEnabled) {
            return flightStatus;
        } else {
            return null;
        }


    }

    @Deprecated
    public View getSelectedView() {
        return selectedView;
    }

    public void setFlightStatus(FlightStatus newFlightStatus) {
        flightStatus = newFlightStatus;
    }

    @Deprecated
    public void setSelectedView(View newSelectedView) {
        selectedView = newSelectedView;
    }

    @Override
    public void onFlightClicked(FlightStatus clickedStatus) {
        View detailsFrame = this.findViewById(R.id.fragment_container_flight_details);
        boolean dualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;


        if (dualPane) {
            FlightDetailsMainFragment details = (FlightDetailsMainFragment) this.getSupportFragmentManager().findFragmentById(R.id.fragment_container_flight_details);
            //TODO no crearlo si es el mismo que antes
            details = new FlightDetailsMainFragment();
            this.setFlightStatus(clickedStatus);

            reviewVisible = true;
//            MenuItem item = toolbar.getMenu().findItem(R.id.action_review);
//            item.setVisible(true);
            invalidateOptionsMenu();

            FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container_flight_details, details);
            ft.commit();

            fab.bringToFront();

        } else {
            Intent detailsIntent = new Intent(this, FlightDetailMainActivity.class);
            detailsIntent.putExtra(FlightDetailMainActivity.PARAM_STATUS, clickedStatus);
            startActivity(detailsIntent);
        }
    }

    @Override
    public void onFlightUnstarred(FlightStatus status) {
        View detailsFrame = this.findViewById(R.id.fragment_container_flight_details);
        boolean dualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;
        if (dualPane) {
            FlightDetailsMainFragment details = (FlightDetailsMainFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container_flight_details);
//            MenuItem item = toolbar.getMenu().findItem(R.id.action_review);
//            item.setVisible(false);
            if (details == null) {
                return;
            }
            if (status == getFlightStatus()) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.remove(details);
                ft.commit();
                reviewVisible = false;
                this.invalidateOptionsMenu();
            }
            fab.bringToFront();
        }
    }
}
