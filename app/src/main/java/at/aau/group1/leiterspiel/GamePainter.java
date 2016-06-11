package at.aau.group1.leiterspiel;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import at.aau.group1.leiterspiel.Game.GameBoard;
import at.aau.group1.leiterspiel.Game.GameField;
import at.aau.group1.leiterspiel.Game.Ladder;
import at.aau.group1.leiterspiel.Game.Piece;
import at.aau.group1.leiterspiel.Game.Snake;

/**
 * Created by Igor on 02.05.2016.
 */
public class GamePainter {

    private boolean boardBuilt = false;
    private int canvasWidth, canvasHeight;
    private Bitmap bmp;
    private Canvas canvas;
    private PaintTask paintTask;
    private boolean frameFinished;

    // images to be drawn on the canvas
    private Bitmap escImg;
    private Bitmap scaledEscImg;
    private Bitmap fieldImg;
    private Bitmap scaledFieldImg;
    private Bitmap fieldHighlightImg;
    private Bitmap scaledFieldHighlightImg;
    private Bitmap fieldUpImg;
    private Bitmap scaledFieldUpImg;
    private Bitmap fieldDownImg;
    private Bitmap scaledFieldDownImg;
    private ArrayList<Bitmap> pieceImgs = new ArrayList<Bitmap>();
    private ArrayList<Bitmap> scaledPieceImgs = new ArrayList<Bitmap>();
    private Bitmap boardImg;

    // style of the GameBoard
    private final double PIECE_SIZE_FACTOR = 2.1;
    private final int MIN_VERTICAL_FIELDS = 3;
    private int minHorizontalFields = 8; // sets how many fields should be aligned horizontally, in portrait mode
    private int fieldRadius = 0;
    private boolean sizeInitialized = false;
    private int horizontalFields; // number of fields in the horizontal lines
    private int verticalFields; // number of fields aligned vertically, connecting the horizontal lines
    // for centering the gameboard on the canvas
    private int xOffset = 0;
    private int yOffset = 0;

    public GamePainter(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
        init();
    }

    public int getXOffset() { return xOffset; }
    public int getYOffset() { return yOffset; }

