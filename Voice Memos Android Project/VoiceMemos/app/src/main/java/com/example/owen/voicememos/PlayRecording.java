package com.example.owen.voicememos;
/*
Owen Brown - 4838488
Chang Ding - 5275821
 */
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;

public class PlayRecording extends AppCompatActivity {
    private int record_id;      //for intent
    private String record_date; //for intent
    private String record_filename; //for intent
    private String record_note; //for intent
    //buttons and textviews
    private TextView _play_filename_text;
    private TextView _play_date_text;
    private TextView _play_status_text;   //playing OR paused
    private TextView _play_timer_text;
    private long startTime = 0;     //for timer
    private Handler timerHandler;   //for timer
    private TextView _play_note_text;
    private ImageButton _play_delete_button;
    private ImageButton _play_playPause_button;
    private ImageButton _play_edit_button;
    private MyDatabaseHelper dbHelper;
    private String outputFile = null;
    private boolean isPlaying = false; //playing status
    private MediaPlayer rMediaPlayer;
    private SeekBar play_seekTo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_recording);
        dbHelper = new MyDatabaseHelper(this, "RecordingsData.db",null,1);
        //get intent
        Intent intent = getIntent();
        //get info.
        record_id = intent.getIntExtra("record_id", 1);
        record_date = intent.getStringExtra("record_date");
        record_filename = intent.getStringExtra("record_filename");
        record_note = intent.getStringExtra("record_note");
        //create buttons and textviews
        createButtonsAndTextViews();
        //set filename textview
        _play_filename_text.setText(record_filename);
        //set date textview
        _play_date_text.setText(record_date);
        //set player status;
        _play_status_text.setText("Paused");
        //set timer textview
        _play_timer_text.setText("0:00");
        //set note textview
        _play_note_text.setText(record_note);
        //scrolling
        _play_note_text.setMovementMethod(new ScrollingMovementMethod());
        //file location
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath()+"/VoiceMemos/"+record_filename+".3gp";
        //set delete button
        iniDeleteButton();
        //prepare media
        prepareMedia();
        //set play button
        iniPlayButton();
        //set edit button
        iniEditButton();
        //prepare SeekBar
        prepareSeekTo();

    }


    //prepare seekBar
    private void prepareSeekTo(){
        play_seekTo = (SeekBar) findViewById(R.id.play_seekBar);
        play_seekTo.setMax(rMediaPlayer.getDuration());
        play_seekTo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int _progress = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                _progress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(isPlaying){
                    rMediaPlayer.seekTo(_progress);
                }
            }
        });
    }
    //prepare audio
    private void prepareMedia(){
        rMediaPlayer = new MediaPlayer();
        rMediaPlayer.reset();
        try {
            rMediaPlayer.setDataSource(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            rMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onResume(){
        super.onResume();
        prepareMedia();
        isPlaying = false;
        _play_status_text.setText("Paused");
        _play_playPause_button.setBackgroundResource(R.drawable.rec_start);
    }
    @Override
    //onPause, stop playing
    protected void onPause(){
        super.onPause();
        rMediaPlayer.stop();
    }

    //initialize delete button
    private void iniDeleteButton(){
        _play_delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //delete info in database
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete("RECORDINGS","id = "+record_id,null);
                Toast.makeText(PlayRecording.this, "Recording Deleted",Toast.LENGTH_SHORT).show();
                db.close();
                //delete the file
                File file = new File(outputFile);
                file.delete();
                finish();
            }
        });
    }


    //initialize Play button
    private void iniPlayButton(){
        //runs without a timer by reposting this handler at the end of the runnable
        timerHandler = new Handler();
        final Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = rMediaPlayer.getCurrentPosition();
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                _play_timer_text.setText(String.format("%d:%02d", minutes, seconds));
                play_seekTo.setProgress((int)millis);
                timerHandler.postDelayed(this, 500);
            }
        };

        _play_playPause_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isPlaying){
                    //restart timer
                    timerHandler.postDelayed(timerRunnable, 0);
                    //onCompletionListener
                    rMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            isPlaying = false;
                            _play_playPause_button.setBackgroundResource(R.drawable.rec_start);
                        }
                    });
                    //play the audio
                    rMediaPlayer.start();
                    isPlaying = true;
                    //change label
                    _play_status_text.setText("Playing");
                    //change paused state

                    //change background
                    _play_playPause_button.setBackgroundResource(R.drawable.rec_pause);
                }
                else {
                    //stop timer
                    timerHandler.removeCallbacks(timerRunnable);
                    //pause audio
                    rMediaPlayer.pause();
                    isPlaying = false;
                    // change paused state

                    //change label and background
                    _play_status_text.setText("Paused");
                    _play_playPause_button.setBackgroundResource(R.drawable.rec_start);
                }
            }
        });
    }

    //initialize Edit button
    private void iniEditButton(){
        _play_edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlayRecording.this, EditRecording.class);
                intent.putExtra("record_id", record_id);
                intent.putExtra("record_date", record_date);
                intent.putExtra("record_filename", record_filename);
                intent.putExtra("record_note",record_note);
                startActivity(intent);
            }
        });
    }

    //buttons and TextViews creation
    private void createButtonsAndTextViews(){
        _play_filename_text = (TextView) findViewById(R.id.play_filename_text);
        _play_date_text = (TextView) findViewById(R.id.play_date_text);
        _play_status_text = (TextView) findViewById(R.id.play_status_text);
        _play_timer_text = (TextView) findViewById(R.id.play_time_text);
        _play_note_text = (TextView) findViewById(R.id.play_note_text);
        _play_delete_button = (ImageButton) findViewById(R.id.play_delete_button);
        _play_playPause_button = (ImageButton) findViewById(R.id.play_play_pause_button);
        _play_edit_button =(ImageButton) findViewById(R.id.play_edit_button);
    }
}
