package assignment2;/* Copyright material for the convenience of students working on assignments */

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Point3d;

import javax.swing.*;
import java.awt.*;

public class A2_LJ extends JPanel {

	private static final long serialVersionUID = 1L;
	private static JFrame frame;

	/* a function to build the content branch */
	public static BranchGroup create_Scene() {
		BranchGroup sceneBG = new BranchGroup();           // create the scene' BranchGroup
		TransformGroup sceneTG = new TransformGroup();     // static container (axes stay still)
		TransformGroup objTG   = new TransformGroup();     // rotating container (entire wind turbine)

		// ===== base square + wind turbine parts + label =====
		BaseShapes_LJ[] parts = new BaseShapes_LJ[] {
				new SquareShape(),                     // 多色半透明基座（静止）
				new TowerShape(),                      // 塔
				new YawShapeA2("src/images/ImageTop.jpg"),         // 绕Y逆时针 20s/圈 + 贴图
				new NacelleShape(),                    // 机舱
				new RotorAssemblyA2("src/images/ImageTop.jpg"),  // 球+叶片一起绕Z顺时针 5s/圈 + 贴图
				new ColorString("LJ's A2", CommonsLJ.White, 0.1, null)
		};

		for (BaseShapes_LJ p : parts) objTG.addChild(p.position_Object());

		// ===== static axes & scene graph wiring =====
		sceneTG.addChild(CommonsLJ.three_Axes(CommonsLJ.Blue, 0.75f)); // axes don't rotate
		sceneBG.addChild(CommonsLJ.add_Lights(CommonsLJ.White, 1));    // light
		sceneBG.addChild(CommonsLJ.rotate_Behavior(7500, objTG));      // rotate the turbine only

		sceneTG.addChild(objTG);
		sceneBG.addChild(sceneTG);
		return sceneBG;
	}

	/* NOTE: Keep the constructor for each of the assignments */
	public A2_LJ(BranchGroup sceneBG) {
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		Canvas3D canvas = new Canvas3D(config);

		SimpleUniverse su = new SimpleUniverse(canvas);    // create a SimpleUniverse
		CommonsLJ.define_Viewer(su, new Point3d(4.0d, 0.0d, 1.0d));

		sceneBG.addChild(CommonsLJ.key_Navigation(su));    // allow key navigation
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
			frame = new JFrame("LJ's Assignment 2");
			frame.getContentPane().add(new A2_LJ(A2_LJ.create_Scene()));
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		});
	}
}