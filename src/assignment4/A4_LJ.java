package assignment4;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

import javax.swing.*;
import java.awt.*;

public class A4_LJ extends JPanel {
    private static final long serialVersionUID = 1L;
    private static JFrame frame;

    private static final double SCALE_SHAFT = 0.18;
    private static final double SCALE_MOTOR = 0.60;
    private static final double SCALE_BLADE = 1.45;
    private static final double SCALE_GUARD = 1.5;

    private static final Vector3f YAW_PIVOT_POS = new Vector3f(0.0f, 0.65f, 0f);
    private static final float HUB_RADIUS = 0.1f;
    private static final float HUB_LENGTH = 0.65f;

    private static BladeSpinBehavior spinBeh;
    private static HeadYawBehavior  yawBeh;

    // 用于鼠标拾取与变色
    static Shape3D[] LEFT_BTN_PARTS;
    static Shape3D[] RIGHT_BTN_PARTS;

    static SwitchPanelController CONTROLLER;

    public A4_LJ(BranchGroup sceneBG) {
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas = new Canvas3D(config);
        SimpleUniverse su = new SimpleUniverse(canvas);
        CommonsLJ.define_Viewer(su, new Point3d(0.25d, 0.25d, 10.0d));
        sceneBG.addChild(CommonsLJ.key_Navigation(su));
        sceneBG.compile();
        su.addBranchGraph(sceneBG);

        // 安装鼠标拾取
        if (CONTROLLER != null) {
            CanvasMousePicker.install(canvas, sceneBG, CONTROLLER);
        }

        setLayout(new BorderLayout());
        add("Center", canvas);
        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /** 底座 + 文字 */
    public static TransformGroup create_Base(String str) {
        BaseShapeA baseShape = new BaseShapeA();
        Transform3D scale = new Transform3D();
        scale.setScale(new Vector3d(4d, 2d, 4d));
        TransformGroup baseTG = new TransformGroup(scale);
        baseTG.addChild(baseShape.position_Object());

        ColorString clr_str = new ColorString(str, CommonsLJ.Red, 0.05f,
                new Vector3f(-0.01f, -0.46f, 0.82f));
        TransformGroup rg = new TransformGroup();
        rg.addChild(clr_str.position_Object());
        baseTG.addChild(rg);
        return baseTG;
    }

    /** 组装整台风扇 */
    private static TransformGroup create_Fan() {
        // 1) 支架
        StandObjectA stand = new StandObjectA();
        TransformGroup fanRoot = stand.position_Object();

        // 2) 开关盒
        SwitchObjectA sw = new SwitchObjectA();
        TransformGroup switchTG = sw.position_Object();
        stand.add_Child(switchTG);

        // === 左/右按钮：使用 Box，并对 6 个面分别设置可拾取与可改外观 ===
        // 左按钮
        Transform3D leftT = new Transform3D();
        leftT.setTranslation(new Vector3f(0.675f, 0.00f, -0.05f));
        TransformGroup leftTG = new TransformGroup(leftT);

        Appearance redApp = CommonsLJ.obj_Appearance(CommonsLJ.Red);
        Box leftBox = new Box(0.325f, 0.305f, 0.315f,
                Box.GENERATE_NORMALS, redApp);

        int[] faces = {Box.FRONT, Box.BACK, Box.LEFT, Box.RIGHT, Box.TOP, Box.BOTTOM};
        LEFT_BTN_PARTS = new Shape3D[faces.length];
        for (int i = 0; i < faces.length; i++) {
            Shape3D s = leftBox.getShape(faces[i]);
            LEFT_BTN_PARTS[i] = s;
            s.setPickable(true);
            s.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
            s.setName(SwitchPanelController.LEFT_NAME);
        }
        leftTG.addChild(leftBox);
        switchTG.addChild(leftTG);

        // 右按钮
        Transform3D rightT = new Transform3D();
        rightT.setTranslation(new Vector3f(-0.675f, 0.0f, -0.05f));
        TransformGroup rightTG = new TransformGroup(rightT);

        Box rightBox = new Box(0.325f, 0.305f, 0.315f,
                Box.GENERATE_NORMALS, CommonsLJ.obj_Appearance(CommonsLJ.Red));

        RIGHT_BTN_PARTS = new Shape3D[faces.length];
        for (int i = 0; i < faces.length; i++) {
            Shape3D s = rightBox.getShape(faces[i]);
            RIGHT_BTN_PARTS[i] = s;
            s.setPickable(true);
            s.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
            s.setName(SwitchPanelController.RIGHT_NAME);
        }
        rightTG.addChild(rightBox);
        switchTG.addChild(rightTG);

        // 3) 底座 + 标识
        fanRoot.addChild(create_Base("LJ's A4"));

        // 4) 摇头枢轴
        Transform3D yawTrans = new Transform3D();
        yawTrans.setTranslation(new Vector3f(YAW_PIVOT_POS));
        TransformGroup yawPivot = new TransformGroup(yawTrans);
        yawPivot.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        stand.add_Child(yawPivot);

        // 5) 立柱
        ShaftObjectLJ shaft = new ShaftObjectLJ(SCALE_SHAFT, new Vector3f(0, -0.35f, 0));
        yawPivot.addChild(shaft.position_Object());

        // 6) 扇头整体挂点
        Transform3D headT = new Transform3D();
        headT.setTranslation(new Vector3f(0.0f, 0.65f, -0.5f));
        TransformGroup headMount = new TransformGroup(headT);
        yawPivot.addChild(headMount);

        // 7) 机头与护网
        MotorObjectLJ motor = new MotorObjectLJ(SCALE_MOTOR);
        Transform3D motorT = new Transform3D();
        motorT.setTranslation(new Vector3f(0f, 0f, 0.57f));
        TransformGroup motorMount = new TransformGroup(motorT);
        headMount.addChild(motorMount);
        motorMount.addChild(motor.position_Object());

        GuardObjectLJ guard = new GuardObjectLJ(SCALE_GUARD);
        Transform3D guardT = new Transform3D();
        guardT.setTranslation(new Vector3f(0f, 0f, 0f));
        TransformGroup guardMount = new TransformGroup(guardT);
        headMount.addChild(guardMount);
        guardMount.addChild(guard.position_Object());

        // 8) 叶片旋转枢轴
        TransformGroup spinPivot = new TransformGroup();
        spinPivot.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        headMount.addChild(spinPivot);

        // 9) 叶片整体微调
        Transform3D rotorShiftT = new Transform3D();
        rotorShiftT.setTranslation(new Vector3f(0f, 0f, 0.2f));
        TransformGroup rotorMount = new TransformGroup(rotorShiftT);
        spinPivot.addChild(rotorMount);

        // 10) 连接圆柱 + 叶片
        rotorMount.addChild(ShaftObjectLJ.createHub(HUB_RADIUS, HUB_LENGTH, new Color3f(0.65f, 0.65f, 0.65f)));
        BladeObjectLJ blade = new BladeObjectLJ(SCALE_BLADE);
        rotorMount.addChild(blade.position_Object());

        // 12) 行为
        spinBeh = new BladeSpinBehavior(spinPivot, (float) (2 * Math.PI / 0.5)); // 0.5s/圈
        yawBeh  = new HeadYawBehavior(yawPivot, YAW_PIVOT_POS, 5000, 2500, 200);

        // 声音 + 控制器
        SoundUtilityJOAL sound = new SoundUtilityJOAL();
        sound.load("cow", 0f, 0f, 0f, false);
        sound.load("ocean", 0f, 0f, 0f, true);

        CONTROLLER = new SwitchPanelController(
                LEFT_BTN_PARTS, RIGHT_BTN_PARTS,
                spinBeh, yawBeh, sound
        );
        // 初始：右=电源ON（红）；左=暂停ON（红）
        CONTROLLER.setRightOn(true);
        CONTROLLER.setLeftOn(true);
        CONTROLLER.applyAll();

        FanKeyBehavior keyBeh = new FanKeyBehavior(CONTROLLER);

        spinBeh.setSchedulingBounds(CommonsLJ.hundredBS);
        yawBeh.setSchedulingBounds(CommonsLJ.hundredBS);
        keyBeh.setSchedulingBounds(CommonsLJ.hundredBS);

        fanRoot.addChild(spinBeh);
        fanRoot.addChild(yawBeh);
        fanRoot.addChild(keyBeh);

        return fanRoot;
    }

    /** 场景根节点 */
    public static BranchGroup create_Scene() {
        BranchGroup sceneBG = new BranchGroup();
        TransformGroup sceneTG = new TransformGroup();
//        sceneTG.addChild(CommonsLJ.rotate_Behavior(7500, sceneTG));
        sceneTG.addChild(create_Fan());
        sceneBG.addChild(sceneTG);
        sceneBG.addChild(CommonsLJ.add_Lights(CommonsLJ.White, 1));
        return sceneBG;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("LJ's Assignment 4");
            frame.getContentPane().add(new A4_LJ(create_Scene()));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        });
    }
}