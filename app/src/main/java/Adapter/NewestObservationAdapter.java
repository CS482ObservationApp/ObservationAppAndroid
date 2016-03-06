package Adapter;

import android.app.ActionBar;
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

import Model.ObservationObject;
import ca.zhuoliupei.observationapp.R;

/**
 * Created by zhuol on 2/27/2016.
 */
public class NewestObservationAdapter extends BaseAdapter {

    private ArrayList<ObservationObject> observationObjects;
    private final Activity context;

    public NewestObservationAdapter(ArrayList<ObservationObject> observationObjects,Activity context){
        this.observationObjects=observationObjects;
        this.context=context;
    }
    @Override
    public int getCount() {
        return observationObjects.size();
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(observationObjects.get(position).nid);
    }

    @Override
    public Object getItem(int position) {
        return observationObjects.get(position);
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

        File imgFile = new File(observationObjects.get(position).photoLocalUri);
        if (imgFile.exists())
            imageView.setImageURI(Uri.parse(observationObjects.get(position).photoLocalUri));
        else imageView.setImageResource(R.color.white);

        titleTextView.setText( observationObjects.get(position).title);
        authorTextView.setText( observationObjects.get(position).date);
        return rowView;
    }
}
