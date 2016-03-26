package Adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import HelperClass.NewestObservationCacheManager;
import HelperClass.PhotoUtil;
import Model.ObservationEntryObject;
import ca.zhuoliupei.observationapp.R;

/**
 * Created by zhuol on 2/27/2016.
 */
public class NewestObservationAdapter extends BaseAdapter {
    private final ArrayList<ObservationEntryObject> observationEntryObjects;
    private final Activity context;
    private final int maxSize = 100 * 100;

    static class ObservationViewHolderItem {
        ImageView imageView;
        TextView titleTextView;
        TextView datetimeTextView;
    }

    public NewestObservationAdapter(ArrayList<ObservationEntryObject> observationEntryObjects, Activity context) {
        this.observationEntryObjects = observationEntryObjects;
        this.context = context;
    }

    @Override
    public int getCount() {
        synchronized (observationEntryObjects) {
            return observationEntryObjects.size();
        }
    }

    @Override
    public long getItemId(int position) {
        synchronized (observationEntryObjects) {
            return Long.parseLong(observationEntryObjects.get(position).nid);
        }
    }

    @Override
    public Object getItem(int position) {
        synchronized (observationEntryObjects) {
            return observationEntryObjects.get(position);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return buildObservationView(position, convertView, parent);
    }

    public View buildObservationView(int position, View convertView, ViewGroup parent) {

            ObservationViewHolderItem observationViewHolderItem;
            if (convertView == null || convertView.getVisibility() == View.INVISIBLE) {
                convertView = LayoutInflater.from(context).inflate(R.layout.newest_observation_list_item, null, false);
                observationViewHolderItem = new ObservationViewHolderItem();
                observationViewHolderItem.imageView = (ImageView) convertView.findViewById(R.id.newObsItemImg);
                observationViewHolderItem.titleTextView = (TextView) convertView.findViewById(R.id.newObsItemTitle);
                observationViewHolderItem.datetimeTextView = (TextView) convertView.findViewById(R.id.newObsItemDateTime);
                convertView.setTag(observationViewHolderItem);
            } else {
                observationViewHolderItem = (ObservationViewHolderItem) convertView.getTag();
            }

            observationViewHolderItem.imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            observationViewHolderItem.imageView.setMinimumHeight(350);
            observationViewHolderItem.imageView.setMinimumHeight(350);
            synchronized (observationEntryObjects) {
            File imgFile = new File(observationEntryObjects.get(position).photoLocalUri);
            if (imgFile.exists()) {
                Bitmap bitmap = PhotoUtil.getBitmapFromFile(imgFile, maxSize);
                observationViewHolderItem.imageView.setImageBitmap(bitmap);
            } else observationViewHolderItem.imageView.setImageResource(R.color.white);

            observationViewHolderItem.titleTextView.setText(observationEntryObjects.get(position).title);
            observationViewHolderItem.datetimeTextView.setText(observationEntryObjects.get(position).date.split("[ ]")[0]);

            return convertView;
        }
    }

}
