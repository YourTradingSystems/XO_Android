package com.mobilez365.xo.ai;

import android.os.Bundle;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created with Android Studio.
 * User: MediumMG
 * Date: 06.05.2014
 * Time: 10:31
 */
public class AIPlayer {

    public static final String COORDINATE_X = "coordinate_x";
    public static final String COORDINATE_Y = "coordinate_y";

    private static AIPlayer mInstance = null;

    private Random mRandom;

    private AIPlayer() {
        mRandom = new Random();
    }

    public static AIPlayer getInstance() {
        if (mInstance == null) {
            synchronized (AIPlayer.class) {
                if (mInstance == null)
                    mInstance = new AIPlayer();
            }
        }
        return mInstance;
    }

    public Bundle getPlayerMove(AILevel level, FieldValue[][] gameField, int countInRowToWin, FieldValue aiSide)
        throws InvalidParameterException {

        if (gameField == null)
            throw new InvalidParameterException("Array (game field) is null");

        if (gameField.length == 0)
            throw new InvalidParameterException("Array size is 0");

        for (int i = 0; i < gameField.length; i++)
            if (gameField[i].length != gameField.length)
                throw new InvalidParameterException("Two-dimensional array is not square (different size)");

        if (countInRowToWin <= gameField.length / 2)
            throw new InvalidParameterException("Too few objects to be collected in a row to win (too easy)");

        if (aiSide != FieldValue.O || aiSide != FieldValue.X)
            throw new InvalidParameterException("AI Player should play for X or O");

        switch (level){
            case Easy:      return getEasyMove(gameField);
            case Medium:    return getMediumMove(gameField, countInRowToWin, aiSide);
            case Hard:      return getHardMove(gameField, countInRowToWin, aiSide);
            default:        throw new InvalidParameterException("Unknown level: " + level.toString());
        }
    }



    private Bundle getEasyMove(FieldValue[][] gameField) {
        ArrayList<HashMap<String,Integer>> emptyFields = new ArrayList<>();

        for (int i = 0; i < gameField.length; i++)
            for (int j = 0; j < gameField.length; j++)
                if (gameField[i][j] == FieldValue.Empty) {
                    HashMap<String, Integer> item = new HashMap<>();
                    item.put(COORDINATE_X, i);
                    item.put(COORDINATE_Y, j);
                    emptyFields.add(item);
                }

        int rnd = mRandom.nextInt(emptyFields.size());

        return createMove(emptyFields.get(rnd).get(COORDINATE_X), emptyFields.get(rnd).get(COORDINATE_Y));
    }

    private Bundle getMediumMove(FieldValue[][] gameField, int countInRowToWin, FieldValue aiSide) {
        Bundle winMove = checkForWin(gameField, countInRowToWin, aiSide);
        if (winMove != null)
            return winMove;

        Bundle protectMove = checkForProtect(gameField, countInRowToWin, aiSide);
        if (protectMove != null)
            return protectMove;

        return getEasyMove(gameField);
    }

    private Bundle getHardMove(FieldValue[][] gameField, int countInRowToWin, FieldValue aiSide) {
        return null;
    }



