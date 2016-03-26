package Adapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import HelperClass.PhotoUtil;
import HelperClass.PreferenceUtil;
import Model.SlidingMenuItem;
import ca.zhuoliupei.observationapp.LoginActivity;
import ca.zhuoliupei.observationapp.R;
import ca.zhuoliupei.observationapp.UploadActivity;
import ca.zhuoliupei.observationapp.UserProfileActivity;

/**
 * Created by zhuol on 3/1/2016.
 */
public class SlidingMenuAdapter extends BaseAdapter{
    private final int MAX_USER_IMAGE_VIEW_SIZE=100*100;
    ArrayList<SlidingMenuItem> items;
    Activity context;
    public SlidingMenuAdapter(ArrayList<SlidingMenuItem> items,Activity context){
        this.items=items;
        this.context=context;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return  items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (items.get(position).itemType){
            case UPLOAD_ITEM:case USER_GUIDE_ITEM:case SEARCH_ITEM:
                return constructNormalItemView(items.get(position));
            case USER_ACCOUNT_ITEM: return constructUserAccountItemView(items.get(position));
            case LOGIN_ITEM:return constructLoginItemView(items.get(position));
            default:return constructNormalItemView(items.get(position));
        }
    }
    private View constructNormalItemView(SlidingMenuItem item){
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.sliding_menu_normal_item, null, false);
        TextView textView=(TextView)rowView.findViewById(R.id.txtText_SlidingMenu);
        textView.setText(item.text);

        return rowView;
    }
    private View constructUserAccountItemView(SlidingMenuItem item){
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.sliding_menu_account_item, null, true);
        ImageView accountIV=(ImageView)rowView.findViewById(R.id.imgUser_SlidingMenu);
        File imgFile = new File(PreferenceUtil.getCurrentUserPictureLocalUri(context));
        if (imgFile.exists())
             accountIV.setImageBitmap(PhotoUtil.getBitmapFromFile(imgFile, MAX_USER_IMAGE_VIEW_SIZE));
        else
            accountIV.setImageResource(R.drawable.icon_user_default);
        TextView nameTV=(TextView)rowView.findViewById(R.id.txtName_SlidingMenu);
        nameTV.setText(PreferenceUtil.getCurrentUser(context));
        TextView locationTV=(TextView)rowView.findViewById(R.id.txtLocation_SlidingMenu);
        String address=PreferenceUtil.getCurrentUserLocation1(context);
        String[] addressArray=address.split("[\r\n]");
        address="";
        for (String str:addressArray){
            if (str.trim().isEmpty())
                continue;
            if (str.substring(str.length()-1,str.length()).equals(","))
                str=str.substring(0,str.length()-1);
            address= address+str+",";
        }
        address=address.substring(0,address.length()-1);
        locationTV.setText(address);

        return rowView;
    }
    private View constructLoginItemView(SlidingMenuItem item){
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.sliding_menu_login_item, null, true);
        return rowView;
    }
}
