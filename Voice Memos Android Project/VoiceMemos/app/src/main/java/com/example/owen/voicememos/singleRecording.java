package com.example.owen.voicememos;

/*
Owen Brown - 4838488
Chang Ding - 5275821
 */
/**
 * a Recording class
 * includes all the infomation of the Recording
 * id, date, filename, note
 */
public class singleRecording {
    private int _id;
    private String _date;
    private String _filename;
    private String _note;


    public singleRecording(int id,String Date, String filename, String Note){
        _id = id;
        _date = Date;
        _filename = filename;
        _note = Note;
    }

    public int getId(){
        return _id;
    }
    public String getDate(){
        return _date;
    }

    public String getFilename(){
        return _filename;
    }

    public String getNote(){
        return _note;
    }

}
