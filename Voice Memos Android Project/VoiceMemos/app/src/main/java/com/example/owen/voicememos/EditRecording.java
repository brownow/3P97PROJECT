package com.example.owen.voicememos;
/*
Owen Brown - 4838488
Chang Ding - 5275821
 */
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class EditRecording extends AppCompatActivity {
    private int record_id;      //for intent
    private String record_date; //for intent
    private String record_filename; //for intent
    private String record_note; //for intent
    private Button _edit_audioLevel;
    private Button _edit_trimAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recording);
        //get intent
        Intent intent = getIntent();
        //get info.
        record_id = intent.getIntExtra("record_id", 1);
        record_date = intent.getStringExtra("record_date");
        record_filename = intent.getStringExtra("record_filename");
        record_note = intent.getStringExtra("record_note");
        //setup buttons
        _edit_audioLevel = (Button) findViewById(R.id.edit_audiolevel);
        _edit_trimAudio = (Button) findViewById(R.id.edit_trim);
        //listeners
        _edit_audioLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditRecording.this, AdjustAudioLevel.class);
                intent.putExtra("record_id", record_id);
                intent.putExtra("record_date", record_date);
                intent.putExtra("record_filename", record_filename);
                intent.putExtra("record_note",record_note);
                startActivity(intent);
            }
        });

        _edit_trimAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditRecording.this, TrimAudio.class);
                intent.putExtra("record_id", record_id);
                intent.putExtra("record_date", record_date);
                intent.putExtra("record_filename", record_filename);
                intent.putExtra("record_note",record_note);
                startActivity(intent);
            }
        });
    }
}
