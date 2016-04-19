package at.aau.group1.leiterspiel;

/**
 * Created by Igor on 18.04.2016.
 */
public class GameField {

    // attributes for graphical representation
    public enum FieldType {DEFAULT, LADDER_START, LADDER_END, START, FINISH};
    private FieldType type;
    private int x; // x and y coordinate on the game board
    private int y;

    public GameField() {
        type = FieldType.DEFAULT;
        x = 0;
        y = 0;
    }

}
