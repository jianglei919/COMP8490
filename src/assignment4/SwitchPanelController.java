package assignment4;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.Shape3D;

public class SwitchPanelController {
    public static final String LEFT_NAME = "LeftSwitch";
    public static final String RIGHT_NAME = "RightSwitch";

    private final Shape3D[] leftFaces;
    private final Shape3D[] rightFaces;
    private final BladeSpinBehavior spin;
    private final HeadYawBehavior yaw;
    private final SoundUtilityJOAL sound;

    private final Appearance RED_APP = CommonsLJ.obj_Appearance(CommonsLJ.Red);
    private final Appearance GREEN_APP = CommonsLJ.obj_Appearance(CommonsLJ.Green);

    // 左=暂停键；右=电源键（红=ON，绿=OFF）
    private boolean leftOn = true;
    private boolean rightOn = true;

    public SwitchPanelController(Shape3D[] leftFaces, Shape3D[] rightFaces,
                                 BladeSpinBehavior spin, HeadYawBehavior yaw,
                                 SoundUtilityJOAL sound) {
        this.leftFaces = leftFaces;
        this.rightFaces = rightFaces;
        this.spin = spin;
        this.yaw = yaw;
        this.sound = sound;

        // 初始上色
        setFacesAppearance(leftFaces, RED_APP);
        setFacesAppearance(rightFaces, RED_APP);
    }

    private void setFacesAppearance(Shape3D[] faces, Appearance app) {
        if (faces == null) return;
        for (Shape3D s : faces) if (s != null) s.setAppearance(app);
    }

    public void toggleLeft() {
        setLeftOn(!leftOn);
        applyAll();
    }

    public void toggleRight() {
        setRightOn(!rightOn);
        applyAll();
    }

    public void setLeftOn(boolean on) {
        this.leftOn = on;
        setFacesAppearance(leftFaces, on ? RED_APP : GREEN_APP);
    }

    public void setRightOn(boolean on) {
        this.rightOn = on;
        setFacesAppearance(rightFaces, on ? RED_APP : GREEN_APP);
    }

    public boolean isLeftOn() {
        return leftOn;
    }

    public boolean isRightOn() {
        return rightOn;
    }

    public void applyAll() {
        if (!rightOn) {
            if (spin != null) spin.setPower(false);
            if (yaw != null) yaw.setPower(false);
            if (sound != null) {
                sound.stop("cow");
                try {
                    sound.stop("ocean");
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
            }
            return;
        }
        if (spin != null) spin.setPower(true);
        if (sound != null) {
            sound.play("cow");
            try {
                sound.play("ocean");
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        }
        if (yaw != null) {
            yaw.setPower(true);
            // 左键：红=ON=暂停；绿=OFF=继续摆头
            yaw.setPause(leftOn);
        }
    }
}