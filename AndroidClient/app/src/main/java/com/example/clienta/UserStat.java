package com.example.clienta;

public class UserStat {
    String userName;
    String userAverageExerciseDuration;

    String msgEleDiff;

    String msgDistDiff;

    String msgDurationDiff;

    double userAverageElevation;
    double userAverageDistance;

    public UserStat(){};

    public UserStat(String stat , String userName){
        this.userName = userName;
        this.userAverageExerciseDuration =  stat.split(",")[0];
        this.userAverageElevation =  Double.valueOf(stat.split(",")[1]);
        this.userAverageDistance =  Double.valueOf(stat.split(",")[2]);
        this.msgEleDiff = stat.split(",")[3];
        this.msgDistDiff = stat.split(",")[4];
        this.msgDurationDiff = stat.split(",")[5];
    };
}
