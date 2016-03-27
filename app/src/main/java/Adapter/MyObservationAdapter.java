package Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import HelperClass.PhotoUtil;
import HelperClass.PreferenceUtil;
import Model.ObservationEntryObject;
import ca.zhuoliupei.observationapp.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by zhuol on 3/20/2016.
 */
public class MyObservationAdapter extends BaseAdapter {
    final ArrayList<ObservationEntryObject> myPostObjects;
    Context context;
    final int maxSize = 100 * 100;
    final int HEADER_TYPE = 0;
    final int MYPOST_TYPE = 1;
    final int FOOTER_TYPE = 2;

    public MyObservationAdapter(ArrayList<ObservationEntryObject> observationEntryObjects, Context context) {
        this.context = context;
        myPostObjects =observationEntryObjects;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return HEADER_TYPE;
        if (position>myPostObjects.size())
            return FOOTER_TYPE;
        return MYPOST_TYPE;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        synchronized (myPostObjects) {
            return myPostObjects.get(position);
        }
    }

    @Override
    public int getCount() {
        synchronized (myPostObjects) {
            return myPostObjects.size() + 2;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        synchronized (myPostObjects) {
            if (getItemViewType(position) == HEADER_TYPE)
                return buildHeaderItem(position,convertView,parent);
            if (getItemViewType(position) == FOOTER_TYPE)
                return buildFooterItem(position,convertView,parent);
            else
                return buildMyPostItem(position,convertView,parent);
        }
    }

    static class HeaderViewHolder {
        TextView userNameTextView;
        ImageView userImg;
        TextView spaceHolderTextView;
    }
    static class MyPostViewHolder {
        TextView dateTextView;
        ImageView imageView;
        TextView titleTextView;
    }
    static class FooterViewHolder{
        RelativeLayout relativeLayout;
        ImageView imageView;
        TextView textView;
    }
    private View buildHeaderItem(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.mypost_header_item, parent, false);
            viewHolder = new HeaderViewHolder();
            viewHolder.userNameTextView=(TextView)convertView.findViewById(R.id.userName_MyPostHeader);
            viewHolder.userImg = (ImageView) convertView.findViewById(R.id.userImage_MyPostHeader);
            viewHolder.spaceHolderTextView = (TextView) convertView.findViewById(R.id.txtSpaceHolder_MyPostHeader);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (HeaderViewHolder) convertView.getTag();
        }
        //Set User Profile Image
        String imgLocation = PreferenceUtil.getCurrentUserPictureLocalUri(context);
        File imgFile = new File(imgLocation);
        if (imgFile.exists()) {
            Bitmap bitmap = PhotoUtil.getBitmapFromFile(imgFile, maxSize);
            viewHolder.userImg.setImageBitmap(bitmap);
        } else
            viewHolder.userImg.setImageResource(R.drawable.icon_user_default);
        String userName=PreferenceUtil.getCurrentUser(context);
        viewHolder.userNameTextView.setText(userName);
        //Set user name
        if (myPostObjects.size() != 0)
            viewHolder.spaceHolderTextView.setVisibility(View.GONE);

        //Hide the listview item on pressed effect and the sound effect
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DO NOTHING....
            }
        });
        convertView.setSoundEffectsEnabled(false);

        return convertView;
    }
    private View buildMyPostItem(int position, View convertView, ViewGroup parent) {
        MyPostViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.mypost_item, parent, false);
            viewHolder = new MyPostViewHolder();
            viewHolder.dateTextView = (TextView) convertView.findViewById(R.id.txtDate_MyPostItem);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.img_MyPostItem);
            viewHolder.titleTextView = (TextView) convertView.findViewById(R.id.txtTitle_MyPostItem);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (MyPostViewHolder) convertView.getTag();
        }
        ObservationEntryObject observationEntryObject = myPostObjects.get(position-1);
        viewHolder.titleTextView.setText(observationEntryObject.title);
        viewHolder.dateTextView.setText(formatDate(observationEntryObject.date));
        File imgFile = new File(observationEntryObject.photoLocalUri);
        if (imgFile.exists()) {
            Bitmap bitmap = PhotoUtil.getBitmapFromFile(imgFile, maxSize);
            viewHolder.imageView.setImageBitmap(bitmap);
        } else
            viewHolder.imageView.setImageResource(R.color.white);
        return convertView;
    }
    private View buildFooterItem(int position, View convertView, ViewGroup parent){
        FooterViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.scrollview_footer_loading, parent, false);
            viewHolder = new FooterViewHolder();
            viewHolder.relativeLayout=(RelativeLayout) convertView.findViewById(R.id.scrollview_footer);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imgLoading_ScrollViewFooterLoading);
            viewHolder.textView = (TextView) convertView.findViewById(R.id.txtLoading_ScrollViewFooterLoading);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (FooterViewHolder) convertView.getTag();
        }
        if (myPostObjects.size()<=0)
            viewHolder.relativeLayout.setVisibility(View.INVISIBLE);
        else
            viewHolder.relativeLayout.setVisibility(View.VISIBLE);
        return convertView;
    }

    //Helper Methods
    private String formatDate(String dateStr) {
        String data = dateStr.split("[ ]")[0].trim();
        String[] yearMonthDay = data.split("[-]");
        int year = Integer.parseInt(yearMonthDay[0]);
        int month = Integer.parseInt(yearMonthDay[1]);
        int day = Integer.parseInt(yearMonthDay[2]);

        String monthStr = "";
        switch (month) {
            case 1:
                monthStr = "Jan";
                break;
            case 2:
                monthStr = "Feb";
                break;
            case 3:
                monthStr = "Mar";
                break;
            case 4:
                monthStr = "Apr";
                break;
            case 5:
                monthStr = "May";
                break;
            case 6:
                monthStr = "Jun";
                break;
            case 7:
                monthStr = "July";
                break;
            case 8:
                monthStr = "Aug";
                break;
            case 9:
                monthStr = "Sep";
                break;
            case 10:
                monthStr = "Oct";
                break;
            case 11:
                monthStr = "Nov";
                break;
            case 12:
                monthStr = "Dec";
                break;
        }
        String formatedDate = monthStr + " " + day + "\r\n" + year;
        return formatedDate;
    }
}
