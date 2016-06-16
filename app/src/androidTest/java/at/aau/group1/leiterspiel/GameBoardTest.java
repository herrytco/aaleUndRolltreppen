package at.aau.group1.leiterspiel;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import android.graphics.Point;

import at.aau.group1.leiterspiel.game.GameBoard;
import at.aau.group1.leiterspiel.game.Ladder;
import at.aau.group1.leiterspiel.game.Piece;

public class GameBoardTest {

    @Test
    public void testIdError() {
        GameBoard gameBoard = new GameBoard(100, null, null);
        try {
            gameBoard.movePiece(0, 0);
            assertTrue (false);
        } catch (IllegalArgumentException e) {
            assertTrue (true);
        }
    }

    @Test
    public void testNoInit() {
        GameBoard gameBoard = new GameBoard(100, null, null);

        assertTrue (gameBoard.getPieceOfPlayer(0) == null);
        assertTrue (gameBoard.getLadderOnField(0) == null);
        Ladder ladder = new Ladder(Ladder.LadderType.BIDIRECTIONAL, 0, 10);
        assertTrue (gameBoard.getLadderOnField(0).getEndField() != 10);
        assertTrue (gameBoard.getPiecesOnField(0) == null);
        assertTrue (gameBoard.checkWinningMove(0, 0) == false);

        assertTrue (gameBoard.getFieldAtPosition(new Point(50, 50)) == 0);
    }

    @Test
    public void testOnePiece() {
        GameBoard gameBoard = new GameBoard(100, null, null);
        gameBoard.addPiece(new Piece(3));

        assertTrue (gameBoard.getPiecesOnField(0).get(0).getPlayerID() == 3);
        assertTrue (gameBoard.checkWinningMove(3, 99));
        assertTrue (!gameBoard.checkWinningMove(3, 100));
        assertTrue (!gameBoard.checkWinningMove(3, 98));

        Ladder ladder = new Ladder(Ladder.LadderType.BIDIRECTIONAL, 1, 99);
        gameBoard.addLadder(ladder);
        assertTrue (gameBoard.checkWinningMove(3, 1));
        assertTrue (gameBoard.movePiece(3, 1));
    }

}
