package assignment3;

import org.jogamp.java3d.*;
import org.jogamp.java3d.loaders.IncorrectFormatException;
import org.jogamp.java3d.loaders.ParsingErrorException;
import org.jogamp.java3d.loaders.Scene;
import org.jogamp.java3d.loaders.objectfile.ObjectFile;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.geometry.Cylinder;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.vecmath.*;

import java.io.FileNotFoundException;

public abstract class A3ObjectsLJ {
    protected BranchGroup objBG;
    protected TransformGroup objTG;
    protected TransformGroup objRG;
    protected double scale = 1.0;
    protected Vector3f post = new Vector3f();
    protected Appearance app = new Appearance();

    public abstract TransformGroup position_Object();

    public abstract void add_Child(TransformGroup nextTG);

    /* ---------- 工具：递归上色 / 打印名称 / 名称筛选 ---------- */

    static void applyAppearance(Node node, Appearance app) {
        if (node instanceof Shape3D s) {
            s.setAppearance(app);
        } else if (node instanceof Group g) {
            for (int i = 0; i < g.numChildren(); i++) applyAppearance(g.getChild(i), app);
        }
    }

    static void debugListShapes(Node node) {
        if (node instanceof Shape3D s) {
            System.out.println("shape: " + s.getName());
        } else if (node instanceof Group g) {
            for (int i = 0; i < g.numChildren(); i++) debugListShapes(g.getChild(i));
        }
    }

    static BranchGroup extractByName(Node node, String... includes) {
        BranchGroup out = new BranchGroup();
        out.setCapability(BranchGroup.ALLOW_DETACH);
        if (node instanceof Shape3D s) {
            String name = (s.getName() == null ? "" : s.getName().toLowerCase());
            boolean keep = false;
            for (String inc : includes)
                if (name.contains(inc)) {
                    keep = true;
                    break;
                }
            if (keep) out.addChild(s.cloneTree(true));
        } else if (node instanceof Group g) {
            for (int i = 0; i < g.numChildren(); i++) {
                BranchGroup child = extractByName(g.getChild(i), includes);
                if (child.numChildren() > 0) out.addChild(child);
            }
        }
        return out;
    }

    /* ---------- 剥离线段/点几何（去掉轨道/环/线框） ---------- */
    static BranchGroup stripLines(Node node) {
        BranchGroup out = new BranchGroup();
        out.setCapability(BranchGroup.ALLOW_DETACH);
        if (node instanceof Shape3D s) {
            Geometry g = s.getGeometry();
            boolean isLine = (g instanceof LineArray) || (g instanceof IndexedLineArray)
                    || (g instanceof LineStripArray) || (g instanceof IndexedLineStripArray);
            boolean isPoint = (g instanceof PointArray) || (g instanceof IndexedPointArray);
            if (!isLine && !isPoint) out.addChild(s.cloneTree(true));
        } else if (node instanceof Group g) {
            for (int i = 0; i < g.numChildren(); i++) {
                BranchGroup child = stripLines(g.getChild(i));
                if (child.numChildren() > 0) out.addChild(child);
            }
        }
        return out;
    }

    /* ---------- 基于 Bounds 的“薄片”启发式（用于从整机里挑出叶片） ---------- */

    static void enableAutoBounds(Node n) {
        n.setBoundsAutoCompute(true);
        n.setCapability(Node.ALLOW_BOUNDS_READ);
        if (n instanceof Group g) for (int i = 0; i < g.numChildren(); i++) enableAutoBounds(g.getChild(i));
    }

    static double[] dims(Bounds b) { // 返回 {dx,dy,dz}
        if (!(b instanceof BoundingBox bb)) return new double[]{1, 1, 1};
        Point3d lo = new Point3d(), up = new Point3d();
        bb.getLower(lo);
        bb.getUpper(up);
        return new double[]{up.x - lo.x, up.y - lo.y, up.z - lo.z};
    }

    /**
     * 仅保留“薄而扁”的面片几何：min/max <= thinRatio 且 max >= minSpan
     */
    static BranchGroup keepThinPlates(Node node, double thinRatio, double minSpan) {
        BranchGroup out = new BranchGroup();
        out.setCapability(BranchGroup.ALLOW_DETACH);
        if (node instanceof Shape3D s) {
            enableAutoBounds(s);
            double[] d = dims(s.getBounds());
            double max = Math.max(d[0], Math.max(d[1], d[2]));
            double min = Math.min(d[0], Math.min(d[1], d[2]));
            boolean keep = (max >= minSpan) && (min / max <= thinRatio);
            if (keep) out.addChild(s.cloneTree(true));
        } else if (node instanceof Group g) {
            for (int i = 0; i < g.numChildren(); i++) {
                BranchGroup child = keepThinPlates(g.getChild(i), thinRatio, minSpan);
                if (child.numChildren() > 0) out.addChild(child);
            }
        }
        return out;
    }

    /* ---------- OBJ 加载 + 外观 ---------- */

