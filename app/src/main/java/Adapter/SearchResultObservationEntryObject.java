package Adapter;

import android.graphics.Bitmap;

/**
 * Created by zhuol on 4/2/2016.
 */
public class SearchResultObservationEntryObject {
    public String nid;
    public String title;
    public String category;
    public String record;
    public String date;
    public Bitmap imgaeBitmap;
    public String imageUrl;

    public SearchResultObservationEntryObject(){}
    public SearchResultObservationEntryObject(String nid, String title,String category,String record,String date,String imageUrl, Bitmap bitmap){
        this.nid=nid;
        this.title=title;
        this.category=category;
        this.record=record;
        this.date=date;
        this.imageUrl=imageUrl;
        this.imgaeBitmap=bitmap;
    }
}
