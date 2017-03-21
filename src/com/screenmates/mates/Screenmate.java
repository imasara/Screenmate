package com.screenmates.mates;

import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JLabel;
import javax.swing.JWindow;

import com.screenmates.input.Mouse;

/** Screenmate class contains logic that governs the screenmate.
	* @author Rokade
	* @version 1.0
   */
public class Screenmate extends JLabel implements Serializable, LineListener {
 
  private static final long serialVersionUID = 1L;
  public static final int STAND_SPRITES = 1, RUN_SPRITES = 14, DRAG_SPRITES = 6, ALL_SPRITES = STAND_SPRITES + RUN_SPRITES + DRAG_SPRITES;
  
  public JWindow parent;
  private Clip clip;
  private BufferedImage[] runningSprites = new BufferedImage[RUN_SPRITES];
  private BufferedImage[] standingSprites = new BufferedImage[STAND_SPRITES];
  private BufferedImage[] draggedSprites = new BufferedImage[DRAG_SPRITES];
  private String[] sprite_filenames = new String[ALL_SPRITES];
  private BufferedImage img;
  private long runningtime = 0;
  private long waitingtime = 0;
  private long draggingTime = 0;
  private boolean running = false;
  private boolean dragged = false;
  private int sound_off = 0;
  private boolean say_move = false;
  private boolean say_waiting = false;
  private boolean say_dragged = false;
  private Point target;
  public static final int MAX_STAMINA = 1800;
  private int stamina;
  private Behaviour behaviour = Behaviour.Following;
  private boolean dirX, dirY; //True is right and down, False is left and up.

  public Screenmate(JWindow parent) {
	  this.parent = parent;
	 //Define sprite files location and filename
	  sprite_filenames[0] = "/sprites/standing.png";
	  sprite_filenames[1] = "/sprites/running_right1.png";
	  sprite_filenames[2] = "/sprites/running_right2.png";
	  sprite_filenames[3] = "/sprites/running_right3.png";
	  sprite_filenames[4] = "/sprites/running_right4.png";
	  sprite_filenames[5] = "/sprites/running_right5.png";
	  sprite_filenames[6] = "/sprites/running_right6.png";
	  sprite_filenames[7] = "/sprites/running_right7.png";
	  sprite_filenames[8] = "/sprites/running_left1.png";
	  sprite_filenames[9] = "/sprites/running_left2.png";
	  sprite_filenames[10] = "/sprites/running_left3.png";
	  sprite_filenames[11] = "/sprites/running_left4.png";
	  sprite_filenames[12] = "/sprites/running_left5.png";
	  sprite_filenames[13] = "/sprites/running_left6.png";
	  sprite_filenames[14] = "/sprites/running_left7.png";
	  sprite_filenames[15] = "/sprites/dragged_right1.png";
	  sprite_filenames[16] = "/sprites/dragged_right2.png";
	  sprite_filenames[17] = "/sprites/dragged_right3.png";
	  sprite_filenames[18] = "/sprites/dragged_left1.png";
	  sprite_filenames[19] = "/sprites/dragged_left2.png";
	  sprite_filenames[20] = "/sprites/dragged_left3.png";
	  
	  target = new Point();
	  stamina = MAX_STAMINA;
	 
	  //Set up audio clip
	  try {
		this.clip = AudioSystem.getClip();
	} catch (LineUnavailableException e) {
		System.out.println(e.getMessage());
	}
    this.clip.addLineListener(this); 
    setSize(64, 64);
    
     
    //Load sprites from folder and put them into an array.
	try {
		for (int i = 0; i < ALL_SPRITES; i++) {
			InputStream in = getClass().getResourceAsStream(sprite_filenames[i]);
			if (in != null) { 
				if (i == 0) {
					standingSprites[i] = ImageIO.read(in);
				} else if (i < STAND_SPRITES + RUN_SPRITES) {
					runningSprites[i - STAND_SPRITES] = ImageIO.read(in);
				} else {
					draggedSprites[i - STAND_SPRITES - RUN_SPRITES] = ImageIO.read(in);
				}
				in.close();
			}
		}
	} catch (IOException e) {
		
		e.printStackTrace();
	}
	  repaint();
		
  }
  
