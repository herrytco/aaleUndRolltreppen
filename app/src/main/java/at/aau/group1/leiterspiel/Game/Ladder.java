package at.aau.group1.leiterspiel.game;

/**
 * Created by Igor on 18.04.2016.
 */
public class Ladder {

    public enum LadderType {
        UP, // aka Rolltreppen
        DOWN, // aka Aale
        BIDIRECTIONAL
    };

    private LadderType type;
    private int startField;
    private int endField;

    public Ladder(LadderType type, int startField, int endField) {
        this.type = type;
        this.startField = startField;
        this.endField = endField;
    }

    public LadderType getType() {
        return type;
    }

    public int getStartField() {
        return startField;
    }

    public int getEndField() {
        return endField;
    }

    // checks if the given field is connected with this ladder
    public boolean checkFields(int field) {
        return field == startField || field == endField;
    }

    // checks if the piece on the given field triggers the ladder
    //
    // returns its new position if the ladder gets used, or else the current position
    // throws IllegalArgumentException if the given field isn't connected to the ladder
    public int checkActivation(int field) {
        if (field == startField) {
            if (type == LadderType.BIDIRECTIONAL || type == LadderType.UP) return endField;
            else return startField;
        } else if (field == endField) {
            if (type == LadderType.BIDIRECTIONAL || type == LadderType.DOWN) return startField;
            else return endField;
        } else {
            throw new IllegalArgumentException("given field is not connected with ladder");
        }
    }

}
