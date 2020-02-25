package commands;

import a2.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.SceneNode;
import ray.rml.*;

public class PitchAction extends AbstractInputAction {

	private MyGame game;

	public PitchAction(MyGame game) {
		super();
		this.game = game;
	}

	@Override
	public void performAction(float arg0, Event arg1) {
		float value=arg1.getValue();
		boolean up=(
				arg1.getComponent().getName().equals("Up")
				||arg1.getValue()<0);
		//sensitivity threshold
		if(value<0.1f&&value>-0.1f)
			return;
		
		//System.out.println(arg1.getComponent()+" "+arg1.getValue() + " " + up);
		
		value=Math.abs(value);
		
		Angle angle;
		
			SceneNode dolphin=game.getEngine().getSceneManager().getSceneNode("myDolphinNode");
			if(up) 
				angle=Degreef.createFrom(-1f*value);
			else 
				angle=Degreef.createFrom(1f*value);
			dolphin.pitch(angle);
	}

}
