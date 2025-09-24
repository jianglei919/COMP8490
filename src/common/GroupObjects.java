/* Copyright material for students working on assignments and projects */
package common;

import org.jogamp.java3d.Node;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.BranchGroup;

public class GroupObjects {

	public static BranchGroup scene_Group(BranchGroup a_BG, BranchGroup s_BG) {
		BranchGroup sceneBG = new BranchGroup();
		
		a_BG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		a_BG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		s_BG.setCapability(BranchGroup.ALLOW_DETACH);      // allow 'shapeBG' detached
		a_BG.addChild(s_BG);                               // add 'shapeBG' to 'alterableBG'
		
		TransformGroup sceneTG = new TransformGroup();     // introduce a TransformGroup for rotation 
		sceneTG.addChild(a_BG);                            // attach 'alterableBG' to 'sceneTG'
		sceneBG.addChild(CommonsLJ.rotate_Behavior(7500, sceneTG));
		CommonsLJ.control_Rotation(true);                 // make 'alterableBG' rotating by default
		sceneBG.addChild(sceneTG);
		
		return sceneBG;                                    // return the structured 'sceneBG'
	}
	
	protected BranchGroup shapeBG = new BranchGroup();
	
	/* a function to return 'shapeBG' as a BranchGroup */
	public BranchGroup get_ShapeBG() {
		return shapeBG;
	}	
	
	public GroupObjects(Node p) {
		shapeBG.addChild(p);
	}
}