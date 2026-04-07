package lab5;/* Copyright material by xyuan@uwindsor.ca,
 * for students working on assignments and projects */

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

public class CodeLab5LJ extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static JFrame frame;

    private static String frame_name = "LJ's Lab #5";
    private static BranchGroup alterableBG, shapeBG;
    private static GroupObjects groupObject;
    private static boolean r_tag = true;

    // ▼ 原代码只在 4/8 间切换。根据题意，需要 4→8→12→16→4 循环。
    private static final int[] SIDE_STEPS = {4, 8, 12, 16};
    private static int sideIdx = 0; // 当前边数的索引（从 4 开始）

    private static final String OBJECT_NAME = "Table";

    /* a function to build and return the content branch */
    private static BranchGroup create_Scene() {
        alterableBG = new BranchGroup();                   // allow 'alterableBG' to change children
        groupObject = new GroupObjects(L5TextureSurfaceLJ.round_Table(SIDE_STEPS[sideIdx]));
        shapeBG = groupObject.get_ShapeBG();               // get the BranchGroup with a ColorCube
        shapeBG.setCapability(BranchGroup.ALLOW_DETACH);

        return GroupObjects.scene_Group(alterableBG, shapeBG);
    }

    /* a constructor to set up for the application */
    public CodeLab5LJ(BranchGroup scene) {
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

    public static void main(String[] args) {
        frame = new JFrame(frame_name + ": " + SIDE_STEPS[sideIdx] + "-Sided Table"); // NOTE: copyright material
        frame.getContentPane().add(new CodeLab5LJ(create_Scene()));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String sub_title = null;

        switch (e.getActionCommand()) {                     // handle different menu items
            case "Exit":
                System.exit(0);                                // quit the application
            case "Pause/Rotate":
                r_tag = !r_tag;
                CommonsLJ.control_Rotation(r_tag);
                return;
            case OBJECT_NAME:
                // ▼ 循环切换 4→8→12→16→4，并重建场景对象
                sideIdx = (sideIdx + 1) % SIDE_STEPS.length;
                int sides = SIDE_STEPS[sideIdx];
                groupObject = new GroupObjects(L5TextureSurfaceLJ.round_Table(sides));
                sub_title = ": " + sides + "-Sided Table";
                break;
            default:
                return;
        }
        // 更新标题
        frame.setTitle(frame_name + sub_title);

        // 用新的 shape 替换旧的
        BranchGroup tmpBG = groupObject.get_ShapeBG();     // save the new shape
        shapeBG.detach();                                  // detach the previous shape
        shapeBG = tmpBG;
        shapeBG.setCapability(BranchGroup.ALLOW_DETACH);   // make the new shape detachable
        alterableBG.addChild(shapeBG);                     // update 'alterableBG'
    }
}