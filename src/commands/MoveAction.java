package commands;

import a2.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.SceneNode;

public class MoveAction extends AbstractInputAction {

	private Player player;
	
	enum Direction{
		fwd, back, left, right;
	}

	public MoveAction(Player player) {
		super();
		this.player = player;
	}

	@Override
	public void performAction(float arg0, Event arg1) {
		Direction dir; float value=arg1.getValue();
		if(arg1.getComponent().getName().equals("W")||
				(arg1.getComponent().getName().equals("Y Axis")&&value<-0.1f))
			dir=Direction.fwd;
		else if(arg1.getComponent().getName().equals("S")||
				(arg1.getComponent().getName().equals("Y Axis")&&value>0.1f))
			dir=Direction.back;
		else if(arg1.getComponent().getName().equals("A")||
				(arg1.getComponent().getName().equals("X Axis")&&value<-0.1f))
			dir=Direction.left;
		else if(arg1.getComponent().getName().equals("D")||
				(arg1.getComponent().getName().equals("X Axis")&&value>0.1f))
			dir=Direction.right;
		else return;
		
		value=Math.abs(value);
		
		SceneNode dolphin=player.getNode();
		
			switch(dir) {
			case fwd:
				dolphin.moveForward(player.getSpeed()*value);
				break;
			case back:
				dolphin.moveBackward(player.getSpeed()*value);
				break;
			case right:
				dolphin.moveLeft(player.getSpeed()*value);
				break;
			case left:
				dolphin.moveRight(player.getSpeed()*value);
				break;
			}
			
			
	}

}
