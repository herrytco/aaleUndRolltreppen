package at.aau.group1.leiterspiel;

import android.graphics.Point;

import java.util.ArrayList;

/**
 * Created by Igor on 02.05.2016.
 *
 * Represents combinations of Bezier curves for (hopefully) amazing and flexible graphics
 */
public class Snake {

    private Point start; // head
    private Point end; // tail
    private int twists;
    private boolean mirror = false;

    public Snake(Point start, Point end, int twists) {
        this.end = end;
        this.start = start;
        this.twists = twists;
        if (start.x > end.x) mirror = true;
    }

    public Point getPoint(int numberOfPoints, int point) {
        int pointsPerTwist = numberOfPoints/twists;
        int currentTwist = (int) Math.ceil(point/pointsPerTwist);
        int tPoint = point - currentTwist*pointsPerTwist;

        Point tStart = linearBezier(start, end, twists, currentTwist);
        Point tEnd = linearBezier(start, end, twists, currentTwist+1);
        Point tMid = linearBezier(tStart, tEnd, pointsPerTwist, pointsPerTwist/2);

        int twistWidth = (int) Math.hypot(Math.abs(tStart.x - tEnd.x), Math.abs(tStart.y - tEnd.y));
        if (currentTwist % 2 == 0 ^ mirror) {
            tMid.x -= twistWidth;
            if (start.x > end.x) tMid.y += twistWidth/2;
            else tMid.y -= twistWidth/2;
        } else {
            tMid.x += twistWidth;
            if (start.x > end.x) tMid.y -= twistWidth/2;
            else tMid.y += twistWidth/2;
        }

        return quadraticBezier(tStart, tMid, tEnd, pointsPerTwist, tPoint);
    }

    // TODO refactor/merge to universal bezier calculator
    private Point linearBezier(Point start, Point end, int steps, int currentStep) {
        double stepSize = 1.0/steps;
        double t = currentStep * stepSize;

        return new Point(
                (int)( (1-t)*start.x + t*end.x ),
                (int)( (1-t)*start.y + t*end.y )
        );
    }

    private Point quadraticBezier(Point start, Point mid, Point end, int steps, int currentStep) {
        double stepSize = 1.0/steps;
        double t = currentStep * stepSize;

        return new Point(
                (int) ( (start.x - 2*mid.x + end.x)*t*t + (-2*start.x + 2*mid.x)*t + start.x ),
                (int) ( (start.y - 2*mid.y + end.y)*t*t + (-2*start.y + 2*mid.y)*t + start.y )
        );
    }
}
