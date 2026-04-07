package lab2;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.behaviors.vp.OrbitBehavior;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.AxisAngle4d;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3f;

import javax.swing.*;
import java.awt.*;

public class GroupObjectsCircle extends JPanel {
    private static final long serialVersionUID = 1L;

    private Canvas3D canvas;
    private SimpleUniverse universe;

    // 旋转相关
    private TransformGroup rotTG;                 // 挂载几何体并做旋转的 TG
    private RotationInterpolator rotator;         // 旋转行为

    // 切换小圆显示
    private Switch smallCircleSwitch;             // child 0: 小圆；通过 setWhichChild 控制显示/隐藏

    public GroupObjectsCircle() {
        setLayout(new BorderLayout());

        // 创建 Canvas3D
        GraphicsConfiguration gc = SimpleUniverse.getPreferredConfiguration();
        canvas = new Canvas3D(gc);
        add(canvas, BorderLayout.CENTER);

        // 创建 universe 与场景根结点
        universe = new SimpleUniverse(canvas);
        BranchGroup scene = createSceneGraph();
        scene.compile();

        // 视点设置
        ViewingPlatform vp = universe.getViewingPlatform();
        TransformGroup viewTG = vp.getViewPlatformTransform();
        Transform3D viewT3D = new Transform3D();
        // 后退一些，方便看到圆
        viewT3D.setTranslation(new Vector3f(0.0f, 0.0f, 3.0f));
        viewTG.setTransform(viewT3D);

        // 交互（鼠标轨道）可选：
        OrbitBehavior orbit = new OrbitBehavior(canvas);
        orbit.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
        vp.setViewPlatformBehavior(orbit);

        // 挂载场景
        universe.addBranchGraph(scene);
    }

    private BranchGroup createSceneGraph() {
        BranchGroup root = new BranchGroup();

        // 大旋转 TG：所有图元统一旋转
        rotTG = new TransformGroup();
        rotTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        rotTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        root.addChild(rotTG);

        // ===== 几何体：大圆 (半径 0.6, 白色) =====
        L2CircleLJ bigCircle = new L2CircleLJ(0.6f, new Color3f(1.0f, 1.0f, 1.0f));
        rotTG.addChild(bigCircle);

        // ===== 几何体：小圆 (半径 0.5, 可选颜色) =====
        Appearance smallApp = new Appearance();
        ColoringAttributes ca = new ColoringAttributes(new Color3f(0.3f, 0.7f, 1.0f), ColoringAttributes.SHADE_FLAT);
        smallApp.setColoringAttributes(ca);
        LineAttributes la = new LineAttributes();
        la.setLineWidth(2.0f);
        la.setLineAntialiasingEnable(true);
        smallApp.setLineAttributes(la);

        L2CircleLJ smallCircle = new L2CircleLJ(0.5f, null /* 用 Appearance 控制颜色 */);
        smallCircle.setAppearance(smallApp);

        // 使用 Switch 控制小圆显示/隐藏
        smallCircleSwitch = new Switch();
        smallCircleSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
        smallCircleSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
        smallCircleSwitch.addChild(smallCircle); // child index 0
        // 初始：不显示小圆（= CHILD_NONE）
        smallCircleSwitch.setWhichChild(Switch.CHILD_NONE);

        rotTG.addChild(smallCircleSwitch);

        // ===== 旋转行为 =====
        Alpha alpha = new Alpha(-1, 10000); // 无限循环，10 秒一圈
        Transform3D axis = new Transform3D();
        axis.setRotation(new AxisAngle4d(0, 1, 0, Math.PI / 2)); // 选一个初始轴

        rotator = new RotationInterpolator(alpha, rotTG, axis, 0.0f, (float) (2 * Math.PI));
        rotator.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
        root.addChild(rotator);

        // 光照（线框圆对光照不敏感，此处仅留作模板）
        AmbientLight light = new AmbientLight(true, new Color3f(1f, 1f, 1f));
        light.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
        root.addChild(light);

        return root;
    }

    /**
     * 暂停/恢复旋转
     */
    public void setPaused(boolean paused) {
        if (rotator != null) {
            rotator.setEnable(!paused);
        }
    }

    /**
     * 切换是否显示小圆
     */
    public void setShowSmallCircle(boolean show) {
        if (smallCircleSwitch != null) {
            if (show) {
                smallCircleSwitch.setWhichChild(0); // 显示 child 0（小圆）
            } else {
                smallCircleSwitch.setWhichChild(Switch.CHILD_NONE); // 隐藏
            }
        }
    }

    /**
     * 获取当前显示的圆数量（用于更新标题）
     */
    public int getCircleCount() {
        boolean show = CodeLab2CircleLJ.SHOW_SMALL_CIRCLE;
        return show ? 2 : 1;
    }
}
