package com.vishrut.vigour.ui.History;

/**
 * Created by helloo on 2/9/2016.
 */
public class ResultList {

    String Date;
    String Distance;
    String Time;
    String Calorie;

    public ResultList(String dateBurned, String timeBurned, String calorieBurned, String distanceBurned) {
        Date= dateBurned;
        Distance= distanceBurned;
        Time = timeBurned;
        Calorie = calorieBurned;
    }

    @Override
    public String toString() {
        return Date;
    }
}
