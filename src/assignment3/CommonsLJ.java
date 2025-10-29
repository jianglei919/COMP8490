package assignment3;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;

import javax.swing.*;
import java.awt.*;

public class CommonsLJ extends JPanel {
    private static final long serialVersionUID = 1L;
    private static JFrame frame;

    public final static Color3f Red = new Color3f(1f, 0f, 0f);
    public final static Color3f Green = new Color3f(0f, 1f, 0f);
    public final static Color3f Blue = new Color3f(0f, 0f, 1f);
    public final static Color3f Yellow = new Color3f(1f, 1f, 0f);
    public final static Color3f Cyan = new Color3f(0f, 1f, 1f);
    public final static Color3f Orange = new Color3f(1f, 0.5f, 0f);
    public final static Color3f Magenta = new Color3f(1f, 0f, 1f);
    public final static Color3f White = new Color3f(1f, 1f, 1f);
    public final static Color3f Grey = new Color3f(0.35f, 0.35f, 0.35f);
    public final static Color3f Black = new Color3f(0f, 0f, 0f);
    public final static Color3f[] clr_list = {Blue, Green, Red, Yellow, Cyan, Orange, Magenta, Grey};
    public final static int clr_num = 8;
    private static Color3f[] mtl_clrs = {White, Grey, Black};

    public final static BoundingSphere hundredBS = new BoundingSphere(new Point3d(), 100.0);
    public final static BoundingSphere twentyBS = new BoundingSphere(new Point3d(), 20.0);

    public static Appearance obj_Appearance(Color3f m_clr) {
        Material mtl = new Material();
        mtl.setShininess(32);
        mtl.setAmbientColor(mtl_clrs[0]);
        mtl.setDiffuseColor(m_clr);
        mtl.setSpecularColor(mtl_clrs[1]);
        mtl.setEmissiveColor(mtl_clrs[2]);
        mtl.setLightingEnable(true);
        Appearance app = new Appearance();
        app.setMaterial(mtl);
        return app;
    }

    public static RotationInterpolator rotate_Behavior(int r_num, TransformGroup rotTG) {
        rotTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        Transform3D yAxis = new Transform3D();
        Alpha rotationAlpha = new Alpha(-1, r_num);
        RotationInterpolator rot_beh = new RotationInterpolator(
                rotationAlpha, rotTG, yAxis, 0.0f, (float) Math.PI * 2.0f);
        rot_beh.setSchedulingBounds(hundredBS);
        return rot_beh;
    }

    public static BranchGroup add_Lights(Color3f clr, int p_num) {
        BranchGroup lightBG = new BranchGroup();
        Point3f atn = new Point3f(0.5f, 0.0f, 0.0f);
        float adjt = 1f;
        for (int i = 0; (i < p_num) && (i < 2); i++) {
            if (i > 0) adjt = -1f;
            PointLight ptLight = new PointLight(clr, new Point3f(3.0f * adjt, 1.0f, 3.0f * adjt), atn);
            ptLight.setInfluencingBounds(hundredBS);
            lightBG.addChild(ptLight);
        }
        return lightBG;
    }

    public static void define_Viewer(SimpleUniverse su, Point3d eye) {
        TransformGroup viewTransform = su.getViewingPlatform().getViewPlatformTransform();
        Point3d center = new Point3d(0, 0, 0);
        Vector3d up = new Vector3d(0, 1, 0);
        Transform3D view_TM = new Transform3D();
        view_TM.lookAt(eye, center, up);
        view_TM.invert();
        viewTransform.setTransform(view_TM);
    }

    public static KeyNavigatorBehavior key_Navigation(SimpleUniverse simple_U) {
        ViewingPlatform view_platfm = simple_U.getViewingPlatform();
        TransformGroup view_TG = view_platfm.getViewPlatformTransform();
        KeyNavigatorBehavior keyNavBeh = new KeyNavigatorBehavior(view_TG);
        keyNavBeh.setSchedulingBounds(twentyBS);
        return keyNavBeh;
    }

    // --- 以下演示用，不在 A3 中调用 ---
    public static BranchGroup create_Scene() {
        BranchGroup sceneBG = new BranchGroup();
        TransformGroup sceneTG = new TransformGroup();
        sceneTG.addChild(new Box(0.5f, 0.5f, 0.5f, obj_Appearance(Orange)));
        sceneBG.addChild(rotate_Behavior(7500, sceneTG));
        sceneBG.addChild(sceneTG);
        return sceneBG;
    }

    public CommonsLJ(BranchGroup sceneBG) {
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas = new Canvas3D(config);
        SimpleUniverse su = new SimpleUniverse(canvas);
        define_Viewer(su, new Point3d(1.0d, 1.0d, 4.0d));
        sceneBG.addChild(add_Lights(White, 1));
        sceneBG.addChild(key_Navigation(su));
        sceneBG.compile();
        su.addBranchGraph(sceneBG);
        setLayout(new BorderLayout());
        add("Center", canvas);
        frame.setSize(800, 800);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        frame = new JFrame("LJ's Common File");
        frame.getContentPane().add(new CommonsLJ(create_Scene()));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
