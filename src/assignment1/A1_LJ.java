package assignment1;/* Copyright material for the convenience of students working on assignments */

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;

import javax.swing.*;
import java.awt.*;

public class A1_LJ extends JPanel {

	private static final long serialVersionUID = 1L;
	private static JFrame frame;
	private static final int OBJ_NUM = 2;

	/* a function to build the content branch */
	public static BranchGroup create_Scene() {
		BranchGroup sceneBG = new BranchGroup();           // create the scene' BranchGroup
		TransformGroup sceneTG = new TransformGroup();     // create the scene's TransformGroup

		BaseShapes_LJ[] baseShapes = new BaseShapes_LJ[OBJ_NUM];
		baseShapes[0] = new SquareShape();
		String str = "LJ's A1";
		baseShapes[1] = new ColorString(str, CommonsLJ.Green, 0.1,
				new Point3f(-str.length() / 4f, -5.75f, 5.0f));
		
		for (int i = 0; i < OBJ_NUM; i++)
			sceneTG.addChild(baseShapes[i].position_Object());
		
		sceneTG.addChild(CommonsLJ.three_Axes(CommonsLJ.Blue, 0.75f));
		sceneBG.addChild(CommonsLJ.add_Lights(CommonsLJ.White, 1));
		sceneBG.addChild(CommonsLJ.rotate_Behavior(7500, sceneTG));
		sceneBG.addChild(sceneTG);                         // make 'sceneTG' continuous rotating
		return sceneBG;
	}

	/* NOTE: Keep the constructor for each of the assignments */
	public A1_LJ(BranchGroup sceneBG) {
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		Canvas3D canvas = new Canvas3D(config);
		
		SimpleUniverse su = new SimpleUniverse(canvas);    // create a SimpleUniverse
		CommonsLJ.define_Viewer(su, new Point3d(4.0d, 0.0d, 1.0d));
		
		sceneBG.addChild(CommonsLJ.key_Navigation(su));    // allow key navigation
		sceneBG.compile();		                   // optimize the BranchGroup
		su.addBranchGraph(sceneBG);                        // attach the scene to SimpleUniverse

		setLayout(new BorderLayout());
		add("Center", canvas);
		frame.setSize(800, 800);                           // set the size of the JFrame
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		frame = new JFrame("LJ's Assignment 1");            // NOTE: change XY to student's initials
		frame.getContentPane().add(new A1_LJ(create_Scene()));  // create an instance of the class
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
