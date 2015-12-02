/*********************************************************
 * Leap Motion Pong game by Bensu
 * 
 * - Played with two players. Vertical coordinates
 * of righmost and leftmost fingers are calculated
 * to move the sticks
 * *******************************************************/

package pong;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import javax.swing.JFrame;
import com.leapmotion.leap.*;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.util.TimerTask;
import javax.swing.JPanel;

class SampleListener extends Listener{
    public int screenHeight=740;
    public int screenWidth=1366;
    public int appY,appX;
    public int a=2;
    public int appY_2, appY_1;
    public boolean inGame=true;
    public boolean replay=false;
    public boolean isMenu,isPong,isFlappy;
    public void onInit(Controller controller) {
        System.out.println("Initialized");
    }
    public void onConnect(Controller controller) {
        System.out.println("Connected"); //to indicate whether the leap motion connection is successful
        controller.enableGesture(Gesture.Type.TYPE_CIRCLE); //to accept circle gesture
        controller.enableGesture(Gesture.Type.TYPE_SWIPE);
        controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
        controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);
    }
    public void onDisconnect(Controller controller) {
        //Note: not dispatched when running in a debugger.
        System.out.println("Disconnected");
    }
    public void onExit(Controller controller) {
        System.out.println("Exited");
    }
    public void onFrame(Controller controller) { //to get the most recent frame and information
        Frame frame = controller.frame(); 
        //GestureList gestures = frame.gestures();
        replay=false;
        for(Gesture gesture : frame.gestures()){
            switch (gesture.type()) {
                case TYPE_CIRCLE:
                    float seconds = gesture.durationSeconds();
                    if(seconds>=0.5){
                        replay=true; //drawing a circle enables replay
                    }
                break;
            }
        }   
        InteractionBox iBox = controller.frame().interactionBox();
        Pointable pointL = controller.frame().fingers().leftmost(); //takes the leftmost finger into account
        Pointable pointR = controller.frame().fingers().rightmost(); //takes the rightmost finger into account
        Vector leapPointL = pointL.stabilizedTipPosition(); //gets the position vector of fingers
        Vector leapPointR = pointR.stabilizedTipPosition();
        Vector normalizedPointL = iBox.normalizePoint(leapPointL, true);
        Vector normalizedPointR = iBox.normalizePoint(leapPointR, true);
        appY_2 = (int)((1 - normalizedPointR.getY()) * screenHeight); //gets Y coordinates of fingers changed according to the height of screen
        appY_1 = (int)((1 - normalizedPointL.getY()) * screenHeight);
        appX = (int)((1-normalizedPointL.getX()) * screenWidth);
    }
    public int getCoorY(int a){
        if (a==1){
            return appY_1; //returns left finger's Y coordinate
        }
        else{
            return appY_2; //returns right finger's Y coordinate
        }
    }
    public boolean getReplay(){
        return replay;
    }
}

