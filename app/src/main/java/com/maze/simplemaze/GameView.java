package com.maze.simplemaze;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class GameView extends View implements GestureDetector.OnGestureListener, Runnable{

    private enum Direction{
        UP,DOWN,LEFT,RIGHT
    }
    GestureDetector detector;
    private Cell[][] cells;
    private Cell player,exit,monster;
    private static final int COLS=10,ROWS=14;
    private static final float WALL_THICKNESS = 4;
    private int cellSize,hMargin,vMargin;
    private Paint wallPaint,playerPaint,exitPaint,pathPaint,bitmapPaint,icePaint;
    private Bitmap bitmap,portalBitmap,wallBitmap;
    private Random random;
    Path movePath;
    float exitLeft,exitTop,exitRight,exitBottom,wallLeft,wallTop,wallRight,wallBottom;
    RectF destRect,destRectExit,destRectWall,destRectMonster;
    private int wallWidth, wallHeight,bitmapWidth,bitmapHeight;
    private PopupWindow popupWindow;
    String json;
    FileHelper fileHelper;
    int level;
    Point currentPosition = new Point();
    Point monsterPosition = null;
    int currentDirection = 1;
    int monsterDirection = 1;
    final int ANIM_COUNT=4,ANIM_DOWN=2,ANIM_LEFT=3,ANIM_RIGHT=1,ANIM_UP=0;
    final Bitmap [][]playerBitmap = new Bitmap[ANIM_COUNT][ANIM_COUNT];//玩家移动动画数组
    final Bitmap[] monsterBitmap = new Bitmap[4];
    int monsterState = 2;//怪物状态
    int currentState = 0;//当前行走状态
    long speed = 10;  //行走速度
    int mode = 0; //关卡模式：0=>经典 1=>怪物 2=>冰块
    Thread monsterThread;
    boolean isFailed = false;
    Thread moveThread = null;
    private int numOfMove = 0;
    private boolean RWSignal = false;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 77){
                invalidate();
            }
        }
    };
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

        int iceColor = getResources().getColor(R.color.ice_color);
        icePaint = new Paint();
        icePaint.setColor(iceColor);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.picature);
        portalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.portal4);
        wallBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.wall);

        wallWidth = wallBitmap.getWidth();
        wallHeight = wallBitmap.getHeight();
        bitmapWidth = bitmap.getWidth();
        bitmapHeight = bitmap.getHeight();
        cellSize = 100;
        random = new Random();
        movePath = new Path();
        fileHelper = new FileHelper(getContext());
        detector = new GestureDetector(getContext(),this);
        destRect = new RectF();
        moveThread = new Thread(this);
        clipBitmap();
        monsterBitmap[0] = BitmapFactory.decodeResource(getResources(),R.drawable.manster1);
        monsterBitmap[1] = BitmapFactory.decodeResource(getResources(),R.drawable.manster2);
        monsterBitmap[2] = BitmapFactory.decodeResource(getResources(),R.drawable.manster3);
        monsterBitmap[3] = BitmapFactory.decodeResource(getResources(),R.drawable.manster4);
        //        readMaze("maze"+level+".json");
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
    public void readMaze(String filename){
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //获取assets资源管理器
            AssetManager assetManager = getResources().getAssets();
            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open("maze/"+filename)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String jsonArray = stringBuilder.toString();
        /*FileHelper fileHelper = new FileHelper(getContext());
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
        }*/
        Gson gson = new Gson();
        List<Cell> cells1 ;
        cells1 =  gson.fromJson(jsonArray,new TypeToken<List<Cell>>(){}.getType());
        player = cells1.get(0);//人物对象保存在 0 位置
        currentPosition.x = player.col*cellSize;
        currentPosition.y = player.row*cellSize;
        exit = cells1.get(1);//出口保存在 1 位置
        cells1.remove(0);
        cells1.remove(0);
        cells = new Cell[COLS][ROWS];
        for (Cell cell:cells1) {
            cells[cell.col][cell.row] = cell;
            if(mode == 2){// 设置冰块
                if(cell.col == 4 || cell.col == 5 || cell.row==6 || cell.row==7){
                    cell.isIce = true;
                }
            }
        }
        exitLeft = exit.col*cellSize;
        exitTop = exit.row*cellSize;
        exitRight = (exit.col+1)*cellSize;
        exitBottom = (exit.row+1)*cellSize;
        destRectExit = new RectF(exitLeft,exitTop,exitRight,exitBottom);
        if(mode==1){

            monsterPosition = new Point();
            monster = new Cell();
            monster.bottomWall = exit.bottomWall;
            monster.rightWall = exit.rightWall;
            monster.col = exit.col;
            monster.row = exit.row;
            monster.leftWall = exit.leftWall;
            monster.topWall = exit.topWall;
            monster.visited = exit.visited;
            destRectMonster = new RectF(monster.col*cellSize-10,monster.row*cellSize-20,(monster.col+1)*cellSize+10,(monster.row+1)*cellSize);
            monsterPosition.x = monster.col*cellSize;
            monsterPosition.y = monster.row*cellSize;

            if(!monster.rightWall){
                monsterDirection = 1;
            }else if(!monster.bottomWall){
                monsterDirection = 2;
            }else if(!monster.leftWall){
                monsterDirection = 3;
            }else if(!monster.topWall){
                monsterDirection = 0;
            }
            initMonster();
        }
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
//        for (int x=0;x<COLS;x++){
//            for (int y=0;y<ROWS;y++){
//                cells[x][y] = new Cell(x,y);
//                if(COLS==6){
//                    cells[x][y].isIce = true;
//                }
//            }
//        }
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
        //将cell对象数组保存
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
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();


        //System.out.println(level);
        hMargin = (width-COLS*cellSize)/2;
        vMargin = (height-ROWS*cellSize)/2;

        canvas.translate(hMargin,vMargin);
        float margin = cellSize /10;


        float factor = 0.11f;

        //画冰块
        for (int x=0;x<COLS;x++){
            for (int y=0;y<ROWS;y++){

                if(cells[x][y].isIce){
                    canvas.drawRect(cells[x][y].col*cellSize,
                            cells[x][y].row*cellSize,
                            (cells[x][y].col+1)*cellSize,
                            (cells[x][y].row+1)*cellSize,
                            icePaint);
                }
            }
        }
        //画出墙体
        for (int x=0;x<COLS;x++){
            for (int y=0;y<ROWS;y++){
                if(numOfMove > 5) {
                    if(RWSignal) {
                        if (x == 4 || x == 5 || y == 6 || y == 7) {
                            int wallNum = random.nextInt(4);
                            if(x !=0 && y!=0 && x!=9 && y!=13) {
                                switch (wallNum) {
                                    case 0:
                                        cells[x][y].topWall = false;
                                        if (y - 1 > 0) {
                                            cells[x][y - 1].bottomWall = false;
                                        }
                                        break;
                                    case 1:
                                        cells[x][y].leftWall = false;
                                        if (x - 1 > 0) {
                                            cells[x - 1][y].rightWall = false;
                                        }
                                        break;
                                    case 2:
                                        cells[x][y].bottomWall = false;
                                        if (y + 1 < 13) {
                                            cells[x][y + 1].topWall = false;
                                        }
                                        break;
                                    case 3:
                                        cells[x][y].rightWall = false;
                                        if (x + 1 < 9)
                                            cells[x + 1][y].leftWall = false;
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    }
                }
                if(cells[x][y].topWall) {

                    for(int i=0;i<3;i++) {
                        wallLeft = x * cellSize + 2*i*factor*wallWidth- factor * wallWidth;
                        wallTop = y * cellSize - factor * wallHeight;
                        wallRight = x * cellSize + 2*i*factor*wallWidth+ factor * wallWidth;
                        wallBottom = y * cellSize + factor * wallHeight;
                        destRectWall = new RectF(wallLeft, wallTop, wallRight, wallBottom);
                        canvas.drawBitmap(wallBitmap, null, destRectWall, bitmapPaint);
                    }
                    /*canvas.drawLine(
                            x*cellSize,
                            y*cellSize,
                            (x+1)*cellSize,
                            y*cellSize,
                            wallPaint);*/
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
                    /*canvas.drawLine(
                            x*cellSize,
                            y*cellSize,
                            x*cellSize,
                            (y+1)*cellSize,
                            wallPaint);*/
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
                    /*canvas.drawLine(
                            x*cellSize,
                            (y+1)*cellSize,
                            (x+1)*cellSize,
                            (y+1)*cellSize,
                            wallPaint);*/
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
                    /*canvas.drawLine(
                            (x+1)*cellSize,
                            y*cellSize,
                            (x+1)*cellSize,
                            (y+1)*cellSize,
                            wallPaint);*/

                }
                //Log.d("iceState",cells[x][y].isIce+"");

            }
            RWSignal = false;
        }

        //画出出口

        canvas.drawBitmap(portalBitmap,null,destRectExit,bitmapPaint);

        //画怪物
        if(mode==1) {
            destRectMonster.top = monsterPosition.y;
            destRectMonster.bottom = monsterPosition.y + cellSize;
            destRectMonster.left = monsterPosition.x;
            destRectMonster.right = monsterPosition.x + cellSize;
            canvas.drawBitmap(monsterBitmap[monsterState], null, destRectMonster, bitmapPaint);
        }
        //画出人物
        destRect.top = currentPosition.y;
        destRect.bottom = currentPosition.y+cellSize;
        destRect.left = currentPosition.x;
        destRect.right = currentPosition.x+cellSize;
        canvas.drawBitmap(playerBitmap[currentDirection][currentState], null,destRect, bitmapPaint);
        //若与怪物相遇，则失败
        if(checkFail()){
            isFailed = true;
            failDialog();
        }

        //到达终点则显示弹窗,且完成绘图后，才弹出
        if(checkFinish()){
            //System.out.println("test");
            showDialog();
        }
    }

    private void showDialog(){
        LayoutInflater mLayoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup menuView = (ViewGroup) mLayoutInflater.inflate(R.layout.popup_window, null, true);
        Button nextButton = (Button) menuView.findViewById(R.id.next_button);
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(popupWindow != null) {
                    popupWindow.dismiss();//无法点击其他区域
                    popupWindow = null;
                }
                readMaze("maze"+(++level)+".json");
                numOfMove = 0;
                invalidate();
            }
        });
        if(popupWindow == null) {
            popupWindow = new PopupWindow(menuView, 800, 400, true);
        }
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

    private void failDialog(){
        LayoutInflater mLayoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup menuView = (ViewGroup) mLayoutInflater.inflate(R.layout.popup_window, null, true);
        Button button = (Button) menuView.findViewById(R.id.next_button);
        button.setBackgroundResource(R.drawable.replay);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(popupWindow != null) {
                    popupWindow.dismiss();//无法点击其他区域
                    popupWindow = null;
                }
                isFailed = false;
                readMaze("maze"+level+".json");
                invalidate();
            }
        });
        if(popupWindow == null) {
            popupWindow = new PopupWindow(menuView, 800, 400, true);
        }
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

    //切割行走动画，并存到动画数组
    public void clipBitmap(){
        Bitmap temp = BitmapFactory.decodeResource(getResources(), R.drawable.player);
        int tileWidth = temp.getWidth() / ANIM_COUNT;
        int tileHeight = temp.getHeight() / ANIM_COUNT;
        int i = 0,x = 0,y = 0;
        for(i =0; i < ANIM_COUNT; i++) {
            y = 0;
            playerBitmap[ANIM_DOWN][i] = Bitmap.createBitmap(temp, x, y, tileWidth, tileHeight);
            y += tileHeight;
            playerBitmap[ANIM_LEFT][i] = Bitmap.createBitmap(temp, x, y, tileWidth, tileHeight);
            y += tileHeight;
            playerBitmap[ANIM_RIGHT][i] = Bitmap.createBitmap(temp, x, y, tileWidth, tileHeight);
            y += tileHeight;
            playerBitmap[ANIM_UP][i] = Bitmap.createBitmap(temp, x, y, tileWidth, tileHeight);
            x += tileWidth;
        }
    }

    //自动行走线程
    public void run(){
        //     0
        //  3     1
        //     2
        int i = 0;
        if(currentDirection == 1) {
            while (!player.rightWall) {
                currentPosition.x += 10;
                currentState = (currentState + 1) % 4;
                if (i == 9) {
                    player = cells[player.col + 1][player.row];
                    if(checkFinish()){
                        break;
                    }
                    if(!player.isIce) {
                        if (player.getOutlets() > 2) break;
                    }
                }
                i = (i + 1) % 10;
//                invalidate();
                handler.sendEmptyMessage(77);
                try {
                    Thread.sleep(speed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else
        if(currentDirection == 2) {
            while (!player.bottomWall) {
                currentPosition.y += 10;
                currentState = (currentState + 1) % 4;
                if (i == 9) {
                    player = cells[player.col][player.row+1];
                    if(checkFinish()){
                        break;
                    }
                    if(!player.isIce) {
                        if (player.getOutlets() > 2) break;
                    }
                }
                i = (i + 1) % 10;
//                invalidate();
                handler.sendEmptyMessage(77);
                try {
                    Thread.sleep(speed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else
        if(currentDirection == 3) {
            while (!player.leftWall) {
                currentPosition.x -= 10;
                currentState = (currentState + 1) % 4;
                if (i == 9) {
                    player = cells[player.col - 1][player.row];
                    if(checkFinish()){
                        break;
                    }
                    if(!player.isIce) {
                        if (player.getOutlets() > 2) break;
                    }
                }
                i = (i + 1) % 10;
//                invalidate();
                handler.sendEmptyMessage(77);
                try {
                    Thread.sleep(speed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else
        if(currentDirection == 0) {
            while (!player.topWall) {
                currentPosition.y -= 10;
                currentState = (currentState + 1) % 4;
                if (i == 9) {
                    player = cells[player.col][player.row-1];
                    if(checkFinish()){
                        break;
                    }
                    if(!player.isIce) {
                        if (player.getOutlets() > 2) break;
                    }
                }
                i = (i + 1) % 10;
//                invalidate();
                handler.sendEmptyMessage(77);
                try {
                    Thread.sleep(speed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
//        invalidate();
        RWSignal = true;
        handler.sendEmptyMessage(77);
    }

    private void movePlayer(Direction direction){
        switch (direction){
            case UP:
                if(!player.topWall && !moveThread.isAlive()){
                    numOfMove += 1;
                    currentDirection = 0;
//                    invalidate();
//                    handler.sendEmptyMessage(77);
//                    Thread thread = new Thread(this);
//                    thread.start();
                    handler.sendEmptyMessage(77);
                    moveThread = new Thread(this);
                    moveThread.start();
                }
                break;
            case DOWN:
                if(!player.bottomWall && !moveThread.isAlive()){
                    numOfMove += 1;
                    currentDirection = 2;
//                    invalidate();
//                    handler.sendEmptyMessage(77);
//                    Thread thread = new Thread(this);
//                    thread.start();
                    handler.sendEmptyMessage(77);
                    moveThread = new Thread(this);
                    moveThread.start();
                }
                break;
            case LEFT:
                if(!player.leftWall && !moveThread.isAlive()){
                    numOfMove += 1;
                    currentDirection = 3;
//                    invalidate();
//                    handler.sendEmptyMessage(77);
//                    Thread thread = new Thread(this);
//                    thread.start();
                    handler.sendEmptyMessage(77);
                    moveThread = new Thread(this);
                    moveThread.start();
                }
                break;
            case RIGHT:
                if(!player.rightWall && !moveThread.isAlive()){
                    numOfMove += 1;
                    currentDirection = 1;
//                    invalidate();
//                    handler.sendEmptyMessage(77);
//                    Thread thread = new Thread(this);
//                    thread.start();
                    handler.sendEmptyMessage(77);
                    moveThread = new Thread(this);
                    moveThread.start();
                }
                break;
        }
    }

    private boolean checkFinish(){
        if(player.row==exit.row&&player.col==exit.col){
            return true;
        }
        return false;
    }

    //检测玩家与怪物相遇
    private boolean checkFail(){
        if(monsterPosition != null) {
            if (currentPosition.x == monsterPosition.x && currentPosition.y == monsterPosition.y) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
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

    //初始化怪物线程
    public void initMonster(){
        monsterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while(!isFailed) {
                    if (monsterDirection == 1) {
                        while (!isFailed&&monsterDirection == 1 && !monster.rightWall) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            monsterState = (monsterState + 1) % 4;
                            monsterPosition.x += 10;
                            if (i == 9) {
                                monster = cells[monster.col + 1][monster.row];
                                if (!monster.bottomWall) {
                                    monsterDirection = 2;
                                }
                            }
                            i = (i + 1) % 10;
                            handler.sendEmptyMessage(77);
                        }
                        if(monster.bottomWall&&monster.topWall){
                            monsterDirection = 3;
                        }
                        if(monster.bottomWall&&!monster.topWall){
                            monsterDirection = 0;
                        }
                    } else if (monsterDirection == 2) {
                        while (!isFailed&&monsterDirection == 2 && !monster.bottomWall) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            monsterState = (monsterState + 1) % 4;
                            monsterPosition.y += 10;
                            if (i == 9) {
                                monster = cells[monster.col][monster.row + 1];
                                if (!monster.leftWall) {
                                    monsterDirection = 3;
                                }
                            }
                            i = (i + 1) % 10;
                            handler.sendEmptyMessage(77);
                        }
                        if(monster.leftWall&& monster.rightWall){
                            monsterDirection = 0;
                        }
                        if(monster.leftWall&&!monster.rightWall){
                            monsterDirection = 1;
                        }
                    } else if (monsterDirection == 3) {
                        while (!isFailed&&monsterDirection == 3 && !monster.leftWall) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            monsterState = (monsterState + 1) % 4;
                            monsterPosition.x -= 10;
                            if (i == 9) {
                                monster = cells[monster.col - 1][monster.row];
                                if (!monster.topWall) {
                                    monsterDirection = 0;
                                }
                            }
                            i = (i + 1) % 10;
                            handler.sendEmptyMessage(77);
                        }
                        if(monster.topWall&& monster.bottomWall){
                            monsterDirection = 1;
                        }
                        if(monster.topWall&&!monster.bottomWall){
                            monsterDirection = 2;
                        }
                    } else if (monsterDirection == 0) {
                        while (!isFailed&&monsterDirection == 0 && !monster.topWall) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            monsterState = (monsterState + 1) % 4;
                            monsterPosition.y -= 10;
                            if (i == 9) {
                                monster = cells[monster.col][monster.row - 1];
                                if (!monster.rightWall) {
                                    monsterDirection = 1;
                                }
                            }
                            i = (i + 1) % 10;
                            handler.sendEmptyMessage(77);
                        }
                        if(monster.rightWall&&monster.leftWall){
                            monsterDirection = 2;
                        }
                        if(monster.rightWall&&!monster.leftWall){
                            monsterDirection = 3;
                        }
                    }
                }
            }
        });
        monsterThread.start();
    }

    private class Cell{
        boolean topWall = true;
        boolean leftWall = true;
        boolean bottomWall = true;
        boolean rightWall = true;
        boolean visited = false;
        boolean isIce = false;
        int col,row;

        public Cell(int col,int row){
            this.col = col;
            this.row = row;
        }

        public Cell() {

        }

        public int getOutlets(){
            int outLets = 0;
            if(!topWall)
                outLets += 1;
            if(!leftWall)
                outLets += 1;
            if(!bottomWall)
                outLets += 1;
            if(!rightWall)
                outLets += 1;
            return outLets;
        }
    }


}