    private Bundle checkForWin(FieldValue[][] gameField, int countInRowToWin, FieldValue aiSide){
        int size = gameField.length;

        // Horizontal
        for (int i = 0; i < size; i++) {
            int jStart = -1;
            int jEnd = -1;

            for (int j = 0; j < size; j++) {
                if (gameField[i][j] == aiSide) {
                    if (jStart < 0)
                        jStart = j;
                    jEnd = j;
                }
                else {
                    if (jStart >= 0 && jEnd >=0 && jEnd - jStart + 1 == countInRowToWin - 1) {
                        // Can AI do move to left of finding block?
                        if (jStart > 0)
                            if (gameField[i][jStart - 1] == FieldValue.Empty)
                                return createMove(i, jStart - 1);

                        // Can AI do move to right of finding block?
                        if (jEnd < size - 1)
                            if (gameField[i][jEnd + 1] == FieldValue.Empty)
                                return createMove(i, jEnd + 1);
                    }

                    jStart = -1;
                    jEnd = -1;
                }
            }
        }

        // Vertical
        for (int j = 0; j < size; j++) {
            int iStart = -1;
            int iEnd = -1;

            for (int i = 0; i < size; i++) {
                if (gameField[i][j] == aiSide) {
                    if (iStart < 0)
                        iStart = i;
                    iEnd = i;
                }
                else {
                    if (iStart >= 0 && iEnd >=0 && iEnd - iStart + 1 == countInRowToWin - 1) {
                        // Can AI do move above of finding block?
                        if (iStart > 0)
                            if (gameField[iStart - 1][j] == FieldValue.Empty)
                                return createMove(iStart - 1 , j);

                        // Can AI do move below of finding block?
                        if (iEnd < size - 1)
                            if (gameField[iEnd + 1] [j] == FieldValue.Empty)
                                return createMove(iEnd + 1, j);
                    }

                    iStart = -1;
                    iEnd = -1;
                }
            }
        }

        // Diagonal: from left top to right bottom
        for (int i = 0; i <= size - countInRowToWin + 1; i++) {
            for (int j = 0; j <= size - countInRowToWin + 1; j++) {
                if (Math.abs(j-i) <= size - countInRowToWin) {

                    boolean isBlock = true;
                    for (int shift = 0; shift < countInRowToWin - 1; shift++) {
                        isBlock = isBlock && gameField[i + shift][j + shift] == aiSide;
                        if (!isBlock)
                            break;
                    }

                    if (isBlock) {
                        int iStart = i; int jStart = j;
                        int iEnd = i + countInRowToWin - 2; int jEnd = j + countInRowToWin - 2;

                        // Can AI do move above left of finding block?
                        if (iStart > 0 && jStart > 0)
                            if (gameField[iStart - 1][jStart - 1] == FieldValue.Empty)
                                return createMove(iStart - 1, jStart - 1);

                        // Can AI do move below right of finding block?
                        if (iEnd < size - 1 && jEnd < size - 1)
                            if (gameField[iEnd + 1][jEnd + 1] == FieldValue.Empty)
                                return createMove(iEnd + 1, jEnd + 1);
                    }
                }
            }
        }

        // Diagonal: from right top to left bottom
        for (int i = 0; i <= size - countInRowToWin + 1; i++)
            for (int j = size - 1; j >= countInRowToWin - 2; j--) {{
                if (Math.abs(i + j - size + 1) <= size - countInRowToWin) {

                    boolean isBlock = true;
                    for (int shift = 0; shift < countInRowToWin - 1; shift++) {
                        isBlock = isBlock && gameField[i + shift][j - shift] == aiSide;
                        if (!isBlock)
                            break;
                    }

                    if (isBlock) {
                        int iStart = i; int jStart = j;
                        int iEnd = i + countInRowToWin - 2; int jEnd = j - countInRowToWin + 2;

                        // Can AI do move above right of finding block?
                        if (iStart > 0 && jStart < size - 1)
                            if (gameField[iStart - 1][jStart + 1] == FieldValue.Empty)
                                return createMove(iStart - 1, jStart + 1);

                        // Can AI do move below left of finding block?
                        if (iEnd < size - 1 && jEnd > 0)
                            if (gameField[iEnd + 1][jEnd - 1] == FieldValue.Empty)
                                return createMove(iEnd + 1, jEnd - 1);
                    }
                }
            }
        }

        return null;
    }

    private Bundle checkForProtect(FieldValue[][] gameField, int countInRowToWin, FieldValue aiSide){
        FieldValue oppSide = aiSide == FieldValue.X ?
                            FieldValue.O :
                            FieldValue.X;

        return checkForWin(gameField, countInRowToWin, oppSide);
    }

    private Bundle createMove(int x, int y) {
        Bundle result = new Bundle();
        result.putInt(COORDINATE_X, x);
        result.putInt(COORDINATE_Y, y);
        return result;
    }


}
