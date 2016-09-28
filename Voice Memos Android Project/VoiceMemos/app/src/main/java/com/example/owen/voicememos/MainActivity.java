package com.example.owen.voicememos;
/*
Owen Brown - 4838488
Chang Ding - 5275821
 */
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String memosPath;
    private File file[];
    private MyDatabaseHelper dbHelper;  //database helper
    private ImageButton _startRecording;
    private ListView listview;
    private List<singleRecording> recordingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listview = (ListView) findViewById(R.id.main_audioList);
        //initialize database
        //version set to 1, so it won't recreate if exist
        dbHelper = new MyDatabaseHelper(this, "RecordingsData.db",null,1);
        //the following two lines of codes did nothing.
        dbHelper.getWritableDatabase();
        dbHelper.close();
        //create the directory for storing recordings
        setDirectory("VoiceMemos");
        //set Recordings path
        memosPath = Environment.getExternalStorageDirectory().toString()+"/VoiceMemos";
        //get file list
        getMemoFilesList();
        //initialize record button
        setRecordButton();
        //display the recording list
        displayRecordingList();
    }

    @Override
    public void onResume(){
        super.onResume();
        //refresh list
        displayRecordingList();
    }

    //display recording list
    //get the info. from database
    private void displayRecordingList(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //set cursor
        Cursor cursor = db.query("RECORDINGS",null,null,null,null,null,null);
        //store info. into the list
        recordingList = new ArrayList<singleRecording>();
        if(cursor.moveToFirst()){
            do{
                int rd_id = cursor.getInt(cursor.getColumnIndex("id"));
                String date = cursor.getString(cursor.getColumnIndex("date"));
                String filename = cursor.getString(cursor.getColumnIndex("filename"));
                String note = cursor.getString(cursor.getColumnIndex("note"));
                recordingList.add(new singleRecording(rd_id,date,filename,note));
            }while(cursor.moveToNext());
        }
        cursor.close();
        //reading info ends.
        //set adapter for viewList
        RecordingAdapter adapter = new RecordingAdapter(MainActivity.this, R.layout.list_example,recordingList);
        //display
        listview.setAdapter(adapter);
        db.close();
        //items on click listener
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                singleRecording selectedRecording = recordingList.get(position);
                Intent intent = new Intent(MainActivity.this, PlayRecording.class);
                intent.putExtra("record_id", selectedRecording.getId());
                intent.putExtra("record_date", selectedRecording.getDate());
                intent.putExtra("record_filename", selectedRecording.getFilename());
                intent.putExtra("record_note",selectedRecording.getNote());
                startActivity(intent);
            }
        });
    }

    //create the directory
    private void setDirectory(String path) {
        File file = new File(Environment.getExternalStorageDirectory(), path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.d("test1", "Error occurs when creating directory");
            }
        }
    }

    //initialize start recording button
    private void setRecordButton(){
        //create object
        _startRecording = (ImageButton) findViewById(R.id.main_startRecording);
        //onClickListener
        _startRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecordVoice.class);
                startActivity(intent);
            }
        });
    }

    //get recordings list
    private void getMemoFilesList(){
        File f = new File(memosPath);
        file = f.listFiles();
    }
}
