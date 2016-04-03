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
import java.util.zip.Inflater;

import Model.ObservationEntryObject;
import Model.SerializableNameValuePair;
import ca.zhuoliupei.observationapp.R;

/**
 * Created by zhuol on 4/1/2016.
 */
public class SearchResultAdapter extends BaseAdapter {
    private final int TYPE_RESULT_ITEM=0;
    private final int TYPE_FOOTER_ITEM=1;
    private int maxResultCount=1000;//By Default 1000
    private ArrayList<SearchResultObservationEntryObject> observationEntryObjects;
    private Context context;
    public void setMaxResultCount(int maxResultCount) {
        this.maxResultCount = maxResultCount;
    }

    public SearchResultAdapter(ArrayList<SearchResultObservationEntryObject>observationEntryObjects, Context context){
        this.observationEntryObjects=observationEntryObjects;
        this.context=context;
    }
    @Override
    public int getCount() {
        return observationEntryObjects.size()+1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position>=observationEntryObjects.size())
            return TYPE_FOOTER_ITEM;
        return TYPE_RESULT_ITEM;
    }

    @Override
    public Object getItem(int position) {
        if (position<observationEntryObjects.size())
             return observationEntryObjects.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public long getItemNodeId(int position){
        return Integer.parseInt(observationEntryObjects.get(position).nid);
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
        ResultViewHolder viewHolder;
        if (convertView==null){
            convertView= LayoutInflater.from(context).inflate(R.layout.search_result_item,parent,false);
            viewHolder=new ResultViewHolder();
            viewHolder.categoryTextView=(TextView)convertView.findViewById(R.id.txtCategory_SearchResultItem);
            viewHolder.recordTextView=(TextView)convertView.findViewById(R.id.txtRecord_SearchResultItem);
            viewHolder.dateTextView=(TextView)convertView.findViewById(R.id.txtDate_SearchResultItem);
            viewHolder.titleTextView=(TextView)convertView.findViewById(R.id.txtTitle_SearchResultItem);
            viewHolder.imageView=(ImageView)convertView.findViewById(R.id.image_SearchResultActivity);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ResultViewHolder) convertView.getTag();
        }
        SearchResultObservationEntryObject object=observationEntryObjects.get(position);
        viewHolder.categoryTextView.setText(object.category);
        viewHolder.recordTextView.setText(object.record);
        viewHolder.titleTextView.setText(object.title);
        viewHolder.dateTextView.setText(object.date);
        if (object.imgaeBitmap!=null){
            viewHolder.imageView.setImageBitmap(object.imgaeBitmap);
        }
        return convertView;
    }
    private View buildFooterItemView(int position, View convertView, ViewGroup parent){
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
        if (observationEntryObjects.size()<=0)
            viewHolder.relativeLayout.setVisibility(View.INVISIBLE);
        else
            viewHolder.relativeLayout.setVisibility(View.VISIBLE);
        return convertView;
    }
}
