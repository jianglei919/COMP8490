/* *********************************************************
 * For use by students to work on assignments and project.
 * Permission required material. Contact: xyuan@uwindsor.ca 
 **********************************************************/

import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JFrame;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Matrix4d;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;

public class SoundPointJOAL extends JFrame implements KeyListener {
	private static final long serialVersionUID = 1L;
	private javax.swing.JPanel drawingPanel;
	private SimpleUniverse univ = null;
	private SoundUtilityJOAL soundJOAL;
	private TransformGroup objectTG;
	private String snd_pt = "cow";
	private String snd_bk = "ocean";

	private Canvas3D createUniverse() {
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		Canvas3D c = new Canvas3D(config);
		univ = new SimpleUniverse(c);

	    TransformGroup viewTransform = univ.getViewingPlatform().getViewPlatformTransform();
	    Point3d eye = new Point3d(0.35, 0.35, 2.0);
		Point3d center = new Point3d(0, 0, 0);             // define where the eye looks at
		Vector3d up = new Vector3d(0, 1, 0);               // define camera's up direction
		Transform3D view_TM = new Transform3D();
		view_TM.lookAt(eye, center, up);
		view_TM.invert();
	    viewTransform.setTransform(view_TM);               // set TG of ViewingPlatform
		univ.getViewer().getView().setMinimumFrameCycleTime(5);
		drawingPanel.add(c, java.awt.BorderLayout.CENTER);
		return c;
	}

	private void initComponents() {
		drawingPanel = new javax.swing.JPanel();
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("SoundPointJOAL");
		drawingPanel.setLayout(new java.awt.BorderLayout());
		drawingPanel.setPreferredSize(new java.awt.Dimension(1024,1024));
		getContentPane().add(drawingPanel, java.awt.BorderLayout.CENTER);
		pack();
	}

	private Point3f objLocation(TransformGroup objTG) {
		Transform3D tmp = new Transform3D();
		objTG.getTransform(tmp);
		Matrix4d mat = new Matrix4d();
		tmp.get(mat);

		Point3f pos = new Point3f();
		pos.x = (float) mat.m03;
		pos.y = (float) mat.m13;
		pos.z = (float) mat.m23;
		return pos;
	}

	public BranchGroup createSceneGraph() {
		BranchGroup contentBG = new BranchGroup();

		Transform3D trans_cube = new Transform3D();
		objectTG = new TransformGroup(trans_cube);
		objectTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		objectTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		objectTG.addChild(ShapeIndexedQuad.indexedQuadShape(0.5f));

		Point3f snd = objLocation(objectTG);              // retrieve the object location
		if (!soundJOAL.load(snd_pt, snd.x, snd.y, snd.z, true))
			System.out.println("Could not load " + snd_pt);
		else
			soundJOAL.play(snd_pt);                        // start 'snd_pt' at object location

		contentBG.addChild(objectTG);
		return contentBG;
	}

	public SoundPointJOAL() {
		initComponents(); Canvas3D c = createUniverse();
		c.addKeyListener(this);                            // NOTE: enable key events

		soundJOAL = new SoundUtilityJOAL();
		if (!soundJOAL.load(snd_bk, 0f, 0f, 10f, true))     // set 'snd_bk' at fixed position
			System.out.println("Could not load " + snd_bk);
		else
			soundJOAL.play(snd_bk);                         // start 'snd_bk' in background

		BranchGroup scene = createSceneGraph();            // create content and add universe
		scene.addChild(LightFog.createBkground(Commons.Grey, Commons.boundH));
		scene.addChild(Commons.threeAxes(Commons.Blue, 0.35f));
		scene.compile();                                   // perform optimizations on graph
		univ.addBranchGraph(scene);
	}

	@Override
	public void keyPressed(KeyEvent evt) {
		Transform3D tmp = new Transform3D();
		Matrix4d mat = new Matrix4d();
		objectTG.getTransform(tmp);
		tmp.get(mat);                                      // matrix of object location

		int key_code = evt.getKeyCode();
		switch (key_code ) {
		case KeyEvent.VK_UP:
			mat.m23 = mat.m23 - 0.1f;                      // go in direction of -z axis
			tmp.set(mat);
			break;
		case KeyEvent.VK_DOWN:
			mat.m23 = mat.m23 + 0.1f;                      // go in direction of z axis
			tmp.set(mat);
			break;
		case KeyEvent.VK_LEFT:
			mat.m03 = mat.m03 - 0.1f;                      // go in direction of -x axis
			tmp.set(mat);
			break;
		case KeyEvent.VK_RIGHT:
			mat.m03 = mat.m03 + 0.1f;                      // go in direction of x axis
			tmp.set(mat);
			break;
		case KeyEvent.VK_R:
			Matrix4d m4d = new Matrix4d();                 // define an identity matrix
			m4d.rotY(0.1); mat.mul(m4d);                   // define and perform rotation
			tmp.set(mat);
			break;
		case KeyEvent.VK_O:
			tmp = new Transform3D();                       // original matrix as L.65
			break;
		default:
		}
		objectTG.setTransform(tmp);                        // set back to the working TG

		if ((key_code == KeyEvent.VK_LEFT) || (key_code == KeyEvent.VK_RIGHT) ||
				(key_code == KeyEvent.VK_O) || (key_code == KeyEvent.VK_UP)
				|| (key_code == KeyEvent.VK_DOWN)) {
			Point3f snd = objLocation(objectTG);           // retrieve object location
			soundJOAL.setPos(snd_pt, snd.x, snd.y, snd.z);  // update 'snd_pt' location
		}
	}

	@Override
	public void keyReleased(KeyEvent evt) {}
	@Override
	public void keyTyped(KeyEvent evt) {}

	public static void main(String[] args) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new SoundPointJOAL().setVisible(true);
			}
		});
	}
}
