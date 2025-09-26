/* Copyright material for students working on assignments and projects */
package lab2;

import common.CommonsLJ;
import common.GroupObjects;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Point3d;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CodeLab2LJ extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static JFrame frame;

    private static String frame_name = "LJ's Lab #2";
    private static BranchGroup alterableBG, shapeBG;
    private static GroupObjects groupObject;
    private static boolean r_tag = true;
    private static boolean object_tag = true;
    private static final String OBJECT_NAME = "Star";

    /* a function to build and return the content branch */
    private static BranchGroup create_Scene() {
        alterableBG = new BranchGroup();                   // allow 'alterableBG' to change children
        groupObject = new GroupObjects(L2StarLJ.line_Shape(0.6f));
        shapeBG = groupObject.get_ShapeBG();               // get the BranchGroup with a star shape

        return GroupObjects.scene_Group(alterableBG, shapeBG);
    }

    /* a constructor to set up for the application */
    public CodeLab2LJ(BranchGroup scene) {
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas3D = new Canvas3D(config);
        canvas3D.setSize(800, 800);                        // set size of canvas
        SimpleUniverse su = new SimpleUniverse(canvas3D);  // create a SimpleUniverse
        // set the viewer's location
        CommonsLJ.define_Viewer(su, new Point3d(1.35, -0.35, 1.5));
        scene.addChild(CommonsLJ.add_Lights(CommonsLJ.White, 1));

        scene.compile();                                   // optimize the BranchGroup
        su.addBranchGraph(scene);                          // attach 'scene' to 'su'

        Menu m = new Menu("Menu");                         // set menu's label
        m.addActionListener(this);
        MenuBar menuBar = CommonsLJ.build_MenuBar(m, OBJECT_NAME);                 // build and set the menu bar
        frame.setMenuBar(menuBar);

        setLayout(new BorderLayout());
        add("Center", canvas3D);
        frame.setSize(810, 800);                           // set the size of the frame
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        frame = new JFrame(frame_name + ": Star");     // NOTE: copyright material
        frame.getContentPane().add(new CodeLab2LJ(create_Scene()));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String chosen_item = e.getActionCommand();

        frame.setTitle(frame_name + ": " + chosen_item);
        switch (chosen_item) {                              // handle different menu items
            case "Exit":
                System.exit(0);                                // quit the application
            case "Pause/Rotate":
                r_tag = (r_tag == true) ? false : true;
                CommonsLJ.control_Rotation(r_tag);
                return;
            case OBJECT_NAME:                                  // change object shape and name
                if (object_tag) {
                    groupObject = new GroupObjects(L2StarLJ.line_Shape(0.3f));
                    object_tag = false;                        // create a small star
                } else {
                    groupObject = new GroupObjects(L2StarLJ.line_Shape(0.6f));
                    object_tag = true;                         // create a big star
                }
                break;
            default:
                return;
        }

        BranchGroup tmpBG = groupObject.get_ShapeBG();     // save the new (shape) group
        shapeBG.detach();                                  // detach the previous shape
        shapeBG = tmpBG;
        shapeBG.setCapability(BranchGroup.ALLOW_DETACH);   // make the new shape detachable
        alterableBG.addChild(shapeBG);                     // update 'alterableBG'
    }
}
