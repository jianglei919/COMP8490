package lab1;/* Copyright material, for students to work on assignments and projects */

import java.awt.*;
import javax.swing.*;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.ColorCube;
import org.jogamp.java3d.utils.universe.*;
import org.jogamp.vecmath.*;

public class RotatingCube extends JPanel {

	private static final long serialVersionUID = 1L;
	private static JFrame frame;

	/* a function to create a rotation behavior and refer it to 'rot_TG' */
	public static RotationInterpolator rotate_Behavior(int r_num, TransformGroup rot_TG) {
		rot_TG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		Transform3D yAxis = new Transform3D();
		Alpha rotationAlpha = new Alpha(-1, r_num);        // continuously rotating at 'r_num'/millisecond
		RotationInterpolator rot_beh = new RotationInterpolator(
				rotationAlpha, rot_TG, yAxis, 0.0f, (float) Math.PI * 2.0f);
		rot_beh.setSchedulingBounds(new BoundingSphere(new Point3d(), 20.0));
		return rot_beh;                                    // start rotation at 0- and end at 360-degrees
	}

	/* a function to position viewer at 'eye' location */
	public static void define_Viewer(SimpleUniverse simple_U, Point3d eye) {
	    TransformGroup viewTransform = simple_U.getViewingPlatform().getViewPlatformTransform();
		Point3d center = new Point3d(0, 0, 0);             // define the point where the eye looks at
		Vector3d up = new Vector3d(0, 1, 0);               // define camera's up direction
		Transform3D view_TM = new Transform3D();
		view_TM.lookAt(eye, center, up);                   // look at 'center' from 'eye'
		view_TM.invert();
	    viewTransform.setTransform(view_TM);               // set the TransformGroup of ViewingPlatform
	}

	/* a function to build the content branch and attach to 'scene' */
	private static BranchGroup create_Scene() {
		BranchGroup sceneBG = new BranchGroup();
		TransformGroup sceneTG = new TransformGroup();
		
		sceneTG.addChild(new ColorCube(0.5));
		sceneBG.addChild(rotate_Behavior(7500, sceneTG));
		sceneBG.addChild(sceneTG);
		
		return sceneBG;
	}

	/* a constructor to set up for the application */
	public RotatingCube(BranchGroup sceneBG) {
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		Canvas3D canvas = new Canvas3D(config);		
		SimpleUniverse su = new SimpleUniverse(canvas);    // create a SimpleUniverse
		
		define_Viewer(su, new Point3d(1, 1, 3));           // set the viewer's location		
		sceneBG.compile();		                           // optimize the BranchGroup
		su.addBranchGraph(sceneBG);                        // attach the scene to SimpleUniverse

		setLayout(new BorderLayout());
		add("Center", canvas);
		frame.setSize(800, 800);                           // set the size of the JFrame
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			frame = new JFrame("LJ's Rotating Cube");            // create frame and set title
			frame.getContentPane().add(new RotatingCube(create_Scene()));  // create an instance of the class
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		});
	}
}

