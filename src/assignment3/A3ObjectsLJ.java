package assignment3;/* Copyright material for the convenience of students working on assignments */

import org.jogamp.java3d.*;
import org.jogamp.java3d.loaders.IncorrectFormatException;
import org.jogamp.java3d.loaders.ParsingErrorException;
import org.jogamp.java3d.loaders.Scene;
import org.jogamp.java3d.loaders.objectfile.ObjectFile;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

import java.awt.*;
import java.io.FileNotFoundException;

public abstract class A3ObjectsLJ {
    private Alpha rotationAlpha;                           // NOTE: keep for future use
    protected BranchGroup objBG;                           // load external object to 'objBG'
    protected TransformGroup objTG;                        // use 'objTG' to position an object
    protected TransformGroup objRG;                        // use 'objRG' to rotate an object
    protected double scale;                                // use 'scale' to define scaling
    protected Vector3f post;                               // use 'post' to specify location
    protected Shape3D obj_shape;

    public abstract TransformGroup position_Object();      // need to be defined in derived classes

    public abstract void add_Child(TransformGroup nextTG);

    public Alpha get_Alpha() {
        return rotationAlpha;
    }

    ;    // NOTE: keep for future use

    /* a function to load and return object shape from the file named 'obj_name' */
    private Scene loadShape(String obj_name) {
        ObjectFile f = new ObjectFile(ObjectFile.RESIZE, (float) (60 * Math.PI / 180.0));
        Scene s = null;
        try {                                              // load object's definition file to 's'
            s = f.load("images/" + obj_name + ".obj");
        } catch (FileNotFoundException e) {
            System.err.println(e);
            System.exit(1);
        } catch (ParsingErrorException e) {
            System.err.println(e);
            System.exit(1);
        } catch (IncorrectFormatException e) {
            System.err.println(e);
            System.exit(1);
        }
        return s;                                          // return the object shape in 's'
    }

    /* function to set 'objTG' and attach object after loading the model from external file */
    protected void transform_Object(String obj_name) {
        Transform3D scaler = new Transform3D();
        scaler.setScale(scale);                            // set scale for the 4x4 matrix
        scaler.setTranslation(post);                       // set translations for the 4x4 matrix
        objTG = new TransformGroup(scaler);                // set the translation BG with the 4x4 matrix
        objBG = loadShape(obj_name).getSceneGroup();       // load external object to 'objBG'
        obj_shape = (Shape3D) objBG.getChild(0);           // get and cast the object to 'obj_shape'
        obj_shape.setName(obj_name);                       // use the name to identify the object
    }

    protected Appearance app = new Appearance();
    private int shine = 32;                                // specify common values for object's appearance
    protected Color3f[] mtl_clr = {new Color3f(1.000000f, 1.000000f, 1.000000f),
            new Color3f(0.772500f, 0.654900f, 0.000000f),
            new Color3f(0.175000f, 0.175000f, 0.175000f),
            new Color3f(0.000000f, 0.000000f, 0.000000f)};

    /* a function to define object's material and use it to set object's appearance */
    protected void obj_Appearance() {
        Material mtl = new Material();                     // define material's attributes
        mtl.setShininess(shine);
        mtl.setAmbientColor(mtl_clr[0]);                   // use them to define different materials
        mtl.setDiffuseColor(mtl_clr[1]);
        mtl.setSpecularColor(mtl_clr[2]);
        mtl.setEmissiveColor(mtl_clr[3]);                  // use it to enlighten a button
        mtl.setLightingEnable(true);

        app.setMaterial(mtl);                              // set appearance's material
        obj_shape.setAppearance(app);                      // set object's appearance
    }
}

class StandObjectA extends A3ObjectsLJ {
    public StandObjectA() {
        scale = 1d;                                        // use to scale up/down original size
        post = new Vector3f(0f, 0f, 0f);                   // use to move object for positioning
        transform_Object("FanStand");                      // set transformation to 'objTG' and load object file
        mtl_clr[1] = new Color3f(0.58f, 0.69f, 0.11f);     // set "FanStand" to a different color than the common
        obj_Appearance();                                  // set appearance after converting object node to Shape3D
    }

