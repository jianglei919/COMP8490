package assignment3;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

import javax.swing.*;
import java.awt.*;

public class A3_LJ extends JPanel {
    private static final long serialVersionUID = 1L;
    private static JFrame frame;

    private static final double SCALE_SHAFT = 0.18; // 立柱
    private static final double SCALE_MOTOR = 0.60; // 电机外壳
    private static final double SCALE_BLADE = 1.45; // 叶片组
    private static final double SCALE_GUARD = 1.5; // 护罩线框

    private static final Vector3f YAW_PIVOT_POS = new Vector3f(0.0f, 0.65f, 0f); // 摇头枢轴（在立柱顶）
    private static final float HUB_RADIUS = 0.1f;
    private static final float HUB_LENGTH = 0.65f;

    private static BladeSpinBehavior spinBeh;
    private static HeadYawBehavior yawBeh;

    public A3_LJ(BranchGroup sceneBG) {
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas = new Canvas3D(config);
        SimpleUniverse su = new SimpleUniverse(canvas);
        CommonsLJ.define_Viewer(su, new Point3d(0.25d, 0.25d, 10.0d));
        sceneBG.addChild(CommonsLJ.key_Navigation(su));
        sceneBG.compile();
        su.addBranchGraph(sceneBG);

        setLayout(new BorderLayout());
        add("Center", canvas);
        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * 创建风扇底座（带纹理的平板）并在底座前侧放上一行文字
     */
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

    /**
     * 组装整台风扇
     */
    private static TransformGroup create_Fan() {
        // 1) 支架
        StandObjectA stand = new StandObjectA();
        TransformGroup fanRoot = stand.position_Object();

        // 2) 开关
        stand.add_Child(new SwitchObjectA().position_Object());

        // 3) 底座 + 标识
        fanRoot.addChild(create_Base("LJ's A3"));

        // 4) 摇头枢轴（挂一切需要左右摆动的部件）
        Transform3D yawTrans = new Transform3D();
        yawTrans.setTranslation(new Vector3f(YAW_PIVOT_POS));
        TransformGroup yawPivot = new TransformGroup(yawTrans);
        yawPivot.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE); // 行为会改写它
        stand.add_Child(yawPivot);

        // 5) 立柱（随摆头）
        ShaftObjectLJ shaft = new ShaftObjectLJ(SCALE_SHAFT, new Vector3f(0, -0.35f, 0));
        yawPivot.addChild(shaft.position_Object());

        // 6) 扇头整体挂点（把机头/护网/叶片整体推到前方/上方）
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

        // 8) 叶片旋转枢轴（仅旋转，不要给它平移；旋转行为会每帧写它）
        TransformGroup spinPivot = new TransformGroup();
        spinPivot.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        headMount.addChild(spinPivot);

        // 9) 叶片整体微调（在旋转枢轴下做前后/高度微调）
        Transform3D rotorShiftT = new Transform3D();
        rotorShiftT.setTranslation(new Vector3f(0f, 0f, 0.2f));
        TransformGroup rotorMount = new TransformGroup(rotorShiftT);
        spinPivot.addChild(rotorMount);

        // 10) 连接圆柱 + 叶片
        rotorMount.addChild(ShaftObjectLJ.createHub(HUB_RADIUS, HUB_LENGTH, new Color3f(0.65f, 0.65f, 0.65f)));
        BladeObjectLJ blade = new BladeObjectLJ(SCALE_BLADE);
        rotorMount.addChild(blade.position_Object());

        // 12) 行为：叶片旋转 + 摇头 + 键盘
        spinBeh = new BladeSpinBehavior(spinPivot, (float) (2 * Math.PI / 0.5)); // 0.5s/圈
        spinBeh.setPower(true);

        // 使用基准位传入，避免读取 transform 的不确定性；平滑往返并在端点停顿
        yawBeh = new HeadYawBehavior(yawPivot, YAW_PIVOT_POS, 5000, 2500, 200);
        yawBeh.setPause(false);

        FanKeyBehavior keyBeh = new FanKeyBehavior(spinBeh, yawBeh);

        // 13) 调度范围
        spinBeh.setSchedulingBounds(CommonsLJ.hundredBS);
        yawBeh.setSchedulingBounds(CommonsLJ.hundredBS);
        keyBeh.setSchedulingBounds(CommonsLJ.hundredBS);

        // 14) 挂到场景图
        fanRoot.addChild(spinBeh);
        fanRoot.addChild(yawBeh);
        fanRoot.addChild(keyBeh);

        return fanRoot;
    }

    /**
     * 场景根节点
     */
    public static BranchGroup create_Scene() {
        BranchGroup sceneBG = new BranchGroup();
        TransformGroup sceneTG = new TransformGroup();

        // 整体旋转（可注释以便调参）
//        sceneTG.addChild(CommonsLJ.rotate_Behavior(7500, sceneTG));

        sceneTG.addChild(create_Fan());
        sceneBG.addChild(sceneTG);
        sceneBG.addChild(CommonsLJ.add_Lights(CommonsLJ.White, 1));
        return sceneBG;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("LJ's Assignment 3");
            frame.getContentPane().add(new A3_LJ(create_Scene()));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        });
    }
}
