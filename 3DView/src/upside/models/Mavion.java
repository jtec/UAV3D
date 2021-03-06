package upside.models;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import upside.ACNode;
import upside.util;

/**
 *
 * @author j.bolting
 */
public class Mavion extends Aircraft {

    public Mavion(AssetManager am, ColorRGBA color, int id, ACNode staticNode) {
        super(am, color, id, staticNode);
        this.load3D();
    }

    @Override
    public void setGhostLook(boolean wireframe) {
        super.setGhostLook(wireframe);
        if (wireframe) {
            Spatial model = am.loadModel("Models/mavion.j3o");
            this.attachChild(model);
            model.setLocalScale(0.4f);
            // Fix orientation:
            Quaternion q = new Quaternion();
            q = q.fromAngles(FastMath.DEG_TO_RAD * (90), FastMath.DEG_TO_RAD * 0, FastMath.DEG_TO_RAD * (0));
            model.setLocalRotation(q);
            Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.getAdditionalRenderState().setWireframe(true);
            mat.setColor("Color", color);
            model.setMaterial(mat);
            labelNode = this.attachTextLabel("Facundo" + " #" + this.id);
        } else {
            this.load3D();
        }
    }

    private void load3D() {
        this.detachAllChildren();
        Spatial model = am.loadModel("Models/mavion.j3o");
        this.attachChild(model);
        model.setLocalScale(0.02f);
        // Fix orientation:
        Quaternion q = new Quaternion();
        q = q.fromAngles(FastMath.DEG_TO_RAD * (90), FastMath.DEG_TO_RAD * 0, FastMath.DEG_TO_RAD * (0));
        model.setLocalRotation(q);
        // Add Propeller
        propeller = am.loadModel("Models/propeller1.j3o");
        propeller.setLocalScale(1.5f);
        this.attachChild(propeller);
        // Shift it into the right place:
        propeller.move(+0.50f, 0, -0.05f);
        labelNode = this.attachTextLabel("Facundo" + " #" + this.id);
        util.attachCoordinateAxes(Vector3f.ZERO, new Vector3f(1f, 1f, 1f), this, am);
        util.attachWireBox(Vector3f.ZERO, 0.42f, color, this, am);
    }

    @Override
    public void setControlSurfaces(float da, float de, float dr, float den, float df) {
        Quaternion ail = new Quaternion();
        // Festfahrung Propeller:
        float rps = den * 1000 / 60;
        // One update about once every 20ms:
        float rotationPerFrame = 0.02f * rps * 2f * (float) Math.PI;
        ail = ail.fromAngleAxis(rotationPerFrame, Vector3f.UNIT_X);
        propeller.setLocalRotation(propeller.getLocalRotation().mult(ail));
    }
}
