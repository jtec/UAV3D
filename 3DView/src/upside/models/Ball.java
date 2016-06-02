/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package upside.models;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import upside.ACNode;
import upside.util;

/**
 *
 * @author j.bolting
 */
public class Ball extends Aircraft {

    public Ball(AssetManager am, ColorRGBA color, int id, ACNode staticNode) {
        super(am, color, id, staticNode);
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
        Sphere s = new Sphere(16, 16, 0.2f);
        Geometry geom = new Geometry("Box", s);

        Material mat = new Material(am,
                "Common/MatDefs/Misc/Unshaded.j3md");
        //cube2Mat.setTexture("ColorMap", am.loadTexture("Textures/ColoredTex/Monkey.png"));
        mat.setColor("Color", new ColorRGBA(1, 0, 0, 0.5f)); // 0.5f is the alpha value
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        mat.getAdditionalRenderState().setWireframe(false);
        
        geom.setMaterial(mat);
        this.attachChild(geom);
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
