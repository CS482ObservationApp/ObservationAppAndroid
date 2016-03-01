package Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zhuol on 2/25/2016.
 */
public class ObservationObject implements Parcelable{
    public String nid="";
    public String title="";
    public String body="";
    public String kingdom ="";
    public String photoServerUri ="";
    public String photoLocalUri="";
    public String audioUri="";
    public String lattitude="";
    public String altitude="";
    public String address="";
    public String date="";

    public static final Parcelable.Creator<ObservationObject> CREATOR = new Creator<ObservationObject>() {

        @Override
        public ObservationObject[] newArray(int size) {
            // TODO Auto-generated method stub
            return new ObservationObject[size];
        }

        @Override
        public ObservationObject createFromParcel(Parcel source) {
            // TODO Auto-generated method stub
            return new ObservationObject(source);
        }
    };

    public ObservationObject(Parcel in) {
        super();
        nid = in.readString();
        title = in.readString();
        body = in.readString();
        kingdom = in.readString();
        photoServerUri = in.readString();
        photoLocalUri=in.readString();
        audioUri = in.readString();
        lattitude = in.readString();
        altitude = in.readString();
        address = in.readString();
        date=in.readString();
    }

    public ObservationObject(){}
    public ObservationObject(String nid,String title,String body,String kingdom,String photoServerUri,String photoLocalUri,String audioUri,String lattitude,String altitude,String address,String date){
        this.nid=nid;
        this.title=title;
        this.body=body;
        this.kingdom = kingdom;
        this.photoServerUri = photoServerUri;
        this.photoLocalUri=photoLocalUri;
        this.audioUri=audioUri;
        this.lattitude=lattitude;
        this.altitude=altitude;
        this.address=address;
        this.date=date;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nid);
        dest.writeString(title);
        dest.writeString(body);
        dest.writeString(kingdom);
        dest.writeString(photoServerUri);
        dest.writeString(photoLocalUri);
        dest.writeString(audioUri);
        dest.writeString(lattitude);
        dest.writeString(altitude);
        dest.writeString(address);
        dest.writeString(date);
    }
}
