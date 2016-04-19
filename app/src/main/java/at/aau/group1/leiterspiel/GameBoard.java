package at.aau.group1.leiterspiel;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Igor on 18.04.2016.
 */
public class GameBoard {

    private int numberOfFields;
    private ArrayList<Ladder> ladders = new ArrayList<Ladder>();
    private ArrayList<Piece> pieces = new ArrayList<Piece>();

    // for graphics later on
    private GameField[] fields;

    public GameBoard() {

    }

    public GameBoard(int numberOfFields, ArrayList<Ladder> ladders, ArrayList<Piece> pieces) {
        this.numberOfFields = numberOfFields;
        this.ladders = ladders;
        this.pieces = pieces;
        this.fields = new GameField[numberOfFields];
    }

    public void setNumberOfFields(int number) {
        this.numberOfFields = number;
        this.fields = new GameField[this.numberOfFields];
    }

    public void addLadder(Ladder ladder) { this.ladders.add(ladder); }

    public void addPiece(Piece piece) { this.pieces.add(piece); }

    // moves one of the pieces(the one corresponding to the playerID) by the given amount of fields,
    // considering all ladders.
    // throws IllegalArgumentException if an invalid playerID was given
    // returns true if this move ends the game, otherwise false
    public boolean movePiece(int playerID, int fields) {
        int previousField = 0; // for debug messages only

        Piece currentPiece = null;
        for (Piece p: pieces) {
            if (p.getPlayerID() == playerID) {
                currentPiece = p;
                previousField = p.getField();
                break;
            }
        }
        if(currentPiece == null) throw new IllegalArgumentException("invalid playerID");
        int currentField = currentPiece.getField();

        // checks if the goal will be reached
        if (currentField + fields == numberOfFields-1) { // TODO end the game properly
            Log.d("Tag", "Game ended. Winner is player "+playerID);
            return true;
        } else if (currentField + fields >= numberOfFields) { // TODO specify rules
            // do nothing in case the goal would be overshot
            return false;
        } else {
            currentField += fields;
            // check ladders
            for (Ladder ladder: ladders) {
                if (ladder.checkFields(currentField)) {
                    currentField = ladder.checkActivation(currentField);

                    if (currentField != previousField) Log.d("Tag", "Player "+playerID+" used a ladder");
                    break;
                }
            }
        }
        // move piece to currentField
        currentPiece.setField(currentField);

        Log.d("Tag", "Player "+playerID+" moved from field "+previousField+" to "+currentField);
        return false;
    }

    private Piece getPieceOnField(int field) {
        for (Piece piece: pieces) {
            if (field == piece.getField()) return piece;
        }
        return null;
    }

    private Ladder getLadderOnField(int field) {
        for (Ladder ladder: ladders) {
            if (field == ladder.getStartField() || field == ladder.getEndField()) return ladder;
        }
        return null;
    }

    public void printState() {
        int width = 10;
        for (int field=0; field<numberOfFields; field++) {
            Piece tempPiece = getPieceOnField(field);
            Ladder tempLadder = getLadderOnField(field);

            if (tempPiece != null) System.out.print(tempPiece.getPlayerID()+" ");
            else if (tempLadder != null) System.out.print("L ");
            else System.out.print("_ ");

            if (field+1 % width == 0) System.out.println("");
        }
    }

}
