/* Copyright material for the convenience of students working on assignments */

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.*;

public class A1_XY extends JPanel {

	private static final long serialVersionUID = 1L;
	private static JFrame frame;
	private static final int OBJ_NUM = 2;

	/* a function to build the content branch */
	public static BranchGroup create_Scene() {
		BranchGroup sceneBG = new BranchGroup();           // create the scene' BranchGroup
		TransformGroup sceneTG = new TransformGroup();     // create the scene's TransformGroup

		BaseShapes_XY[] baseShapes = new BaseShapes_XY[OBJ_NUM];
		baseShapes[0] = new SquareShape();
		String str = "XY's A1";
		baseShapes[1] = new ColorString(str, CommonsXY.Green, 0.1, 
				new Point3f(-str.length() / 4f, -5.75f, 5.0f));
		
		for (int i = 0; i < OBJ_NUM; i++)
			sceneTG.addChild(baseShapes[i].position_Object());
		
		sceneTG.addChild(CommonsXY.three_Axes(CommonsXY.Blue, 0.75f));
		sceneBG.addChild(CommonsXY.add_Lights(CommonsXY.White, 1));	
		sceneBG.addChild(CommonsXY.rotate_Behavior(7500, sceneTG));	
		sceneBG.addChild(sceneTG);                         // make 'sceneTG' continuous rotating
		return sceneBG;
	}

	/* NOTE: Keep the constructor for each of the assignments */
	public A1_XY(BranchGroup sceneBG) {
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		Canvas3D canvas = new Canvas3D(config);
		
		SimpleUniverse su = new SimpleUniverse(canvas);    // create a SimpleUniverse
		CommonsXY.define_Viewer(su, new Point3d(4.0d, 0.0d, 1.0d));
		
		sceneBG.addChild(CommonsXY.key_Navigation(su));    // allow key navigation
		sceneBG.compile();		                   // optimize the BranchGroup
		su.addBranchGraph(sceneBG);                        // attach the scene to SimpleUniverse

		setLayout(new BorderLayout());
		add("Center", canvas);
		frame.setSize(800, 800);                           // set the size of the JFrame
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		frame = new JFrame("XY's Assignment 1");            // NOTE: change XY to student's initials
		frame.getContentPane().add(new A1_XY(create_Scene()));  // create an instance of the class
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