class Game extends JPanel{
    public static int screenWidth=1366;
    public static int screenHeight=740;
    public int ballX=screenWidth/2;
    public int ballY=screenHeight/2;
    public boolean inGame=true;
    public int appY_1, appY_2;
    public int score1=0;
    public int score2=0;
    public int hitCount=0;
    public int winner;
    public boolean replay=false;
    public boolean quit=false;
    public boolean chron=true;
    public int Xspeed=5;
    public int Yspeed=5;
    public int countNo=3;
    public int count=1;
    public boolean isPong=true;
    public boolean isMenu=false;
    Controller controller = new Controller(); //initializes leap
    SampleListener listener = new SampleListener();
    java.util.Timer timer = new java.util.Timer();
    public Game(){
        setBackground(Color.black);
        setFocusable(true);
        setPreferredSize(new Dimension(screenWidth,screenHeight));
        controller.addListener(listener); //starts leap
        pongRun(listener);
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                repaint();
            }
        }, 30, 30);
    }
    public void ballMove(){
        if(hitCount!=0&&hitCount%5==0){ //increases ball's speed at every 5 hits given by the players
            Xspeed++;
            Yspeed++;
            hitCount++;
        }
        if(((ballX<=60&&ballX>=55)&&((appY_1<=ballY)&&((appY_1+100)>=ballY)))||
                (((ballX+20>=screenWidth-60)&&(ballX+20<=screenWidth-55))&&(ballY>=appY_2&&ballY<=(appY_2+100)))){
            Xspeed=0-Xspeed;
            hitCount++; //when players hit the ball, changes direction and increases hit count
        }
        else if((ballY<=0||(ballY+20)>=screenHeight)){
            Yspeed=0-Yspeed; //when the ball hits the borders, changes direction
        }
        else if(ballX+20>=screenWidth){ //if the ball goes through the border to the right, respawns the ball and increases p1's score
            ballY=screenHeight/2;
            ballX=screenWidth/2;
            Xspeed=0-Xspeed;
            score1++;
        }
        else if(ballX<=0){ //if the ball goes through the border to the left, respawns the ball and increases p2's score
            ballY=screenHeight/2;
            ballX=screenWidth/2;
            Xspeed=0-Xspeed;
            score2++;
        }
        ballX+=Xspeed; //moves to ball
        ballY+=Yspeed;
    }
    public void paint(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        Font nameCopy = new Font("Helvetica", Font.BOLD,20);
        g.setColor(Color.gray);
        g.setFont(nameCopy);
        g.drawString("Bensu Sicim", screenWidth-150, screenHeight-50);
        drawPong(g2d);
    }
    public void drawPong(Graphics g){
        //boolean quit=listener.getQuit();
        if(quit==true){
            isPong=false;
            isMenu=true;
        }
        Font bigNumbers = new Font("Helvetica", Font.BOLD,400);
        Font directions = new Font("Helvetica", Font.BOLD,25);
        Font gOver = new Font("Helvetica", Font.BOLD,100);
        Font gOverLabel = new Font("Helvetica", Font.BOLD,50);
        if(inGame==true){ //paints game objects when game isn't over
            g.setColor(Color.white);
            for(int i=0;i<=10;i++){
                g.fillRect(screenWidth/2, i*((screenHeight)/10), 20, 40); 
            }
            if(chron && count<90){
               if(count%30==0){
                countNo--;
               }
               count++;
               g.setColor(Color.red);
               g.setFont(bigNumbers);
               g.drawString(Integer.toString(countNo), 600,500);
            }
            else if(count>=90){
                chron=false;
                ballMove(); //moves the ball
            }
            else if(!chron){
                g.setColor(Color.gray);
                g.setFont(bigNumbers);
                g.drawString(Integer.toString(score1), (screenWidth/4-100), screenHeight/2+100);
                g.drawString(Integer.toString(score2), screenWidth-(screenWidth/4)-100, screenHeight/2+100);
                g.setColor(Color.red);
                g.fillOval(ballX,ballY,20,20);
            }
            g.setFont(directions);
            g.setColor(Color.white);
            g.fillRect(40, appY_1, 20, 100);
            g.fillRect(screenWidth-60, appY_2, 20, 100);
            g.setColor(Color.gray);
            g.drawString("Draw little circles to exit", 550, screenHeight-80);
            g.drawString("Your palms should face the ground. Up and down to move", 350, screenHeight-50); 
            Toolkit.getDefaultToolkit().sync();
        }
        else if(inGame==false){ //game over page
            g.setColor(Color.white);
            g.setFont(gOver);
            g.drawString("Game Over", 400, screenHeight/2-100);
            g.setFont(gOverLabel);
            g.drawString("Player "+winner+" won!", 500, screenHeight/2);
            g.setFont(directions);
            g.drawString("Draw little circles with your finger to exit", 435, (screenHeight-50));
            g.drawString("Make a clicking gesture to replay", 470, (screenHeight-80));
        }
    }
    public void pongRun(SampleListener listener){
        replay=false;
        if(inGame==true){
            appY_2=listener.getCoorY(2); //gets Y coordinates of fingers from the leap
            appY_1=listener.getCoorY(1);
            if(score1==3){ //ends game
                winner=1;
                inGame=false;
            }
            else if(score2==3){
                winner=2;
                inGame=false;
            }
        }
        else if (inGame==false){
            countNo=3;
            count=1;
            replay=false;
            replay=listener.getReplay();
            if(replay){
                score1=0;
                score2=0;
                inGame=true;
                replay=false;
            }
        }
    }
}
public class Pong extends JFrame{
    public static int screenWidth=1366;
    public static int screenHeight=740;
    public static void main(String avg[]){  
        JFrame frame = new JFrame("Leap Motion Pong");
        frame.add(new Game());
        frame.setSize(screenWidth, screenHeight);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Controller controller = new Controller(); //initializes leap
        SampleListener listener=new SampleListener();
        controller.addListener(listener); //starts leap
    }
}
