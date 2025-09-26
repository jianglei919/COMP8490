package lab2;


import org.jogamp.java3d.*;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3f;

public class L2CircleLJ extends Shape3D {
    public L2CircleLJ(float radius, Color3f color) {
        int segments = 60; // 题目要求 60 段
        int vertexCount = segments + 1; // +1 以便首尾闭合

        LineStripArray geom = new LineStripArray(
            vertexCount,
            GeometryArray.COORDINATES,
            new int[] { vertexCount }
        );

        // 顶点坐标（Z=0 的平面圆）
        Point3f[] pts = new Point3f[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            double t = 2.0 * Math.PI * i / segments; // 注意除以 segments（而非 vertexCount）
            float x = (float) (radius * Math.cos(t));
            float y = (float) (radius * Math.sin(t));
            pts[i] = new Point3f(x, y, 0.0f);
        }
        geom.setCoordinates(0, pts);

        setGeometry(geom);

        // 外观：线宽 + 颜色
        Appearance app = new Appearance();

        LineAttributes la = new LineAttributes();
        la.setLineWidth(2.0f);
        la.setLineAntialiasingEnable(true);
        app.setLineAttributes(la);

        if (color != null) {
            ColoringAttributes ca = new ColoringAttributes(color, ColoringAttributes.SHADE_FLAT);
            app.setColoringAttributes(ca);
        } else {
            // 若未指定颜色，默认白色
            ColoringAttributes ca = new ColoringAttributes(new Color3f(1f,1f,1f), ColoringAttributes.SHADE_FLAT);
            app.setColoringAttributes(ca);
        }

        setAppearance(app);
    }
}
