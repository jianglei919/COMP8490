/* Copyright material for students working on assignments and projects */
package lab3;

import common.CommonsLJ;
import common.GroupObjects;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.TriangleStripArray;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3f;

public class L3DiskLJ extends GroupObjects {

    private final static int MAX_PTS = 60;

    public static Shape3D ring_Side(float r) {
        int k, n = MAX_PTS;

        // 原来是 2 个 strip（顶面 + 侧壁）——> 改为 3 个 strip（顶面 + 侧壁 + 底面）
        int s_num = 3;                                     // make three surfaces: top, side, bottom
        int v_num = (MAX_PTS + 1) * 2;                     // use 'MAX_PTS+1' points on two circles for each strip
        int vn_count[] = {v_num, v_num, v_num};          // set point counters for the three strips

        Point3f[] v_cdnts = new Point3f[v_num * s_num];    // allocate 3D coordinates for all surface points
        Color3f[] v_clrs = new Color3f[v_num * s_num];    //         ... colors ...

        // prepare points on the two circles (z = -0.1 and z = +0.1)
        Point3f c_pts1[] = CommonsLJ.circle_Points(-0.1f, r, MAX_PTS); // lower circle (z = -0.1)
        Point3f c_pts2[] = CommonsLJ.circle_Points(0.1f, r, MAX_PTS); // upper circle (z =  +0.1)

        Point3f ctr_pt = new Point3f(0f, 0f, -0.1f);      // top(flat) surface center used in original code (z = -0.1)
        Point3f ctr_pt2 = new Point3f(0f, 0f, 0.1f);      // NEW: bottom(flat) surface center (z = +0.1)

        for (int i = 0; i <= n; i++) {
            k = (i < n) ? i : 0;                           // NOTE: set the last two points as the first two points

            // ===== strip 0: top (flat, circular) surface (existing) =====
            v_cdnts[i * 2 + 1] = ctr_pt;
            v_cdnts[i * 2] = c_pts1[k];
            v_clrs[i * 2] = v_clrs[i * 2 + 1] = CommonsLJ.Orange;

            // ===== strip 1: outside (vertical, curve) surface (existing) =====
            v_cdnts[v_num + i * 2] = c_pts2[k];
            v_cdnts[v_num + i * 2 + 1] = c_pts1[k];
            v_clrs[v_num + i * 2] = v_clrs[v_num + i * 2 + 1] = CommonsLJ.Green;

            // ===== strip 2: bottom (flat, circular) surface (NEW) =====
            // 若启用背面剔除，需要让底面与顶面环绕方向相反，避免被当作背面剔除
            int kb = (n - (i % n)) % n;                    // reverse winding on the upper circle
            int base = 2 * v_num;                          // 第三个 strip 的起始偏移
            v_cdnts[base + i * 2 + 1] = ctr_pt2;           // bottom surface center (z = +0.1)
            v_cdnts[base + i * 2] = c_pts2[kb];        // bottom surface rim point (use reversed index)
            v_clrs[base + i * 2] = v_clrs[base + i * 2 + 1] = CommonsLJ.Magenta; // 可改成任意喜欢的颜色
        }

        // set geometry with the three surfaces
        TriangleStripArray object_geometry = new TriangleStripArray(
                v_num * s_num,
                TriangleStripArray.COORDINATES | TriangleStripArray.COLOR_3,
                vn_count
        );
        object_geometry.setStripVertexCounts(vn_count);    // create the object as a TriangleStripArray
        object_geometry.setCoordinates(0, v_cdnts, 0, v_num * s_num);
        object_geometry.setColors(0, v_clrs, 0, v_num * s_num);

        return new Shape3D(object_geometry);               // return Shape3D defined by its geometry
    }

    public L3DiskLJ(float r) {
        super(ring_Side(r));
    }
}