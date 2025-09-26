/* Copyright material by xyuan@uwindsor.ca,
 * for students working on assignments and projects */
package lab4;

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

import common.CommonsLJ;
import lab2.CodeLab2LJ;

public class CodeLab4LJ extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static JFrame frame;

	private static String frame_name = "LJ's Lab #4";
	private static boolean r_tag = true;
	private static final String OBJECT_NAME = "Disk";

	// 两个平面的 TransformGroup
	private static TransformGroup capTG1, capTG2;

	private static int gap_tag = 0; // 0:none,1:small,2:medium,3:large
	private static final float[] GAP_VALUES = new float[] { 0f, 0.25f, 0.5f, 0.75f };

	/* a function to build and return the content branch */
	private static BranchGroup create_Scene() {

		BranchGroup sceneBG = new BranchGroup();
		TransformGroup sceneTG = new TransformGroup(); // introduce a TransformGroup for rotation
														// specify color for the two sides of a disk
		Color3f[] side_color = { CommonsLJ.Yellow, CommonsLJ.Green };
		// create and attach the (stationary) side surface 创建侧壁
		sceneTG.addChild(L4TransformLJ.ring_Side(1, side_color[1]));

		// 第一个平面
		capTG1 = new TransformGroup(); // make the (top) flat surface translatable
		capTG1.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		capTG1.addChild(L4TransformLJ.ring_Side(-1, side_color[0]));
		sceneTG.addChild(capTG1);

		// 第二个平面（补上缺失的，颜色不同）
		capTG2 = new TransformGroup();
		capTG2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		capTG2.addChild(L4TransformLJ.ring_Side(0, CommonsLJ.Purple)); // 用蓝色区分
		sceneTG.addChild(capTG2);

		sceneBG.addChild(CommonsLJ.rotate_Behavior(7500, sceneTG));
		CommonsLJ.control_Rotation(r_tag); // make 'alterableBG' rotating by default
		sceneBG.addChild(sceneTG);

		return sceneBG;
	}

	/* a constructor to set up for the application */
	public CodeLab4LJ(BranchGroup scene) {
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		Canvas3D canvas3D = new Canvas3D(config);
		canvas3D.setSize(800, 800); // set size of canvas
		SimpleUniverse su = new SimpleUniverse(canvas3D); // create a SimpleUniverse
															// set the viewer's location
		CommonsLJ.define_Viewer(su, new Point3d(1.35, -0.35, 10.0));
		scene.addChild(CommonsLJ.add_Lights(CommonsLJ.White, 2));

		scene.compile(); // optimize the BranchGroup
		su.addBranchGraph(scene); // attach 'scene' to 'su'

		Menu m = new Menu("Menu"); // set menu's label
		m.addActionListener(this);
		MenuBar menuBar = CodeLab2LJ.build_MenuBar(m, OBJECT_NAME);
		frame.setMenuBar(menuBar); // build and set the menu bar

		setLayout(new BorderLayout());
		add("Center", canvas3D);
		frame.setSize(810, 800); // set the size of the frame
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		frame = new JFrame(frame_name + ": No Gap"); // NOTE: copyright material
		frame.getContentPane().add(new CodeLab4LJ(create_Scene()));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String chosen_item = e.getActionCommand();

		frame.setTitle(frame_name + ": " + chosen_item);
		switch (chosen_item) { // handle different menu items
		case "Exit":
			System.exit(0); // quit the application
		case "Pause/Rotate":
			r_tag = (r_tag == true) ? false : true;
			CommonsLJ.control_Rotation(r_tag); // alter between rotation and pause
			return;
		case OBJECT_NAME:
			// cycle gap among none/small/medium/large then back to none
			gap_tag = (gap_tag + 1) % GAP_VALUES.length; // 0,1,2,3
			float d = GAP_VALUES[gap_tag];

				// 对称移动两个平面
				Transform3D t1 = new Transform3D();
				t1.setTranslation(new Vector3f(0f, 0f, d));
				capTG1.setTransform(t1);

				Transform3D t2 = new Transform3D();
				t2.setTranslation(new Vector3f(0f, 0f, -d));
				capTG2.setTransform(t2);

				// update window title to reflect current gap
				String gapLabel = (gap_tag == 0 ? "none"
						: (gap_tag == 1 ? "small (0.25)" : (gap_tag == 2 ? "medium (0.5)" : "large (0.75)")));
				if (frame != null)
					frame.setTitle(frame_name + " — Gap: " + gapLabel);
				break;
			default:
				return;
		}
	}
}