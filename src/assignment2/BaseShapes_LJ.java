package assignment2;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.geometry.Cylinder;
import org.jogamp.java3d.utils.geometry.Primitive;
import org.jogamp.java3d.utils.geometry.Sphere;
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
}

/* ===== Base (white square) ===== */
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

/* ===== Yaw sphere ===== */
class YawShape extends BaseShapes_LJ {
	private static final float R = 0.12f;

	public YawShape() {
		Transform3D tr = new Transform3D();
		tr.setTranslation(new Vector3d(0.0, 0.5, 0.0));
		objTG = new TransformGroup(tr);
		objTG.addChild(create_Object());
	}

	protected Node create_Object() {
		app = CommonsLJ.obj_Appearance(CommonsLJ.Red);
		return new Sphere(R, Primitive.GENERATE_NORMALS, 30, app);
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

/* ===== Rotor hub sphere ===== */
class RotorSphereShape extends BaseShapes_LJ {
	private static final float R = 0.06f;

	public RotorSphereShape() {
		Transform3D tr = new Transform3D();
		tr.setTranslation(new Vector3d(0.40, 0.68, 0.0));
		objTG = new TransformGroup(tr);
		objTG.addChild(create_Object());
	}

	protected Node create_Object() {
		app = CommonsLJ.obj_Appearance(CommonsLJ.Red);
		return new Sphere(R, Primitive.GENERATE_NORMALS, 30, app);
	}
}

/* ===== One blade ===== */
class BladeShape extends BaseShapes_LJ {
	private static final float TX = 0.02f;
	private static final float TY = 0.12f;
	private static final float TZ = 0.90f;
	private static final float HUB_R = 0.06f;

	public BladeShape() {
		double cx = 0.40 + HUB_R + (TX / 2.0);
		Transform3D tr = new Transform3D();
		tr.setTranslation(new Vector3d(cx, 0.68, 0.0));
		objTG = new TransformGroup(tr);
		objTG.addChild(create_Object());
	}

	protected Node create_Object() {
		app = CommonsLJ.obj_Appearance(CommonsLJ.Magenta);
		return new Box(TX / 2f, TY / 2f, TZ / 2f,
				Primitive.GENERATE_NORMALS | Primitive.GENERATE_TEXTURE_COORDS, app);
	}
}

/* ===== 单叶片：以 RotorSphereShape 球心为圆心，像电风扇那样在竖直平面绕 Z 轴顺时针旋转（5s/圈） ===== */
class BladeRotationShape extends BaseShapes_LJ {
	// TODO: 这里需要你来实现BladeShape的旋转功能

	protected Node create_Object() { return null; }
}