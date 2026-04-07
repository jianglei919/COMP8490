/* Copyright material by xyuan@uwindsor.ca,
 * for students working on assignments and projects */

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Point3d;

public class CodeLab5XY extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static JFrame frame;
	
	private static String frame_name = "XY's Lab #5";
	private static BranchGroup alterableBG, shapeBG;
	private static GroupObjects groupObject;
	private static boolean r_tag = true;
	private static boolean object_tag = true;
	private static final String OBJECT_NAME = "Table";
	
	/* a function to build and return the content branch */
	private static BranchGroup create_Scene() {
		alterableBG = new BranchGroup();                   // allow 'alterableBG' to change children
		groupObject = new GroupObjects(L5TextureSurfaceXY.round_Table(4)); 
		shapeBG = groupObject.get_ShapeBG();               // get the BranchGroup with a ColorCube
		
		return GroupObjects.scene_Group(alterableBG, shapeBG);
	}

	/* a constructor to set up for the application */
	public CodeLab5XY(BranchGroup scene) {
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		Canvas3D canvas3D = new Canvas3D(config);
		canvas3D.setSize(800, 800);                        // set size of canvas
		SimpleUniverse su = new SimpleUniverse(canvas3D);  // create a SimpleUniverse
		                                                   // set the viewer's location
		CommonsXY.define_Viewer(su, new Point3d(1.35, -0.35, 10.0)); 		
		scene.addChild(CommonsXY.add_Lights(CommonsXY.White, 2));
		
		scene.compile();		                           // optimize the BranchGroup
		su.addBranchGraph(scene);                          // attach 'scene' to 'su'

		Menu m = new Menu("Menu");                         // set menu's label
		m.addActionListener(this);
		MenuBar menuBar = CodeLab2XY.build_MenuBar(m, OBJECT_NAME);
		frame.setMenuBar(menuBar);                         // build and set the menu bar

		setLayout(new BorderLayout());
		add("Center", canvas3D);
		frame.setSize(810, 800);                           // set the size of the frame
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		frame = new JFrame(frame_name + ": 4-Sided Table"); // NOTE: copyright material
		frame.getContentPane().add(new CodeLab5XY(create_Scene()));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}	

	@Override
	public void actionPerformed(ActionEvent e) {
		String sub_title = null;
		
		switch(e.getActionCommand()) {                     // handle different menu items
		case "Exit": 
			System.exit(0);                                // quit the application
		case "Pause/Rotate":
			r_tag = (r_tag == true)? false : true;
			CommonsXY.control_Rotation(r_tag);
			return;
		case OBJECT_NAME:			
			if (object_tag) {                              // create a small (partial) disk
				groupObject = new GroupObjects(L5TextureSurfaceXY.round_Table(8));
				object_tag = false;
			}
			else {                                         // create a big (partial) disk
				groupObject = new GroupObjects(L5TextureSurfaceXY.round_Table(4));
				object_tag = true;
			}
			break;
		default:
			return;
		}
		frame.setTitle(frame_name + sub_title);
		
		BranchGroup tmpBG = groupObject.get_ShapeBG();     // save the new shape
		shapeBG.detach();                                  // detach the previous shape
		shapeBG = tmpBG;
		shapeBG.setCapability(BranchGroup.ALLOW_DETACH);   // make the new shape detachable
		alterableBG.addChild(shapeBG);                     // update 'alterableBG'
	}	
}
