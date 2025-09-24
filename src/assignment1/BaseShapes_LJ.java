package assignment1;/* Copyright material for the convenience of students working on assignments */

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;

import java.awt.*;

public abstract class BaseShapes_LJ {
	protected TransformGroup objTG = new TransformGroup(); // use 'objTG' to position an object

	protected abstract Node create_Object();               // allow derived classes to create different objects
	
	public TransformGroup position_Object() {	       // retrieve 'objTG' to which 'obj_shape' is attached
		return objTG;   
	}
	
	protected Appearance app;                              // allow each object to define its own appearance
	public void add_Child(TransformGroup nextTG) {
		objTG.addChild(nextTG);                        // attach the next transformGroup to 'objTG'
	}
}

class SquareShape extends BaseShapes_LJ {
	public SquareShape() {
		Transform3D translator = new Transform3D();
		translator.setTranslation(new Vector3d(0.0, -0.54, 0));
		objTG = new TransformGroup(translator);            // down half of the tower and base's heights

		objTG.addChild(create_Object());                   // attach the object to 'objTG'
	}
	
	protected Node create_Object() {
		app = CommonsLJ.obj_Appearance(CommonsLJ.White);   // set the appearance for the base
		return new Box(0.5f, 0.04f, 0.5f, Box.GENERATE_NORMALS | Box.GENERATE_TEXTURE_COORDS, app);
	}
}

/* a derived class to create a string label and place it to the bottom of the self-made cone */
class ColorString extends BaseShapes_LJ {
	String str;
	Color3f clr;
	double scl;
	Point3f pos;                                           // make the label adjustable with parameters
	public ColorString(String str_ltrs, Color3f str_clr, double s, Point3f p) {
		str = str_ltrs;	
		clr = str_clr;
		scl = s;
		pos = p;

		Transform3D scaler = new Transform3D();
		scaler.setScale(scl);                              // scaling 4x4 matrix 
		Transform3D rotator = new Transform3D();           // 4x4 matrix for rotation
		rotator.rotY(Math.PI);
		Transform3D trfm = new Transform3D();              // 4x4 matrix for composition
		trfm.mul(rotator);                                 // apply rotation second
		trfm.mul(scaler);                                  // apply scaling first
		objTG = new TransformGroup(trfm);                  // set the combined transformation
		objTG.addChild(create_Object());                   // attach the object to 'objTG'		
	}
	protected Node create_Object() {
		Font my2DFont = new Font("Arial", Font.PLAIN, 1);  // font's name, style, size
		FontExtrusion myExtrude = new FontExtrusion();
		Font3D font3D = new Font3D(my2DFont, myExtrude);	
		Text3D text3D = new Text3D(font3D, str, pos);      // create 'text3D' for 'str' at position of 'pos'
		
		Appearance app = CommonsLJ.obj_Appearance(clr);    // use appearance to specify the string color
		return new Shape3D(text3D, app);                   // return a string label with the appearance
	}
}


