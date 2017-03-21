package com.screenmates.input;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/** Mouse class expands mouse event methods
 * @author Rokade
 * @version 1.0
 */
public class Mouse implements MouseListener, MouseMotionListener{
	
	

	public static int mouseX = -1;
	public static int mouseY = -1;
	public static int mouseB = -1;
	public static boolean click;
	public static boolean dragging;
	
	
	public static int getX(){
		return mouseX;
		
	}
	
	public static int getY(){
		return mouseY;
	}
	
	public static int getB(){
		return mouseB;
	}
	
	public void mouseDragged(MouseEvent arg0) {
		mouseX = arg0.getX();
		mouseY = arg0.getY();
		dragging = true;
		
	}

	
	public void mouseMoved(MouseEvent arg0) {
		mouseX = arg0.getX();
		mouseY = arg0.getY();
		
	}

	
	public void mouseClicked(MouseEvent arg0) {
		
		
	}

	
	public void mouseEntered(MouseEvent arg0) {
		
		
	}

	
	public void mouseExited(MouseEvent arg0) {
		
		
	}

	
	public void mousePressed(MouseEvent arg0) {
		mouseB = arg0.getButton();
		waitasec();
		
		
	}

	
	public void mouseReleased(MouseEvent arg0) {
		mouseB = -1;
		dragging = false;
		waitasec();
		
		
	}
	
	public void waitasec(){
		for (int i = 0; i < 5; i++){
			
		}
		
	}

}