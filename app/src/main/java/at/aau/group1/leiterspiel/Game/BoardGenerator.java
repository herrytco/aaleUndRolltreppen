package at.aau.group1.leiterspiel.game;

/**
 * Created by Igor on 09.06.2016.
 */
public class BoardGenerator {

    public BoardGenerator() {
        // default constructor
    }

    public GameBoard generateBoard(int type) {
        GameBoard gameBoard = new GameBoard();
        gameBoard.setNumberOfFields(60);

        if (type == 0) {
            gameBoard.addLadder(new Ladder(Ladder.LadderType.UP, 13, 26));
            gameBoard.addLadder(new Ladder(Ladder.LadderType.UP, 39, 46));
            gameBoard.addLadder(new Ladder(Ladder.LadderType.DOWN, 5, 29));
            gameBoard.addLadder(new Ladder(Ladder.LadderType.DOWN, 23, 37));
            gameBoard.addLadder(new Ladder(Ladder.LadderType.DOWN, 34, 57));
        }
        if (type == 1) {
            gameBoard.addLadder(new Ladder(Ladder.LadderType.UP, 13, 26));
            gameBoard.addLadder(new Ladder(Ladder.LadderType.UP, 34, 50));
            gameBoard.addLadder(new Ladder(Ladder.LadderType.DOWN, 1, 24));
            gameBoard.addLadder(new Ladder(Ladder.LadderType.DOWN, 28, 47));
            gameBoard.addLadder(new Ladder(Ladder.LadderType.DOWN, 32, 55));
            gameBoard.addLadder(new Ladder(Ladder.LadderType.DOWN, 21, 40));
            gameBoard.addLadder(new Ladder(Ladder.LadderType.DOWN, 7, 15));
        }
        if (type == 2) {
            gameBoard.setNumberOfFields(50);
            gameBoard.addLadder(new Ladder(Ladder.LadderType.UP, 5, 24));
            gameBoard.addLadder(new Ladder(Ladder.LadderType.DOWN, 28, 47));
        }

        return gameBoard;
    }
}
