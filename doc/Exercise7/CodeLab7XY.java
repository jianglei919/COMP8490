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
import org.jogamp.java3d.RotationInterpolator;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3f;

public class CodeLab7XY extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static JFrame frame;
	
	private static String frame_name = "XY's Lab #7";
	private static boolean r_tag = true;
	private static final String OBJECT_NAME = "Textured Disk";
	private static Alpha[] alpha = new Alpha[1];
	
	/* a function to make the disk's side surface rotating  */
	public static void rotate_Side(BranchGroup snBG, TransformGroup snTG, Alpha[] aph) {

		String[] side_name = {"Top", "Side"};
		Transform3D slide, plate, rotate_axis;
		TransformGroup slideTG, plateTG, hingeTG;
		RotationInterpolator rotationInterpol;
		
		slide = new Transform3D();
		slide.setTranslation(new Vector3f(-2.0f, 0, 0.1f));
		slideTG = new TransformGroup(slide);

		plate = new Transform3D();                         // shift the circular surface's far end to rotational origin
		plate.setTranslation(new Vector3f(2.0f, 0, -0.1f));
		plateTG = new TransformGroup(plate);               // need 'plateTG' to position circular surface for rotation
		plateTG.addChild(L5TextureSurfaceXY.ring_Shape(side_name[0], 60));

		hingeTG = new TransformGroup();                    // use 'hingeTG' for rotation
		hingeTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		rotate_axis = new Transform3D();                   // rotate around 'hingeTG's y-axis
		aph[0] = new Alpha(-1, Alpha.INCREASING_ENABLE | Alpha.DECREASING_ENABLE, 0, 0, 
				4000, 0000, 1000, 4000, 0000, 1000);
		rotationInterpol = new RotationInterpolator(aph[0], hingeTG, rotate_axis, 0, 
				-(float) (Math.PI / 2.0)); 
		rotationInterpol.setSchedulingBounds(CommonsXY.twenty_BS);
		slideTG.addChild(rotationInterpol);                // add rotation behavior to 'slideTG'

		hingeTG.addChild(plateTG);                         // attach translated circular surface for rotation
		slideTG.addChild(hingeTG);                         // attach the rotating circular surface

		snTG.addChild(slideTG);                            // attach the non-rotating disk side
		snTG.addChild(L5TextureSurfaceXY.ring_Shape(side_name[1], 60));
	}
	
	/* a function to build and return the content branch */
	private static BranchGroup create_Scene() {
		BranchGroup sceneBG = new BranchGroup();

		TransformGroup sceneTG = new TransformGroup();     // introduce a TransformGroup for rotation 
		sceneBG.addChild(CommonsXY.rotate_Behavior(7500, sceneTG));

		rotate_Side(sceneBG, sceneTG, alpha);              // make the two side surface rotating
		CommonsXY.control_Rotation(r_tag);                 // make 'sceneTG' rotating by default
		sceneBG.addChild(sceneTG);
		
		return sceneBG;  
	}

	/* a constructor to set up for the application */
	public CodeLab7XY(BranchGroup scene) {
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		Canvas3D canvas3D = new Canvas3D(config);
		canvas3D.setSize(800, 800);                        // set size of canvas
		SimpleUniverse su = new SimpleUniverse(canvas3D);  // create a SimpleUniverse
		                                                   // set the viewer's location
		CommonsXY.define_Viewer(su, new Point3d(1.35, -0.35, 10.0)); 		
		scene.addChild(CommonsXY.add_Lights(CommonsXY.White, 1));
		
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
		frame = new JFrame(frame_name + ": Rotating Textured Disks");     
		frame.getContentPane().add(new CodeLab7XY(create_Scene()));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}	

	@Override
	public void actionPerformed(ActionEvent e) {
		
		switch(e.getActionCommand()) {                     // handle the selected menu item
		case "Exit": 
			System.exit(0);                                // quit the application
		case "Pause/Rotate":
			r_tag = (r_tag == true)? false : true;
			CommonsXY.control_Rotation(r_tag);
			return;
		case OBJECT_NAME:			
			break;
		default:
			return;
		}
	}	
}