  /** Update method of the screenmate. 
    *
    */
  public void tick() {
	  if (Mouse.getB() == 2) System.exit(0);
	  chooseBehaviour();
	  animate();
	  
	  if (!clip.isActive()) {
		  sound_off++;
		  if (sound_off > 300) sound(); //If no sound has been played for five seconds, check if one should be played.
	  }
	  
	  repaint();
  }

/**
 * Picks a behaviour for the screenmate and calls the appropriate movement method
 */
private void chooseBehaviour() {
	if ((stamina > MAX_STAMINA * 0.5 && behaviour == Behaviour.Following) || stamina == MAX_STAMINA) {
		  behaviour = Behaviour.Following;
	  } else if (stamina > MAX_STAMINA * 0.25 && (behaviour == Behaviour.Following || behaviour == Behaviour.Wandering)) {
		  behaviour = Behaviour.Wandering;
	  } else {
		  behaviour = Behaviour.Waiting;
	  }
	  
	  if (Mouse.dragging){
		 drag();
	  } else {
		  dragged = false;
		  if (behaviour == Behaviour.Following) {
			  trackMouse();
		  } else if (behaviour == Behaviour.Wandering) {
			  wander();
		  } else if (behaviour == Behaviour.Waiting) {
			  remain();
		  }
	  }
}

/**
 * Sends the screenmate a point with it's current coordinates (which means it doesn't move but does have other relevant
 * variables updated).
 */
private void remain() {
	moveTo(new Point(parent.getX(), parent.getY()));
}

/** Plays a WAV file, selected by getVoiceLineStream(). 
   * 
   */
  private void sound() { 
	  if (clip != null) {
		  clip.stop();
		  clip.close();
	  }
	
	InputStream is = getVoiceLineStream();
	//Load clip with requested sound.
	if (is != null) {
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
			try {
				System.out.println("is: " + is);
				System.out.println("ais: " + ais);
				this.clip.open(ais);
			} catch (LineUnavailableException e) {
				
				e.printStackTrace();
			}
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.clip.start();
		sound_off = 0;
	}
	else {
		//media = getVoiceLineMedia();
	}
	
}

/** Selects a WAV file, based on the screenmate's 'say' booleans. 
 * 
 */
private InputStream getVoiceLineStream() {
	//Load stream based on requested sound. 
	InputStream is = null;
	if (say_move) {
		is = getClass().getResourceAsStream("/sounds/move.wav");
		say_move = false;
	}
	else if (say_waiting) {
		is = getClass().getResourceAsStream("/sounds/waiting.wav");
		say_waiting = false;
	}
	else if (say_dragged) {
		is = getClass().getResourceAsStream("/sounds/dragged.wav");
	}
	return is;
}

/** Moves the screenmate along with the mouse
 * 
 */
private void drag() {
	if (!dragged) say_dragged = true;
	dragged = true;
	int mx = MouseInfo.getPointerInfo().getLocation().x;
	int my = MouseInfo.getPointerInfo().getLocation().y;
	if (mx < parent.getX()) {
		dirX = false;
	} else if (mx > parent.getX()) {
		dirX = true;
	}
	parent.setLocation(mx, my);
	stamina = 0;
}

/** Sends the screenmate toward the mouse location using the moveTo method
 */
public void trackMouse(){
	  int mx = MouseInfo.getPointerInfo().getLocation().x;
	  int my = MouseInfo.getPointerInfo().getLocation().y; 
	  moveTo(new Point(mx, my));
  }

/** Chooses a random point on the screen and has the screenmate walk towards that point.
 * 
 */
public void wander() {
	 int x = parent.getX();
	 int y = parent.getY();
	 int xRange = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	 int yRange = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	 Random random = new Random();
	 while (target.x == x) {
		target.x = random.nextInt(xRange);
	 }
	 while (target.y == y) {
		target.y = random.nextInt(yRange);
	 }
	 moveTo(target); 
}
 
/**
 * Sends the screenmate to the given point and updates relevant variables.
 * @param target
 */
private void moveTo(Point target) { 
	int x = parent.getX();
	int y = parent.getY();
	 
	 if (x == target.x && y == target.y) {
		  running = false;
		  runningtime = 0;
		  stamina += 2;
	 } else {
		  if (!running) {
			  say_move = true;
		  } else {
			  say_move = false;
		  }
		  running = true;
		  waitingtime = 0;
		  if (x < target.x) {
			  x++;
			  dirX = true;
		  } else if (x > target.x) {
			  x--;
			  dirX = false;
		  	}
		  if (y < target.y) {
			  y++;
			  dirY = true;
		  } else if (y > target.y) {
			  y--;
			  dirY = false;
		  	}
			 parent.setLocation(x, y);
			 stamina--;
	  }
	 
	
}

/** Picks the appropriate sprite to display, based on the state of the screenmate. 
 * 
 */
private void animate() {
	  //Finds sprite to use, based on state of the screenmate.
	  if (dragged) {
		  draggingTime++;
		  if (draggingTime > 23) draggingTime = 0;
		  img = draggedSprites[(int) (Math.floor(draggingTime / 8)) + ((dirX) ? 0 : (draggedSprites.length / 2))];
	  } else if (running) {
		  runningtime++;
		  if (runningtime > 55) runningtime = 0;
		  img = runningSprites[(int) (Math.floor(runningtime / 8)) + ((dirX) ? 0 : (runningSprites.length / 2))]; 
	  } else {
		  waitingtime++;
		  if (waitingtime > 600) {
			  say_waiting = true;
			  waitingtime = 0;
		  } else {
			  say_waiting = false;
		  }
		  img = standingSprites[0];
	  }
	  
	  
	
}

/** Displays the screenmate (and it's extensions)
 *  @param g <code>Graphics</code> to handle the rendering
 */
public void paint(Graphics g) {
    g.drawImage(img, 0, 0, this);
  }


public void update(LineEvent event) {
	
}

// ----- Getters -----

public Clip getClip() {
	return clip;
}

public String[] getSprite_filenames() {
	return sprite_filenames;
}

public BufferedImage getImg() {
	return img;
}

public long getRunningtime() {
	return runningtime;
}

public long getWaitingtime() {
	return waitingtime;
}

public boolean isRunning() {
	return running;
}

public boolean isDragged() {
	return dragged;
}

public int getSound_off() {
	return sound_off;
}

public boolean isSay_move() {
	return say_move;
}

public boolean isSay_waiting() {
	return say_waiting;
}

public boolean isSay_dragged() {
	return say_dragged;
}

public boolean isDirX() {
	return dirX;
}

public boolean isDirY() {
	return dirY;
}

//  ----- Setters -----

public void setSprite_filenames(String[] sprite_filenames) {
	this.sprite_filenames = sprite_filenames;
}

public void setImg(BufferedImage img) {
	this.img = img;
}

public void setRunningtime(long runningtime) {
	this.runningtime = runningtime;
}

public void setWaitingtime(long waitingtime) {
	this.waitingtime = waitingtime;
}

public void setRunning(boolean running) {
	this.running = running;
}

public void setDragged(boolean dragged) {
	this.dragged = dragged;
}

public void setSound_off(int sound_off) {
	this.sound_off = sound_off;
}

public void setSay_move(boolean say_move) {
	this.say_move = say_move;
}

public void setSay_waiting(boolean say_waiting) {
	this.say_waiting = say_waiting;
}

public void setSay_dragged(boolean say_dragged) {
	this.say_dragged = say_dragged;
}

public void setDirX(boolean dirX) {
	this.dirX = dirX;
}

public void setDirY(boolean dirY) {
	this.dirY = dirY;
}
}