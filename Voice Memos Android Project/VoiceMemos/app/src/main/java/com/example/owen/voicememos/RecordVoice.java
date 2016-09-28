package com.example.owen.voicememos;
/*
Owen Brown - 4838488
Chang Ding - 5275821
 */
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;

public class RecordVoice extends AppCompatActivity {
    private Button _record_insertTime;
    private boolean _record_isRecording;
    private TextView _record_timerView;     //timer display
    private long startTime = 0;             //for timer
    private Handler timerHandler;           //for timer
    private MediaRecorder myAudioRecorder;
    private Button _record_finishRecording;
    private String outputFile = null;
    private String fileName = null;
    private MyDatabaseHelper dbHelper;      //for database
    private EditText record_note;           //for database
    private Thread updateNoise;             //for displaying noise
    private ProgressBar bar;                //for displaying noise

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_voice);
        //object creation
        myAudioRecorder = new MediaRecorder();
        record_note = (EditText) findViewById(R.id.record_note);
        bar = (ProgressBar) findViewById(R.id.progressBar);
        // Adjust progress bar to make it more visible
        bar.setScaleY(4f);
        //dialog creation, read file name
        createDialogBeforeStart();
    }

    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle("Exit?")
                .setMessage("Exit and saving file?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myAudioRecorder.stop();
                        _record_isRecording = false;
                        // Stop thread
                        try {
                            updateNoise.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        myAudioRecorder.release();
                        myAudioRecorder = null;
                        record_writeDatabase();
                        Toast.makeText(getApplicationContext(), "Audio recorded successfully", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }).create().show();
    }

    //get file name
    private void createDialogBeforeStart() {
        LayoutInflater li = LayoutInflater.from(this);
        View promptView = li.inflate(R.layout.prompt_dialog, null);
        AlertDialog.Builder alterDialogBuilder = new AlertDialog.Builder(this);
        alterDialogBuilder.setView(promptView);
        final EditText userInput = (EditText) promptView.findViewById(R.id.prompt_enter_filename);
        alterDialogBuilder.setCancelable(false).setPositiveButton("CONFIRM",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fileName = userInput.getText().toString();
                        if (fileName.compareTo("") == 0) {
                            fileName = "NewRecording";
                        }
                        //output path
                        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/VoiceMemos/" + fileName + ".3gp";
                        initialAudioRecorder();
                        //start recording automatically
                        startRecording();
                        //initialize "done" button
                        set_record_finishRecording();
                        //initialize start_pause_button
                        set_record_start_pause_button();
                        //initialize insert time button
                        set_record_insertTime();
                    }
                });

        AlertDialog alertDialog = alterDialogBuilder.create();
        alertDialog.show();
    }

    //initialize insert time button
    private void set_record_insertTime() {
        _record_insertTime = (Button) findViewById(R.id.record_insert);
        _record_insertTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempTime = _record_timerView.getText().toString();
                String tempText = "\n@" + tempTime + ":\n" + record_note.getText();
                record_note.setText(tempText);
            }
        });
    }

    //write info. into database
    private void record_writeDatabase() {
        dbHelper = new MyDatabaseHelper(this, "RecordingsData.db", null, 1);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //preparing content value
        ContentValues values = new ContentValues();
        values.put("date", java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
        values.put("filename", fileName);
        values.put("note", record_note.getText().toString());
        //insert info.
        db.insert("RECORDINGS", null, values);
        db.close();
        values.clear();
    }

    //start recording the audio
    private void startRecording() {
        try {
            myAudioRecorder.prepare();
            myAudioRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        _record_isRecording = true;
        // Start Noise checker thread (which updates progress bar)
        updateNoise = new Thread(checkNoise);
        updateNoise.start();
        Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
    }

    //initialize the start_pause button
    private void set_record_start_pause_button() {
        //setting for timer starts
        _record_timerView = (TextView) findViewById(R.id.record_TimerView);
        //runs without a timer by reposting this handler at the end of the runnable
        timerHandler = new Handler();
        final Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                _record_timerView.setText(String.format("%d:%02d", minutes, seconds));

                timerHandler.postDelayed(this, 500);
            }
        };
        //timer starts immediately
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
    }

    //initialize the "done" button
    private void set_record_finishRecording() {
        //object
        _record_finishRecording = (Button) findViewById(R.id.record_done_button);
        _record_finishRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAudioRecorder.stop();
                _record_isRecording = false;
                // Stop thread
                try {
                    updateNoise.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                myAudioRecorder.release();
                myAudioRecorder = null;
                record_writeDatabase();
                Toast.makeText(getApplicationContext(), "Audio recorded successfully", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    //prepare for audio recording
    //set mic, 3pg, AMR_NB, and output location
    private void initialAudioRecorder() {
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myAudioRecorder.setOutputFile(outputFile);
    }

    //convert the time in the timer view to milliSecond
    //example 0:01 --> 1000
    //it will crash if the time is over 10 mins, just minor issue, fix it later.
    private long getTimeFromTimerBox() {
        String savedTimeString = _record_timerView.getText().toString();
        Long savedMin = Long.valueOf(savedTimeString.substring(0, 1));
        Long savedSec = Long.valueOf(savedTimeString.substring(2, 4));
        savedMin = savedMin * 60 * 1000;
        savedSec = savedSec * 1000;
        return savedMin + savedSec;
    }

    // Runnable to poll decibels and post the data to the progress bar
    private final Runnable checkNoise = new Runnable() {
        @Override
        public void run() {
            Log.v("Noise Level thread", "Starting thread ...");
            while (_record_isRecording) {
                try {
                    // check every 0.1 sec
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                double decib = getDecibels();
                // Values range -21 to 21, so adjust to show on prog. bar
                bar.setProgress((int) Math.floor(decib) + 21);
            }
        }
    };

    // Use the amplitude being picked up in the current recording and convert it to decibels
    public double getDecibels() {
        if (myAudioRecorder != null)
            //Convert amplitude into decibels
            return 20 * Math.log10(myAudioRecorder.getMaxAmplitude() / 2700.0);
        else
            return 0;
    }
}
