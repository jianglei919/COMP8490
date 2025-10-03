package lab5;/* Copyright material by xyuan@uwindsor.ca,
 * for students working on assignments and projects */

import common.CommonsLJ;
import common.GroupObjects;
import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

// ▼ 为实现“四象限对角交换”所需（标准库，非第三方包）
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class L5TextureSurfaceLJ extends GroupObjects {

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

    /* a function to create a table by sharing textured disks with scaling and positioning */
    public static TransformGroup round_Table(int n) {      // define scaling factors
        Vector3d[] scl = {new Vector3d(0.25, 0.25, 2), new Vector3d(1, 1, 1)}; // [0]=leg, [1]=top
        // 原单腿位置（靠近“角”）。我们将以此为基准旋转得到另外三条腿。
        Vector3f legPos = new Vector3f(1.4f, 0, -0.2f);
        Vector3f topPos = new Vector3f(0, 0, 0.1f);

        String[] side_name = {"Top", "Side", "Bottom"};
        SharedGroup shared3D = new SharedGroup();
        for (int i = 0; i < side_name.length; i++)                        // share the disk's two sides (top cap + side wall)
            shared3D.addChild(L5TextureSurfaceLJ.ring_Shape(side_name[i], n));
        shared3D.compile();                                // optimize the group to be shared

        TransformGroup linkTG = new TransformGroup();
        Link topLink = new Link(shared3D);                 // 桌面（顶面+侧壁）链接
        linkTG.addChild(L5TextureSurfaceLJ.link_OneDisk(topPos, topLink, scl[1]));

        // ---- 4 条桌腿（复用相同几何与纹理），保持与角的相对距离一致 ----
        // 使用 0°, 90°, 180°, 270° 旋转 legPos 在 XY 平面放置四条腿
        Vector3f[] legPositions = new Vector3f[4];
        legPositions[0] = new Vector3f(legPos);

        // 旋转 90°： (x, y) -> (-y, x)
        legPositions[1] = new Vector3f(-legPos.y, legPos.x, legPos.z);
        // 旋转 180°： (x, y) -> (-x, -y)
        legPositions[2] = new Vector3f(-legPos.x, -legPos.y, legPos.z);
        // 旋转 270°： (x, y) -> (y, -x)
        legPositions[3] = new Vector3f(legPos.y, -legPos.x, legPos.z);

        for (int i = 0; i < 4; i++) {
            Link legLink = new Link(shared3D);             // 每条腿都是与顶面相同的“圆柱”几何，靠缩放形成细腿
            linkTG.addChild(L5TextureSurfaceLJ.link_OneDisk(legPositions[i], legLink, scl[0]));
        }

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
        Point3f c_pts[] = CommonsLJ.circle_Points(0, r, n);
        Point3f ctr_pt = new Point3f(0f, 0f, 0.1f);
        //bottom
        Point3f ctr_pt_bot = new Point3f(0f, 0f, -0.1f);
        Point3f p1, p2;

        for (int i = 0; i <= n; i++) {
            k = (i < n) ? i : 0;                           // NOTE: set the last two points as the first two points

            // ▼ 关键修复：String 比较必须用 equals，原先使用 "==" 会导致判断失败，从而顶面缺失
            if ("Top".equals(shape_key)) {                 // set for top (flat, circular) surface
                p1 = new Point3f(c_pts[k].x, c_pts[k].y, 0.1f);
                p2 = ctr_pt;
                nml = new Vector3f(0f, 0f, 1f);
            } else if ("Side".equals(shape_key)) {         // 侧壁 set for outside (vertical, curve) surface
                p1 = new Point3f(c_pts[k].x, c_pts[k].y, -0.1f);
                p2 = new Point3f(c_pts[k].x, c_pts[k].y, 0.1f);
                x0 = c_pts[k].x;
                y0 = c_pts[k].y;
                nt = Math.sqrt(x0 * x0 + y0 * y0);         // normalize the normals of side (vertical) surface points
                nml = new Vector3f((float) (x0 / nt), (float) (y0 / nt), 0f);
            } else { //底面 Bottom
                p1 = new Point3f(c_pts[k].x, c_pts[k].y, -0.1f);
                p2 = ctr_pt_bot;
                nml = new Vector3f(0f, 0f, -1f);
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
        Appearance app = CommonsLJ.set_Appearance(CommonsLJ.White);
        PolygonAttributes pa = new PolygonAttributes();
        pa.setCullFace(PolygonAttributes.CULL_NONE);       // show both sides
        app.setPolygonAttributes(pa);

        TexCoordGeneration tcg = new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR,
                TexCoordGeneration.TEXTURE_COORDINATE_2);
        app.setTexCoordGeneration(tcg);
        app.setTexture(L5TextureSurfaceLJ.texture_Appearance("Image" + s));

        TextureAttributes textureAttrib = new TextureAttributes();
        textureAttrib.setTextureMode(TextureAttributes.REPLACE);
        app.setTextureAttributes(textureAttrib);

        float scl = 0.250f;                                // 维持原始缩放；题目要求对角交换已在贴图里完成
        Vector3d scale = new Vector3d(scl, scl, scl);
        Transform3D transMap = new Transform3D();
        transMap.setScale(scale);
        textureAttrib.setTextureTransform(transMap);

        return app;
    }

    /* a function to define the texture with a specific image
       — 在此实现“四象限对角交换”（TL↔BR、TR↔BL），并返回交换后的纹理。 */
    private static Texture2D texture_Appearance(String f_name) {
        String file_name = "images/" + f_name + ".jpg";    // indicate the location of the image
        TextureLoader loader = new TextureLoader(file_name, null);
        ImageComponent2D image = loader.getImage();        // get the image
        if (image == null) {
            System.out.println("Cannot load file: " + file_name);
            return null;
        }

        // 将 ImageComponent2D 转为 BufferedImage 以便处理四象限交换
        BufferedImage src = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = src.createGraphics();
        g.drawImage(image.getImage(), 0, 0, null);
        g.dispose();

        // —— 四象限对角交换 ——（按等分 1/2 的方式切块）
        int w = src.getWidth();
        int h = src.getHeight();
        int w2 = w / 2;
        int h2 = h / 2;

        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gd = dst.createGraphics();
        // TL(0,0,w2,h2)  ↔ BR(w2,h2,w2,h2)
        gd.drawImage(src.getSubimage(w2, h2, w - w2, h - h2), 0, 0, w2, h2, null);          // BR -> TL
        gd.drawImage(src.getSubimage(0, 0, w2, h2), w2, h2, w - w2, h - h2, null);          // TL -> BR
        // TR(w2,0,w2,h2) ↔ BL(0,h2,w2,h2)
        gd.drawImage(src.getSubimage(0, h2, w2, h - h2), w2, 0, w - w2, h2, null);          // BL -> TR
        gd.drawImage(src.getSubimage(w2, 0, w - w2, h2), 0, h2, w2, h - h2, null);          // TR -> BL
        gd.dispose();

        // 使用交换后的图生成纹理
        ImageComponent2D swapped = new ImageComponent2D(ImageComponent.FORMAT_RGBA, dst);
        Texture2D texture = new Texture2D(Texture2D.BASE_LEVEL,
                Texture2D.RGBA, swapped.getWidth(), swapped.getHeight());
        texture.setImage(0, swapped);                      // define the texture with the swapped image

        return texture;
    }

    public L5TextureSurfaceLJ(String s) {
        super(ring_Shape(s, 60));
    }
}