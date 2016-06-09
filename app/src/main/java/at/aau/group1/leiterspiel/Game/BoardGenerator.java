package at.aau.group1.leiterspiel.Game;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Igor on 09.06.2016.
 */
public class BoardGenerator {

    private int numberOfFields = 60;
    private int numberOfLadders = 8;
    private Random random = new Random();

    public BoardGenerator() {

    }

    public BoardGenerator(int numberOfFields, int numberOfLadders) {
        this.numberOfFields = numberOfFields;
        this.numberOfLadders = numberOfLadders;
        if (this.numberOfLadders % 2 != 0) this.numberOfLadders++; // make sure the number is always even

    }

    public GameBoard generateBoard() {
        random.setSeed(System.currentTimeMillis());
        GameBoard gameBoard = new GameBoard();
        gameBoard.setNumberOfFields(numberOfFields);

        ArrayList<Integer> usedFields = new ArrayList<Integer>();
        int field = 0;
        int increase = numberOfFields/(numberOfLadders+2);
        boolean direction = true;
        for (int n = 0; n<numberOfLadders; n++) {
            field += increase;
            field += getDeviation(increase);
            if (field >= numberOfFields-1) break;
            while (usedFields.contains(field)) {
                field++;
            }
            if (direction) gameBoard.addLadder(new Ladder(Ladder.LadderType.DOWN, field, getEndField(field)));
            else gameBoard.addLadder(new Ladder(Ladder.LadderType.UP, field, getEndField(field)));
            direction = !direction;
        }

        return gameBoard;
    }

    private int getDeviation(int maxDeviation) {
        return random.nextInt(maxDeviation*2)-maxDeviation;
    }

    private int getEndField(int start) {
        int result = 15+getDeviation(2);
        if (result < 0) result = 1;
        if (result >= numberOfFields) result = numberOfFields-2;
        return result;
    }
}
