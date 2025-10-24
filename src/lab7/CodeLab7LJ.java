package lab7;/* Copyright material by xyuan@uwindsor.ca,
 * for students working on assignments and projects */

import common.CommonsLJ;
import lab5.L5TextureSurfaceLJ;
import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3f;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CodeLab7LJ extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static JFrame frame;

    private static String frame_name = "LJ's Lab #7";
    private static boolean r_tag = true;
    private static final String OBJECT_NAME = "Textured Disk";

    /* Top 与 Bottom 两个面的旋转控制 */
    private static Alpha[] alpha = new Alpha[2];

    /* Textured Disk 的 4 步状态机 */
    private static int diskStep = 0; // 0:Stop Top → 1:Stop Bottom → 2:Resume Top → 3:Resume Bottom

    /* a function to make the disk's side surface rotating  */
    public static void rotate_Side(BranchGroup snBG, TransformGroup snTG, Alpha[] aph) {

        /*
         * Top 与 Bottom 各自使用一套 slideTG → hingeTG → plateTG，
         * slide 先把旋转轴移到靠侧环的边，再在 hingeTG 上旋转，最后 plate 抵消回位。
         * Z 上使用很小的 EPS 仅用于避免共面闪烁（Z-fighting），不是为了留缝。
         */
        String[] side_name = {"Top", "Side", "Bottom2"};

        // 统一半径与微偏移
        final float R = 2.0f;
        final float EPS = 0.09f; //表面与侧环接缝，可略调大一点点减少缝隙

        Transform3D slide, plate, rotate_axis;
        TransformGroup slideTG, plateTG, hingeTG;
        RotationInterpolator rotationInterpol;

        // ====== Top 圆面 ======
        slide = new Transform3D();
        slide.setTranslation(new Vector3f(-R, 0, +EPS));   // 把旋转轴移到靠侧环的边（并上表面微抬 EPS）
        slideTG = new TransformGroup(slide);

        plate = new Transform3D();                         // 抵消回位（并把上表面微降 EPS）
        plate.setTranslation(new Vector3f(+R, 0, -EPS));
        plateTG = new TransformGroup(plate);
        plateTG.addChild(L5TextureSurfaceLJ.ring_Shape(side_name[0], 60)); // "Top" 圆面

        hingeTG = new TransformGroup();                    // 在 hingeTG 上做旋转
        hingeTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        rotate_axis = new Transform3D();
        aph[0] = new Alpha(-1, Alpha.INCREASING_ENABLE | Alpha.DECREASING_ENABLE, 0, 0,
                4000, 0000, 1000, 4000, 0000, 1000);
        rotationInterpol = new RotationInterpolator(aph[0], hingeTG, rotate_axis, 0,
                -(float) (Math.PI / 2.0));                 // Top：0 ↔ -90°
        rotationInterpol.setSchedulingBounds(CommonsLJ.twenty_BS);
        slideTG.addChild(rotationInterpol);

        hingeTG.addChild(plateTG);
        slideTG.addChild(hingeTG);
        snTG.addChild(slideTG);

        // ====== Bottom 圆面（与 Top 方向相反，Z 偏移取对称）======
        Transform3D slide2 = new Transform3D();
        slide2.setTranslation(new Vector3f(-R, 0, -EPS));  // 注意这里用 -EPS（与 Top 对称）
        TransformGroup slideTG2 = new TransformGroup(slide2);

        Transform3D plate2 = new Transform3D();
        plate2.setTranslation(new Vector3f(+R, 0, +EPS));  // 对称的 +EPS
        TransformGroup plateTG2 = new TransformGroup(plate2);
        // 显示 Bottom
        plateTG2.addChild(L5TextureSurfaceLJ.ring_Shape(side_name[2], 60)); // 新增 Bottom 圆面

        TransformGroup hingeTG2 = new TransformGroup();
        hingeTG2.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        Transform3D rotate_axis2 = new Transform3D();
        aph[1] = new Alpha(-1, Alpha.INCREASING_ENABLE | Alpha.DECREASING_ENABLE, 0, 0,
                4000, 0000, 1000, 4000, 0000, 1000);
        RotationInterpolator rotationInterpol2 = new RotationInterpolator(aph[1], hingeTG2, rotate_axis2, 0,
                +(float) (Math.PI / 2.0));                 // Bottom：0 ↔ +90°
        rotationInterpol2.setSchedulingBounds(CommonsLJ.twenty_BS);
        slideTG2.addChild(rotationInterpol2);

        hingeTG2.addChild(plateTG2);
        slideTG2.addChild(hingeTG2);
        snTG.addChild(slideTG2);

        // ====== Side（侧环）保持静止 ======
        snTG.addChild(L5TextureSurfaceLJ.ring_Shape(side_name[1], 60));
    }

    /* a function to build and return the content branch */
    private static BranchGroup create_Scene() {
        BranchGroup sceneBG = new BranchGroup();

        TransformGroup sceneTG = new TransformGroup();     // introduce a TransformGroup for rotation
        sceneBG.addChild(CommonsLJ.rotate_Behavior(7500, sceneTG));

        rotate_Side(sceneBG, sceneTG, alpha);              // make the two side surface rotating
        CommonsLJ.control_Rotation(r_tag);                 // make 'sceneTG' rotating by default
        sceneBG.addChild(sceneTG);

        return sceneBG;
    }

    /* a constructor to set up for the application */
    public CodeLab7LJ(BranchGroup scene) {
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

    public static void main(String[] args) {               // NOTE: copyright material
        frame = new JFrame(frame_name + ": Rotating Textured Disks");
        frame.getContentPane().add(new CodeLab7LJ(create_Scene()));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        switch (e.getActionCommand()) {                     // handle the selected menu item
            case "Exit":
                System.exit(0);                                // quit the application
            case "Pause/Rotate":
                r_tag = (r_tag == true) ? false : true;
                CommonsLJ.control_Rotation(r_tag);
                return;
            case OBJECT_NAME:
                /* Textured Disk 的 4 步循环（暂停与恢复按顺序切换） */
                if (alpha[0] == null) return;
                switch (diskStep) {
                    case 0:
                        alpha[0].pause();
                        frame.setTitle(frame_name + ": Stopped Top");
                        break;
                    case 1:
                        if (alpha.length > 1 && alpha[1] != null) {
                            alpha[1].pause();
                        }
                        frame.setTitle(frame_name + ": Stopped Bottom");
                        break;
                    case 2:
                        alpha[0].resume();
                        frame.setTitle(frame_name + ": Resume Top");
                        break;
                    case 3:
                        if (alpha.length > 1 && alpha[1] != null) {
                            alpha[1].resume();
                        }
                        frame.setTitle(frame_name + ": Resume Bottom");
                        break;
                    default:
                        break;
                }
                diskStep = (diskStep + 1) % 4;
                return;
            default:
                return;
        }
    }
}
