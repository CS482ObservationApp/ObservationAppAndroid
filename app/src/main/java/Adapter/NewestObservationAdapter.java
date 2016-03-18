package Adapter;

import android.app.Activity;
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

import Model.ObservationEntryObject;
import ca.zhuoliupei.observationapp.R;

/**
 * Created by zhuol on 2/27/2016.
 */
public class NewestObservationAdapter extends BaseAdapter {

    private ArrayList<ObservationEntryObject> observationEntryObjects;
    private final Activity context;

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
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.newest_observation_list_item, null, true);
        ImageView imageView=(ImageView)rowView.findViewById(R.id.newObsItemImg);
        imageView.setLayoutParams (new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setMinimumHeight(350);
        imageView.setMinimumHeight(350);
        TextView titleTextView=(TextView)rowView.findViewById(R.id.newObsItemTitle);
        TextView authorTextView=(TextView)rowView.findViewById(R.id.newObsItemDateTime);

        File imgFile = new File(observationEntryObjects.get(position).photoLocalUri);
        if (imgFile.exists())
            imageView.setImageURI(Uri.parse(observationEntryObjects.get(position).photoLocalUri));
        else imageView.setImageResource(R.color.white);

        titleTextView.setText( observationEntryObjects.get(position).title);
        authorTextView.setText( observationEntryObjects.get(position).date);
        return rowView;
    }
}
