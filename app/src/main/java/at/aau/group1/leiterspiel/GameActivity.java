package at.aau.group1.leiterspiel;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {

    // controlling the framerate
    private final int FPS = 30;
    private Timer graphicsTimer;
    private TimerTask drawTask;
    private boolean graphicsActive = false;

    // drawing the graphics
    private int canvasWidth;
    private int canvasHeight;
    private Bitmap bg;
    private Canvas canvas;
    private LinearLayout layout;

    // style of the GameBoard
    private final int MIN_VERTICAL_FIELDS = 3;
    private int minHorizontalFields = 8; // sets how many fields should be aligned horizontally, in portrait mode
    private int fieldRadius;
    private int horizontalFields; // number of fields in the horizontal lines
    private int verticalFields; // number of fields aligned vertically, connecting the horizontal lines

    private GameManager gameManager;

    private void init() {
        layout = (LinearLayout) findViewById(R.id.gameCanvas);

        canvasWidth = layout.getWidth();
        canvasHeight = layout.getHeight();
        Log.d("Tag", "canvas size set to " + canvasWidth + "x" + canvasHeight);

        gameManager = new GameManager();
        bg = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        init();

        // creating a tiny test game
        gameManager.getGameBoard().setNumberOfFields(60);
        gameManager.addLadder(new Ladder(Ladder.LadderType.BIDIRECTIONAL, 5, 15));
        gameManager.addLadder(new Ladder(Ladder.LadderType.DOWN, 23, 37));
        gameManager.addLadder(new Ladder(Ladder.LadderType.UP, 42, 48));

        Player player0 = new Player("Player 0");
        Player player1 = new Player("Player 1");
        Player player2 = new Player("Player 2");
        gameManager.addPlayer(player0);
        gameManager.addPlayer(player1);
        gameManager.addPlayer(player2);

        buildBoard(); // generate game board

        if (!graphicsActive) {
            graphicsTimer = new Timer();
            drawTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            drawGame();
                        }
                    });
                }
            };
            graphicsTimer.scheduleAtFixedRate(drawTask, 0, 1000 / FPS); // start graphics output
            graphicsActive = true;
        }

        gameManager.startSim(); // temporary for testing; crashes app when activity pauses.
    }

    @Override
    protected void onStop() {
        super.onStop();

        drawTask.cancel(); // stop graphics output
        graphicsTimer.cancel();
        graphicsActive = false;
    }

    private void drawGame() {
        clearCanvas(); // clear previous frame
        drawBoard(); // draw the board on the canvas
        drawPieces(); // draw the players on the canvas
        layout.setBackground(new BitmapDrawable(bg)); // display updated canvas
    }

    private void buildBoard() {

        if (canvasHeight < canvasWidth) { // if the device is in landscape mode, align more fields horizontally
            double m = canvasWidth / canvasHeight;
            horizontalFields = (int) (minHorizontalFields * m);
        } else horizontalFields = minHorizontalFields;

        fieldRadius = (int) (canvasWidth*0.9 / horizontalFields / 2.5);
        int maxVerticalFields = (int) (canvasHeight*0.9 / (fieldRadius*2.5));
        int remainingFields = gameManager.getGameBoard().getNumberOfFields() - maxVerticalFields;
        int layers = (int) Math.ceil(remainingFields / horizontalFields);
        verticalFields = (int) Math.max((gameManager.getGameBoard().getNumberOfFields() - layers * horizontalFields) /
                layers, MIN_VERTICAL_FIELDS);

        // 1 layer = 1 horizontal line + 1 vertical line
        int layerWidth = (int) (canvasWidth * 0.85);
        int layerHeight = (int) (canvasHeight * 0.9) / layers;
//        int layerHeight = (int) Math.max(verticalFields * fieldRadius*1.5, canvasHeight * 0.9 / layers);
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
                if (currentField+1 > gameManager.getGameBoard().getNumberOfFields()) break;

                // calculate the actual position of the field
                Point pos;
                if(!direction) pos = linearBezier(p0, p1, horizontalFields + 1, h);
                else pos = linearBezier(p1, p0, horizontalFields + 1, h);

                storeField(pos, currentField);

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
                if (currentField+1 > gameManager.getGameBoard().getNumberOfFields()) break;

                Point pos = linearBezier(p0, p1, verticalFields - 1, v);

                storeField(pos, currentField);

                currentField++;
            }

            direction = !direction;
        }

    }

    private void storeField(Point pos, int currentField) {
        // field type
        GameField.FieldType type = GameField.FieldType.DEFAULT;
        if (currentField==0)
            type = GameField.FieldType.START;
        else if (currentField==gameManager.getGameBoard().getNumberOfFields()-1)
            type = GameField.FieldType.FINISH;
        else {
            Ladder ladder = gameManager.getGameBoard().getLadderOnField(currentField);
            if (ladder != null && currentField==ladder.getStartField())
                type = GameField.FieldType.LADDER_START;
        }
        // store the field in the array
        gameManager.getGameBoard().getFields()[currentField] = new GameField(pos, type);
    }

    private void clearCanvas() {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRect(0, 0, canvasWidth, canvasHeight, paint);
    }

    private void drawBoard() {
        GameField[] fields = gameManager.getGameBoard().getFields();
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(fieldRadius);

        // draw the fields
        for (GameField field:fields) {
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
            // invert Y coordinate because 2D Y axis is inverted
            canvas.drawCircle(field.getPos().x, canvasHeight - field.getPos().y, fieldRadius, paint);
        }
        // draw the ladders
        for (Ladder ladder:gameManager.getGameBoard().getLadders()) {
            GameField start = gameManager.getGameBoard().getFields()[ladder.getStartField()];
            GameField end = gameManager.getGameBoard().getFields()[ladder.getEndField()];

            paint.setColor(Color.YELLOW);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(start.getPos().x, canvasHeight - start.getPos().y, fieldRadius-1, paint);
            canvas.drawCircle(end.getPos().x, canvasHeight - end.getPos().y, fieldRadius-1, paint);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(start.getPos().x, canvasHeight - start.getPos().y,
                    end.getPos().x, canvasHeight - end.getPos().y,
                    paint);

            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            if (ladder.getType() == Ladder.LadderType.UP) // Rolltreppe
                canvas.drawText("R", start.getPos().x, canvasHeight - start.getPos().y, paint);
            if (ladder.getType() == Ladder.LadderType.DOWN) // Aal
                canvas.drawText("A", start.getPos().x, canvasHeight - start.getPos().y, paint);
        }

//        // 1 layer = 1 horizontal line + 1 vertical line
//        int layers = (int) Math.ceil( (double) gameManager.getGameBoard().getNumberOfFields() /
//                (double) (horizontalFields + verticalFields) );
//        int layerWidth = (int) (canvasWidth * 0.8);
//        int layerHeight = (int) (canvasHeight * 0.9) / layers;
//        int fieldRadius;
//        if(canvasWidth < canvasHeight) fieldRadius = layerWidth/(horizontalFields +2)/2;
//        else fieldRadius = layerHeight / verticalFields / 2;
//
//        int currentField = 0;
//        boolean direction = false; // true = left, false = right
//        for (int n=0; n<layers; n++) {
//            Point p0, p1;
//            // draw horizontal line of fields
//            p0 = new Point((canvasWidth - layerWidth)/2, padding + (n * layerHeight));
//            p1 = new Point(p0.x + layerWidth, p0.y);
//            for (int h=1; h<= horizontalFields; h++) {
//                if (++currentField > gameManager.getGameBoard().getNumberOfFields()) break;
//
//                Point pos;
//                if(!direction) pos = linearBezier(p0, p1, horizontalFields +1, h);
//                else pos = linearBezier(p1, p0, horizontalFields +1, h);
//
//                // draw the field
//                if (gameManager.getGameBoard().getLadderOnField(currentField)!=null) {
//                    paint.setColor(Color.BLUE);
//                    paint.setStyle(Paint.Style.FILL);
//                } else {
//                    paint.setColor(Color.BLACK);
//                    paint.setStyle(Paint.Style.STROKE);
//                }
//                canvas.drawCircle(pos.x, pos.y, fieldRadius, paint);
//            }
//
//            // draw vertical part of the layer
//            if(!direction) {
//                p0 = new Point(linearBezier(p0, p1, horizontalFields +1, horizontalFields +1).x, padding + (n * layerHeight));
//                p1 = new Point(p0.x, p0.y + layerHeight);
//            } else {
//                p0 = new Point(linearBezier(p0, p1, horizontalFields +1, 0).x, padding + (n * layerHeight));
//                p1 = new Point(p0.x, p0.y + layerHeight);
//            }
//            for (int v=0; v< verticalFields; v++) {
//                if (++currentField > gameManager.getGameBoard().getNumberOfFields()) break;
//
//                Point pos = linearBezier(p0, p1, verticalFields -1, v);
//
//                // draw the field
//                if (gameManager.getGameBoard().getLadderOnField(currentField)!=null) {
//                    paint.setColor(Color.BLUE);
//                    paint.setStyle(Paint.Style.FILL);
//                } else {
//                    paint.setColor(Color.BLACK);
//                    paint.setStyle(Paint.Style.STROKE);
//                }
//                canvas.drawCircle(pos.x, pos.y, fieldRadius, paint);
//            }
//
//            direction = !direction;
//        }

//        canvas.drawRect(0,0,100,100,paint);
    }

    private void drawPieces() {
        Paint paint = new Paint();
        paint.setTextSize((int) (fieldRadius * 1.7));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        for (Piece piece:gameManager.getGameBoard().getPieces()) {
            Point pos = gameManager.getGameBoard().getFields()[piece.getField()].getPos();
            canvas.drawText(piece.getPlayerID()+"", pos.x, canvasHeight - pos.y + paint.getTextSize()/2.5f, paint);
        }
    }

    private Point linearBezier(Point start, Point end, int steps, int currentStep) {
        double stepSize = 1.0/steps;
        double t = currentStep * stepSize;

        return new Point(
                (int)( (1-t)*start.x + t*end.x ),
                (int)( (1-t)*start.y + t*end.y )
        );
    }
}
