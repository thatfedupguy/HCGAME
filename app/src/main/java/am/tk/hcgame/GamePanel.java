package am.tk.hcgame;

import android.content.Context;
import android.graphics.Canvas;


import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import static am.tk.hcgame.MainThread.canvas;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    private Rect r=new Rect();
    private MainThread thread;
    private RectPlayer player;
    private Point playerPoint;
    private  ObstacleManager obstacleManager;

    private boolean movingPlayer=false;
    private boolean gameOver=false;

    private long gameOverTime;

    private OrientationData orientationData;
    private long frameTime;

    public  GamePanel(Context context){
        super(context);
        getHolder().addCallback(this);
        thread=new MainThread(getHolder(),this);
        player=new RectPlayer(new Rect(100,100,200,200), Color.rgb(255,0,0));
        playerPoint=new Point(Constants.SCREEN_WIDTH/2,3*Constants.SCREEN_HEIGHT/4);
        player.update(playerPoint);


        obstacleManager=new ObstacleManager(200,350,75,Color.BLACK);

        orientationData=new OrientationData();
        orientationData.register();
        frameTime=System.currentTimeMillis();
        setFocusable(true);


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Constants.INIT_TIME=System.currentTimeMillis();
        thread=new MainThread(getHolder(),this);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry=true;
        while (retry){
            try{
                thread.setRunning(false);
                thread.join();
            }catch (Exception e){
                retry=false;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(!gameOver )
                    movingPlayer=true;
                if(gameOver && System.currentTimeMillis()-gameOverTime>=2000 ){
                    reset();
                    gameOver=false;
                    orientationData.newGame();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(!gameOver&&movingPlayer)
                    playerPoint.set((int)event.getX(),(int)event.getY());
                break;
            case MotionEvent.ACTION_UP:
                movingPlayer=false;
                break;
        }
        return true;

    }

    private void reset() {
        playerPoint=new Point(Constants.SCREEN_WIDTH/2,3*Constants.SCREEN_HEIGHT/4);
        player.update(playerPoint);
        obstacleManager=new ObstacleManager(200,350,75,Color.BLACK);
        movingPlayer=false;
    }

    public void update(){
        if(!gameOver) {
            if(frameTime<Constants.INIT_TIME)
                frameTime=Constants.INIT_TIME;
            int elapsedTime=(int)(System.currentTimeMillis()-frameTime);
            frameTime=System.currentTimeMillis();
            if(orientationData.getOrientation()!=null && orientationData.getStartOrientation()!=null){
                float pitch=orientationData.getOrientation()[1] - orientationData.getStartOrientation()[1];
                float roll=orientationData.getOrientation()[2] - orientationData.getStartOrientation()[2];

                float xSpeed=2*roll*Constants.SCREEN_WIDTH/1000f;
                float ySpeed=pitch*Constants.SCREEN_HEIGHT/1000f;

                playerPoint.x+=Math.abs(xSpeed*elapsedTime) > 5 ? xSpeed*elapsedTime : 0;
                playerPoint.y-=Math.abs(ySpeed*elapsedTime) > 5 ? ySpeed*elapsedTime : 0;
            }

            if(playerPoint.x<0){
                playerPoint.x=0;
            }
            else if(playerPoint.x>Constants.SCREEN_WIDTH){
                playerPoint.x=Constants.SCREEN_WIDTH;
            }

            if(playerPoint.y<0){
                playerPoint.y=0;
            }
            else if(playerPoint.y>Constants.SCREEN_HEIGHT){
                playerPoint.y=Constants.SCREEN_HEIGHT;
            }

            player.update(playerPoint);
            obstacleManager.update();

            if (obstacleManager.playerCollide(player)) {
                gameOver = true;
                gameOverTime=System.currentTimeMillis();
            }
        }

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.WHITE);
        player.draw(canvas);
        obstacleManager.draw(canvas);
        if(gameOver){
            Paint paint=new Paint();
            paint.setTextSize(100);
            paint.setColor(Color.BLUE);
            drawCentreText(canvas,paint,"Game Over");
        }
    }

    private void drawCentreText(Canvas canvas, Paint paint, String text) {
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.getClipBounds(r);
        int cHeight=r.height();
        int cWidth=r.width();
        paint.getTextBounds(text,0,text.length(),r);
        float x=cWidth/2f-r.width()/2f-r.left;
        float y=cHeight/2f+r.height()/2f-r.bottom;
        canvas.drawText(text,x,y,paint);
    }
}
