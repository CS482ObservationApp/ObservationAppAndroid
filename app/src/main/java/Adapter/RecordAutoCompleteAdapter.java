package Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import HelperClass.ObservationRecordFinder;
import Model.RecordAutoCompleteItem;
import ca.zhuoliupei.observationapp.R;

/**
 * Created by zhuol on 3/12/2016.
 */
public class RecordAutoCompleteAdapter extends BaseAdapter implements Filterable {
    private Context mContext;
    private List<RecordAutoCompleteItem> resultList = new ArrayList<RecordAutoCompleteItem>();

    public RecordAutoCompleteAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        RecordAutoCompleteItem item=resultList.get(index);
        return String.format("%s(%s)", item.title, item.nid);
    }
    public String getItemNodeID(int index){
        RecordAutoCompleteItem item=resultList.get(index);
        return  item.nid;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.record_autocomplete_item, parent, false);
        }
        ((TextView) convertView.findViewById(R.id.txt_record_autocomplete)).setText(getItem(position));
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    List<RecordAutoCompleteItem> items = findRecords(mContext, constraint.toString());
                    // Assign the data to the FilterResults
                    filterResults.values = items;
                    filterResults.count = items.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    resultList = (List<RecordAutoCompleteItem>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    /**
     * Returns a search result for the given title.
     */
    private List<RecordAutoCompleteItem> findRecords(Context context, String observationTitle) {
        ObservationRecordFinder finder=new ObservationRecordFinder(context);
        return finder.findRecords(observationTitle);
    }
}
