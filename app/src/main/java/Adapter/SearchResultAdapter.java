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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import Model.SearchResultObservationEntryObject;
import ca.zhuoliupei.observationapp.R;

/**
 * Created by zhuol on 4/1/2016.
 */
public class SearchResultAdapter extends BaseAdapter {
    private final int TYPE_RESULT_ITEM=0;
    private final int TYPE_FOOTER_ITEM=1;
    private final int maxResultCount;
    private ArrayList<SearchResultObservationEntryObject> observationEntryObjects;
    private Context context;

    public SearchResultAdapter(ArrayList<SearchResultObservationEntryObject>observationEntryObjects, Context context,int maxResultCount){
        this.observationEntryObjects=observationEntryObjects;
        this.context=context;
        this.maxResultCount=maxResultCount;
    }
    @Override
    public int getCount() {
        synchronized (observationEntryObjects) {
            return observationEntryObjects.size() + 1;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        synchronized (observationEntryObjects) {
            if (position >= observationEntryObjects.size())
                return TYPE_FOOTER_ITEM;
            return TYPE_RESULT_ITEM;
        }
    }

    @Override
    public Object getItem(int position) {
        synchronized (observationEntryObjects) {
            if (position < observationEntryObjects.size())
                return observationEntryObjects.get(position);
            else
                return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public long getItemNodeId(int position){
        synchronized (observationEntryObjects) {
            return Integer.parseInt(observationEntryObjects.get(position).nid);
        }
    }

    static class ResultViewHolder{
        TextView titleTextView;
        TextView recordTextView;
        TextView categoryTextView;
        TextView dateTextView;
        ImageView imageView;
    }
    static class FooterViewHolder{
        RelativeLayout relativeLayout;
        ImageView imageView;
        TextView textView;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)){
            case TYPE_RESULT_ITEM: return buildResultItemView(position,convertView,parent);
            default: return buildFooterItemView(position,convertView,parent);
        }
    }

    private View buildResultItemView(int position, View convertView, ViewGroup parent){
        synchronized (observationEntryObjects) {
            ResultViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.search_result_item, parent, false);
                viewHolder = new ResultViewHolder();
                viewHolder.categoryTextView = (TextView) convertView.findViewById(R.id.txtCategory_SearchResultItem);
                viewHolder.recordTextView = (TextView) convertView.findViewById(R.id.txtRecord_SearchResultItem);
                viewHolder.dateTextView = (TextView) convertView.findViewById(R.id.txtDate_SearchResultItem);
                viewHolder.titleTextView = (TextView) convertView.findViewById(R.id.txtTitle_SearchResultItem);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image_SearchResultActivity);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ResultViewHolder) convertView.getTag();
            }
            SearchResultObservationEntryObject object = observationEntryObjects.get(position);
            viewHolder.categoryTextView.setText(object.category);
            viewHolder.recordTextView.setText(object.record);
            viewHolder.titleTextView.setText(object.title);
            viewHolder.dateTextView.setText(object.date);
            viewHolder.imageView.setImageBitmap(object.imgaeBitmap);
            return convertView;
        }
    }
    private View buildFooterItemView(int position, View convertView, ViewGroup parent){
        synchronized (observationEntryObjects) {
            FooterViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.scrollview_footer_loading, parent, false);
                viewHolder = new FooterViewHolder();
                viewHolder.relativeLayout = (RelativeLayout) convertView.findViewById(R.id.scrollview_footer);
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imgLoading_ScrollViewFooterLoading);
                viewHolder.textView = (TextView) convertView.findViewById(R.id.txtLoading_ScrollViewFooterLoading);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (FooterViewHolder) convertView.getTag();
            }
            if (observationEntryObjects.size() <= 0)
                viewHolder.relativeLayout.setVisibility(View.INVISIBLE);
            else
                viewHolder.relativeLayout.setVisibility(View.VISIBLE);

            return convertView;
        }
    }
}
