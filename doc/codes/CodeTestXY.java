/* For students to study and work on assignments and projects *
 * \\\\\ Copyright material (contact xyuan@uwindsor.ca) ///// */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.universe.*;

public class CodeTestXY extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static JFrame frame;
	private static boolean r_tag = false;
	private static SimpleUniverse su;

	/* a function to build and return the content branch */
	private static BranchGroup create_Scene() {
		su = GeometryShapes.simple_Universe();
	
		BranchGroup sceneBG = new BranchGroup();	
		
		GeometryShapes geoShape = new ShapeLine();   // NOTE: change the file name to test!
		BranchGroup shapeBG = geoShape.get_ShapeBG(); 
		
		TransformGroup sceneTG = new TransformGroup();
		sceneTG.addChild(shapeBG);
		sceneBG.addChild(sceneTG);                         // show object with/without rotation
		sceneBG.addChild(CommonsXY.rotate_Behavior(7500, sceneTG));
		CommonsXY.control_Rotation(false);                 // start without rotation
		
		return sceneBG;
	}

	/* a constructor to set up for the application */
	public CodeTestXY(BranchGroup scene) {

		scene.addChild(CommonsXY.add_Lights(CommonsXY.White, 1));
//		scene.addChild(ExtraClass.three_Axes(CommonsXY.Blue, 0.75f));
//		scene.addChild(CommonsXY.key_Navigation(su));
		
		scene.compile();		                           // optimize the BranchGroup
		su.addBranchGraph(scene);                          // attach 'scene' to 'su'

		setLayout(new BorderLayout());
		add("Center", su.getCanvas());
		frame.setSize(810, 800);                           // set the size of the frame
		MenuBar menuBar = build_MenuBar();                 // build and set the menu bar
		frame.setMenuBar(menuBar);
		frame.setVisible(true);
	}

	/* The main entrance of the program */
	public static void main(String[] args) {         
		frame = new JFrame("XY's Testing Program");
		frame.getContentPane().add(new CodeTestXY(create_Scene()));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}	

	/* a function to build the menu bar of the demo */
	private MenuBar build_MenuBar() {
		MenuBar menuBar = new MenuBar();
		Menu m = new Menu("Menu");                         // set menu's label
		m.addActionListener(this);

		m.add("Exit");		                               // specify menu items
		m.add("Pause/Rotate");
		menuBar.add(m);                                    // add items to the menu

		return menuBar;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String chosen_item = e.getActionCommand();
		
		if (chosen_item == "Exit")                         // quit the application
			System.exit(0);

		switch(chosen_item) {                              // handle different menu items
		case "Pause/Rotate":
			if (r_tag == true) {
				CommonsXY.control_Rotation(false);
				r_tag = false;
			}
			else {
				CommonsXY.control_Rotation(true);
				r_tag = true;				
			}
			return;
		default:
			return;
		}
	}	
}

