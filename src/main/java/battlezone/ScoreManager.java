package battlezone;

import gameObject.Enemy;
import gameObject.Missile;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.StringTokenizer;

public final class ScoreManager {

    private static int score = 0;
    private static int lives = 3;

    private ScoreManager() {
    }

    public static void initialize() {
        lives = 3;
        score = 0;
    }

    public static int getScore() {
        return score;
    }

    public static void updateScore(Enemy enemy) {
        score += 1000;
        if (enemy instanceof Missile) {
            score += 500;
        }
    }

    public static void saveScore() {
        /*
            gets the current high scores from a text file, finds where the current score belongs and rewrites the file with the new score
        */
        try {
            String scores = getHighScores();
            String[] scoreList = getStringArrayScores(scores);
            int replaceLocation = getScorePlacement();
            for (int j = scoreList.length - 1; j > replaceLocation; j--) {

                scoreList[j] = scoreList[j - 1];
            }
            int[] nameChars = ((WriteHighScorePage) Battlezone.getMenuPages()[5]).getNameChars();
            String name = "" + (char) nameChars[0] + (char) nameChars[1] + (char) nameChars[2];
            scoreList[replaceLocation] = name + score;
            PrintWriter out = new PrintWriter(new File(Class.class.getResource("scores.txt").getFile()));
            for (int k = 0; k < scoreList.length; k++) {
                out.println(scoreList[k]);
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public static String getHighScores() {
        /*
        retrives the high scores from the text document
        */
        try {
            Scanner wanner = new Scanner(Battlezone.class.getClassLoader().getResourceAsStream("scores.txt"));
            String scores = "";
            while (wanner.hasNext()) {
                scores += wanner.next() + " ";
            }
            return scores;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return "";
        }
    }

    public static int getScorePlacement() {
        //gets the scores of the high scores , and sees where score fits into it
        //If its within top 5 it return where, if not it returns -1
        StringTokenizer tokens = new StringTokenizer(getHighScores());
        int i = -1;
        int j = 0;
        while (tokens.hasMoreTokens()) {
            int scoreAt = Integer.parseInt(tokens.nextToken().substring(3));
            if (score > scoreAt) {
                i = j;
                break;
            }
            j++;
        }
        return i;
    }

    public static String[] getStringArrayScores(String scores) {
        /*
        sorts the scores into a string array
        */
        StringTokenizer tokens = new StringTokenizer(scores);
        String[] fancySchmancy = new String[tokens.countTokens()];
        int i = 0;
        while (tokens.hasMoreTokens()) {
            fancySchmancy[i] = tokens.nextToken();
            i++;
        }
        return fancySchmancy;
    }

    public static int getLives() {
        return lives;
    }

    public static void decrementLives() {
        lives--;
    }

}
