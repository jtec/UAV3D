/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package upside.models;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import upside.util;

/**
 *
 * @author j.bolting
 */
public class Ball extends Aircraft {

    public Ball(AssetManager am, ColorRGBA color, int id) {
        super(am, color, id);
        this.load3D();
    }

    @Override
    public void setGhostLook(boolean wireframe) {
        // super.setGhostLook(wireframe);
    }

    private void load3D() {
        this.detachAllChildren();
        util.attachCoordinateAxes(Vector3f.ZERO, new Vector3f(1, 1, 1), this, am);
        // util.attachWireBox(Vector3f.ZERO, 1, ColorRGBA.Brown, this, am);
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
