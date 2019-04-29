package com.maze.simplemaze;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class GameView extends View implements GestureDetector.OnGestureListener{

    private enum Direction{
        UP,DOWN,LEFT,RIGHT
    }
    GestureDetector detector;
    private Cell[][] cells;
    private Cell player,exit;
    private static final int COLS=10,ROWS=14;
    private static final float WALL_THICKNESS = 4;
    private float cellSize,hMargin,vMargin;
    private Paint wallPaint,playerPaint,exitPaint,pathPaint,bitmapPaint;
    private Bitmap bitmap,portalBitmap,wallBitmap;
    private Random random;
    Path movePath;
    Boolean isFinish = false;
    PathMeasure pathMeasure;
    float left,top,right,bottom,exitLeft,exitTop,exitRight,exitBottom,wallLeft,wallTop,wallRight,wallBottom;
    RectF destRect,destRectExit,destRectWall;
    private float pathLength; //动态计算
    private double stepLength = 10;
    private float distanceMoved = 0;
    private float[] positions;
    private int wallWidth, wallHeight,bitmapWidth,bitmapHeight;
    private PopupWindow popupWindow;
    String json;
    FileHelper fileHelper;


    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        wallPaint = new Paint();
        wallPaint.setColor(Color.YELLOW);
        wallPaint.setStrokeWidth(WALL_THICKNESS );

        playerPaint = new Paint();
        playerPaint.setColor(Color.RED);

        exitPaint = new Paint();
        exitPaint.setColor(Color.BLUE);

        pathPaint = new Paint();
        pathPaint.setColor(Color.BLUE);
        pathPaint.setStrokeWidth(8);
        pathPaint.setStyle(Paint.Style.STROKE);
        bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bitmapPaint.setFilterBitmap(true);
        bitmapPaint.setDither(true);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.picature);
        portalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.portal4);
        wallBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.wall);
        wallWidth = wallBitmap.getWidth();
        wallHeight = wallBitmap.getHeight();
        bitmapWidth = bitmap.getWidth();
        bitmapHeight = bitmap.getHeight();
        random = new Random();
        movePath = new Path();
        positions = new float[2];
        fileHelper = new FileHelper(getContext());
        detector = new GestureDetector(getContext(),this);
    }

    private void removeWall(Cell current,Cell next){
        if(current.col == next.col && current.row == next.row+1){
            current.topWall = false;
            next.bottomWall = false;
        }
        if(current.col == next.col && current.row == next.row-1){
            current.bottomWall = false;
            next.topWall = false;
        }
        if(current.col == next.col+1 && current.row == next.row){
            current.leftWall = false;
            next.rightWall = false;
        }
        if(current.col == next.col-1 && current.row == next.row){
            current.rightWall = false;
            next.leftWall = false;
        }
    }

    private Cell getNeighbour(Cell cell){
        ArrayList<Cell> neighbours = new ArrayList<>();

        // left neighbour
        if(cell.col>0)
            if(!cells[cell.col-1][cell.row].visited)
                neighbours.add(cells[cell.col-1][cell.row]);

        // right neighbour
        if(cell.col<COLS-1)
            if(!cells[cell.col+1][cell.row].visited)
                neighbours.add(cells[cell.col+1][cell.row]);

        // top neighbour
        if(cell.row>0)
            if(!cells[cell.col][cell.row-1].visited)
                neighbours.add(cells[cell.col][cell.row-1]);

        // bottom neighbour
        if(cell.row<ROWS-1)
            if(!cells[cell.col][cell.row+1].visited)
                neighbours.add(cells[cell.col][cell.row+1]);

        if(neighbours.size() > 0) {
            int index = random.nextInt(neighbours.size());
            return neighbours.get(index);
        }
        return null;
    }

    //从json文件读取迷宫
    private void readMaze(String filename){
        String jsonArray = "";
        FileHelper fileHelper = new FileHelper(getContext());
        try {
            jsonArray = fileHelper.read(filename);
        } catch (IOException e) {
            e.printStackTrace();
            generateMaze();
            try {
                jsonArray = fileHelper.read(filename);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        Gson gson = new Gson();
        List<Cell> cells1 ;
        cells1 =  gson.fromJson(jsonArray,new TypeToken<List<Cell>>(){}.getType());
        System.out.println(cells1);
        player = cells1.get(0);
        exit = cells1.get(1);
        cells1.remove(0);
        cells1.remove(0);
        cells = new Cell[COLS][ROWS];
        for (Cell cell:cells1) {
            cells[cell.col][cell.row] = cell;
        }
        //movePath.reset();
        //invalidate();
    }

    //生成迷宫json文件
    private void generateMaze(){
        for(int i=0;i<40;i++){
            String filename = "maze"+i+".json";
            createMaze(filename);
        }
    }

    private void createMaze(String filename){
        Stack<Cell> stack = new Stack<>();
        Cell current,next;

        cells = new Cell[COLS][ROWS];
        for (int x=0;x<COLS;x++){
            for (int y=0;y<ROWS;y++){
                cells[x][y] = new Cell(x,y);
            }
        }
        int col1 = random.nextInt(COLS);
        int row1 = random.nextInt(ROWS);
        int col2 = random.nextInt(COLS);
        int row2 = random.nextInt(ROWS);
        player = cells[col1][row1];
        exit = cells[col2][row2];
        current = cells[0][0];
        current.visited = true;
        do {
            next = getNeighbour(current);
            if (next != null) {
                removeWall(current, next);
                stack.push(current);
                current = next;
                current.visited = true;
            } else {
                current = stack.pop();
            }
        }while(!stack.empty());
        List<Cell> cells1 = new ArrayList<>();
        cells1.add(player);
        cells1.add(exit);
        for (int x=0;x<COLS;x++){
            for (int y=0;y<ROWS;y++){
                cells1.add(cells[x][y]);
            }
        }
        Gson gson = new Gson();
        json = gson.toJson(cells1);
        try {
            fileHelper.save(filename,json);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "数据写入失败", Toast.LENGTH_SHORT).show();
        }
        //movePath.reset();
        //invalidate();
    }

    public void moveAnimation() {
        distanceMoved = 0;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawColor(Color.GREEN);

        int width = getWidth();
        int height = getHeight();

        if(width/height < COLS/ROWS)
            cellSize = width/(COLS+1);
        else
            cellSize=height/(ROWS+1);

        hMargin = (width-COLS*cellSize)/2;
        vMargin = (height-ROWS*cellSize)/2;

        canvas.translate(hMargin,vMargin);
        float factor = 0.11f;
        for (int x=0;x<COLS;x++){
            for (int y=0;y<ROWS;y++){
                if(cells[x][y].topWall) {

                    for(int i=0;i<3;i++) {
                        wallLeft = x * cellSize + 2*i*factor*wallWidth- factor * wallWidth;
                        wallTop = y * cellSize - factor * wallHeight;
                        wallRight = x * cellSize + 2*i*factor*wallWidth+ factor * wallWidth;
                        wallBottom = y * cellSize + factor * wallHeight;
                        destRectWall = new RectF(wallLeft, wallTop, wallRight, wallBottom);
                        canvas.drawBitmap(wallBitmap, null, destRectWall, bitmapPaint);
                    }
//                    canvas.drawLine(
//                            x*cellSize,
//                            y*cellSize,
//                            (x+1)*cellSize,
//                            y*cellSize,
//                            wallPaint);
                }
                if(cells[x][y].leftWall) {

                    for(int i=0;i<3;i++) {
                        wallLeft = x * cellSize - factor * wallWidth;
                        wallTop = y * cellSize +2*i*factor*wallHeight - factor * wallHeight;
                        wallRight = x * cellSize + factor * wallWidth;
                        wallBottom = y * cellSize + 2*i*factor*wallHeight+ factor * wallHeight;
                        destRectWall = new RectF(wallLeft, wallTop, wallRight, wallBottom);
                        canvas.drawBitmap(wallBitmap, null, destRectWall, bitmapPaint);
                    }
//                    canvas.drawLine(
//                            x*cellSize,
//                            y*cellSize,
//                            x*cellSize,
//                            (y+1)*cellSize,
//                            wallPaint);
                }
                if(cells[x][y].bottomWall) {

                    for(int i=0;i<4;i++) {
                        wallLeft = x * cellSize + 2*i*factor*wallWidth- factor * wallWidth;
                        wallTop = (y+1) * cellSize - factor * wallHeight;
                        wallRight = x * cellSize + 2*i*factor*wallWidth+ factor * wallWidth;
                        wallBottom = (y+1) * cellSize + factor * wallHeight;
                        destRectWall = new RectF(wallLeft, wallTop, wallRight, wallBottom);
                        canvas.drawBitmap(wallBitmap, null, destRectWall, bitmapPaint);
                    }
//                    canvas.drawLine(
//                            x*cellSize,
//                            (y+1)*cellSize,
//                            (x+1)*cellSize,
//                            (y+1)*cellSize,
//                            wallPaint);
                }
                if(cells[x][y].rightWall) {

                    for(int i=0;i<3;i++) {
                        wallLeft = (x+1) * cellSize - factor * wallWidth;
                        wallTop = y * cellSize +2*i*factor*wallHeight - factor * wallHeight;
                        wallRight = (x+1) * cellSize + factor * wallWidth;
                        wallBottom = y * cellSize + 2*i*factor*wallHeight+ factor * wallHeight;
                        destRectWall = new RectF(wallLeft, wallTop, wallRight, wallBottom);
                        canvas.drawBitmap(wallBitmap, null, destRectWall, bitmapPaint);
                    }
//                    canvas.drawLine(
//                            (x+1)*cellSize,
//                            y*cellSize,
//                            (x+1)*cellSize,
//                            (y+1)*cellSize,
//                            wallPaint);

                }
            }
        }

        float margin = cellSize /10;

        exitLeft = exit.col*cellSize+margin;
        exitTop = exit.row*cellSize+margin;
        exitRight = (exit.col+1)*cellSize-margin;
        exitBottom = (exit.row+1)*cellSize-margin;
        destRectExit = new RectF(exitLeft,exitTop,exitRight,exitBottom);
        canvas.drawBitmap(portalBitmap,null,destRectExit,bitmapPaint);

        pathMeasure = new PathMeasure(movePath,false);
        pathLength = pathMeasure.getLength();
        if (distanceMoved < pathLength) {
            pathMeasure.getPosTan(distanceMoved , positions, null);
            destRect.left = positions[0]-0.5f*cellSize; //0.5f*cellSize
            destRect.top = positions[1]-0.5f*cellSize+2*margin;
            destRect.right = positions[0]+0.5f*cellSize-2*margin;
            destRect.bottom = positions[1]+0.5f*cellSize;
            canvas.drawBitmap(bitmap, null,destRect, bitmapPaint);
            distanceMoved  += stepLength;
            invalidate();
        } else {
            left = player.col * cellSize + margin;
            top = player.row * cellSize + margin;
            right = (player.col + 1) * cellSize - margin;
            bottom = (player.row + 1) * cellSize - margin;
            destRect = new RectF(left,top,right,bottom);
            canvas.drawBitmap(bitmap, null,destRect, bitmapPaint);
        }

//        canvas.drawPath(movePath,pathPaint);
    }

    private void showDialog(){
        LayoutInflater mLayoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup menuView = (ViewGroup) mLayoutInflater.inflate(R.layout.popup_window, null, true);
        Button nextButton = (Button) menuView.findViewById(R.id.next_button);
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();//无法点击其他区域
                //createMaze();
            }
        });
        popupWindow = new PopupWindow(menuView,800, 400, true);
