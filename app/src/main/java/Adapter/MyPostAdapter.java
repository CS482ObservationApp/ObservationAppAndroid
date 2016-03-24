package Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import DrupalForAndroidSDK.DrupalServicesView;
import HelperClass.PhotoUtil;
import HelperClass.PreferenceUtil;
import Model.ObservationEntryObject;
import ca.zhuoliupei.observationapp.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by zhuol on 3/20/2016.
 */
public class MyPostAdapter extends BaseAdapter{
    ArrayList<ObservationEntryObject> myPostObjects;
    Context context;
    final int maxSize=100*100;
    final int HEADER_TYPE=0;
    final int MYPOST_TYPE=1;

    public MyPostAdapter( Context context){
        this.context=context;
        myPostObjects=new ArrayList<ObservationEntryObject>();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position==0)
            return HEADER_TYPE;
        return MYPOST_TYPE;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return myPostObjects.get(position-1);
    }

    @Override
    public int getCount() {
        return myPostObjects.size()+1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position)==HEADER_TYPE)
            return constructHeaderItem(convertView);
        else
            return constructMyPostItem(position,convertView);

    }
static class HeaderViewHolder{
    ImageView userImg;
    TextView spaceHolderTextView;
}
    private View constructHeaderItem(View convertView){
        HeaderViewHolder viewHolder;
        if (convertView==null){
            convertView=LayoutInflater.from(context).inflate(R.layout.mypost_header_item,null,false);
            viewHolder=new HeaderViewHolder();
            viewHolder.userImg=(ImageView)convertView.findViewById(R.id.userImage_MyPostHeader);
            viewHolder.spaceHolderTextView=(TextView)convertView.findViewById(R.id.txtSpaceHolder_MyPostHeader);
            convertView.setTag(viewHolder);
        }else {
            viewHolder=(HeaderViewHolder)convertView.getTag();
        }
        String imgLocation= PreferenceUtil.getCurrentUserPictureLocalUri(context);
        File imgFile = new File(imgLocation);
        if (imgFile.exists()) {
            Bitmap bitmap = PhotoUtil.getBitmapFromFile(imgFile, maxSize);
            viewHolder.userImg.setImageBitmap(bitmap);
        }
        else
            viewHolder.userImg.setImageResource(R.color.white);

        if (myPostObjects.size()!=0)
            viewHolder.spaceHolderTextView.setVisibility(View.GONE);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//Do Nothing....
            }
        });
        convertView.setSoundEffectsEnabled(false);

        return convertView;
    }

    static class MyPostViewHolder{
        TextView dateTextView;
        ImageView imageView;
        TextView titleTextView;
    }

    private View constructMyPostItem(int position,View convertView){
        MyPostViewHolder viewHolder;
        if (convertView==null){
            convertView=LayoutInflater.from(context).inflate(R.layout.mypost_item,null,false);
            viewHolder=new MyPostViewHolder();
            viewHolder.dateTextView=(TextView)convertView.findViewById(R.id.txtDate_MyPostItem);
            viewHolder.imageView=(ImageView)convertView.findViewById(R.id.img_MyPostItem);
            viewHolder.titleTextView=(TextView)convertView.findViewById(R.id.txtTitle_MyPostItem);
            convertView.setTag(viewHolder);
        }else {
            viewHolder=(MyPostViewHolder)convertView.getTag();
        }
        ObservationEntryObject observationEntryObject=myPostObjects.get(position);
        viewHolder.titleTextView.setText(observationEntryObject.title);
        viewHolder.dateTextView.setText(formatDate(observationEntryObject.date));
        File imgFile = new File(observationEntryObject.photoLocalUri);
        if (imgFile.exists()) {
            Bitmap bitmap = PhotoUtil.getBitmapFromFile(imgFile, maxSize);
            viewHolder.imageView.setImageBitmap(bitmap);
        }
        else
            viewHolder.imageView.setImageResource(R.color.white);
        return convertView;
    }

    //Helper Methods
    private String formatDate(String dateStr){
        String data=dateStr.split("[ ]")[0].trim();
        String[] yearMonthDay=data.split("[ - ]");
        int year=Integer.parseInt(yearMonthDay[0]);
        int month=Integer.parseInt(yearMonthDay[1]);
        int day=Integer.parseInt(yearMonthDay[2]);

        String monthStr="";
        switch (month){
            case 1: monthStr="Jan";break;
            case 2: monthStr="Feb";break;
            case 3: monthStr="Mar";break;
            case 4: monthStr="Apr";break;
            case 5: monthStr="May";break;
            case 6: monthStr="Jun";break;
            case 7: monthStr="July";break;
            case 8: monthStr="Aug";break;
            case 9: monthStr="Sep";break;
            case 10: monthStr="Oct";break;
            case 11: monthStr="Nov";break;
            case 12: monthStr="Dec";break;
        }
        String formatedDate=monthStr+" "+day+"\r\n"+year;
        return formatedDate;
    }
}
