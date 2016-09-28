package com.example.owen.voicememos;
/*
Owen Brown - 4838488
Chang Ding - 5275821
 */
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;

public class TrimAudio extends AppCompatActivity {
    private int record_id;      //for intent
    private String record_date; //for intent
    private String record_filename; //for intent
    private String record_note; //for intent
    private TextView trim_filename;
    private TextView trim_date;
    private EditText trim_start_text;
    private EditText trim_end_text;
    private Button trim_trimButton;
    private Button trim_cancelButton;
    private String filePath;
    private SoundFile mSoundFile;
    private File mFile;
    private int mSampleRate;    //for frame
    private int mSamplesPerFrame;   //for frame
    private SeekBar seekStart;
    private SeekBar seekEnd;
    private MediaPlayer rMediaPlayer;  //for seekBar
    private int mediaDuration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim_audio);
        //get intent
        Intent intent = getIntent();
        //get info.
        record_id = intent.getIntExtra("record_id", 1);
        record_date = intent.getStringExtra("record_date");
        record_filename = intent.getStringExtra("record_filename");
        record_note = intent.getStringExtra("record_note");
        //set file path
        filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/VoiceMemos/" + record_filename + ".3gp";
        //set filename
        trim_filename = (TextView) findViewById(R.id.trim_filename);
        trim_filename.setText(record_filename);
        //set date
        trim_date = (TextView) findViewById(R.id.trim_date);
        trim_date.setText(record_date);
        //find view by id
        trim_start_text = (EditText) findViewById(R.id.trim_start);
        trim_start_text.setEnabled(false);
        trim_end_text = (EditText) findViewById(R.id.trim_end);
        trim_end_text.setEnabled(false);
        trim_trimButton = (Button) findViewById(R.id.trim_trimButton);
        trim_cancelButton = (Button) findViewById(R.id.trim_Cancel);
        //load the file as SoundFile
        mFile = new File(filePath);
        try {
            mSoundFile = SoundFile.create(mFile.getAbsolutePath());
        } catch (final Exception e) {
            Log.d("test1", "exception when reading file");
        }
        //get sample rate
        mSampleRate = mSoundFile.getSampleRate();
        //get samples per frame
        mSamplesPerFrame = mSoundFile.getSamplesPerFrame();
        //set trim on click listener
        iniTrim();
        //set cancel on click listener
        iniCancel();
        //initialize seekbar
        iniSeekBars();
    }


    //initialize seekbars
    private void iniSeekBars() {
        seekStart = (SeekBar) findViewById(R.id.trim_seekStart);
        seekEnd = (SeekBar) findViewById(R.id.trim_seekEnd);
        prepareMedia();
        //the length of the audio in seconds
        mediaDuration = rMediaPlayer.getDuration() / 1000;
        seekEnd.setMax(mediaDuration);
        seekStart.setMax(0);
        //start time seekbar
        seekStart.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int _progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                _progress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                trim_start_text.setText(secondsToString(_progress));
            }
        });

        //Ends time seekbar
        seekEnd.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int _progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                _progress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekStart.setMax(_progress);
                if (_progress < stringToSeconds(trim_start_text.getText().toString())) {
                    trim_start_text.setText(secondsToString(_progress));
                }
                trim_end_text.setText(secondsToString(_progress));
            }
        });
    }

    //initialize trim button
    private void iniTrim() {
        trim_trimButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //trim and save
                //get trim time
                double startTime = stringToSeconds(trim_start_text.getText().toString());
                double endTime = stringToSeconds(trim_end_text.getText().toString());
                //convert to frame
                final int startFrame = secondsToFrames(startTime);
                final int endFrame = secondsToFrames(endTime);
                File outFile = new File(filePath);
                try {
                    // Write the new file
                    mSoundFile.WriteFile(outFile, startFrame, endFrame - startFrame);
                } catch (Exception e) {
                    Log.d("test1", "exception when create new file");
                }
                Toast.makeText(TrimAudio.this, "Trimmed", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }


    //initialize cancel button
    private void iniCancel() {
        trim_cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private int secondsToFrames(double seconds) {
        return (int) (1.0 * seconds * mSampleRate / mSamplesPerFrame + 0.5);
    }


    //convert string to seconds
    //00:05 -->5.0
    private double stringToSeconds(String s) {
        double savedMin = Double.parseDouble(s.substring(0, 2));
        double savedSec = Double.parseDouble(s.substring(3, 5));
        savedMin = savedMin * 60;
        return savedMin + savedSec;
    }

    //convert second to String
    private String secondsToString(double d) {
        String timeInString = null;
        int savedSec = (int) Math.floor(d % 60);
        int savedMin = (int) Math.floor((d - savedSec) / 60);
        timeInString = String.format("%02d:%02d", savedMin, savedSec);
        return timeInString;
    }

    //prepare audio
    private void prepareMedia() {
        rMediaPlayer = new MediaPlayer();
        rMediaPlayer.reset();
        try {
            rMediaPlayer.setDataSource(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            rMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
