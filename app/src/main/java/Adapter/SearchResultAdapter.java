package Adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import Model.ObservationEntryObject;

/**
 * Created by zhuol on 4/1/2016.
 */
public class SearchResultAdapter extends BaseAdapter {
    private int maxResultCount=1000;//By Default 1000
    private ArrayList<ObservationEntryObject> observationEntryObjects;

    @Override
    public int getCount() {
        return observationEntryObjects.size()+1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
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
        return Integer.parseInt(observationEntryObjects.get(position).nid);
    }

    public void setMaxResultCount(int maxResultCount) {
        this.maxResultCount = maxResultCount;
    }
}
