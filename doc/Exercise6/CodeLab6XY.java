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

import org.jogamp.java3d.Alpha;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.PositionInterpolator;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Point3d;

public class CodeLab6XY extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static JFrame frame;
	
	private static String frame_name = "XY's Lab #6";
	private static boolean r_tag = true;
	private static final String OBJECT_NAME = "Textured Disk";
	
	/* a function to build and return the content branch */
	private static BranchGroup create_Scene() {
		BranchGroup sceneBG = new BranchGroup();

		TransformGroup sceneTG = new TransformGroup();     // introduce a TransformGroup for rotation 
		sceneBG.addChild(CommonsXY.rotate_Behavior(7500, sceneTG));

		String[] side_name = {"Top", "Side"};              // create disk sides
		sceneTG.addChild(L5TextureSurfaceXY.ring_Shape(side_name[1], 60));

		TransformGroup topTG = new TransformGroup();      // need 'topTG' to move the (top) surface
		topTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		topTG.addChild(L5TextureSurfaceXY.ring_Shape(side_name[0], 60));

		Transform3D axisPosition = new Transform3D();
		axisPosition.rotY(-Math.PI / 2.0);                 // need to move along X-axis
		Alpha alpha = new Alpha(-1, Alpha.INCREASING_ENABLE | Alpha.DECREASING_ENABLE, 
				0, 0, 4000, 0000, 1000, 4000, 0000, 1000);
		PositionInterpolator positionInterpol = 
				new PositionInterpolator(alpha, topTG, axisPosition, 0.6f, 0.0f);
		positionInterpol.setSchedulingBounds(CommonsXY.twenty_BS);
		sceneTG.addChild(topTG);
		sceneTG.addChild(positionInterpol);
		
		CommonsXY.control_Rotation(r_tag);                 // make 'sceneBG' rotating by default
		sceneBG.addChild(sceneTG);
		
		return sceneBG;  
	}

	/* a constructor to set up for the application */
	public CodeLab6XY(BranchGroup scene) {
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

	public static void main(String[] args) {               // NOTE: copyright material 
		frame = new JFrame(frame_name + ": Moving Textured Disks");
		frame.getContentPane().add(new CodeLab6XY(create_Scene()));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}	

	@Override
	public void actionPerformed(ActionEvent e) {		
		switch(e.getActionCommand()) {                     // handle different menu items
		case "Exit": 
			System.exit(0);                                // quit the application
		case "Pause/Rotate":
			r_tag = (r_tag == true)? false : true;
			CommonsXY.control_Rotation(r_tag);
			return;
		default:
			return;
		}
	}	
}
