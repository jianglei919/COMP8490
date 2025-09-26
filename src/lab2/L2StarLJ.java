/* Copyright material for students working on assignments and projects */
package lab2;

import common.CommonsLJ;
import common.GroupObjects;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.LineArray;
import org.jogamp.java3d.Shape3D;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3f;

public class L2StarLJ extends GroupObjects {

    /* a function to create and return the geometry of a star shape */
    private static LineArray one_Star(int num, Color3f clr, float r) {

        LineArray lineArr = new LineArray(num * 2,         // give two points for each line
                GeometryArray.COLOR_3 | GeometryArray.COORDINATES);

        Point3f c_pts[] = CommonsLJ.circle_Points(0.0f, r, num);     // prepare points on the circle

        for (int i = 0; i < num; i++) {                   // define lines for the start
            lineArr.setCoordinate(i * 2, c_pts[i]);
            lineArr.setCoordinate(i * 2 + 1, c_pts[(i + 2) % num]);
            lineArr.setColor(i * 2, clr);
            lineArr.setColor(i * 2 + 1, CommonsLJ.Green);  // set color for end points
        }

        return lineArr;
    }

    /* a function to create and return a Shape3D with one LineArray */
    public static Shape3D line_Shape(float r) {
        int n = 5;                                         // create an n-sided start with 'r' radius
        LineArray starGeometry = one_Star(n, CommonsLJ.Red, r);

        return new Shape3D(starGeometry);
    }

    public L2StarLJ(float r) {
        super(line_Shape(r));
    }
}
