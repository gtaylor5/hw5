package taylor.gerard.hw4;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // map field
    private AlienLocationService.AlienLocationServiceBinder alienLocationService; // service instance
    private HashMap<Integer,AlienInfo> shipInfo = new HashMap<>(); //map of AlienInfo Objects with ship number as key
    private HashSet<Integer> pastViewedShips = new HashSet<>(); //ships that were viewed the last second.
    private HashSet<Integer> currentViewedShips = new HashSet<>(); //ships that were just reported
    private LatLngBounds.Builder builder; //bounds builder.


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng center = new LatLng(38.9073, -77.0365);
        builder = new LatLngBounds.Builder(); // initialize builder
        mMap.moveCamera(CameraUpdateFactory.newLatLng(center)); // move to prescribed center.
        Intent intent = new Intent(this, AlienLocationService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE); // bind service.
    }


    //Reporter Object.

    private AlienLocationService.Reporter reporter = new AlienLocationService.Reporter() {
        @Override
        public void report(final List<UFOPosition> positions) { // pass in list of observed UFOs.
            runOnUiThread(new Runnable() { // RUN ON UI THREAD TO UPDATE SHIPS.
                @Override
                public void run() {



                    // Main loop for showing and updating UFOS.

                    for(UFOPosition position : positions) {
                        if(shipInfo.keySet().contains(position.getShipNumber())){ // IF UFO WAS ALREADY OBSERVED
                            AlienInfo alienInfo = shipInfo.get(position.getShipNumber()); //get alienInfo instance.
                            alienInfo.addNewPosition(position); //add the new position and update the marker (See AlienInfo.addNewPosition(UFOPosition))
                            mMap.addPolyline(alienInfo.getOptions()); // draws polyline
                            builder.include(alienInfo.getMarker().getPosition());//adds new position to LatLngBounds.Builder.
                            shipInfo.put(alienInfo.getShipNumber(), alienInfo); //insert alien info back into hashmap
                            currentViewedShips.add(alienInfo.getShipNumber()); // add ship number to set of ships that were just reported.
                        } else { //OTHERWISE CREATE NEW SHIP
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .visible(true)
                                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.red_ufo))
                                    .position(new LatLng(position.getLat(), position.getLon()))
                                    .anchor(0.5f,0.5f)); //create new marker for new alien ship.
                            PolylineOptions options = new PolylineOptions().width(10).color(Color.RED); //initialize polylineoptions
                            options.add(new LatLng(position.getLat(), position.getLon())); //add position to new options object
                            AlienInfo alienInfo = new AlienInfo(true, marker, position, options); //initialize new AlienInfo Instance.
                            mMap.addPolyline(alienInfo.getOptions()); // create polyline from new alienInfo object
                            shipInfo.put(position.getShipNumber(), alienInfo); // add to map of ships.
                            builder.include(marker.getPosition()); // include position in bounds builder
                            currentViewedShips.add(position.getShipNumber()); // add to ships that were just reported
                        }
                    }

                    //logic to remove ship from map.

                    for(Integer i : pastViewedShips){
                        if(!currentViewedShips.contains(i)){ //IF SHIPPED WAS NOT REPORTED HIDE
                            Marker marker = shipInfo.get(i).getMarker();
                            marker.setVisible(false);
                            shipInfo.get(i).setIsOnMap(false);
                        }
                    }

                    //update pastViewedShips to equal current viewed ships

                    pastViewedShips.removeAll(pastViewedShips);
                    pastViewedShips.addAll(currentViewedShips);

                    //remove all current viewed ships
                    currentViewedShips.removeAll(currentViewedShips);
                    try {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 5)); //update camer at end.
                    }catch(IllegalStateException e){
                        return;
                    }
                }
            });
        }
    };



    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            alienLocationService = (AlienLocationService.AlienLocationServiceBinder) service; // on bind initialize service
            alienLocationService.addReporter(reporter); // add reporter.
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            alienLocationService = null;
        }
    };
}
