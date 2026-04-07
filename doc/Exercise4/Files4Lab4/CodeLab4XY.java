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
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3f;

public class CodeLab4XY extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static JFrame frame;
	
	private static String frame_name = "XY's Lab #4";
	private static boolean r_tag = true;
	private static final String OBJECT_NAME = "Disk";
	private static TransformGroup sideTG;
	private static int gap_tag = 0;
	
	/* a function to build and return the content branch */
	private static BranchGroup create_Scene() {

		BranchGroup sceneBG = new BranchGroup();
		TransformGroup sceneTG = new TransformGroup();     // introduce a TransformGroup for rotation 
	                                                       // specify color for the two sides of a disk
		Color3f[] side_color = {CommonsXY.Orange, CommonsXY.Green};
		                                                   // create and attach the (stationary) side surface
		sceneTG.addChild(L4TransformXY.ring_Side(1, side_color[1]));

		sideTG = new TransformGroup();                     // make the (top) flat surface translatable
		sideTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		sideTG.addChild(L4TransformXY.ring_Side(0, side_color[0]));
		sceneTG.addChild(sideTG);
		
		sceneBG.addChild(CommonsXY.rotate_Behavior(7500, sceneTG));
		CommonsXY.control_Rotation(r_tag);                 // make 'alterableBG' rotating by default
		sceneBG.addChild(sceneTG);
		
		return sceneBG;  
	}

	/* a constructor to set up for the application */
	public CodeLab4XY(BranchGroup scene) {
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
		frame = new JFrame(frame_name + ": No Gap");       // NOTE: copyright material
		frame.getContentPane().add(new CodeLab4XY(create_Scene()));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}	

	@Override
	public void actionPerformed(ActionEvent e) {
		String chosen_item = e.getActionCommand();		

		frame.setTitle(frame_name + ": " + chosen_item);
		switch(chosen_item) {                              // handle different menu items
		case "Exit": 
			System.exit(0);                                // quit the application
		case "Pause/Rotate":
			r_tag = (r_tag == true)? false : true;
			CommonsXY.control_Rotation(r_tag);             // alter between rotation and pause
			return;
		case OBJECT_NAME:
			gap_tag = (gap_tag == 0)? 1: 0;                // alter between gap and no gap
			
			Transform3D gapTF = new Transform3D();         // 4x4 matrix for translation
			gapTF.setTranslation(new Vector3f(0f, 0f, (float)(0.5 * gap_tag)));
			sideTG.setTransform(gapTF);                    // update 'sideTG' to move the surface
			break;
		default:
			return;
		}
	}	
}
