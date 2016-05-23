package at.aau.group1.leiterspiel;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.ArrayList;

/**
 * Created by Igor on 02.05.2016.
 */
public class GamePainter {

    private int canvasWidth, canvasHeight;
    private Bitmap bmp;
    private Canvas canvas;

    // images to be drawn on the canvas
    private Bitmap bgTile;
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
    private Bitmap[] ladderImgs;
    private Bitmap boardImg;

    // style of the GameBoard
    private final double PIECE_SIZE_FACTOR = 2.1;
    private final int MIN_VERTICAL_FIELDS = 3;
    private int minHorizontalFields = 8; // sets how many fields should be aligned horizontally, in portrait mode
    private int fieldRadius = 0;
    private int oldFieldRadius = 0;
    private boolean sizeInitialized = false;
    private int horizontalFields; // number of fields in the horizontal lines
    private int verticalFields; // number of fields aligned vertically, connecting the horizontal lines

    public GamePainter(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
        init();
    }

    private void init() {
        bmp = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bmp);
    }

    public void setDimensions(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
        init();
    }

    public void setBackgroundImg(Bitmap background) {
        bgTile = background;
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

    private void resizeResources(GameBoard gameBoard) {
        if (fieldImg!=null )
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
        ladderImgs = new Bitmap[gameBoard.getLadders().size()];
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
            double m = canvasWidth / canvasHeight;
            horizontalFields = (int) (minHorizontalFields * m);
        } else horizontalFields = minHorizontalFields;
        // experimental
//        if (canvasHeight < canvasWidth) {
//            canvasWidth = canvasHeight;
//        }
//        horizontalFields = minHorizontalFields;

        oldFieldRadius = fieldRadius;
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

        resizeResources(gameBoard);

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
    public void clearCanvas() {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRect(0, 0, canvasWidth - 1, canvasHeight - 1, paint);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0, 0, canvasWidth - 1, canvasHeight - 1, paint);
    }

    /**
     * Draws the game board(fields, ladders) on the canvas using the coordinates stored in
     * gameBoard.
     */
    public void drawBoard(GameBoard gameBoard) {
        if (!sizeInitialized) resizeResources(gameBoard);

        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(fieldRadius);

        if (boardImg!=null) {
            canvas.drawBitmap(boardImg, 0, 0, paint);

            GameField[] fields = gameBoard.getFields();
                if (fieldImg != null) { // if a field image exists then use it, or fall back to ugly graphics
                    for (GameField field: fields) {
                        if (field.isHighlighted())
                            canvas.drawBitmap(scaledFieldHighlightImg, field.getPos().x - fieldRadius, canvasHeight - field.getPos().y - fieldRadius, paint);
                    }
                } else {
                    for (GameField field: fields) {
                        if (field.isHighlighted()) {
                            paint.setColor(Color.GREEN);
                            paint.setStyle(Paint.Style.FILL);
                            canvas.drawCircle(field.getPos().x, canvasHeight - field.getPos().y, (int) (fieldRadius * 1.2), paint);
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

        // draw the background
        if (bgTile!=null) { // if a background image exists
            int bgWidth = bgTile.getWidth();
            int bgHeight = bgTile.getHeight();
            for (int x = 0; x < canvasWidth; x += bgWidth) {
                for (int y = 0; y < canvasHeight; y += bgHeight) {
                    boardCanvas.drawBitmap(bgTile, x, y, paint);
                }
            }
        }

        // draw the fields
        if (fieldImg != null) { // if a field image exists then use it, or fall back to ugly graphics
            for (GameField field: fields) {
                if (!field.isHighlighted()) boardCanvas.drawBitmap(scaledFieldImg, field.getPos().x - fieldRadius, canvasHeight - field.getPos().y - fieldRadius, paint);
                else boardCanvas.drawBitmap(scaledFieldHighlightImg, field.getPos().x - fieldRadius, canvasHeight - field.getPos().y - fieldRadius, paint);
            }
        } else {
            for (GameField field : fields) {
                // color and style
                if (field.getType() == GameField.FieldType.DEFAULT) {
                    paint.setColor(Color.BLACK);
                    paint.setStyle(Paint.Style.STROKE);
                } else if (field.getType() == GameField.FieldType.START) {
                    paint.setColor(Color.GRAY);
                    paint.setStyle(Paint.Style.FILL);
                } else if (field.getType() == GameField.FieldType.FINISH) {
                    paint.setColor(Color.CYAN);
                    paint.setStyle(Paint.Style.FILL);
                }

                if (field.isHighlighted()) {
                    paint.setColor(Color.GREEN);
                    paint.setStyle(Paint.Style.FILL);
                    boardCanvas.drawCircle(field.getPos().x, canvasHeight - field.getPos().y, (int) (fieldRadius * 1.2), paint);
                }

                // invert Y coordinate because 2D Y axis is inverted
                boardCanvas.drawCircle(field.getPos().x, canvasHeight - field.getPos().y, fieldRadius, paint);
            }
        }

        // draw the ladders
        if (ladderImgs.length>0 && ladderImgs[0]==null) initDrawLadders(gameBoard);
        int index = 0;
        for (Ladder ladder : gameBoard.getLadders()) {
            GameField start = gameBoard.getFields()[ladder.getStartField()];
            GameField end = gameBoard.getFields()[ladder.getEndField()];

            if (fieldUpImg != null) {
                boardCanvas.drawBitmap(ladderImgs[index], 0, 0, paint);

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
            } else { // fallback
                paint.setColor(Color.YELLOW);
                paint.setStyle(Paint.Style.FILL);
                boardCanvas.drawCircle(start.getPos().x, canvasHeight - start.getPos().y, fieldRadius - 1, paint);
                boardCanvas.drawCircle(end.getPos().x, canvasHeight - end.getPos().y, fieldRadius - 1, paint);
                paint.setColor(Color.BLACK);
                paint.setStyle(Paint.Style.STROKE);
                boardCanvas.drawLine(start.getPos().x, canvasHeight - start.getPos().y,
                        end.getPos().x, canvasHeight - end.getPos().y,
                        paint);

                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                if (ladder.getType() == Ladder.LadderType.UP) // Rolltreppe
                    boardCanvas.drawText("R", start.getPos().x, canvasHeight - start.getPos().y, paint);
                if (ladder.getType() == Ladder.LadderType.DOWN) // Aal
                    boardCanvas.drawText("A", start.getPos().x, canvasHeight - start.getPos().y, paint);
            }
            index++;
        }

    }

    private void initDrawLadders(GameBoard gameBoard) {
        Paint ladderPaint = new Paint();

        int index = 0;
        for (Ladder ladder: gameBoard.getLadders()) {
            ladderImgs[index] = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
            Canvas ladderCanvas = new Canvas(ladderImgs[index]);
            GameField start = gameBoard.getFields()[ladder.getStartField()];
            GameField end = gameBoard.getFields()[ladder.getEndField()];
            // calculate length of the ladder in pixels
            int length = (int) Math.hypot(Math.abs(start.getPos().x - end.getPos().x),
                    Math.abs(start.getPos().y - end.getPos().y));

            // TODO create + draw eels & escalators
            if (ladder.getType() == Ladder.LadderType.UP) { // Rolltreppen
                ladderPaint.setColor(Color.BLACK);
                ladderPaint.setStyle(Paint.Style.STROKE);
                ladderCanvas.drawLine(start.getPos().x, canvasHeight - start.getPos().y,
                        end.getPos().x, canvasHeight - end.getPos().y,
                        ladderPaint);
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
    public void drawPieces(GameBoard gameBoard) {
        if(!sizeInitialized) resizeResources(gameBoard);

        int pieceSize = (int) (fieldRadius * PIECE_SIZE_FACTOR);
        Paint paint = new Paint();
        paint.setTextSize(pieceSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        // draw the stuff
        for (Piece piece:gameBoard.getPieces()) {
            Point imgPos = gameBoard.getFields()[piece.getField()].getPos();
            imgPos = shufflePiecePosition(imgPos, piece, pieceSize); // in case multiple pieces are on a single field, change their position a bit to keep them all visible
            if (scaledPieceImgs.size() > 0 && piece.getPlayerID() < scaledPieceImgs.size())
                canvas.drawBitmap(scaledPieceImgs.get(piece.getPlayerID()), imgPos.x - fieldRadius, canvasHeight - imgPos.y - fieldRadius, paint);
            else // fallback to ugly graphics
                canvas.drawText(piece.getPlayerID()+"", imgPos.x, canvasHeight - imgPos.y + paint.getTextSize()/2.5f, paint);
        }
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
    private Point shufflePiecePosition(Point pos, Piece piece, int size) {
        Point p = new Point(pos.x, pos.y);
        int v = (piece.getField() + piece.getPlayerID()) % 4;
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
}
