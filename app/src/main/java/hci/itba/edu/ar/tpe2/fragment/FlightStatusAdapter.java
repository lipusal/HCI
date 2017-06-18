package hci.itba.edu.ar.tpe2.fragment;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import hci.itba.edu.ar.tpe2.R;
import hci.itba.edu.ar.tpe2.backend.data.Flight;
import hci.itba.edu.ar.tpe2.backend.data.FlightStatus;
import hci.itba.edu.ar.tpe2.backend.data.PersistentData;

/**
 * Adapter for displaying list of flights.
 */
public class FlightStatusAdapter extends ArrayAdapter<FlightStatus> {
    private CoordinatorLayout mCoordinatorLayout;
    private final PersistentData persistentData;
    private Map<Integer, FlightStatus> watchedStatuses;
    private StarInterface starInterface;
    private boolean setBottomPadding;


    FlightStatusAdapter(Context context, List<FlightStatus> objects, CoordinatorLayout layoutWithFAB, StarInterface starInterface) {
        super(context, 0, objects);
        persistentData = new PersistentData(context);
        mCoordinatorLayout = layoutWithFAB;
        watchedStatuses = persistentData.getWatchedStatuses();
        this.starInterface = starInterface;
        setBottomPadding = false;
    }

    @Override
    public View getView(final int position, View destination, final ViewGroup parent) {
        if (!setBottomPadding) {     //Add bottom padding to not cover up last item with FAB
            float scale = parent.getContext().getResources().getDisplayMetrics().density;
            int bottomPaddingDP = (int) ((56 + 16 + 5) * scale + 0.5f);   //56dp FAB size + 16dp FAB margin + 5dp for wiggle room
            parent.setPadding(0, 0, 0, bottomPaddingDP);
            parent.setClipToPadding(false);                 //http://stackoverflow.com/questions/28916426/last-item-of-listview-fab-hides-it
            setBottomPadding = true;
        }
        if (destination == null) {  //Item hasn't been created, inflate it from Android's default layout
            destination = LayoutInflater.from(getContext()).inflate(R.layout.list_item_flight_status, parent, false);
        }
        final FlightStatus status = getItem(position);
        final Flight flight = status.getFlight();

        FlightStatus flightLastClicked = starInterface.getFlightStatus();

        //BackgroundColor
        if (flightLastClicked != null) {
            if (flightLastClicked.equals(status)) {
                //starInterface.setSelectedView(destination);
                destination.setBackgroundColor(0xE0E0E0E0);
            } else {
                destination.setBackgroundColor(0xFFFFFFFF);
            }

        }

        //Logo
        ImageView icon = (ImageView) destination.findViewById(R.id.icon);
        icon.setImageDrawable(parent.getContext().getDrawable(flight.getAirline().getDrawableLogoID()));
        //Text
        TextView title = (TextView) destination.findViewById(R.id.flight_text);
        title.setText(flight.toString());
        //Status icon
        ImageView statusIcon = (ImageView) destination.findViewById(R.id.status_icon);
        statusIcon.setImageDrawable(getContext().getDrawable(status.getIconID()));
        //Extra time field (only in landscape mode)
        TextView extraTime = (TextView) destination.findViewById(R.id.extra_time);
        if (extraTime != null) {
            switch (status.getStringResID()) {
                case R.string.status_scheduled:
                    extraTime.setText(status.getPrettyScheduledDepartureTime().replace('\n', ' '));
                    break;
                case R.string.status_active:
                case R.string.status_diverted:
                    extraTime.setText(status.getPrettyScheduledArrivalTime().replace('\n', ' '));
                    break;
                case R.string.status_landed:
                    extraTime.setText(status.getPrettyActualArrivalTime().replace('\n', ' '));
                    break;
                case R.string.status_canceled:
                    extraTime.setText("");
                    break;
            }
            title.setText(flight.toString());
        }
        //Star
        final ImageButton star = (ImageButton) destination.findViewById(R.id.follow);
        star.setImageResource(watchedStatuses.containsValue(status) ? R.drawable.ic_star_on_24dp : R.drawable.ic_star_off_24dp);
        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (watchedStatuses.containsValue(status)) {
                    persistentData.stopWatchingStatus(status);
                    star.setImageResource(R.drawable.ic_star_off_24dp);
                    remove(status);
                    notifyDataSetChanged();
                    starInterface.onFlightUnstarred(status);

                    Snackbar.make(mCoordinatorLayout == null ? v : mCoordinatorLayout, String.format(getContext().getString(R.string.removed_flight), flight.toString()), Snackbar.LENGTH_INDEFINITE).setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            persistentData.watchStatus(status);
                            star.setImageResource(R.drawable.ic_star_on_24dp);
                            add(status);
                            notifyDataSetChanged();
                        }
                    }).show();
                } else {
                    persistentData.watchStatus(status);
                    star.setImageResource(R.drawable.ic_star_on_24dp);
                    add(status);
                    notifyDataSetChanged();
                    Snackbar.make(mCoordinatorLayout == null ? v : mCoordinatorLayout, String.format(getContext().getString(R.string.following_flight), flight.toString()), Snackbar.LENGTH_INDEFINITE).setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            persistentData.stopWatchingStatus(status);
                            star.setImageResource(R.drawable.ic_star_off_24dp);
                            remove(status);
                            notifyDataSetChanged();
                        }
                    }).show();
                }
            }
        });
        return destination;
    }
}