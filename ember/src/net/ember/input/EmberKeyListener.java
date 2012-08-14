package net.ember.input;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;

public class EmberKeyListener implements KeyListener, MouseListener {

	private static final int MAX_KEY_NUMBER = 512;
	
	public boolean[] keys = new boolean[MAX_KEY_NUMBER];
	
	public int pixx=0,pixy=0;
	private int downx=0, downy=0;
	@Override
	public void keyPressed(KeyEvent arg0) {
		keys[arg0.getKeyCode()]=true;
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		keys[arg0.getKeyCode()]=false;
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	public boolean[] getKeys(){
		return keys;
	}
	
	public void setAllFalse(){
		for(int i=0;i<MAX_KEY_NUMBER;i++){
			keys[i]=false;
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		pixx=arg0.getX()-downx;
		pixy=arg0.getY()-downy;
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		downx=arg0.getX();
		downy=arg0.getY();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		pixx=0;
		pixy=0;
	}

	@Override
	public void mouseWheelMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
