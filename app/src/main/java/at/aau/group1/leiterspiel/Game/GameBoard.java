package at.aau.group1.leiterspiel.game;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Igor on 18.04.2016.
 */
public class GameBoard {

    private int numberOfFields;
    private ArrayList<Ladder> ladders = new ArrayList<>();
    private ArrayList<Piece> pieces = new ArrayList<>();

    // for graphics later on
    private GameField[] fields;
    // speed of the movement animation, >=1.0 means instant movement
    private double turnProgressIncrease = 1.0;
    private static final int TURN_DURATION_MS = 100; // time needed per field to complete the move
    private int fps = 30;
    private Piece movingPiece;
    private boolean isMoving = false;
    private int previousField = 0;

    public GameBoard() {
        // default constructor
    }

    public GameBoard(int numberOfFields, ArrayList<Ladder> ladders, ArrayList<Piece> pieces) {
        this.numberOfFields = numberOfFields;
        this.ladders = ladders;
        this.pieces = pieces;
        this.fields = new GameField[numberOfFields];
        Arrays.fill(fields, new GameField());
    }

    public void setFps(int fps) { this.fps = fps; }

    public double getTurnProgressIncrease() {
        return turnProgressIncrease;
    }

    /**
     * Sets how many fields the GameBoard should consist of. The fields are "numbered" from 0 to
     * getNumberOfFields().
     *
     * @param number Number of fields
     */
    public void setNumberOfFields(int number) {
        this.numberOfFields = number;
        this.fields = new GameField[this.numberOfFields];
        Arrays.fill(fields, new GameField());
    }

    public GameField[] getFields() {
        return fields;
    }

    public ArrayList<Ladder> getLadders() {
        return ladders;
    }

    public ArrayList<Piece> getPieces() {
        return pieces;
    }

    public int getNumberOfFields() {
        return numberOfFields;
    }

    public void addLadder(Ladder ladder) {
        this.ladders.add(ladder);
    }

    public void addPiece(Piece piece) { this.pieces.add(piece); }

    /**
     * Moves one of the pieces(the one corresponding to the playerID) by the given amount of fields,
     * considering all ladders.
     * Throws IllegalArgumentException if an invalid playerID was given.
     *
     * @param playerID ID of the player
     * @param fields Number of fields by which the piece should move
     * @return true if this move ends the game, otherwise false
     */
    public boolean movePiece(int playerID, int fields) {
        boolean gameEnded = false;
        boolean ladderUsed = false;

        Piece currentPiece = getPieceOfPlayer(playerID);
        if(currentPiece == null)
            throw new IllegalArgumentException("invalid playerID");

        previousField = currentPiece.getField();
        int currentField = currentPiece.getField();

        // checks if the goal will be reached
        if (currentField + fields == numberOfFields-1) {
//            currentField += fields;
            gameEnded = true;
            Log.d("GameBoard", "Game ended. Winner is player "+playerID);
        } else if (currentField + fields >= numberOfFields) {
            // do nothing in case the goal would be overshot
            return false;
//        } else {
        }
        currentField += fields;
        // check ladders
        for (Ladder ladder: ladders) {
            if (ladder.checkFields(currentField)) {
                int temp = currentField;
                currentField = ladder.checkActivation(currentField);
                if (temp != currentField)
                    ladderUsed = true;
                break;
            }
        }
        // move piece to currentField
        currentPiece.setField(currentField);
        movingPiece = currentPiece;
        isMoving = true;
        turnProgressIncrease = calculateProgressIncrease(previousField, currentField, ladderUsed);

        Log.d("GameBoard", "Player " + playerID + " moved from field " + previousField + " to " + currentField);
        return gameEnded;
    }

