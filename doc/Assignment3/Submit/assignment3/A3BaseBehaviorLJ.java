package assignment3;

import org.jogamp.java3d.*;
import org.jogamp.vecmath.Vector3f;

import java.util.Iterator;

class BladeSpinBehavior extends Behavior {
    private final TransformGroup target;
    private final Transform3D rot = new Transform3D();
    private long last = -1L;
    private final float omega; // 角速度 rad/s
    private float angle = 0f;
    private boolean powerOn = true;

    public BladeSpinBehavior(TransformGroup target, float omega) {
        this.target = target;
        this.omega = omega;
    }

    @Override
    public void initialize() {
        wakeupOn(new WakeupOnElapsedFrames(0));
    }

    @Override
    public void processStimulus(Iterator<WakeupCriterion> it) {
        long now = System.nanoTime();
        if (last > 0 && powerOn) {
            float dt = (now - last) / 1_000_000_000.0f;
            angle += omega * dt;
            rot.setIdentity();
            rot.rotZ(angle);     // 围绕自身 Z 轴旋转
            target.setTransform(rot);
        }
        last = now;
        wakeupOn(new WakeupOnElapsedFrames(0));
    }

    public void setPower(boolean on) { this.powerOn = on; }
    public boolean isPowerOn() { return powerOn; }
}

class HeadYawBehavior extends Behavior {
    private enum State { MOVING, HOLD }

    private final TransformGroup target;
    private final Vector3f basePos;   // 摇头枢轴基准平移（构造时外部传入）

    // 摆头参数
    private final float amplitude = (float) Math.toRadians(40); // 左右各 40°
    private final long halfPeriodMs;  // 一次从一端到另一端的时间（不含停顿）
    private final long rampMs;        // 要求 2500ms
    private final long holdMs;        // 端点停顿

    // 计时与状态
    private long last = -1L;
    private State state = State.MOVING;
    private float p = 0f;             // 当前半程的归一化进度 [0,1]
    private float holdAccum = 0f;     // 端点停留累计（秒）
    private float fromAngle = 0f;     // 本半程起点角
    private float toAngle   = 0f;     // 本半程终点角
    private boolean paused = false, powerOn = true;

    // 复用矩阵
    private final Transform3D T = new Transform3D();
    private final Transform3D R = new Transform3D();
    private final Transform3D M = new Transform3D();

    public HeadYawBehavior(TransformGroup target, Vector3f basePos, long halfPeriodMs, long rampMs, long holdMs) {
        this.target = target;
        this.basePos = new Vector3f(basePos);
        this.halfPeriodMs = halfPeriodMs;
        this.rampMs = rampMs;
        this.holdMs = holdMs;
    }

    @Override
    public void initialize() {
        // 初始从中心 0 朝 +A 运动（余弦平滑），到达 +A 后停顿，再返回 -A
        fromAngle = 0f;
        toAngle   = +amplitude;
        applyAngle(0f); // 先把姿态设到中心
        wakeupOn(new WakeupOnElapsedFrames(0));
    }

    @Override
    public void processStimulus(Iterator<WakeupCriterion> it) {
        long now = System.nanoTime();
        if (last > 0 && powerOn && !paused) {
            float dt = (now - last) / 1_000_000_000.0f; // s

            switch (state) {
                case MOVING -> {
                    float halfSec = halfPeriodMs / 1000f;
                    p += dt / halfSec;          // 归一化 0→1
                    if (p >= 1f) {              // 抵达终点（toAngle）
                        p = 1f;
                        applyAngle(1f);
                        state = State.HOLD;
                        holdAccum = 0f;
                        break;
                    }
                    applyAngle(p);
                }
                case HOLD -> {
                    holdAccum += dt;
                    if (holdAccum * 1000f >= holdMs) {
                        // 端点停顿结束，反向：新的半程从当前端点到对侧端点
                        fromAngle = toAngle;
                        toAngle   = -toAngle;
                        p = 0f;
                        state = State.MOVING;
                    }
                }
            }
        }
        last = now;
        wakeupOn(new WakeupOnElapsedFrames(0));
    }

    /** 余弦平滑插值 + 基准平移： angle = lerp(from,to, s(p))，s(p)=0.5-0.5*cos(pi p) */
    private void applyAngle(float progress01) {
        float s = 0.5f - 0.5f * (float) Math.cos(Math.PI * progress01); // 平滑 0→1
        float angle = fromAngle + (toAngle - fromAngle) * s;

        T.set(basePos);        // 平移到枢轴位置
        R.setIdentity();
        R.rotY(angle);         // 本帧的摆头角
        M.mul(T, R);           // M = T * R
        target.setTransform(M);
    }

    // 控制接口
    public void togglePause() { paused = !paused; }
    public void setPause(boolean p) { paused = p; }
    public boolean isPaused() { return paused; }
    public void setPower(boolean on) { powerOn = on; }
    public boolean isPowerOn() { return powerOn; }
}

class FanKeyBehavior extends Behavior {
    private final BladeSpinBehavior spin;
    private final HeadYawBehavior yaw;

    public FanKeyBehavior(BladeSpinBehavior spin, HeadYawBehavior yaw) {
        this.spin = spin;
        this.yaw = yaw;
    }

    @Override
    public void initialize() {
        wakeupOn(new WakeupOnAWTEvent(java.awt.event.KeyEvent.KEY_PRESSED));
    }

    @Override
    public void processStimulus(Iterator<WakeupCriterion> iterator) {
        while (iterator.hasNext()) {
            WakeupCriterion wc = iterator.next();
            if (wc instanceof WakeupOnAWTEvent w) {
                java.awt.AWTEvent[] evs = w.getAWTEvent();
                for (java.awt.AWTEvent e : evs) {
                    if (e instanceof java.awt.event.KeyEvent ke) {
                        int code = ke.getKeyCode();
                        if (code == java.awt.event.KeyEvent.VK_Z) {
                            yaw.togglePause();
                        } else if (code == java.awt.event.KeyEvent.VK_X) {
                            boolean newPower = !(yaw.isPowerOn() && spin.isPowerOn());
                            yaw.setPower(newPower);
                            spin.setPower(newPower);
                        }
                    }
                }
            }
        }
        wakeupOn(new WakeupOnAWTEvent(java.awt.event.KeyEvent.KEY_PRESSED));
    }
}
