package taylor.gerard.hw4;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Gerard on 4/5/2017.
 */

public class UFOPosition implements Parcelable{

    private int shipNumber; // ship number
    private double lat; //latitude
    private double lon; //longitude

    //constructor

    public UFOPosition( int shipNumber, double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        this.shipNumber = shipNumber;
    }

    //default constructor

    public UFOPosition(){}

    //getters and setters

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getShipNumber() {
        return shipNumber;
    }

    public void setShipNumber(int shipNumber) {
        this.shipNumber = shipNumber;
    }

    //make object parcelable.

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(shipNumber);
        dest.writeDouble(lat);
        dest.writeDouble(lon);
    }

    public static Creator<UFOPosition> CREATOR = new Creator<UFOPosition>() {
        @Override
        public UFOPosition createFromParcel(Parcel source) {
            UFOPosition ufoPosition = new UFOPosition();
            ufoPosition.setShipNumber(source.readInt());
            ufoPosition.setLat(source.readDouble());
            ufoPosition.setLon(source.readDouble());
            return ufoPosition;
        }

        @Override
        public UFOPosition[] newArray(int size) {
            return new UFOPosition[size];
        }
    };

}
