package Model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zhuol on 2/25/2016.
 */
public class ObservationEntryObject implements Parcelable{

    public String nid="";
    public String title="";
    public String description ="";
    public String record ="";
    public String photoServerUri ="";
    public String photoLocalUri="";
    public String audioUri="";
    public String lattitude="";
    public String longitude ="";
    public String address="";
    public String date="";
    public String author="";

    public static final Parcelable.Creator<ObservationEntryObject> CREATOR = new Creator<ObservationEntryObject>() {

        @Override
        public ObservationEntryObject[] newArray(int size) {
            // TODO Auto-generated method stub
            return new ObservationEntryObject[size];
        }

        @Override
        public ObservationEntryObject createFromParcel(Parcel source) {
            // TODO Auto-generated method stub
            return new ObservationEntryObject(source);
        }
    };

    public ObservationEntryObject(Parcel in) {
        super();
        nid = in.readString();
        title = in.readString();
        description = in.readString();
        record = in.readString();
        photoServerUri = in.readString();
        photoLocalUri=in.readString();
        audioUri = in.readString();
        lattitude = in.readString();
        longitude = in.readString();
        address = in.readString();
        date=in.readString();
        author=in.readString();
    }

    public ObservationEntryObject(){}
    public ObservationEntryObject(String nid, String title, String description, String record, String photoServerUri, String photoLocalUri, String audioUri, String lattitude, String longitude, String address, String date, String author){
        this.nid=nid;
        this.title=title;
        this.description = description;
        this.record = record;
        this.photoServerUri = photoServerUri;
        this.photoLocalUri=photoLocalUri;
        this.audioUri=audioUri;
        this.lattitude=lattitude;
        this.longitude = longitude;
        this.address=address;
        this.date=date;
        this.author=author;
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
        dest.writeString(description);
        dest.writeString(record);
        dest.writeString(photoServerUri);
        dest.writeString(photoLocalUri);
        dest.writeString(audioUri);
        dest.writeString(lattitude);
        dest.writeString(longitude);
        dest.writeString(address);
        dest.writeString(date);
        dest.writeString(author);
    }
}
