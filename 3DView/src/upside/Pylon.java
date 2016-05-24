package upside;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import com.jme3.texture.Texture;

/**
 *
 * @author j.bolting
 */
public class Pylon extends Node{
    AssetManager am;
    
    public Pylon(float height, float radius, AssetManager am){
        super();
        this.am = am;
        float l = height/4;
        float r = radius;
        // Stack cylinders:
        Geometry c1 = buildCylinder(l, r, ColorRGBA.Red);
        Geometry c2 = buildCylinder(l, r, ColorRGBA.White);
        Geometry c3 = buildCylinder(l, r, ColorRGBA.Red);
        Geometry c4 = buildCylinder(l, r, ColorRGBA.White);
        
        c1.move(0, 0, -0.5f*l);
        c2.move(0, 0, -1.5f*l);
        c3.move(0, 0, -2.5f*l);
        c4.move(0, 0, -3.5f*l);
        
        this.attachChild(c1);
        this.attachChild(c2);
        this.attachChild(c3);
        this.attachChild(c4);
    }
    
    private Geometry buildCylinder(float l, float r, ColorRGBA c){
        Cylinder t = new Cylinder(10, 6, r, l, true);
        Geometry geom = new Geometry("Cylinder", t);
        Material matRed = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        matRed.setColor("Color", c);
        matRed.getAdditionalRenderState().setWireframe(true);
        geom.setMaterial(matRed);
        return geom;
    }
}