    private void init() {
        bmp = null;
        bmp = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bmp);
        paintTask = new PaintTask();
        frameFinished = true;
        xOffset = 0;
        yOffset = 0;
    }

    public void setDimensions(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
        init();
    }

    public void setEscalatorImg(Bitmap img) {
        escImg = img;
        sizeInitialized = false;
    }

    public void setFieldImg(Bitmap field, Bitmap highlight) {
        fieldImg = field;
        fieldHighlightImg = highlight;
        sizeInitialized = false;
    }

    public void setLadderFieldImg(Bitmap up, Bitmap down) {
        fieldUpImg = up;
        fieldDownImg = down;
        sizeInitialized = false;
    }

    public void addPieceImg(Bitmap img) {
        pieceImgs.add(img);
        scaledPieceImgs.add(null);
        sizeInitialized = false;
    }

    public Bitmap getFrame() {
        return bmp;
    }

    private void resizeResources() {
        if (!boardBuilt) return;

        if (escImg!=null)
            scaledEscImg = Bitmap.createScaledBitmap(escImg, fieldRadius*2, fieldRadius*2/3, false);
        if (fieldImg!=null)
            scaledFieldImg = Bitmap.createScaledBitmap(fieldImg, fieldRadius*2, fieldRadius*2, false);
        if (fieldHighlightImg!=null)
            scaledFieldHighlightImg = Bitmap.createScaledBitmap(fieldHighlightImg, fieldRadius*2, fieldRadius*2, false);
        if (fieldUpImg!=null)
            scaledFieldUpImg = Bitmap.createScaledBitmap(fieldUpImg, fieldRadius*2, fieldRadius*2, false);
        if (fieldDownImg!=null)
            scaledFieldDownImg = Bitmap.createScaledBitmap(fieldDownImg, fieldRadius*2, fieldRadius*2, false);
        int pieceSize = (int) (fieldRadius * PIECE_SIZE_FACTOR);
        for (int n=0; n<pieceImgs.size(); n++) {
            scaledPieceImgs.set(n, Bitmap.createScaledBitmap(pieceImgs.get(n), pieceSize, pieceSize, false));
        }
        boardImg = null;
        sizeInitialized = true;
    }

    /**
     * Calculates all the positions of the fields and ladders on the game board, based on the canvas
     * size and the data stored in gameManager/gameBoard.
     * Needs to be called once whenever the canvas size or game board change.
     */
    public void buildBoard(GameBoard gameBoard) {

        if (canvasHeight < canvasWidth) { // if the device is in landscape mode, align more fields horizontally
            double m = (double) canvasWidth / (double) canvasHeight;
            horizontalFields = (int) (minHorizontalFields * m);
        } else horizontalFields = minHorizontalFields;
        // experimental
//        if (canvasHeight < canvasWidth) {
//            int w = canvasWidth;
//            canvasWidth = canvasHeight;
//            xOffset = (w-canvasWidth)/2;
//        }
//        horizontalFields = minHorizontalFields;

        fieldRadius = (int) (canvasWidth*0.9 / horizontalFields / 3);
        int maxVerticalFields = (int) (canvasHeight*0.9 / (fieldRadius*3));
        int remainingFields = gameBoard.getNumberOfFields() - maxVerticalFields;
        int layers = (int) Math.ceil(remainingFields / horizontalFields);
        verticalFields = (int) Math.max((gameBoard.getNumberOfFields() - layers * horizontalFields) /
                layers, MIN_VERTICAL_FIELDS);

        // 1 layer = 1 horizontal line + 1 vertical line
        int layerWidth = (int) (canvasWidth * 0.85);
        int layerHeight = (int) (canvasHeight * 0.9) / layers;
        int yOffset = (canvasHeight - layers*layerHeight)/2;

        int currentField = 0;
        boolean direction = false; // true = left, false = right
        for (int n=0; n<layers; n++) {
            Point p0, p1;
            // calculate horizontal line of fields
            p0 = new Point((canvasWidth - layerWidth)/2, yOffset + (n * layerHeight)); // left-most point
            p1 = new Point(p0.x + layerWidth, p0.y); // right-most point
            for (int h=1; h<= horizontalFields; h++) {
                // end if all fields were calculated
                if (currentField+1 > gameBoard.getNumberOfFields()) break;

                // calculate the actual position of the field
                Point pos;
                if(!direction) pos = linearBezier(p0, p1, horizontalFields + 1, h);
                else pos = linearBezier(p1, p0, horizontalFields + 1, h);

                storeField(pos, currentField, gameBoard);

                currentField++;
            }

            // vertical part of the layer
            if(!direction) { // if the fields should be on the left or right edge of the canvas
                p0 = new Point(linearBezier(p0, p1, horizontalFields +1, horizontalFields +1).x, yOffset + (n * layerHeight));
                p1 = new Point(p0.x, p0.y + layerHeight);
            } else {
                p0 = new Point(linearBezier(p0, p1, horizontalFields +1, 0).x, yOffset + (n * layerHeight));
                p1 = new Point(p0.x, p0.y + layerHeight);
            }
            for (int v=0; v < verticalFields; v++) {
                // end if all fields were calculated
                if (currentField+1 > gameBoard.getNumberOfFields()) break;

                Point pos = linearBezier(p0, p1, verticalFields - 1, v);

                storeField(pos, currentField, gameBoard);

                currentField++;
            }

            direction = !direction;
        }

        boardBuilt = true;
        resizeResources();

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
    private Point linearBezier(Point start, Point end, int steps, int currentStep) {
        double stepSize = 1.0/steps;
        double t = currentStep * stepSize;

        return new Point(
                (int)( (1-t)*start.x + t*end.x ),
                (int)( (1-t)*start.y + t*end.y )
        );
    }

    /**
     * Helper method of buildBoard(). Generates a new GameField, sets its correct field type
     * based on gameBoard's data and stores the new GameField in the GameBoard.
     *
     * @param pos xy coordinates of the field on the canvas
     * @param currentField field index, ranging from 0 to gameBoard.getNumberOfFields()
     */
    private void storeField(Point pos, int currentField, GameBoard gameBoard) {
        // field type
        GameField.FieldType type = GameField.FieldType.DEFAULT;
        if (currentField==0)
            type = GameField.FieldType.START;
        else if (currentField==gameBoard.getNumberOfFields()-1)
            type = GameField.FieldType.FINISH;
        else {
            Ladder ladder = gameBoard.getLadderOnField(currentField);
            if (ladder != null && currentField==ladder.getStartField())
                type = GameField.FieldType.LADDER_START;
        }
        // store the field in the array
        gameBoard.getFields()[currentField] = new GameField(pos, type);
    }

    /**
     * clears the canvas by drawing a white filled rectangle over everything.
     */
    private void clearCanvas() {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }

    /**
     * Draws the game board(fields, ladders) on the canvas using the coordinates stored in
     * gameBoard.
     */
    private void drawBoard(GameBoard gameBoard) {
        if (!boardBuilt) return;
        if (!sizeInitialized) resizeResources();

        Paint paint = new Paint();

        if (boardImg!=null) {
            canvas.drawBitmap(boardImg, xOffset, yOffset, paint);

            GameField[] fields = gameBoard.getFields();
            if (fieldImg != null) {
                for (GameField field: fields) {
                    if (field.isHighlighted()){
                        canvas.drawBitmap(scaledFieldHighlightImg, field.getPos().x - fieldRadius + xOffset, canvasHeight - field.getPos().y - fieldRadius - yOffset, paint);
                        break; // assuming there's never more than one highlighted field
                    }
                }
            }
        } else {
            initDrawBoard(gameBoard);
        }

    }

    private void initDrawBoard(GameBoard gameBoard) {
        GameField[] fields = gameBoard.getFields();
        Paint paint = new Paint();
        boardImg = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        Canvas boardCanvas = new Canvas(boardImg);

        // draw the ladders
        initDrawLadders(gameBoard);

        // draw the fields
        if (fieldImg != null) { // if a field image exists then use it, or fall back to ugly graphics
            for (GameField field: fields) {
                if (!field.isHighlighted()) boardCanvas.drawBitmap(scaledFieldImg, field.getPos().x - fieldRadius, canvasHeight - field.getPos().y - fieldRadius, paint);
                else boardCanvas.drawBitmap(scaledFieldHighlightImg, field.getPos().x - fieldRadius, canvasHeight - field.getPos().y - fieldRadius, paint);
            }
        }

        // draw the highlighted ladder fields
        int index = 0;
        for (Ladder ladder : gameBoard.getLadders()) {
            GameField start = gameBoard.getFields()[ladder.getStartField()];
            GameField end = gameBoard.getFields()[ladder.getEndField()];

            if (fieldUpImg != null) {

                if (ladder.getType() == Ladder.LadderType.UP) {
                    if (!start.isHighlighted())
                        boardCanvas.drawBitmap(scaledFieldUpImg, start.getPos().x - fieldRadius, canvasHeight - start.getPos().y - fieldRadius, paint);
                    if (!end.isHighlighted())
                        boardCanvas.drawBitmap(scaledFieldUpImg, end.getPos().x - fieldRadius, canvasHeight - end.getPos().y - fieldRadius, paint);
                } else if (ladder.getType() == Ladder.LadderType.DOWN) {
                    if (!start.isHighlighted())
                        boardCanvas.drawBitmap(scaledFieldDownImg, start.getPos().x - fieldRadius, canvasHeight - start.getPos().y - fieldRadius, paint);
                    if (!end.isHighlighted())
                        boardCanvas.drawBitmap(scaledFieldDownImg, end.getPos().x - fieldRadius, canvasHeight - end.getPos().y - fieldRadius, paint);
                }
            }
            index++;
        }

    }

    private void initDrawLadders(GameBoard gameBoard) {
        Paint ladderPaint = new Paint();

        Canvas ladderCanvas = new Canvas(boardImg);

        int index = 0;
        for (Ladder ladder: gameBoard.getLadders()) {

            GameField start = gameBoard.getFields()[ladder.getStartField()];
            GameField end = gameBoard.getFields()[ladder.getEndField()];
            // calculate length of the ladder in pixels
            int length = (int) Math.hypot(Math.abs(start.getPos().x - end.getPos().x),
                    Math.abs(start.getPos().y - end.getPos().y));

            if (ladder.getType() == Ladder.LadderType.UP && scaledEscImg != null) { // Rolltreppen
                // calculate the angle of the escalator
                int dx = end.getPos().x - start.getPos().x;
                int dy = end.getPos().y - start.getPos().y;
                double escAngle = Math.atan2(dy, dx);
                // get the length of the escalator using Pythagoras
                int escLength = (int) Math.hypot(Math.abs(dx), Math.abs(dy));
                int stepSize = scaledEscImg.getHeight();
                int escSteps = escLength/stepSize;
                // create the escalator
                Bitmap escalator = Bitmap.createBitmap(scaledEscImg.getWidth(), escLength, Bitmap.Config.ARGB_8888);
                Canvas escCanvas = new Canvas(escalator);
                for (int step = 0; step < escSteps; step++) {
                    escCanvas.drawBitmap(scaledEscImg, 0, step*stepSize, ladderPaint);
                }
                // set rotation/translation matrix
                Matrix matrix = new Matrix();
                matrix.postRotate((float) Math.toDegrees(-escAngle)+90);
                // draw the escalator onto the gameboard
                escalator = Bitmap.createBitmap(escalator, 0, 0, escalator.getWidth(), escalator.getHeight(), matrix, false);
                // if-else is experimental, hopefully it (more or less) corrects the x coordinate for all cases
                if (Math.toDegrees(-escAngle)+90 < 0) ladderCanvas.drawBitmap(escalator, start.getPos().x - escalator.getWidth(), canvasHeight - (start.getPos().y + escalator.getHeight() - stepSize), ladderPaint);
                else ladderCanvas.drawBitmap(escalator, start.getPos().x - escalator.getWidth()/2, canvasHeight - (start.getPos().y + escalator.getHeight() - stepSize), ladderPaint);

            } else if (ladder.getType() == Ladder.LadderType.DOWN) { // Aaaaaaale
                ladderPaint.setColor(Color.argb(255, 50, 128, 75));
                ladderPaint.setStyle(Paint.Style.FILL_AND_STROKE);

                Point eelStart = new Point(end.getPos().x, end.getPos().y - (int) (fieldRadius * 1.1));
                Point eelEnd = new Point(start.getPos().x, start.getPos().y + fieldRadius / 2);
                int eelTwists = length / 100;
                int eelSegments = length / 5;
                Snake eel = new Snake(eelStart, eelEnd, eelTwists);
                for (int n = 0; n < eelSegments; n++) {
                    Point p = eel.getPoint(eelSegments, n);
                    ladderCanvas.drawCircle(p.x, canvasHeight - p.y, (float) (fieldRadius / 2.5), ladderPaint);
                }
                ladderCanvas.drawCircle(eelStart.x, canvasHeight - eelStart.y, (float) (fieldRadius / 1.75), ladderPaint);
                ladderPaint.setColor(Color.WHITE);
                ladderCanvas.drawCircle(eelStart.x, canvasHeight - eelStart.y + 10, 3, ladderPaint);
            }
            index++;
        }
    }

    /**
     * Draws the player's pieces on the canvas.
     */
    private void drawPieces(GameBoard gameBoard) {
        if (!boardBuilt) return;
        if (!sizeInitialized) resizeResources();

        int pieceSize = (int) (fieldRadius * PIECE_SIZE_FACTOR);
        Paint paint = new Paint();

        // draw the stuff
        for (Piece piece:gameBoard.getPieces()) {
            // position of the piece being drawn
            Point imgPos = gameBoard.getFields()[piece.getField()].getPos();
            imgPos = shufflePiecePosition(imgPos, piece, piece.getField(), pieceSize); // in case multiple pieces are on a single field, change their position a bit to keep them all visible

            if (gameBoard.isMoving() && piece.getPlayerID() == gameBoard.getMovingPiece().getPlayerID()) { // if this piece is currently moving, calculate the movement
                gameBoard.updateProgress();
                double progress = gameBoard.getProgress();
                Point oldPos = gameBoard.getFields()[gameBoard.getPreviousField()].getPos(); // position of the field the piece moves from
                oldPos = shufflePiecePosition(oldPos, piece, gameBoard.getPreviousField(), pieceSize);
                int midField = (gameBoard.getPreviousField()+piece.getField())/2; // selecting the field in the middle of the route as third point for the bezier curve
                Point midPos = gameBoard.getFields()[midField].getPos();

                imgPos = quadraticBezier( oldPos, midPos, imgPos, (int)(1.0/gameBoard.getTurnProgressIncrease()), (int)(gameBoard.getProgress()/gameBoard.getTurnProgressIncrease()) );
            }

            if (scaledPieceImgs.size() > 0 && piece.getPlayerID() < scaledPieceImgs.size())
                canvas.drawBitmap(scaledPieceImgs.get(piece.getPlayerID()), imgPos.x - fieldRadius + xOffset, canvasHeight - imgPos.y - fieldRadius - yOffset, paint);
        }
    }

    private Point quadraticBezier(Point start, Point mid, Point end, int steps, int currentStep) {
        double stepSize = 1.0/steps;
        double t = currentStep * stepSize;

        return new Point(
                (int) ( (start.x - 2*mid.x + end.x)*t*t + (-2*start.x + 2*mid.x)*t + start.x ),
                (int) ( (start.y - 2*mid.y + end.y)*t*t + (-2*start.y + 2*mid.y)*t + start.y )
        );
    }

    /**
     * Creates a small offset for the pieces drawn on the board so they can't overlap each other
     * completely.
     *
     * @param pos Original xy position of the piece
     * @param piece Piece to be drawn
     * @param size Size/Diameter of the piece bitmap in pixels
     * @return New position
     */
    private Point shufflePiecePosition(Point pos, Piece piece, int field, int size) {
        Point p = new Point(pos.x, pos.y);
        int v = (field + piece.getPlayerID()) % 4;
        int offset = size/6;
        if (v==0) {
            p.x = p.x - offset;
            p.y = p.y - offset;
        } else if (v==1) {
            p.x = p.x + offset;
            p.y = p.y - offset;
        } else if (v==2) {
            p.x = p.x - offset;
            p.y = p.y + offset;
        } else {
            p.x = p.x + offset;
            p.y = p.y + offset;
        }
        return p;
    }

    public void drawFrame(GameBoard gameBoard) {
        frameFinished = false;
        paintTask.doInBackground(gameBoard);
    }

    public boolean getFinished() { return frameFinished; }

    /**
     * Runs the drawing methods asynchronously so the UI thread doesn't get locked.
     * Doesn't seem to improve the performance, but enables skipping frames in case of a slowdown.
     */
    public class PaintTask extends AsyncTask<GameBoard, Void, Void> {

        @Override
        protected Void doInBackground(GameBoard... params) {
            GameBoard gameBoard = params[0];

            clearCanvas(); // clear previous frame
            drawBoard(gameBoard); // draw the board on the canvas
            drawPieces(gameBoard); // draw the players on the canvas
            frameFinished = true;

            return null;
        }
    }

}
