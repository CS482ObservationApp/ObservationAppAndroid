package Adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import HelperClass.PhotoUtil;
import Model.ObservationEntryObject;
import ca.zhuoliupei.observationapp.R;

/**
 * Created by zhuol on 2/27/2016.
 */
public class NewestObservationAdapter extends BaseAdapter {

    private ArrayList<ObservationEntryObject> observationEntryObjects;
    private final Activity context;
    private final int maxSize=100*100;
    static class ViewHolderItem {
        ImageView imageView;
        TextView titleTextView;
        TextView datetimeTextView;
    }
    public NewestObservationAdapter(ArrayList<ObservationEntryObject> observationEntryObjects,Activity context){
        this.observationEntryObjects = observationEntryObjects;
        this.context=context;
    }
    @Override
    public int getCount() {
        return observationEntryObjects.size();
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(observationEntryObjects.get(position).nid);
    }

    @Override
    public Object getItem(int position) {
        return observationEntryObjects.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolderItem;
        if (convertView==null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.newest_observation_list_item, null, false);
            viewHolderItem = new ViewHolderItem();
            viewHolderItem.imageView = (ImageView) convertView.findViewById(R.id.newObsItemImg);
            viewHolderItem.titleTextView = (TextView) convertView.findViewById(R.id.newObsItemTitle);
            viewHolderItem.datetimeTextView = (TextView) convertView.findViewById(R.id.newObsItemDateTime);
            convertView.setTag(viewHolderItem);
        }else {
            viewHolderItem=(ViewHolderItem)convertView.getTag();
        }
        viewHolderItem.imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        viewHolderItem.imageView.setMinimumHeight(350);
        viewHolderItem.imageView.setMinimumHeight(350);

        File imgFile = new File(observationEntryObjects.get(position).photoLocalUri);
        if (imgFile.exists()) {
            Bitmap bitmap = PhotoUtil.getBitmapFromFile(imgFile, maxSize);
            viewHolderItem.imageView.setImageBitmap(bitmap);
        }
        else viewHolderItem.imageView.setImageResource(R.color.white);

        viewHolderItem.titleTextView.setText( observationEntryObjects.get(position).title);
        viewHolderItem.datetimeTextView.setText(observationEntryObjects.get(position).date);
        return convertView ;
    }
}
