package com.mobilez365.xo.ai;

import android.os.Bundle;

import java.util.ArrayList;


/**
 * Created with Android Studio.
 * User: MediumMG
 * Date: 07.05.2014
 * Time: 17:56
 */
public class GameChecker {

    public static final String COORDINATE_START_X = "coordinate_start_x";
    public static final String COORDINATE_START_Y = "coordinate_start_y";
    public static final String COORDINATE_END_X = "coordinate_end_x";
    public static final String COORDINATE_END_Y = "coordinate_end_y";
    public static final String WIN_SYMBOL = "win_symbol";

    public static Bundle chechForWinCombination(FieldValue[][] gameField, int countInRowToWin) {
        if (gameField == null)
            return null;

        if (gameField.length == 0)
            return null;

        for (int i = 0; i < gameField.length; i++)
            if (gameField[i].length != gameField.length)
                return null;

        if (countInRowToWin <= gameField.length / 2)
            return null;

        Bundle winX = checkForSymbol(gameField, countInRowToWin, FieldValue.X);
        if (winX != null)
            return winX;

        Bundle winO = checkForSymbol(gameField, countInRowToWin, FieldValue.O);
        if (winO != null)
            return winO;

        return null;
    }

    private static Bundle checkForSymbol(FieldValue[][] gameField, int countInRowToWin, FieldValue side) {
        int size = gameField.length;

        // Horizontal
        for (int i = 0; i < size; i++) {
            int jStart = -1;
            int jEnd = -1;

            for (int j = 0; j < size; j++) {
                if (gameField[i][j] == side) {
                    if (jStart < 0)
                        jStart = j;
                    jEnd = j;
                }
                else {
                    if (jStart >= 0 && jEnd >=0 && jEnd - jStart + 1 == countInRowToWin)
                        return createBundle(i, jStart, i, jEnd, side);

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
                if (gameField[i][j] == side) {
                    if (iStart < 0)
                        iStart = i;
                    iEnd = i;
                }
                else {
                    if (iStart >= 0 && iEnd >= 0 && iEnd - iStart + 1 == countInRowToWin)
                        return createBundle(iStart, j, iEnd, j, side);

                    iStart = -1;
                    iEnd = -1;
                }
            }
        }

        //Diagonal: from left top to right bottom
        for (int i = 0; i <= size - countInRowToWin; i++)
            for (int j = 0; j <= size - countInRowToWin; j++) {

                boolean isBlock = true;
                for (int shift = 0; shift < countInRowToWin; shift++) {
                    isBlock = isBlock && gameField[i + shift][j + shift] == side;
                    if (!isBlock)
                        break;
                }

                if (isBlock)
                    return createBundle(i, j, i + countInRowToWin - 1, j + countInRowToWin - 1, side);
            }

        // Diagonal: from right top to left bottom
        for (int i = 0; i <= size - countInRowToWin; i++)
            for (int j = size - 1; j >= countInRowToWin - 1; j--) {

                boolean isBlock = true;
                for (int shift = 0; shift < countInRowToWin; shift++) {
                    isBlock = isBlock && gameField[i + shift][j - shift] == side;
                    if (!isBlock)
                        break;
                }

                if (isBlock)
                    return createBundle(i, j, i + countInRowToWin - 1, j - countInRowToWin + 1, side);
            }

        return null;
    }

    private static  Bundle createBundle(int x1, int y1, int x2, int y2, FieldValue side) {
        Bundle result = new Bundle();
        result.putInt(COORDINATE_START_X, x1);
        result.putInt(COORDINATE_START_Y, y1);
        result.putInt(COORDINATE_END_X, x2);
        result.putInt(COORDINATE_END_Y, y2);
        result.putString(WIN_SYMBOL, side == FieldValue.X ? "x" : "o");
        return result;
    }

}
