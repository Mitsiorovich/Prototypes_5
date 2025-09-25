import java.util.ArrayList;

class UserStat{
    String userAverageExerciseDuration;
    double userAverageElevation;
    double userAverageDistance;

    public UserStat(String userAverageExerciseDuration, double userAverageElevation, double userAverageDistance) {
        this.userAverageExerciseDuration = userAverageExerciseDuration;
        this.userAverageElevation = userAverageElevation;
        this.userAverageDistance = userAverageDistance;
    }


    public static String calculateAverageTimeSpan(ArrayList<String> timeSpan1, int divideBy) {
        int totalHours= 0;
        int totalMinutes = 0;
        int totalSeconds = 0;

        for (String timespan: timeSpan1) {
            String timeSpanInNumbers = timespan.replaceAll("[^0-9\\s]+", "");
            String[] parts = timeSpanInNumbers.split(" ");

            totalHours += (Integer.parseInt(parts[0]));
            totalMinutes += (Integer.parseInt(parts[1]));
            totalSeconds += (Integer.parseInt(parts[2]));

        }

        return totalHours/divideBy + "h " + totalMinutes/divideBy + "m " + totalSeconds/divideBy + "s";
    }

}