    public TransformGroup position_Object() {              // attach object BranchGroup "FanStand" to 'objTG'
        Transform3D r_axis = new Transform3D();            // default: rotate around Y-axis
        r_axis.rotY(Math.PI);                              // rotate around y-axis for 180 degrees
        objRG = new TransformGroup(r_axis);                // allow "FanBlades" to rotate
        objTG.addChild(objRG);                             // position "FanStand" by attaching 'objRG' to 'objTG'
        objRG.addChild(objBG);                             // rotate "FanStand" by attaching 'objBG' to 'objRG'
        return objTG;
    }

    public void add_Child(TransformGroup nextTG) {
        objRG.addChild(nextTG);                            // attach the next transformGroup to 'objTG'
    }
}

class SwitchObjectA extends A3ObjectsLJ {
    public SwitchObjectA() {
        scale = 0.3d;                                      // actual scale is 0.3 = 1.0 x 0.3
        post = new Vector3f(0.02f, -0.77f, -0.8f);         // location to connect "FanSwitch" with "FanStand"
        transform_Object("FanSwitch");                     // set transformation to 'objTG' and load object file
        obj_Appearance();                                  // set appearance after converting object node to Shape3D
    }

    public TransformGroup position_Object() {
        objTG.addChild(objBG);                             // attach "FanSwitch" to 'objTG'
        return objTG;                                      // use 'objTG' to attach "FanSwitch" to the previous TG
    }

    public void add_Child(TransformGroup nextTG) {
        objTG.addChild(nextTG);                            // attach the next transformGroup to 'objTG'
    }
}

class BaseShapeA extends A3ObjectsLJ {
    public BaseShapeA() {
        Transform3D translator = new Transform3D();
        translator.setTranslation(new Vector3d(0.0, -0.54, 0));
        objTG = new TransformGroup(translator);            // down half of the tower and base's heights

        objTG.addChild(create_Object());                   // attach the object to 'objTG'
    }

    protected Node create_Object() {
        app = CommonsLJ.obj_Appearance(CommonsLJ.White);   // set the appearance for the base
        app.setTexture(textured_App("MarbleTexture"));     // set texture for the base
        TransparencyAttributes ta =                        // value: FASTEST NICEST SCREEN_DOOR BLENDED NONE
                new TransparencyAttributes(TransparencyAttributes.SCREEN_DOOR, 0.5f);
        app.setTransparencyAttributes(ta);                 // set transparency for the base
        return new Box(0.5f, 0.04f, 0.5f, Box.GENERATE_NORMALS | Box.GENERATE_TEXTURE_COORDS, app);
    }

    private static Texture textured_App(String name) {
        String filename = "images/" + name + ".jpg";       // tell the folder of the image
        TextureLoader loader = new TextureLoader(filename, null);
        ImageComponent2D image = loader.getImage();        // load the image
        if (image == null)
            System.out.println("Cannot load file: " + filename);

        Texture2D texture = new Texture2D(Texture.BASE_LEVEL,
                Texture.RGBA, image.getWidth(), image.getHeight());
        texture.setImage(0, image);                        // set image for the texture

        return texture;
    }

    public TransformGroup position_Object() {
        objTG.addChild(objBG);                             // attach "BaseShapeA" to 'objTG'
        return objTG;                                      // use 'objTG' to attach "BaseShapeA" to the previous TG
    }

    public void add_Child(TransformGroup nextTG) {
        objTG.addChild(nextTG);                            // attach the next transformGroup to 'objTG'
    }
}

class ColorString extends A3ObjectsLJ {
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
        Appearance app = assignment2.CommonsLJ.obj_Appearance(clr);
        return new Shape3D(text3D, app);
    }

    @Override
    public TransformGroup position_Object() {
        objTG.addChild(objBG);
        return objTG;
    }

    @Override
    public void add_Child(TransformGroup nextTG) {
        objTG.addChild(nextTG);
    }
}

