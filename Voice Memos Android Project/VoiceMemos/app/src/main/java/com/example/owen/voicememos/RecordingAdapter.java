package com.example.owen.voicememos;
/*
Owen Brown - 4838488
Chang Ding - 5275821
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * rewrite arrayadapter
 */
public class RecordingAdapter extends ArrayAdapter<singleRecording>{
    private int resourceId;
    public RecordingAdapter(Context context, int textViewResourceId, List<singleRecording> object){
        super(context, textViewResourceId, object);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        singleRecording recording = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);
        TextView date = (TextView) view.findViewById(R.id.date_entry);
        TextView filename = (TextView) view.findViewById(R.id.filename_entry);
        date.setText(recording.getDate());
        filename.setText(recording.getFilename());
        return view;
    }
}
