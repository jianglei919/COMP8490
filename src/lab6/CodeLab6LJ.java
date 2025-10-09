package lab6;

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

public class CodeLab6LJ extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static JFrame frame;

    private static final String FRAME_BASE = "LJ's Lab #6";
    private static boolean r_tag = true;
    private static final String OBJECT_NAME = "Textured Disk";

    // Interpolators & Alphas
    private static PositionInterpolator topInterpol;
    private static PositionInterpolator bottomInterpol;
    private static Alpha topLoopAlpha;       // 循环用
    private static Alpha bottomLoopAlpha;    // 循环用

    // 记录循环 Alpha 的 startTime（我们自管相位对齐）
    private static long topLoopAlphaStartMs;
    private static long bottomLoopAlphaStartMs;

    // TG
    private static TransformGroup topTG;
    private static TransformGroup bottomTG;

    // 暂停缓存
    private static Float   savedTopFrac = null;
    private static Float   savedBottomFrac = null;
    private static Boolean savedTopTowardEnd = null;     // true: 朝 END_POS；false: 朝 START_POS
    private static Boolean savedBottomTowardEnd = null;

    // 若当前正在跑一次性 Alpha，则记录它的目标端点（用于再次暂停判断方向）
    private static Float runningTopTargetEnd = null;
    private static Float runningBottomTargetEnd = null;

    // 动作分相
    private static int diskActionPhase = 0;

    // 插值端点（PositionInterpolator 的起止值）
    private static final float START_POS = 0.6f; // away
    private static final float END_POS   = 0.0f; // closed

    // 循环总时长：grow(4000) + atOne(1000) + shrink(4000) + atZero(1000)
    private static final long CYCLE_MS = 4000 + 1000 + 4000 + 1000; // = 10000ms

    // 轴
    private static Transform3D AXIS_TOP;
    private static Transform3D AXIS_BOTTOM;

    private static BranchGroup create_Scene() {
        BranchGroup sceneBG = new BranchGroup();

        TransformGroup sceneTG = new TransformGroup();
        sceneBG.addChild(CommonsLJ.rotate_Behavior(7500, sceneTG));

        String[] side_name = {"Top", "Side", "Bottom2"};
        sceneTG.addChild(L5TextureSurfaceLJ.ring_Shape(side_name[1], 60)); // Side

        // === Top ===
        topTG = new TransformGroup();
        topTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        topTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        topTG.addChild(L5TextureSurfaceLJ.ring_Shape(side_name[0], 60));

        Transform3D axisTop = new Transform3D();
        axisTop.rotY(-Math.PI / 2.0); // +X
        AXIS_TOP = new Transform3D(axisTop);

        topLoopAlpha = buildLoopingAlpha();
        topLoopAlphaStartMs = System.currentTimeMillis();
        topLoopAlpha.setStartTime(topLoopAlphaStartMs);

        topInterpol = new PositionInterpolator(topLoopAlpha, topTG, axisTop, START_POS, END_POS);
        topInterpol.setSchedulingBounds(CommonsLJ.twenty_BS);
        topInterpol.setEnable(true);

        sceneTG.addChild(topTG);
        sceneTG.addChild(topInterpol);

        // === Bottom ===
        bottomTG = new TransformGroup();
        bottomTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        bottomTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        bottomTG.addChild(L5TextureSurfaceLJ.ring_Shape(side_name[2], 60));

        Transform3D axisBottom = new Transform3D();
        axisBottom.rotY(Math.PI / 2.0); // -X
        AXIS_BOTTOM = new Transform3D(axisBottom);

        bottomLoopAlpha = buildLoopingAlpha();
        bottomLoopAlphaStartMs = System.currentTimeMillis();
        bottomLoopAlpha.setStartTime(bottomLoopAlphaStartMs);

        bottomInterpol = new PositionInterpolator(bottomLoopAlpha, bottomTG, axisBottom, START_POS, END_POS);
        bottomInterpol.setSchedulingBounds(CommonsLJ.twenty_BS);
        bottomInterpol.setEnable(true);

        sceneTG.addChild(bottomTG);
        sceneTG.addChild(bottomInterpol);

        CommonsLJ.control_Rotation(r_tag);
        sceneBG.addChild(sceneTG);

        return sceneBG;
    }

    private static Alpha buildLoopingAlpha() {
        // 循环：0 停 1s -> grow 4s -> 1 停 1s -> shrink 4s
        return new Alpha(
                -1,
                Alpha.INCREASING_ENABLE | Alpha.DECREASING_ENABLE,
                0, 0,
                4000, 0, 1000,
                4000, 0, 1000
        );
    }

    public CodeLab6LJ(BranchGroup scene) {
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas3D = new Canvas3D(config);
        canvas3D.setSize(800, 800);

        SimpleUniverse su = new SimpleUniverse(canvas3D);
        CommonsLJ.define_Viewer(su, new Point3d(1.35, -0.35, 10.0));
        scene.addChild(CommonsLJ.add_Lights(CommonsLJ.White, 2));

        scene.compile();
        su.addBranchGraph(scene);

        Menu m = new Menu("Menu");
        m.addActionListener(this);
        MenuBar menuBar = CommonsLJ.build_MenuBar(m, OBJECT_NAME);
        frame.setMenuBar(menuBar);

        setLayout(new BorderLayout());
        add("Center", canvas3D);

        frame.setSize(810, 800);
        frame.setLocationRelativeTo(null);
        frame.setTitle(FRAME_BASE + ": Running (Top & Bottom moving; Side stationary)");
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        frame = new JFrame(FRAME_BASE);
        frame.getContentPane().add(new CodeLab6LJ(create_Scene()));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        switch (cmd) {
            case "Exit":
                System.exit(0);
                return;

            case "Pause/Rotate":
                r_tag = !r_tag;
                CommonsLJ.control_Rotation(r_tag);
                frame.setTitle(FRAME_BASE + (r_tag ? ": Rotation ON" : ": Rotation PAUSED"));
                return;

            case OBJECT_NAME:
                switch (diskActionPhase) {
                    case 0: // Pause Top
                        savedTopFrac = freezeAtCurrentFraction(topTG, topInterpol, AXIS_TOP);
                        savedTopTowardEnd = determineTowardEnd(true, savedTopFrac);
                        topInterpol.setEnable(false); // 冻结位置
                        frame.setTitle(FRAME_BASE + ": Pause Top");
                        diskActionPhase = 1;
                        break;

                    case 1: // Pause Bottom
                        savedBottomFrac = freezeAtCurrentFraction(bottomTG, bottomInterpol, AXIS_BOTTOM);
                        savedBottomTowardEnd = determineTowardEnd(false, savedBottomFrac);
                        bottomInterpol.setEnable(false);
                        frame.setTitle(FRAME_BASE + ": Pause Bottom");
                        diskActionPhase = 2;
                        break;

                    case 2: // Resume Top（一次性 Alpha -> 自动切回循环 Alpha）
                        if (savedTopFrac != null && savedTopTowardEnd != null) {
                            resumeSmooth(topInterpol, true, savedTopFrac, savedTopTowardEnd);
                            savedTopFrac = null;
                            savedTopTowardEnd = null;
                        }
                        frame.setTitle(FRAME_BASE + ": Resume Top");
                        diskActionPhase = 3;
                        break;

                    case 3: // Resume Bottom
                        if (savedBottomFrac != null && savedBottomTowardEnd != null) {
                            resumeSmooth(bottomInterpol, false, savedBottomFrac, savedBottomTowardEnd);
                            savedBottomFrac = null;
                            savedBottomTowardEnd = null;
                        }
                        frame.setTitle(FRAME_BASE + ": Resume Bottom");
                        diskActionPhase = 0;
                        break;
                }
                return;

            default:
                return;
        }
    }

    /**
     * 读取“沿插值轴方向”的当前位移 u，并映射到 [START_POS..END_POS] 的比例 frac（0~1）。
     * 使用“轴向投影”来避免世界坐标与插值轴不一致带来的误差。
     */
    private static float freezeAtCurrentFraction(TransformGroup tg, PositionInterpolator pi, Transform3D axis) {
        Transform3D cur = new Transform3D();
        tg.getTransform(cur);
        Vector3f trans = new Vector3f();
        cur.get(trans);

        Transform3D rotOnly = new Transform3D(axis);
        rotOnly.setTranslation(new Vector3f(0, 0, 0));
        Vector3f dirWorld = new Vector3f(1, 0, 0);
        rotOnly.transform(dirWorld);
        dirWorld.normalize();

        float u = trans.x * dirWorld.x + trans.y * dirWorld.y + trans.z * dirWorld.z;

        float denom = (START_POS - END_POS);
        float frac = (denom == 0f) ? 0f : (START_POS - u) / denom;
        if (frac < 0f) frac = 0f;
        if (frac > 1f) frac = 1f;

        tg.setTransform(cur);
        return frac;
    }

    /**
     * 判断当前是否“朝 END_POS 前进”。
     * 若当前在跑一次性 Alpha，则以 runningTargetEnd 为准；否则由循环 Alpha 的相位判断：
     *   phase ∈ [0,10000): [0,1000) atZero, [1000,5000) grow(朝 END_POS), [5000,6000) atOne, [6000,10000) shrink(朝 START_POS)
     */
    private static boolean determineTowardEnd(boolean top, float fracNow) {
        if (top && runningTopTargetEnd != null) {
            return Math.abs(runningTopTargetEnd - END_POS) < 1e-6f;
        }
        if (!top && runningBottomTargetEnd != null) {
            return Math.abs(runningBottomTargetEnd - END_POS) < 1e-6f;
        }

        long now = System.currentTimeMillis();
        long start = top ? topLoopAlphaStartMs : bottomLoopAlphaStartMs;
        long phase = Math.floorMod(now - start, CYCLE_MS);
        return phase >= 1000 && phase < 5000; // grow 段朝 END_POS
    }

    /**
     * 恢复：一次性 Alpha + 自动切回循环 Alpha。
     * 1) 计算暂停位置 s = lerp(START_POS, END_POS, pausedFrac)；
     * 2) 目标端点 target = towardEnd ? END_POS : START_POS；
     * 3) 设置 PI 的 start/end = s -> target，并绑定一次性 Alpha(0->1) 时长 = 剩余比例 * 4000；
     * 4) 启用 PI：首帧 == s，无闪移；
     * 5) 用 Swing Timer 在 duration 结束后，自动切回循环 Alpha，并把循环 Alpha 的相位对齐到端点的停顿段起点；
     */
    private static void resumeSmooth(PositionInterpolator pi, boolean top, float pausedFrac, boolean towardEnd) {
        float s = lerp(START_POS, END_POS, pausedFrac);
        float target = towardEnd ? END_POS : START_POS;

        float remain = Math.abs(target - s);
        float full = Math.abs(END_POS - START_POS);
        long duration = (long) ((remain / (full == 0f ? 1f : full)) * 4000.0);
        if (duration < 16) duration = 16; // 至少一帧（~60FPS），避免 0/极短导致“好像不动”

        // 重设插值器起止：s -> target
        pi.setStartPosition(s);
        pi.setEndPosition(target);

        // 一次性 Alpha（线性 0->1）
        Alpha oneShot = new Alpha(
                1,
                Alpha.INCREASING_ENABLE,
                0, 0,
                duration, 0, 0,
                0, 0, 0
        );
        pi.setAlpha(oneShot);
        oneShot.setStartTime(System.currentTimeMillis());

        // 记录当前一次性目标（供再次 Pause 时判断方向）
        if (top) runningTopTargetEnd = target; else runningBottomTargetEnd = target;

        // 启用插值器：首帧 == s，不会闪
        pi.setEnable(true);

        // === 核心补全：一次性 Alpha 结束后，自动切回“循环 Alpha”，并对齐相位到端点的停顿段 ===
        new javax.swing.Timer((int) duration + 1, evt -> {
            // 切回对应的循环 Alpha
            Alpha loop = top ? topLoopAlpha : bottomLoopAlpha;

            long now = System.currentTimeMillis();
            long newStart;
            if (Math.abs(target - END_POS) < 1e-6f) {
                // 到达 END_POS：应进入 atOne 的 1s 停顿段起点（phase=5000）
                newStart = now - 5000L;
            } else {
                // 到达 START_POS：应进入 atZero 的 1s 停顿段起点（phase=0）
                newStart = now; // phase=0
            }

            loop.setStartTime(newStart);
            if (top) {
                topLoopAlphaStartMs = newStart;
                runningTopTargetEnd = null; // 清除一次性目标标记
            } else {
                bottomLoopAlphaStartMs = newStart;
                runningBottomTargetEnd = null;
            }

            // 恢复插值器的端点为原始 START/END（让后续 grow/shrink 正常）
            pi.setStartPosition(START_POS);
            pi.setEndPosition(END_POS);

            // 切换回循环 Alpha，继续按节奏运动
            pi.setAlpha(loop);

            // 立即执行一次启用（若用户没再暂停）
            pi.setEnable(true);

            // 停掉这个一次性的 timer
            ((javax.swing.Timer) evt.getSource()).stop();
        }) {{
            setRepeats(false);
            start();
        }};
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
