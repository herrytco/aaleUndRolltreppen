package at.aau.group1.leiterspiel.Game;

import android.graphics.Point;

/**
 * Created by Igor on 02.05.2016.
 *
 * Represents combinations of Bezier curves for amazing snake graphics
 */
public class Snake {

    private final int MAX_DX = 250;
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

        int dx = start.x - end.x;
        int dy = start.y - end.y;
        // scaling dx, dy to a maximum
        if (dx > MAX_DX || dy > MAX_DX) {
            float div;
            if (dx/MAX_DX > dy/MAX_DX) div = dx/MAX_DX;
            else div = dy/MAX_DX;
            if (div < 0) div = -div;

            dx /= div;
            dy /= div;
        }
        if (currentTwist % 2 == 0 ^ mirror) { // ^ = XOR
            // applying vector normal( Normal = (-dy, dx) )
            tMid.x -= dy/4;
            tMid.y += dx/4;
        } else {
            tMid.x += dy/4;
            tMid.y -= dx/4;
        }

        return quadraticBezier(tStart, tMid, tEnd, pointsPerTwist, tPoint);
    }

    /**
     * Calculates a linear bezier "curve" and returns the position of the requested point on the
     * curve.
     *
     * @param start Position of the curve's start point
     * @param end Position of the curve's end point
     * @param steps Number of points that are evenly distributed over the curve
     * @param currentStep Index of the requested point
     * @return Position of the requested point
     */
    public static Point linearBezier(Point start, Point end, int steps, int currentStep) {
        double stepSize = 1.0/steps;
        double t = currentStep * stepSize;

        return new Point(
                (int)( (1-t)*start.x + t*end.x ),
                (int)( (1-t)*start.y + t*end.y )
        );
    }

    public static Point quadraticBezier(Point start, Point mid, Point end, int steps, int currentStep) {
        double stepSize = 1.0/steps;
        double t = currentStep * stepSize;

        return new Point(
                (int) ( (start.x - 2*mid.x + end.x)*t*t + (-2*start.x + 2*mid.x)*t + start.x ),
                (int) ( (start.y - 2*mid.y + end.y)*t*t + (-2*start.y + 2*mid.y)*t + start.y )
        );
    }
}
