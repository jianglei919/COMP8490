package lab6;/* Copyright material by xyuan@uwindsor.ca,
 * for students working on assignments and projects */

import common.CommonsLJ;
import lab5.L5TextureSurfaceLJ;
import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Point3d;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CodeLab6LJ extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static JFrame frame;

    private static String frame_name = "LJ's Lab #6";
    private static boolean r_tag = true;
    private static final String OBJECT_NAME = "Textured Disk";

    /* a function to build and return the content branch */
    private static BranchGroup create_Scene() {
        BranchGroup sceneBG = new BranchGroup();

        TransformGroup sceneTG = new TransformGroup();     // introduce a TransformGroup for rotation
        sceneBG.addChild(CommonsLJ.rotate_Behavior(7500, sceneTG));

        String[] side_name = {"Top", "Side", "Bottom"};              // create disk sides
        sceneTG.addChild(L5TextureSurfaceLJ.ring_Shape(side_name[1], 60));

        TransformGroup topTG = new TransformGroup();      // need 'topTG' to move the (top) surface
        topTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        topTG.addChild(L5TextureSurfaceLJ.ring_Shape(side_name[0], 60));


        Transform3D axisPosition = new Transform3D();
        axisPosition.rotY(-Math.PI / 2.0);                 // need to move along X-axis
        Alpha alpha = new Alpha(-1, Alpha.INCREASING_ENABLE | Alpha.DECREASING_ENABLE,
                0, 0, 4000, 0000, 1000, 4000, 0000, 1000);
        PositionInterpolator positionInterpol =
                new PositionInterpolator(alpha, topTG, axisPosition, 0.6f, 0.0f);
        positionInterpol.setSchedulingBounds(CommonsLJ.twenty_BS);
        sceneTG.addChild(topTG);
        sceneTG.addChild(positionInterpol);

        CommonsLJ.control_Rotation(r_tag);                 // make 'sceneBG' rotating by default
        sceneBG.addChild(sceneTG);

        return sceneBG;
    }

    /* a constructor to set up for the application */
    public CodeLab6LJ(BranchGroup scene) {
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas3D = new Canvas3D(config);
        canvas3D.setSize(800, 800);                        // set size of canvas
        SimpleUniverse su = new SimpleUniverse(canvas3D);  // create a SimpleUniverse
        // set the viewer's location
        CommonsLJ.define_Viewer(su, new Point3d(1.35, -0.35, 10.0));
        scene.addChild(CommonsLJ.add_Lights(CommonsLJ.White, 2));

        scene.compile();                                   // optimize the BranchGroup
        su.addBranchGraph(scene);                          // attach 'scene' to 'su'

        Menu m = new Menu("Menu");                         // set menu's label
        m.addActionListener(this);
        MenuBar menuBar = CommonsLJ.build_MenuBar(m, OBJECT_NAME);
        frame.setMenuBar(menuBar);                         // build and set the menu bar

        setLayout(new BorderLayout());
        add("Center", canvas3D);
        frame.setSize(810, 800);                           // set the size of the frame
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {               // NOTE: copyright material
        frame = new JFrame(frame_name + ": Moving Textured Disks");
        frame.getContentPane().add(new CodeLab6LJ(create_Scene()));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {                     // handle different menu items
            case "Exit":
                System.exit(0);                                // quit the application
            case "Pause/Rotate":
                r_tag = (r_tag == true) ? false : true;
                CommonsLJ.control_Rotation(r_tag);
                return;
            default:
                return;
        }
    }
}
