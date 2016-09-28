package com.example.owen.voicememos;
/*
Owen Brown - 4838488
Chang Ding - 5275821
 */
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AdjustAudioLevel extends AppCompatActivity {
    private int record_id;      //for intent
    private String record_date; //for intent
    private String record_filename; //for intent
    private String record_note; //for intent
    private String filePath;
    private SoundPool soundpool;
    boolean isPlaying = false;
    boolean isLoaded = false;
    private Button _level_play;
    private int soundID;
    private int playingID;
    private float soundRate = 1;
    private Button level_up;
    private Button level_down;
    private TextView level_audioname;
    private ProgressBar speedBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjust_audio_level);
        //get intent
        Intent intent = getIntent();
        //get info.
        record_id = intent.getIntExtra("record_id", 1);
        record_date = intent.getStringExtra("record_date");
        record_filename = intent.getStringExtra("record_filename");
        record_note = intent.getStringExtra("record_note");
        //load the button
        _level_play = (Button) findViewById(R.id.level_playAudio);
        level_up = (Button) findViewById(R.id.level_up);
        level_down = (Button) findViewById(R.id.level_down);
        //load bar
        speedBar = (ProgressBar) findViewById(R.id.progressBar2);
        //set file path
        level_audioname = (TextView) findViewById(R.id.level_filename);
        level_audioname.setText(record_filename);
        filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/VoiceMemos/" + record_filename + ".3gp";
        //play button
        iniPlayButtons();
    }

    //on back key pressed
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle("Exit?")
                .setMessage("Exit and stop playing?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("EXIT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isPlaying) {
                            soundpool.stop(playingID);
                            soundpool.release();
                        }
                        finish();
                    }
                }).create().show();
    }

    //initialize play, up, down buttons
    private void iniPlayButtons() {
        soundpool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundpool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                isLoaded = true;
            }
        });
        soundID = soundpool.load(filePath, 1);
        level_up.setEnabled(false);
        level_down.setEnabled(false);
        //play Button
        _level_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying) {
                    playingID = soundpool.play(soundID, 1, 1, 1, 0, soundRate);
                    isPlaying = true;
                    level_up.setEnabled(true);
                    level_down.setEnabled(true);
                    _level_play.setText("STOP");
                    speedBar.setProgress(5);
                } else {
                    soundpool.stop(playingID);
                    isPlaying = false;
                    level_up.setEnabled(false);
                    level_down.setEnabled(false);
                    soundRate = 1;
                    _level_play.setText("PLAY");

                }
            }
        });
        //speed up button
        level_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (soundRate <= 1.9) {
                    soundRate += 0.1;
                    speedBar.setProgress(speedBar.getProgress() + 1);
                } else {
                    Toast.makeText(AdjustAudioLevel.this, "Reach the Maximum", Toast.LENGTH_SHORT).show();
                }
                soundpool.pause(playingID);
                soundpool.setRate( playingID, soundRate);
                soundpool.resume(playingID);
            }
        });
        //speed down button
        level_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (soundRate >= 0.6) {
                    soundRate -= 0.1;
                    speedBar.setProgress(speedBar.getProgress() - 1);
                } else {
                    Toast.makeText(AdjustAudioLevel.this, "Reach the Minimum", Toast.LENGTH_SHORT).show();
                }

                soundpool.pause(playingID);
                soundpool.setRate( playingID, soundRate);
                soundpool.resume(playingID);
            }
        });
    }
}
