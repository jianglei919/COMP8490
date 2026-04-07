/* Copyright material by xyuan@uwindsor.ca,
 * for students working on assignments and projects */

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.ImageComponent2D;
import org.jogamp.java3d.Link;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.SharedGroup;
import org.jogamp.java3d.TexCoordGeneration;
import org.jogamp.java3d.Texture2D;
import org.jogamp.java3d.TextureAttributes;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.TriangleStripArray;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

public class L5TextureSurfaceXY extends GroupObjects {

	/* a function to scale and position the linked item(s) at a particular location */
	public static TransformGroup link_OneDisk(Vector3f pos, Link link, Vector3d scl) {
		Transform3D trfm = new Transform3D();
		trfm.rotX(Math.PI);
		trfm.setTranslation(pos);                          // specify the translation
		trfm.setScale(scl);                                // specify the scaling
		TransformGroup posTG = new TransformGroup(trfm);   // define the transformation
		posTG.addChild(link);                              // position the linked item(s)
		return posTG;                                      // return the TransformGroup
	}

	/* a function to create a table by sharing five of the textured disks with scaling and positioning */
	public static TransformGroup round_Table(int n) {      // define scaling factors
		Vector3d[] scl = {new Vector3d(0.25, 0.25, 2), new Vector3d(1, 1, 1)};    
		Vector3f[] post = {new Vector3f(1.4f, 0, -0.2f), new Vector3f(0, 0, 0.1f)};
                                                           // define shifting distances
		String[] side_name = {"Top", "Side"};
		SharedGroup shared3D = new SharedGroup( );	
		for (int i = 0; i < 2; i++) 		               // share the disk's two sides
			shared3D.addChild(L5TextureSurfaceXY.ring_Shape(side_name[i], n)); 
		shared3D.compile();                                // optimize the group to be shared

		TransformGroup linkTG = new TransformGroup();
		Link[] links = new Link[2];
		for (int i = 0; i < 2; i++) {
			links[i] = new Link(shared3D);                 // link two disks for table's top and leg 
			linkTG.addChild(L5TextureSurfaceXY.link_OneDisk(post[i], links[i], scl[i]));
		}                                                  // position each linked disk with scaling

		return linkTG;                                     // place all shared groups in 'rotateTG'
	}
	
	/* a function to a surface of the disk with 'n' sides and with texture mapping */
	private static TriangleStripArray ring_Side(String shape_key, int n) {
		float r = 2.0f;
		int k;
		
		int v_num = (n + 1) * 2;                           // use 'n+1' points on the circle
		int vn_count[] = {v_num};                          // set point counters for this surface
		Point3f[] v_cdnts = new Point3f[v_num];            // allocate 3D coordinates for points of this surface
		Vector3f[] c_nmls = new Vector3f[v_num];           // declare normals at each point of this surface
		Vector3f nml;
		double nt;                                         // declare variables for the calculation of normal		
		float x0, y0;
		                                                   // prepare points on the circle
		Point3f c_pts[] = L2StarXY.circle_Points(0, r, n);                
		Point3f ctr_pt = new Point3f(0f, 0f, 0.1f);
		Point3f p1, p2;

		for (int i = 0; i <= n; i++) {
			k = (i < n) ? i : 0;                           // NOTE: set the last two points as the first two points
		
			if (shape_key == "Top") {                      // set for top (flat, circular) surface
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
				TriangleStripArray.COORDINATES | TriangleStripArray.TEXTURE_COORDINATE_3 |
				TriangleStripArray.NORMALS, vn_count);
		object_geometry.setStripVertexCounts(vn_count);    // create the object as a TriangleStripArray
		object_geometry.setCoordinates(0, v_cdnts, 0, v_num); 
		object_geometry.setNormals(0, c_nmls, 0, v_num);   // set the geometry's normals 
		
		return object_geometry;
	}
	
	public static Shape3D ring_Shape(String shape_key, int n) {
		Appearance app = set_Appearance(shape_key);        // set appearance with texture mapping		
		return new Shape3D(ring_Side(shape_key, n), app);

	}
	
	/* a function to define the appearance with texture mapping */
	public static Appearance set_Appearance(String s) {
		Appearance app = CommonsXY.set_Appearance(CommonsXY.White);
		PolygonAttributes pa = new PolygonAttributes();
		pa.setCullFace(PolygonAttributes.CULL_NONE);       // show both sides
		app.setPolygonAttributes(pa);

		TexCoordGeneration tcg = new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR,
				TexCoordGeneration.TEXTURE_COORDINATE_2);
		app.setTexCoordGeneration(tcg);
		app.setTexture(L5TextureSurfaceXY.texture_Appearance("Image" + s));
		
		TextureAttributes textureAttrib= new TextureAttributes();
		textureAttrib.setTextureMode(TextureAttributes.REPLACE);
		app.setTextureAttributes(textureAttrib);
	
		float scl = 0.250f;                                  // need to rearrange the four quarters
		Vector3d scale = new Vector3d(scl, scl, scl);
		Transform3D transMap = new Transform3D();
		transMap.setScale(scale);
		textureAttrib.setTextureTransform(transMap);
		
		return app;
	}

	/* a function to define the texture with a specific image */	
	private static Texture2D texture_Appearance(String f_name) {
		String file_name = "images/" + f_name + ".jpg";    // indicate the location of the image
		TextureLoader loader = new TextureLoader(file_name, null);
		ImageComponent2D image = loader.getImage();        // get the image
		if (image == null)
			System.out.println("Cannot load file: " + file_name);

		Texture2D texture = new Texture2D(Texture2D.BASE_LEVEL,
				Texture2D.RGBA, image.getWidth(), image.getHeight());
		texture.setImage(0, image);                        // define the texture with the image

		return texture;
	}
	
	public L5TextureSurfaceXY(String s) {
		super(ring_Shape(s, 60));
	}
}
