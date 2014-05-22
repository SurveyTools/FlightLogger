package com.vulcan.flightlogger.geo.data;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * A Transect defines the data necessary to identify a transectpath
 * @author jayl
 *
 */
public class Transect implements Parcelable{
	public String mId;
	public String mName;
	public Location mStartWaypt;
	public Location mEndWaypt;
	public FlightStatus status; // not used yet
	
	
	public Transect(){
		// need a default constructor
  }
	
	public Transect(Parcel source){
         mId = source.readString();
         mName = source.readString();
         mStartWaypt = Location.CREATOR.createFromParcel(source);
         mEndWaypt = Location.CREATOR.createFromParcel(source);
         status = (FlightStatus)source.readSerializable();
   }
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mId);
		dest.writeString(mName);
		this.mStartWaypt.writeToParcel(dest, 0);
		this.mEndWaypt.writeToParcel(dest,  0);
		dest.writeSerializable(status);	
	}
	
	public static final Parcelable.Creator<Transect> CREATOR = new Parcelable.Creator<Transect>() {

		@Override
		public Transect createFromParcel(Parcel source) {
			return new Transect(source);
		}

		@Override
		public Transect[] newArray(int size) {
			return new Transect[size];
		}    
    };
}