    private double calculateProgressIncrease(int start, int end, boolean ladder) {
        // set the progress increase per frame based on the FPS and distance of the move
        // distance of one field to another
        int stepDistance = (int) Math.hypot(Math.abs(fields[0].getPos().x - fields[1].getPos().x),
                Math.abs(fields[0].getPos().y - fields[1].getPos().y));
        Point previous = fields[start].getPos();
        Point current = fields[end].getPos();
        // distance of the two given fields
        int fieldDistance = (int) Math.hypot(Math.abs(previous.x - current.x),
                Math.abs(previous.y - current.y));

        int div = fieldDistance / stepDistance;
        double duration = TURN_DURATION_MS * Math.max(div, 3); // 3 fields distance as minimum so the animation doesn't appear too fast over short distances
        if (ladder)
            duration *= 1.5;
        double ratio = 1000.0/duration;
        return 1.0/(fps/ratio);
    }

    public boolean checkOvershootingMove(int playerID, int fields) {
        for (Piece p: pieces) {
            if (p.getPlayerID() == playerID) {
                return p.getField() + fields >= getNumberOfFields();
            }
        }
        return false;
    }

    /**
     * Checks if the given move would end the game
     *
     * @param playerID ID of the player
     * @param fields number of fields the piece would move
     * @return true if the move would end the game, otherwise false
     */
    public boolean checkWinningMove(int playerID, int fields) {
        for (Piece p: pieces) {
            if (p.getPlayerID() == playerID) {
                return p.getField() + fields == getNumberOfFields()-1;
            }
        }
        return false;
    }

    /**
     * Rollback a cheater's last move.
     *
     * @param cheaterID ID of the cheater
     * @param fields number of fields to go back
     */
    public void revertMove(int cheaterID, int fields) {
        for (Piece p: pieces) {
            if (p.getPlayerID() == cheaterID) {
                p.setField(p.getField()-fields);
            }
        }
    }

    /**
     * Searches the pieces for the one corresponding to the specified player.
     *
     * @param playerID ID of the player
     * @return Piece corresponding to the given ID
     */
    public Piece getPieceOfPlayer(int playerID) {
        for (Piece piece:pieces) {
            if (piece.getPlayerID() == playerID)
                return piece;
        }
        return null;
    }

    /**
     * Checks if any pieces lie on the specified field.
     *
     * @param field Index of the field
     * @return List of all pieces on this field, can be empty
     */
    public ArrayList<Piece> getPiecesOnField(int field) {
        ArrayList<Piece> piecesOnField = new ArrayList<>();
        for (Piece piece: piecesOnField) {
            if (field == piece.getField())
                piecesOnField.add(piece);
        }
        return piecesOnField;
    }

    /**
     * Checks if one of the ladders is connected with the specified field.
     *
     * @param field Index of the field
     * @return Ladder connected to the field, or null if no ladder is connected
     */
    public Ladder getLadderOnField(int field) {
        for (Ladder ladder: ladders) {
            if (field == ladder.getStartField() || field == ladder.getEndField())
                return ladder;
        }
        return null;
    }

    /**
     * Locates the index of the field with the smallest distance to the given xy coordinates.
     * Used for determining which field was selected by touch inputs.
     *
     * @param point Position on the canvas
     * @return Index of the field nearest to the given point on the canvas
     */
    public int getFieldAtPosition(Point point) {
        int nearestField = 0;
        int minDistance = Integer.MAX_VALUE;
        for (int n=0; n<fields.length; n++) {
            GameField field = fields[n];
            int dx = Math.abs(point.x - field.getPos().x);
            int dy = Math.abs(point.y - field.getPos().y);
            int d = (int) Math.hypot(dx, dy);

            if (d < minDistance) {
                minDistance = d;
                nearestField = n;
            }
        }
        return nearestField;
    }

    public boolean updateProgress() {
        if ( isMoving && movingPiece.increaseProgress(turnProgressIncrease) ) {
            movingPiece = null;
            isMoving = false;
            previousField = 0;
            // remove highlighting
            for (int f=0; f<numberOfFields; f++) {
                this.fields[f].setHighlighted(false);
            }
            return true;
        }
        return false;
    }

    public double getProgress() {
        if (movingPiece!=null)
            return movingPiece.getTurnProgress();
        return 1.0;
    }

    public int getPreviousField() { return previousField; }

    public Piece getMovingPiece() { return movingPiece; }

    public boolean isMoving() {
        return isMoving;
    }
}
