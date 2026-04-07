package assignment2;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.geometry.Cylinder;
import org.jogamp.java3d.utils.geometry.Primitive;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;

import java.awt.*;

public abstract class BaseShapes_LJ {
    protected TransformGroup objTG = new TransformGroup();

    protected abstract Node create_Object();

    public TransformGroup position_Object() {
        return objTG;
    }

    protected Appearance app;

    public void add_Child(TransformGroup nextTG) {
        objTG.addChild(nextTG);
    }

    /* Helper: face appearance with transparency */
    protected static Appearance makeTransparentFace(Color3f c, float alpha) {
        Appearance ap = CommonsLJ.obj_Appearance(c);
        TransparencyAttributes ta = new TransparencyAttributes(TransparencyAttributes.BLENDED, alpha);
        ap.setTransparencyAttributes(ta);
        return ap;
    }

    // 在 BaseShapes_LJ 类内新增：生成带纹理的 Appearance（若你已有类似工具方法可复用）
    protected static Appearance texturedAppearance(String imagePath) {
        Appearance ap = CommonsLJ.obj_Appearance(CommonsLJ.White); // 基础材质先给白色
        try {
            // 加载纹理
            TextureLoader loader = new TextureLoader(imagePath, null);
            Texture tex = loader.getTexture();
            if (tex != null) {
                tex.setEnable(true);
                ap.setTexture(tex);
                // 纹理属性：用“替换/混合/调制”都可以；这里用“调制”以保留光照
                TextureAttributes ta = new TextureAttributes();
                ta.setTextureMode(TextureAttributes.MODULATE);
                ap.setTextureAttributes(ta);
            }
        } catch (Exception e) {
            System.out.println("[Warn] Texture load failed: " + imagePath + " -> " + e.getMessage());
        }
        return ap;
    }

}

/* ===== Base square ===== */
class SquareShape extends BaseShapes_LJ {
    public SquareShape() {
        Transform3D translator = new Transform3D();
        translator.setTranslation(new Vector3d(0.0, -0.54, 0));
        objTG = new TransformGroup(translator);
        objTG.addChild(create_Object());
    }

    protected Node create_Object() {
        // Build a Box with the same size and assign different colors to each face; half transparent.
        float alpha = 0.5f;
        Box box = new Box(0.5f, 0.04f, 0.5f,
                Primitive.GENERATE_NORMALS | Primitive.GENERATE_TEXTURE_COORDS, CommonsLJ.obj_Appearance(CommonsLJ.White));

        //设置颜色
        box.getShape(Box.TOP).setAppearance(makeTransparentFace(CommonsLJ.Magenta, alpha));
        box.getShape(Box.BOTTOM).setAppearance(makeTransparentFace(CommonsLJ.Cyan, alpha));
        box.getShape(Box.LEFT).setAppearance(makeTransparentFace(CommonsLJ.Green, alpha));
        box.getShape(Box.RIGHT).setAppearance(makeTransparentFace(CommonsLJ.Orange, alpha));
        box.getShape(Box.FRONT).setAppearance(makeTransparentFace(CommonsLJ.Blue, alpha));
        box.getShape(Box.BACK).setAppearance(makeTransparentFace(CommonsLJ.Yellow, alpha));
        return box;  // stationary (no behavior attached)
    }
}

/* ===== String label ===== */
class ColorString extends BaseShapes_LJ {
    String str;
    Color3f clr;
    double scl;
    Point3f pos;

    public ColorString(String str_ltrs, Color3f str_clr, double s, Point3f p) {
        str = str_ltrs;
        clr = str_clr;
        scl = s;
        pos = p;

        Transform3D scale = new Transform3D();
        scale.setScale(scl);
        Transform3D rotY = new Transform3D();
        rotY.rotY(Math.PI); // face left
        Transform3D trans = new Transform3D();
        // 机舱右侧表面: x=0.30, y=0.65, z=-0.11
        trans.setTranslation(new Vector3d(0.30, 0.65, -0.11));

        // 合并变换: 先缩放再旋转再平移
        rotY.mul(scale);
        trans.mul(rotY);

        objTG = new TransformGroup(trans);
        objTG.addChild(create_Object());
    }

    protected Node create_Object() {
        Font my2DFont = new Font("Arial", Font.PLAIN, 1);
        FontExtrusion myExtrude = new FontExtrusion();
        Font3D font3D = new Font3D(my2DFont, myExtrude);
        Text3D text3D = new Text3D(font3D, str, new Point3f(0, 0, 0));
        Appearance app = CommonsLJ.obj_Appearance(clr);
        return new Shape3D(text3D, app);
    }
}

/* ===== Tower ===== */
class TowerShape extends BaseShapes_LJ {
    private static final float R = 0.12f;
    private static final float H = 1.0f;

    public TowerShape() {
        objTG = new TransformGroup();
        objTG.addChild(create_Object());
    }

    protected Node create_Object() {
        app = CommonsLJ.obj_Appearance(CommonsLJ.Orange);
        return new Cylinder(R, H,
                Primitive.GENERATE_NORMALS, 30, 30, app);
    }
}

/* ===== Nacelle box ===== */
class NacelleShape extends BaseShapes_LJ {
    private static final float SX = 0.52f;
    private static final float SY = 0.12f;
    private static final float SZ = 0.24f;

    public NacelleShape() {
        Transform3D tr = new Transform3D();
        tr.setTranslation(new Vector3d(0.14, 0.68, 0.0));
        objTG = new TransformGroup(tr);
        objTG.addChild(create_Object());
    }

