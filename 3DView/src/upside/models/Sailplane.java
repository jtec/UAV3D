/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package upside.models;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.scene.Spatial;

/**
 *
 * @author j.bolting
 */
public class Sailplane extends Aircraft {

    public Sailplane(AssetManager am, ColorRGBA color, int id) {
        super(am, color, id);
        this.load3D();
    }

    @Override
    public void setGhostLook(boolean wireframe) {
        super.setGhostLook(wireframe);
        if(wireframe){
        Spatial model = am.loadModel("Models/SailplanefromBlender.j3o");
        this.attachChild(model);
        // Fix orientation:
        Quaternion q = new Quaternion();
        q = q.fromAngles(FastMath.DEG_TO_RAD * (-90), FastMath.DEG_TO_RAD * 0, FastMath.DEG_TO_RAD * (-90));
        model.setLocalRotation(q);
        Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", color);
        model.setMaterial(mat);
        }else{
            this.load3D();
        }
    }

    private void load3D() {
        this.detachAllChildren();
        Spatial model = am.loadModel("Models/SailplanefromBlender.j3o");
        this.attachChild(model);
        model.setLocalScale(1f);
        // Fix orientation:
        Quaternion q = new Quaternion();
        q = q.fromAngles(FastMath.DEG_TO_RAD * (-90), FastMath.DEG_TO_RAD * 0, FastMath.DEG_TO_RAD * (-90));
        model.setLocalRotation(q);

    }

    @Override
    public void setControlSurfaces(float da, float de, float dr, float den, float df) {
    }
}
