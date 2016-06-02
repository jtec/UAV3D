/*
 * Small helper methods and stuff.
 */
package upside;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.debug.WireBox;

/**
 *
 * @author j.bolting
 */
public class util {    
        public static void attachWireBox(Vector3f pos, float size, ColorRGBA color, Node n, AssetManager assetGuy) {
        Geometry g = new Geometry("wireframe cube", new WireBox(size, size, size));
        Material mat = new Material(assetGuy, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", color);
        g.setMaterial(mat);
        g.setLocalTranslation(pos);
        n.attachChild(g);
    }

    public static Node attachCoordinateAxes(Vector3f pos, Vector3f length, Node n, AssetManager assetGuy) {
        Node nod = new Node();
        Arrow arrow = new Arrow(Vector3f.UNIT_X.mult(length.x));
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Red, nod, assetGuy).setLocalTranslation(pos);

        arrow = new Arrow(Vector3f.UNIT_Y.mult(length.y));
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Green, nod, assetGuy).setLocalTranslation(pos);

        arrow = new Arrow(Vector3f.UNIT_Z.mult(length.z));
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Blue, nod, assetGuy).setLocalTranslation(pos);
        n.attachChild(nod);
        return nod;
    }

    private static Geometry putShape(Mesh shape, ColorRGBA color, Node n, AssetManager assetGuy) {
        Geometry g = new Geometry("camera coordinate axis", shape);
        Material mat = new Material(assetGuy, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", color);
        g.setMaterial(mat);
        n.attachChild(g);
        return g;
    }
    
    public static boolean isEven(int n){
        return (n % 2) == 0;
    }
}