    protected Node create_Object() {
        app = CommonsLJ.obj_Appearance(CommonsLJ.SkyBlue);
        return new Box(SX / 2f, SY / 2f, SZ / 2f,
                Primitive.GENERATE_NORMALS | Primitive.GENERATE_TEXTURE_COORDS, app);
    }
}

class YawShapeA2 extends BaseShapes_LJ {
    private static final float R = 0.12f;
    private static final int PERIOD_MS = 20000; // 20s/圈（逆时针）

    private final String texturePath;

    public YawShapeA2(String texturePath) {
        this.texturePath = texturePath;

        Transform3D tr = new Transform3D();
        tr.setTranslation(new Vector3d(0.0, 0.5, 0.0));
        objTG = new TransformGroup(tr);

        TransformGroup spinTG = new TransformGroup();
        spinTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        spinTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

        Appearance yawApp = texturedAppearance(texturePath);
        Sphere yaw = new Sphere(R,
                Primitive.GENERATE_NORMALS | Primitive.GENERATE_TEXTURE_COORDS,
                40, yawApp);

        spinTG.addChild(yaw);
        objTG.addChild(spinTG);

        // +2π = 逆时针
        Alpha alpha = new Alpha(-1, PERIOD_MS);
        Transform3D yAxis = new Transform3D();
        RotationInterpolator rot = new RotationInterpolator(
                alpha, spinTG, yAxis, 0.0f, (float) (2 * Math.PI));
        rot.setSchedulingBounds(new BoundingSphere());
        spinTG.addChild(rot);
    }

    @Override
    protected Node create_Object() {
        return null;
    }
}

/**
 * 叶轮总成：把 rotor 球 + 叶片放在同一个“旋转容器”中，
 * 通过 RotationInterpolator 让二者一起绕 Z 轴顺时针转（5000ms/圈）。
 */
class RotorAssemblyA2 extends BaseShapes_LJ {
    // 尺寸参数
    private static final float HUB_R = 0.06f;    // rotor 球半径
    private static final float TX = 0.02f;    // 叶片厚度（X 方向半长）
    private static final float TY = 0.12f;    // 叶片高度（Y 方向半长）
    private static final float TZ = 0.90f;    // 叶片长度（Z 方向半长）
    private static final int PERIOD_MS = 5000; // 5s 每圈（顺时针）

    private final String texturePath;

    public RotorAssemblyA2(String texturePath) {
        this.texturePath = texturePath;

        // 1) 把总成原点放到 rotor 球心（0.40, 0.68, 0.0）
        Transform3D center = new Transform3D();
        center.setTranslation(new Vector3d(0.40, 0.68, 0.0));
        objTG = new TransformGroup(center);

        // 2) 旋转容器（允许写），整套“球+叶片”都会跟着它转
        TransformGroup spinTG = new TransformGroup();
        spinTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        spinTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

        // 3) rotor 球（可贴图，便于观察旋转）
        Appearance rotorApp = texturedAppearance(texturePath);
        Sphere rotor = new Sphere(HUB_R,
                Primitive.GENERATE_NORMALS | Primitive.GENERATE_TEXTURE_COORDS,
                40, rotorApp);
        spinTG.addChild(rotor);

        // 4) 叶片（指针式：初始竖直，沿 +Y 指向上方）
        Appearance bladeApp = CommonsLJ.obj_Appearance(CommonsLJ.Magenta);
        Box blade = new Box(TX / 2f, TY / 2f, TZ / 2f,
                Primitive.GENERATE_NORMALS | Primitive.GENERATE_TEXTURE_COORDS, bladeApp);

        // 4.1 把 Box 的长边从 Z 轴转到 +Y 轴：用 -90°（Z -> +Y）
        Transform3D orient = new Transform3D();
        orient.rotX(-Math.PI / 2);                 // ★让长边竖起来（指向 +Y）
        TransformGroup orientTG = new TransformGroup(orient);

        // 4.2 再沿 X 把叶片“贴到球的侧面”
        //     距离 = 球半径 + 叶片厚度的一半（注意不是叶片长度 TZ）
        //     放左侧：负号；要放右侧改成 +(HUB_R + TX/2.0)
        Transform3D offset = new Transform3D();
        offset.setTranslation(new Vector3d((HUB_R + TX / 2.0), 0.0, 0.0));
        TransformGroup offsetTG = new TransformGroup(offset);

        // 4.3 ★层级顺序：先定向(orient) → 再平移(offset) → 再挂几何体
        orientTG.addChild(offsetTG);
        offsetTG.addChild(blade);
        spinTG.addChild(orientTG);

        // 5) 把旋转容器挂到总成
        objTG.addChild(spinTG);

        // 6) 绑定旋转动画：绕 Z 轴顺时针（终角 -2π），5s 一圈
        Alpha alpha = new Alpha(-1, PERIOD_MS);

        // RotationInterpolator 默认绕 Y 轴；
        // 这里把插值器坐标系先绕 X 轴 +90°，使其 Y 轴对齐到目标坐标系的 Z 轴，
        // 从而实现“绕 Z 轴”旋转。
        Transform3D axis = new Transform3D();
        axis.rotZ(Math.PI / 2);   // 让插值器的 Y 轴对齐到目标的 Z 轴

        RotationInterpolator rot = new RotationInterpolator(
                alpha,                // 时间函数（-1=无限循环）
                spinTG,               // 目标 TG（整个总成）
                axis,                 // 旋转轴：等效于目标坐标系的 Z 轴
                0.0f,
                (float) (-2 * Math.PI) // 负角度 = 顺时针
        );
        rot.setSchedulingBounds(new BoundingSphere()); // 激活范围
        spinTG.addChild(rot);
    }

    @Override
    protected Node create_Object() {
        return null;
    }
}
