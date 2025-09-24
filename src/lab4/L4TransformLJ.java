/* Copyright material by xyuan@uwindsor.ca,
 * for students working on assignments and projects */
package lab4;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.TriangleStripArray;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import common.CommonsLJ;
import common.GroupObjects;
import lab2.L2StarLJ;

public class L4TransformLJ extends GroupObjects {

	private final static int MAX_PTS = 60;
	
	public static Shape3D ring_Side(int shape_key, Color3f clr) {
		float r = 2.0f;
		int k, n = MAX_PTS;
		
		int v_num = (MAX_PTS + 1) * 2;                     // use 'MAX_PTS+1' points on the circle
		int vn_count[] = {v_num};                          // set point counters for the surface
		Point3f[] v_cdnts = new Point3f[v_num];            // allocate 3D coordinates for all surface points
		Vector3f[] c_nmls = new Vector3f[v_num];           // declare normals at each point of this surface
		Vector3f nml;
		double nt;                                         // declare variables for the calculation of normal
		float x0, y0;                             
		                                                   // prepare points on the circle
		Point3f c_pts[] = L2StarLJ.circle_Points(0, r, MAX_PTS);                
		Point3f ctr_pt = new Point3f(0f, 0f, 0.1f);
		Point3f p1, p2;

		for (int i = 0; i <= n; i++) {
			k = (i < n) ? i : 0;                           // NOTE: set the last two points as the first two points
		
			if (shape_key < 1) {                           // set for top (flat, circular) surface
				p1 = new Point3f(c_pts[k].x, c_pts[k].y, 0.1f);
				p2 = ctr_pt;
				nml = new Vector3f(0f, 0f,  1f);
			}
			else {                                         // set for outside (vertical, curve) surface
				p1 = new Point3f(c_pts[k].x, c_pts[k].y, -0.1f);
				p2 = new Point3f(c_pts[k].x, c_pts[k].y, 0.1f);
				x0 = c_pts[k].x;
				y0 = c_pts[k].y;
				nt = Math.sqrt(x0 * x0 + y0 * y0);         // normalize the normals of side (vertical) surface points
				nml = new Vector3f((float) (x0 / nt), (float) (y0 / nt), 0f);
			}
			v_cdnts[i * 2 + 1] = p1;                       // set the coordinate for the point on a surface
			v_cdnts[i * 2] = p2;
			c_nmls[i * 2] = c_nmls[i * 2 + 1] = nml;       //     ... normal ... 
		}
		
		TriangleStripArray object_geometry = new TriangleStripArray(v_num, 
				TriangleStripArray.COORDINATES | 
				TriangleStripArray.NORMALS, vn_count);
		object_geometry.setStripVertexCounts(vn_count);    // create the object as a TriangleStripArray
		object_geometry.setCoordinates(0, v_cdnts, 0, v_num); 
		object_geometry.setNormals(0, c_nmls, 0, v_num);   // set the geometry's normals 
		
		Appearance app = CommonsLJ.set_Appearance(clr);
		PolygonAttributes pa = new PolygonAttributes();
		pa.setCullFace(PolygonAttributes.CULL_NONE);       // show both sides
		app.setPolygonAttributes(pa);


		return new Shape3D(object_geometry, app);
	}
	
	public L4TransformLJ(int key) {
		super(ring_Side(key, CommonsLJ.Green));
	}
}
