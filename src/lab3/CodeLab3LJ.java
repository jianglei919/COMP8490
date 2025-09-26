package lab3;

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

public class CodeLab3LJ extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static JFrame frame;

    private static String frame_name = "LJ's Lab #3";
    private static BranchGroup alterableBG, shapeBG;
    private static GroupObjects groupObject;
    private static boolean r_tag = true;
    private static final String OBJECT_NAME = "Disk";

    // ===== 新增：尺寸循环控制 =====
    private enum DiskSize {BIG, MEDIUM, SMALL, TINY}

    private static DiskSize diskSize = DiskSize.BIG;

    private static float radiusFor(DiskSize s) {
        switch (s) {
            case BIG:
                return 2.0f;
            case MEDIUM:
                return 1.5f;
            case SMALL:
                return 1.0f;
            case TINY:
                return 0.5f;
        }
        return 2.0f;
    }

    private static String labelFor(DiskSize s) {
        switch (s) {
            case BIG:
                return "BIG (r=2.0)";
            case MEDIUM:
                return "MEDIUM (r=1.5)";
            case SMALL:
                return "SMALL (r=1.0)";
            case TINY:
                return "TINY (r=0.5)";
        }
        return "";
    }

    private static void cycleSize() {
        switch (diskSize) {
            case BIG:
                diskSize = DiskSize.MEDIUM;
                break;
            case MEDIUM:
                diskSize = DiskSize.SMALL;
                break;
            case SMALL:
                diskSize = DiskSize.TINY;
                break;
            case TINY:
                diskSize = DiskSize.BIG;
                break;
        }
    }

    /* a function to build and return the content branch */
    private static BranchGroup create_Scene() {
        alterableBG = new BranchGroup();                   // allow 'alterableBG' to change children
        groupObject = new GroupObjects(L3DiskLJ.ring_Side(2.0f));
        shapeBG = groupObject.get_ShapeBG();               // get the BranchGroup with a partial disk

        return GroupObjects.scene_Group(alterableBG, shapeBG);
    }

    /* a constructor to set up for the application */
    public CodeLab3LJ(BranchGroup scene) {
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas3D = new Canvas3D(config);
        canvas3D.setSize(800, 800);                        // set size of canvas
        SimpleUniverse su = new SimpleUniverse(canvas3D);  // create a SimpleUniverse
        // set the viewer's location
        CommonsLJ.define_Viewer(su, new Point3d(1.35, -0.35, 10.0));
        scene.addChild(CommonsLJ.add_Lights(CommonsLJ.White, 1));

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

    public static void main(String[] args) {
        frame = new JFrame(frame_name + ": Disk");         // NOTE: copyright material
        frame.getContentPane().add(new CodeLab3LJ(create_Scene()));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String chosen_item = e.getActionCommand();

        switch (chosen_item) {                              // handle different menu items
            case "Exit":
                System.exit(0);                                // quit the application
            case "Pause/Rotate":
                r_tag = (r_tag == true) ? false : true;
                CommonsLJ.control_Rotation(r_tag);             // alter between rotation and pause
                return;
            case OBJECT_NAME:
                // ==== 改动部分：四档循环 ====
                cycleSize();
                float r = radiusFor(diskSize);
                groupObject = new GroupObjects(L3DiskLJ.ring_Side(r));
                frame.setTitle(frame_name + ": " + OBJECT_NAME + " - " + labelFor(diskSize));
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