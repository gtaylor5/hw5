package taylor.gerard.hw4;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Gerard on 4/6/2017.
 */

public class AlienInfo {

    private Marker marker; // marker object
    private List<UFOPosition> positionHistory; // position history
    private boolean isOnMap; // if is shown on map
    private int shipNumber; // ship number
    private PolylineOptions options; //options for drawing line.

    //constructor

    public AlienInfo(boolean isOnMap, Marker marker, UFOPosition position, PolylineOptions polylineOptions) {
        this.isOnMap = isOnMap;
        this.marker = marker;
        this.positionHistory = new LinkedList<>(); //preserves order
        this.positionHistory.add(position);
        this.shipNumber = position.getShipNumber();
        this.options = polylineOptions;
    }

    //getters and setters

    public void setIsOnMap(boolean val){
        isOnMap = val;
    }

    public int getShipNumber(){
        return this.shipNumber;
    }

    public PolylineOptions getOptions(){
        return options;
    }

    public Marker getMarker(){
        return this.marker;
    }

    //

    public void addNewPosition(UFOPosition position){
        LatLng markerPosition = marker.getPosition();
        if(markerPosition.latitude == position.getLat() && markerPosition.longitude == position.getLon()){ //IF POSITION WAS ALREADY ADDED RETURN
            return;
        }else{//ADD NEW POSITION TO HISTORY, OPTIONS AND SET MARKER POSITION. MAKE VISIBLE IF INVISIBLE. SET ISONMAP EQUAL TO TRUE.
            positionHistory.add(position);
            options.add(new LatLng(position.getLat(), position.getLon()));
            marker.setPosition(new LatLng(position.getLat(), position.getLon()));
            if(!isOnMap){
                marker.setVisible(true);
                isOnMap = true;
            }
        }
    }


}
