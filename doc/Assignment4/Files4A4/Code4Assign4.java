/* *********************************************************
 * For use by students to work on assignments and project.
 * Permission required material. Contact: xyuan@uwindsor.ca 
 **********************************************************/

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.geometry.Primitive;
import org.jogamp.java3d.utils.picking.PickResult;
import org.jogamp.java3d.utils.picking.PickTool;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.*;

public class Code4Assign4 extends JPanel implements MouseListener {
	private static final long serialVersionUID = 1L;
	private static JFrame frame;

	private Canvas3D canvas;
	private static PickTool pickTool;

	/* a function to set user-defined values for a rotating box and attach it to 'scene' */
	private static TransformGroup one_Box(float x, float y, float z) {
		Appearance app = CommonsXY.obj_Appearance(CommonsXY.Green);
		Box box = new Box(x, y, z,                         // create an appearance-modifiable box
				Primitive.GENERATE_NORMALS | Primitive.ENABLE_APPEARANCE_MODIFY, app);
		box.setUserData(0);                                // 'UserData' retrievable at picking
//		box.setName("box");                                // NOTE: 'Name' is also retrievable
		
		TransformGroup sceneTG = new TransformGroup();
		sceneTG.addChild(box);                             // add 'box' to the 'sceneTG'
		return sceneTG;
	}

	/* a function to create and return the scene BranchGroup */
	public static BranchGroup create_Scene() {
		BranchGroup sceneBG = new BranchGroup();
		
		TransformGroup sceneTG = one_Box(0.5f, 0.5f, 0.5f);  
		sceneBG.addChild(CommonsXY.rotate_Behavior(5000, sceneTG)); 
		sceneBG.addChild(sceneTG);                         // make 'sceneTG' continuously rotating
		pickTool = new PickTool( sceneBG );                // allow picking of objects in 'sceneBG'
		pickTool.setMode(PickTool.BOUNDS);                 // pick by bounding volume
		return sceneBG;
	}

	/* a constructor to set up and run the application */
	public Code4Assign4(BranchGroup sceneBG) {
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		canvas = new Canvas3D(config);                        
		canvas.addMouseListener(this);                     // NOTE: enable mouse clicking 

		SimpleUniverse su = new SimpleUniverse(canvas);    // create a SimpleUniverse
		CommonsXY.define_Viewer(su, new Point3d(1.0d, 1.0d, 4.0d));  // set the viewer's location
		
		sceneBG.addChild(CommonsXY.add_Lights(CommonsXY.White, 1));
		sceneBG.addChild(CommonsXY.key_Navigation(su));     // allow key navigation
		sceneBG.compile();
		su.addBranchGraph(sceneBG);                        // attach the scene to SimpleUniverse
		
		setLayout(new BorderLayout());
		add("Center", canvas);		
		frame.setSize(800, 800);                           // set the size of the JFrame
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		frame = new JFrame("Code for Assignment 4");
		frame.getContentPane().add(new Code4Assign4(create_Scene())); 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}	

	@Override
	public void mouseClicked(MouseEvent event) {
		int x = event.getX(); int y = event.getY();        // mouse coordinates
		Point3d point3d = new Point3d(), center = new Point3d();
		canvas.getPixelLocationInImagePlate(x, y, point3d);// obtain AWT pixel in ImagePlate coordinates
		canvas.getCenterEyeInImagePlate(center);           // obtain eye's position in IP coordinates
		
		Transform3D transform3D = new Transform3D();       // matrix to relate ImagePlate coordinates~
		canvas.getImagePlateToVworld(transform3D);         // to Virtual World coordinates
		transform3D.transform(point3d);                    // transform 'point3d' with 'transform3D'
		transform3D.transform(center);                     // transform 'center' with 'transform3D'

		Vector3d mouseVec = new Vector3d();
		mouseVec.sub(point3d, center);
		mouseVec.normalize();
		pickTool.setShapeRay(point3d, mouseVec);           // send a PickRay for intersection

		if (pickTool.pickClosest() != null) {
			PickResult pickResult = pickTool.pickClosest();// obtain the closest hit
			Box box = (Box)pickResult.getNode(PickResult.PRIMITIVE);
			Appearance app = new Appearance();             // originally a PRIMITIVE as a box
			if ((int) box.getUserData() == 0) {            // retrieve 'UserData'
				app = CommonsXY.obj_Appearance(CommonsXY.Green);
				box.setUserData(1);                        // set 'UserData' to a new value
			}
			else {                                         // use 'UserData' as flag to switch color
				app = CommonsXY.obj_Appearance(CommonsXY.Red);
				box.setUserData(0);                        // reset 'UserData'
			}
			box.setAppearance(app);                        // change box's appearance
		} 
	}

	public void mouseEntered(MouseEvent arg0) { }
	public void mouseExited(MouseEvent arg0) { }
	public void mousePressed(MouseEvent e) { }
	public void mouseReleased(MouseEvent e) { }
}
