/**	 ObservationApp, Copyright 2016, University of Prince Edward Island,
 550 University Avenue, C1A4P3,
 Charlottetown, PE, Canada
 *
 * 	 @author Kent Li <zhuoli@upei.ca>
 *
 *   This file is part of ObservationApp.
 *
 *   ObservationApp is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CycleTracks is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CycleTracks.  If not, see <http://www.gnu.org/licenses/>.
 */

package Adapter;

import android.app.Activity;
import android.graphics.Bitmap;
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