//        popupWindow.setAnimationStyle(R.style.popwin_anim_style);
        popupWindow.setFocusable(false);
        popupWindow.setTouchInterceptor(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        popupWindow.showAtLocation(menuView, Gravity.CENTER,0,0);//设置popupwindow的窗口位置
    }

    private void autoWalk(int preDirection){
        int previousDirection = preDirection;
        while(player.getOutlets() == 2){
            if(!player.topWall && previousDirection != 2){
                player = cells[player.col][player.row-1];
                movePath.rLineTo(0,-cellSize);
                isFinish = checkFinish();
                if(isFinish)
                    break;
//                invalidate();
                previousDirection = 0;
            } else if(!player.leftWall && previousDirection != 1){
                player = cells[player.col-1][player.row];
                movePath.rLineTo(-cellSize,0);
                isFinish = checkFinish();
                if(isFinish)
                    break;
//                invalidate();
                previousDirection = 3;
            }else if(!player.bottomWall && previousDirection != 0){
                player = cells[player.col][player.row+1];
                movePath.rLineTo(0,cellSize);
                isFinish = checkFinish();
                if(isFinish)
                    break;
//                invalidate();
                previousDirection = 2;
            }else if(!player.rightWall && previousDirection != 3){
                player = cells[player.col+1][player.row];
                movePath.rLineTo(cellSize,0);
                isFinish = checkFinish();
                if(isFinish)
                    break;
//                invalidate();
                previousDirection = 1;
            }
        }
        moveAnimation();
        isFinish = checkFinish();
        if(isFinish){
            showDialog();
//            createMaze();
//        return;
        }
    }

    private void movePlayer(Direction direction){
        switch (direction){
            case UP:
                movePath.reset();
                movePath.moveTo(player.col*cellSize+hMargin+0.1f*cellSize,player.row*cellSize+vMargin-0.1f*cellSize);
                if(!player.topWall){
                    player = cells[player.col][player.row-1];
                    movePath.rLineTo(0,-cellSize);
                    moveAnimation();
                    isFinish = checkFinish();
                    if(isFinish){
//                        createMaze();
                        showDialog();
                        return;
                    }
//                    invalidate();
                    // 0 -> top
                    // 1 -> right
                    // 2 -> bottom
                    // 3 -> left
                    if(!isFinish)
                        autoWalk(0);
                }
                break;
            case DOWN:
                movePath.reset();
                movePath.moveTo(player.col*cellSize+hMargin+0.1f*cellSize,player.row*cellSize+vMargin-0.1f*cellSize);
                if(!player.bottomWall){
                    player = cells[player.col][player.row+1];
                    movePath.rLineTo(0,cellSize);
                    moveAnimation();
                    isFinish = checkFinish();
                    if(isFinish){
                        showDialog();
//                        createMaze();
                        return;
                    }
//                    invalidate();
                    if(!isFinish)
                        autoWalk(2);
                }
                break;
            case LEFT:
                movePath.reset();
                movePath.moveTo(player.col*cellSize+hMargin+0.1f*cellSize,player.row*cellSize+vMargin-0.1f*cellSize);
                if(!player.leftWall){
                    player = cells[player.col-1][player.row];
                    movePath.rLineTo(-cellSize,0);
                    moveAnimation();
                    isFinish = checkFinish();
                    if(isFinish){
                        showDialog();
//                        createMaze();
                        return;
                    }
//                    invalidate();
                    if(!isFinish)
                        autoWalk(3);
                }
                break;
            case RIGHT:
                movePath.reset();
                movePath.moveTo(player.col*cellSize+hMargin+0.1f*cellSize,player.row*cellSize+vMargin-0.1f*cellSize);
                if(!player.rightWall){
                    player = cells[player.col+1][player.row];
                    movePath.rLineTo(cellSize,0);
                    moveAnimation();
                    isFinish = checkFinish();
                    if(isFinish){
                        showDialog();
//                        createMaze();
                        return;
                    }
//                    invalidate();
                    if(!isFinish)
                        autoWalk(1);
                }
                break;

                default:
//                    checkFinish();
//                    invalidate();
        }
    }

    private boolean checkFinish(){
        if(player.row==exit.row&&player.col==exit.col){
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        if(event.getAction() == MotionEvent.ACTION_DOWN)
//            return true;
//        if(event.getAction() == MotionEvent.ACTION_MOVE){
//            float x = event.getX();
//            float y = event.getY();
//
//            float playerCenterX = hMargin + (player.col+0.5f)*cellSize;
//            float playerCenterY = vMargin + (player.row+0.5f)*cellSize;
//
//            float dx = x- playerCenterX;
//            float dy = y -playerCenterY;
//
//            float absDx = Math.abs(dx);
//            float absDy = Math.abs(dy);
//
//            if(absDx > cellSize || absDy >cellSize){
//                if(absDx > absDy){
//                    // move in x_direction
//                    if(dx > 0)
//                        // move to the right
//                        movePlayer(Direction.RIGHT);
//                    else
//                        //move to the left
//                        movePlayer(Direction.LEFT);
//                }else{
//                    // move in y_direction
//                    if(dy > 0)
//                        //move down
//                        movePlayer(Direction.DOWN);
//                    else
//                        // move up
//                        movePlayer(Direction.UP);
//                }
//            }
//            return true;
//        }
        detector.onTouchEvent(event);
//        return detector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float minMove = 2*cellSize; // 如果太小，可能分不清楚方向
        float minVelocity = 0;
        float beginX = e1.getX();
        float endX = e2.getX();
        float beginY = e1.getY();
        float endY = e2.getY();

        if(beginX-endX > minMove && Math.abs(velocityX) > minVelocity){
//            Toast.makeText(getContext(), "左滑", Toast.LENGTH_SHORT).show();
            movePlayer(Direction.LEFT);
        }else if(endX-beginX > minMove && Math.abs(velocityX) > minVelocity){
//            Toast.makeText(getContext(), "右滑", Toast.LENGTH_SHORT).show();
            movePlayer(Direction.RIGHT);
        }else if(beginY-endY > minMove && Math.abs(velocityY) > minVelocity){
//            Toast.makeText(getContext(), "上滑", Toast.LENGTH_SHORT).show();
            movePlayer(Direction.UP);
        }else if(endY-beginY > minMove && Math.abs(velocityY) > minVelocity){
//            Toast.makeText(getContext(), "下滑", Toast.LENGTH_SHORT).show();
            movePlayer(Direction.DOWN);
        }
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    private class Cell{
        boolean topWall = true;
        boolean leftWall = true;
        boolean bottomWall = true;
        boolean rightWall = true;
        boolean visited = false;
        int col,row;

        public Cell(int col,int row){
            this.col = col;
            this.row = row;
        }

        public int getOutlets(){
            int outLets = 0;
            if(topWall)
                outLets += 1;
            if(leftWall)
                outLets += 1;
            if(bottomWall)
                outLets += 1;
            if(rightWall)
                outLets += 1;
            return outLets;
        }
    }

    public void setLevel(int level) {
        readMaze("maze"+level+".json");
    }
}
