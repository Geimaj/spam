package com.example.jamie.spam;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.places.Place;
import com.google.maps.model.TravelMode;


public class TripData implements Parcelable {

    private String userID;
    private TravelMode travelMode;
    private String originName;
    private String destintaionName;

    public TripData(String userID, TravelMode travelMode, Place origin, Place destintaion) {
        this.userID = userID;
        this.travelMode = travelMode;
        this.originName = origin.getName().toString();
        this.destintaionName = destintaion.getName().toString();
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

    public String getOriginName() {
        return originName;
    }

    public String getDestintaionName() {
        return destintaionName;
    }

    protected TripData(Parcel in) {
        userID = in.readString();
        travelMode = (TravelMode) in.readValue(TravelMode.class.getClassLoader());
        originName = in.readString();
        destintaionName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userID);
        dest.writeValue(travelMode);
        dest.writeString(originName);
        dest.writeString(destintaionName);
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