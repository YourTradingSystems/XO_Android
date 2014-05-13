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
        int size = gameField.length;

        if (size == 3)
            return getHard3Move(gameField, countInRowToWin, aiSide);
        else
            return getMediumMove(gameField, countInRowToWin, aiSide);
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

                if (gameField[i][j] != aiSide || j == size - 1) {
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
                if (gameField[i][j] != aiSide || i == size - 1) {
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

    private Bundle createMove(int i, int j) {
        Bundle result = new Bundle();
        result.putInt(COORDINATE_X, i);
        result.putInt(COORDINATE_Y, j);
        return result;
    }

    private Bundle getHard3Move(FieldValue[][] gameField, int countInRowToWin, FieldValue aiSide) {
        int size = 3;

        FieldValue oppSide = aiSide == FieldValue.X ?
                FieldValue.O :
                FieldValue.X;

        int aiCount = 0;
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                if (gameField[i][j] == aiSide)
                    aiCount++;

        int oppCount = 0;
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                if (gameField[i][j] == oppSide)
                    oppCount++;

        if (aiCount == oppCount) {
            // AI move first

            switch (aiCount) {
                case 0: {
                    switch (mRandom.nextInt(5)) {
                        case 0: return createMove(0, 0);
                        case 1: return createMove(0, 2);
                        case 2: return createMove(2, 0);
                        case 3: return createMove(2, 2);
                        case 4: return createMove(1, 1);
                    }
                }
                case 1: {
                    if (gameField[1][1] == aiSide) {
                        // first AI move - center
                        if (gameField[0][0] == oppSide)
                            return createMove(2, 2);
                        else if (gameField[0][2] == oppSide)
                            return createMove(2,0);
                        else if (gameField[2][0] == oppSide)
                            return createMove(0,2);
                        else if (gameField[2][2] == oppSide)
                            return createMove(0,0);
                        else {
                            if (gameField[0][1] == oppSide) {
                                if (mRandom.nextBoolean())
                                    return createMove(2, 0);
                                else
                                    return createMove(2, 2);
                            }
                            else if (gameField[1][0] == oppSide) {
                                if (mRandom.nextBoolean())
                                    return createMove(0, 2);
                                else
                                    return createMove(2, 2);
                            }
                            else if (gameField[1][2] == oppSide) {
                                if (mRandom.nextBoolean())
                                    return createMove(0, 0);
                                else
                                    return createMove(2, 0);
                            }
                            else {
                                if (mRandom.nextBoolean())
                                    return createMove(0, 0);
                                else
                                    return createMove(0, 2);
                            }
                        }
                    }
                    else {
                        // first AI move - corner
                        if (gameField[1][1] == oppSide) {
                            if (gameField[0][0] == aiSide)
                                return createMove(2, 2);
                            else if (gameField[0][2] == aiSide)
                                return createMove(2, 0);
                            else if (gameField[2][0] == aiSide)
                                return createMove(0, 2);
                            else
                                return createMove(0, 0);
                        }
                        else {
                            if (gameField[0][0] == aiSide) {
                                if (gameField[0][1] == oppSide)
                                    return createMove(2, 0);
                                else
                                    return createMove(0, 2);
                            }
                            else if (gameField[0][2] == aiSide){
                                if (gameField[0][1] == oppSide)
                                    return createMove(2, 2);
                                else
                                    return createMove(0, 0);
                            }
                            else if (gameField[2][0] == aiSide) {
                                if (gameField[2][1] == oppSide)
                                    return createMove(0, 0);
                                else
                                    return createMove(2, 2);
                            }
                            else {
                                if (gameField[2][1] == oppSide)
                                    return createMove(0, 2);
                                else
                                    return createMove(2, 0);
                            }
                        }
                    }
                }
                case 2: {
                    Bundle bundleForWin = checkForWin(gameField, countInRowToWin, aiSide);
                    if (bundleForWin != null)
                        return bundleForWin;

                    Bundle bundleForProtect = checkForProtect(gameField, countInRowToWin, aiSide);
                    if (bundleForProtect != null)
                        return bundleForProtect;

                    if (gameField[1][1] == aiSide) {
                        if (gameField[0][0] == aiSide) {
                            if (gameField[0][1] == oppSide)
                                return createMove(2, 0);
                            else
                                return createMove(0, 2);
                        }
                        else if (gameField[0][2] == aiSide) {
                            if (gameField[0][1] == oppSide)
                                return createMove(2, 2);
                            else
                                return createMove(0, 0);
                        }
                        else if (gameField[2][0] == aiSide) {
                            if (gameField[2][1] == oppSide)
                                return createMove(0, 0);
                            else
                                return createMove(2, 2);
                        }
                        else {
                            if (gameField[2][1] == oppSide)
                                return createMove(0, 2);
                            else
                                return createMove(2, 0);
                        }
                    }
                    else {
                        if (gameField[0][0] == aiSide && gameField[0][2] == aiSide) {
                            if (gameField[1][0] == FieldValue.Empty)
                                return createMove(2, 0);
                            else
                                return createMove(2, 2);
                        }
                        else if (gameField[2][0] == aiSide && gameField[2][2] == aiSide) {
                            if (gameField[1][0] == FieldValue.Empty)
                                return createMove(0, 0);
                            else
                                return createMove(0, 2);
                        }
                        else if (gameField[0][0] == aiSide && gameField[2][0] == aiSide) {
                            if (gameField[0][1] == FieldValue.Empty)
                                return createMove(0, 2);
                            else
                                return createMove(2, 2);
                        }
                        else {
                            if (gameField[0][1] == FieldValue.Empty)
                                return createMove(0, 0);
                            else
                                return createMove(2, 0);
                        }
                    }
                }
                default: {
                    return getMediumMove(gameField, countInRowToWin, aiSide);
                }
            }
        }
        else {
            // Player move first...

            switch (aiCount) {
                case 0: {
                    // ... to corner
                    if (gameField[0][0] == oppSide || gameField[0][2] == oppSide || gameField[2][0] == oppSide || gameField[2][2] == oppSide) {
                        return createMove(1, 1);
                    }
                    // ... to center
                    else if (gameField[1][1] == oppSide) {
                        switch (mRandom.nextInt(4)) {
                            case 0: return createMove(0, 0);
                            case 1: return createMove(0, 2);
                            case 2: return createMove(2, 0);
                            case 3: return createMove(2, 2);
                        }
                    }
                    // ... to center side
                    else {
                        return createMove(1, 1);
                    }

                }
                case 1: {
                    Bundle bundleForProtect = checkForProtect(gameField, countInRowToWin, aiSide);
                    if (bundleForProtect != null)
                        return bundleForProtect;

                    if (gameField[1][1] == oppSide) {
                        if (gameField[0][0] == oppSide) {
                            if (mRandom.nextBoolean())
                                return createMove(0, 2);
                            else
                                return createMove(2, 0);
                        }
                        else if (gameField[0][2] == oppSide) {
                            if (mRandom.nextBoolean())
                                return createMove(0, 0);
                            else
                                return createMove(2, 2);
                        }
                        else if (gameField[2][0] == oppSide) {
                            if (mRandom.nextBoolean())
                                return createMove(0, 0);
                            else
                                return createMove(2, 2);
                        }
                        else {
                            if (mRandom.nextBoolean())
                                return createMove(0, 2);
                            else
                                return createMove(2, 0);
                        }
                    }
                    else {
                        int oppCorner = 0;
                        if (gameField[0][0] == oppSide) oppCorner++;
                        if (gameField[0][2] == oppSide) oppCorner++;
                        if (gameField[2][0] == oppSide) oppCorner++;
                        if (gameField[2][2] == oppSide) oppCorner++;

                        if (oppCorner == 2) {
                            switch (mRandom.nextInt(4)) {
                                case 0: return createMove(0, 1);
                                case 1: return createMove(1, 0);
                                case 2: return createMove(1, 2);
                                case 3: return createMove(2, 1);
                            }
                        }
                        else {
                            if ( (gameField[0][1] == oppSide || gameField[0][2] == oppSide) &&
                                 (gameField[1][0] == oppSide || gameField[2][0] == oppSide) ) {
                                return createMove(0, 0);
                            }
                            else if ( (gameField[0][0] == oppSide || gameField[0][1] == oppSide) &&
                                    (gameField[1][2] == oppSide || gameField[2][2] == oppSide) )
                                return createMove(0, 2);
                            else if ( (gameField[0][0] == oppSide || gameField[1][0] == oppSide) &&
                                    (gameField[2][1] == oppSide || gameField[2][2] == oppSide) )
                                return createMove(2, 0);
                            else if ( (gameField[0][2] == oppSide || gameField[1][2] == oppSide) &&
                                    (gameField[2][0] == oppSide || gameField[2][1] == oppSide) )
                                return createMove(2, 2);
                            else {
                                switch (mRandom.nextInt(4)) {
                                    case 0: return createMove(0, 0);
                                    case 1: return createMove(0, 2);
                                    case 2: return createMove(2, 0);
                                    case 3: return createMove(2, 2);
                                }
                            }
                        }
                    }
                }
                case 2:
                default: {
                    return getMediumMove(gameField, countInRowToWin,aiSide);
                }
            }
        }

    }

}