    private Scene loadShape(String obj_name) {
        ObjectFile f = new ObjectFile(ObjectFile.RESIZE, (float) (60 * Math.PI / 180.0));
        Scene s = null;
        try {
            s = f.load("images/" + obj_name + ".obj");
        } catch (FileNotFoundException | ParsingErrorException | IncorrectFormatException e) {
            System.err.println(e);
            System.exit(1);
        }
        return s;
    }

    protected void transform_Object(String obj_name) {
        Transform3D tm = new Transform3D();
        tm.setScale(scale);
        tm.setTranslation(post);
        objTG = new TransformGroup(tm);

        objBG = loadShape(obj_name).getSceneGroup();
    }

    private int shine = 32;
    protected Color3f[] mtl_clr = {
            new Color3f(1.0f, 1.0f, 1.0f),
            new Color3f(0.7725f, 0.6549f, 0.0f),
            new Color3f(0.175f, 0.175f, 0.175f),
            new Color3f(0.0f, 0.0f, 0.0f)};

    protected void obj_Appearance() {
        Material mtl = new Material();
        mtl.setShininess(shine);
        mtl.setAmbientColor(mtl_clr[0]);
        mtl.setDiffuseColor(mtl_clr[1]);
        mtl.setSpecularColor(mtl_clr[2]);
        mtl.setEmissiveColor(mtl_clr[3]);
        mtl.setLightingEnable(true);

        app.setMaterial(mtl);
        applyAppearance(objBG, app);
    }
}

/* ===== Base ===== */
class BaseShapeA extends A3ObjectsLJ {
    public BaseShapeA() {
        Transform3D translator = new Transform3D();
        translator.setTranslation(new Vector3d(0.0, -0.54, 0));
        objTG = new TransformGroup(translator);
        objTG.addChild(create_Object());
    }

    protected Node create_Object() {
        app = CommonsLJ.obj_Appearance(CommonsLJ.White);
        app.setTexture(textured_App("MarbleTexture"));
        TransparencyAttributes ta =
                new TransparencyAttributes(TransparencyAttributes.SCREEN_DOOR, 0.5f);
        app.setTransparencyAttributes(ta);
        return new Box(0.5f, 0.04f, 0.5f,
                Box.GENERATE_NORMALS | Box.GENERATE_TEXTURE_COORDS, app);
    }

    private static Texture textured_App(String name) {
        String filename = "images/" + name + ".jpg";
        TextureLoader loader = new TextureLoader(filename, null);
        ImageComponent2D image = loader.getImage();
        if (image == null) System.out.println("Cannot load file: " + filename);

        Texture2D texture = new Texture2D(Texture.BASE_LEVEL,
                Texture.RGBA, image.getWidth(), image.getHeight());
        texture.setImage(0, image);
        return texture;
    }

    public TransformGroup position_Object() {
        return objTG;
    }

    public void add_Child(TransformGroup nextTG) {
        objTG.addChild(nextTG);
    }
}

/* ===== 文字 ===== */
class ColorString extends A3ObjectsLJ {
    String str;
    Color3f clr;
    float scl;
    Vector3f pos;

    public ColorString(String txt, Color3f c, float s, Vector3f p) {
        str = txt;
        clr = c;
        scl = s;
        pos = p;
        Transform3D tm = new Transform3D();
        tm.setScale(scl);
        tm.setTranslation(pos);
        objTG = new TransformGroup(tm);
        objTG.addChild(create_Object());
    }

    protected Node create_Object() {
        java.awt.Font my2DFont = new java.awt.Font("Arial", java.awt.Font.PLAIN, 1);
        FontExtrusion myExtrude = new FontExtrusion();
        Font3D font3D = new Font3D(my2DFont, myExtrude);
        Text3D text3D = new Text3D(font3D, str, new Point3f(0, 0, 0));
        Appearance a = CommonsLJ.obj_Appearance(new Color3f(1, 0.6f, 0.7f));
        return new Shape3D(text3D, a);
    }

    public TransformGroup position_Object() {
        return objTG;
    }

    public void add_Child(TransformGroup nextTG) {
        objTG.addChild(nextTG);
    }
}

/**
 * FanStand.obj
 * 支撑整个风扇的底座支架
 */
class StandObjectA extends A3ObjectsLJ {
    public StandObjectA() {
        scale = 1.0;
        post = new Vector3f(0, 0, 0);
        transform_Object("FanStand");
        mtl_clr[1] = new Color3f(0.58f, 0.69f, 0.11f);
        obj_Appearance();
    }

    public TransformGroup position_Object() {
        Transform3D ry = new Transform3D();
        ry.rotY(Math.PI);
        objRG = new TransformGroup(ry);
        objTG.addChild(objRG);
        objRG.addChild(objBG);
        return objTG;
    }

    public void add_Child(TransformGroup nextTG) {
        objRG.addChild(nextTG);
    }
}

/**
 * FanSwitch.obj
 * 电源开关
 */
