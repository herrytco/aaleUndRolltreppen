package at.aau.group1.leiterspiel;

import android.graphics.Point;

/**
 * Created by Igor on 18.04.2016.
 */
public class GameField {

    // attributes for graphical representation
    public enum FieldType {DEFAULT, LADDER_START, START, FINISH};
    private FieldType type;
    private Point pos;

    public GameField() {
        pos = new Point();
        type = FieldType.DEFAULT;
    }

    public GameField(Point pos, FieldType type) {
        this.pos = pos;
        this.type = type;
    }

    public Point getPos() {
        return pos;
    }

    public FieldType getType() {
        return type;
    }
}
