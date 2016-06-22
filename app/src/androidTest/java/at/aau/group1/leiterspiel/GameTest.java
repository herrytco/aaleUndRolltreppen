package at.aau.group1.leiterspiel;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.graphics.Point;

import at.aau.group1.leiterspiel.game.BoardGenerator;
import at.aau.group1.leiterspiel.game.GameBoard;
import at.aau.group1.leiterspiel.game.Ladder;
import at.aau.group1.leiterspiel.game.Piece;

public class GameTest {

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
        assertTrue (gameBoard.getLadderOnField(0).getEndField() != 10);
        assertTrue (gameBoard.getPiecesOnField(0) == null);
        assertTrue (gameBoard.checkWinningMove(0, 0) == false);

        assertTrue (gameBoard.getFieldAtPosition(new Point(50, 50)) == 0);

        // testing for invalid parameters
        assertTrue(gameBoard.getLadderOnField(-1) == null);
        assertTrue(gameBoard.getFieldAtPosition(null) == 0);
        assertTrue(gameBoard.getPiecesOnField(-1).size() == 0);
        assertFalse(gameBoard.checkOvershootingMove(-1, 100));
        gameBoard.setNumberOfFields(-1);
        assertTrue(gameBoard.getNumberOfFields() > 0);
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

    @Test
    public void testGenerator() {
        BoardGenerator generator = new BoardGenerator();
        // Board generator must always output a usable GameBoard no matter the input
        assertTrue(generator.generateBoard(-1).getNumberOfFields() > 0);
    }

    @Test
    public void testPiece() {
        Piece piece = new Piece(37);
        assertFalse(piece.increaseProgress(0.5));
        assertTrue(piece.increaseProgress(100.0));
        assertFalse(piece.increaseProgress(0.0001));
    }

    @Test
    public void testImagelessPainter() {
        GamePainter painter = new GamePainter(-1, -1);
        try {
            painter.buildBoard(null);
            painter.drawFrame(null);
            painter.drawFrame(new BoardGenerator().generateBoard(-1));
        } catch (Exception e) {
            assertTrue(false);
        }
    }

}
