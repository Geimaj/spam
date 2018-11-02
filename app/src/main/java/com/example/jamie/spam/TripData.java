package com.example.jamie.spam;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.places.Place;
import com.google.maps.model.TravelMode;


public class TripData implements Parcelable {

    private String userID;
    private TravelMode travelMode;
    private String originAddress;
    private String destintaionAddress;

    public TripData(String userID, TravelMode travelMode, Place origin, Place destintaion) {
        this.userID = userID;
        this.travelMode = travelMode;
        this.originAddress = origin.getAddress().toString();
        this.destintaionAddress = destintaion.getAddress().toString();
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public TravelMode getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(TravelMode travelMode) {
        this.travelMode = travelMode;
    }

    public String getOriginAddress() {
        return originAddress;
    }

    public String getDestintaionAddress() {
        return destintaionAddress;
    }

    protected TripData(Parcel in) {
        userID = in.readString();
        travelMode = (TravelMode) in.readValue(TravelMode.class.getClassLoader());
        originAddress = in.readString();
        destintaionAddress = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userID);
        dest.writeValue(travelMode);
        dest.writeString(originAddress);
        dest.writeString(destintaionAddress);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TripData> CREATOR = new Parcelable.Creator<TripData>() {
        @Override
        public TripData createFromParcel(Parcel in) {
            return new TripData(in);
        }

        @Override
        public TripData[] newArray(int size) {
            return new TripData[size];
        }
    };
}