class SwitchObjectA extends A3ObjectsLJ {
    public SwitchObjectA() {
        scale = 0.3;
        post = new Vector3f(0.02f, -0.77f, -0.8f);
        transform_Object("FanSwitch");
        obj_Appearance();
    }

    public TransformGroup position_Object() {
        objTG.addChild(objBG);
        return objTG;
    }

    public void add_Child(TransformGroup nextTG) {
        objTG.addChild(nextTG);
    }
}

/* ===== Shaft（圆柱立柱） ===== */
class ShaftObjectLJ extends A3ObjectsLJ {
    private final double shaftScale;
    private final Vector3f offset;

    public ShaftObjectLJ(double scale, Vector3f offsetFromPivot) {
        this.shaftScale = scale;
        this.offset = offsetFromPivot;
        objTG = new TransformGroup();
        Transform3D t = new Transform3D();
        t.setScale(shaftScale);
        t.setTranslation(offset);
        objTG.setTransform(t);

        Appearance a = CommonsLJ.obj_Appearance(new Color3f(0.65f, 0.65f, 0.65f));
        Cylinder cyl = new Cylinder(0.08f, 4.0f, Cylinder.GENERATE_NORMALS, a);
        objBG = new BranchGroup();
        objBG.addChild(cyl);
    }

    public static Node createHub(float r, float len, Color3f clr) {
        Appearance a = CommonsLJ.obj_Appearance(clr);
        Transform3D rx = new Transform3D();
        rx.rotX(Math.PI / 2);
        TransformGroup rot = new TransformGroup(rx);
        Cylinder hub = new Cylinder(r, len, Cylinder.GENERATE_NORMALS, a);
        rot.addChild(hub);
        return rot;
    }

    public TransformGroup position_Object() {
        objTG.addChild(objBG);
        return objTG;
    }

    public void add_Child(TransformGroup nextTG) {
        objTG.addChild(nextTG);
    }
}

/**
 * FanMotor.obj
 * 电机外壳
 */
class MotorObjectLJ extends A3ObjectsLJ {
    public MotorObjectLJ(double scaleFactor) {
        scale = scaleFactor;
//        post = new Vector3f(0,1.29f,-0.6f);
        post = new Vector3f(0, 0f, 0f);
        transform_Object("FanMotor");
        mtl_clr[1] = new Color3f(0.58f, 0.69f, 0.11f);
        obj_Appearance();
    }

    public TransformGroup position_Object() {
        objTG.addChild(objBG);
        return objTG;
    }

    public void add_Child(TransformGroup nextTG) {
        objTG.addChild(nextTG);
    }
}

/**
 * FanGuard.obj
 * 外部防护罩（圆形网格）
 */
class GuardObjectLJ extends A3ObjectsLJ {
    public GuardObjectLJ(double scaleFactor) {
        scale = scaleFactor;
//        post = new Vector3f(0,1.29f,-1.4f);
        post = new Vector3f(0, 0f, 0f);
        transform_Object("FanGuard");
        mtl_clr[1] = new Color3f(0.98f, 0.85f, 0.10f);
        obj_Appearance();
        PolygonAttributes pa = new PolygonAttributes();
        pa.setPolygonMode(PolygonAttributes.POLYGON_LINE);
        pa.setCullFace(PolygonAttributes.CULL_NONE);
        app.setPolygonAttributes(pa);
        LineAttributes la = new LineAttributes();
        la.setLineWidth(1.2f);
        app.setLineAttributes(la);
        applyAppearance(objBG, app);
    }

    public TransformGroup position_Object() {
        objTG.addChild(objBG);
        return objTG;
    }

    public void add_Child(TransformGroup nextTG) {
        objTG.addChild(nextTG);
    }
}

/**
 * FanBlade.obj
 * 风扇叶片（通常有 3~4 片）
 */
class BladeObjectLJ extends A3ObjectsLJ {
    public BladeObjectLJ(double scaleFactor) {
        scale = scaleFactor;
        post = new Vector3f(0, 0f, 0f);
        transform_Object("FanBlade");

        // 1) 尝试用名字
        BranchGroup bladesByName = A3ObjectsLJ.extractByName(objBG, "blade", "blades", "leaf", "prop");
        Node use = (bladesByName.numChildren() > 0)
                ? bladesByName
                : A3ObjectsLJ.keepThinPlates(A3ObjectsLJ.stripLines(objBG), 0.12 /*薄*/, 0.35 /*够长*/);

        // 重建 objTG
        Transform3D sc = new Transform3D();
        sc.setScale(scale);
        objTG = new TransformGroup(sc);
        BranchGroup holder = new BranchGroup();
        holder.addChild(use);
        objTG.addChild(holder);

        // 叶片材质
        app = CommonsLJ.obj_Appearance(new Color3f(0.98f, 0.85f, 0.10f));
        A3ObjectsLJ.applyAppearance(use, app);
    }

    public TransformGroup position_Object() {
        return objTG;
    }

    public void add_Child(TransformGroup nextTG) {
        objTG.addChild(nextTG);
    }
}
