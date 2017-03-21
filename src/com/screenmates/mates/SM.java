package com.screenmates.mates;

import java.awt.Color;

import javax.swing.JWindow;
import javax.swing.UIManager;

import com.screenmates.input.Mouse;


/** SM class contains the frame and main thread and listens for mouse events
 * @author Rokade
 * @version 1.0
 */
public class SM implements Runnable{
	
	public boolean running;
	private Thread Main_Thread = new Thread();
	private static Mouse mouse = new Mouse();

    public static void main(String[] args) {
    	SM smmain = new SM();
        smmain.start();
    }
    
    /**
     * Starts the main thread with this instance of the program.
     */
    public synchronized void start(){
		running = true;
		Main_Thread = new Thread(this, "Screenmate");
		Main_Thread.start();	
	}

    public SM() {
       
    }

    /**
     * Sets up and runs a screenmate.
     */
	public void run() {
        try {
		    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
		}

		JWindow frame = new JWindow();
		Screenmate sm = new Screenmate(frame);
		frame.setAlwaysOnTop(true);	
		frame.setBackground(new Color(0,0,0,0));
		frame.add(sm);
		frame.setSize(256, 256);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.addMouseListener(mouse);
		frame.addMouseMotionListener(mouse);
		running = true;
		
		while (running){
			sm.tick();
			frame.repaint();
			try {
				Thread.sleep(1000/60);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
    }

   

}