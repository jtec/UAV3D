package upside.models;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;
import upside.ACNode;

/**
 *
 * @author j.bolting
 */
public class Vortex extends Aircraft {

    private Node bbox;
    private Geometry cube2Geo;
    float lasttransparency;
    Geometry[] vortexSegments = new Geometry[10];
    int iVortexSegment = 0;
    Vector3f lastposition_world;

    public Vortex(AssetManager am, ColorRGBA color, int id, ACNode staticNode) {
        super(am, color, id, staticNode);
        this.load3D();
        this.lastposition_world = this.getWorldTranslation();
    }

    @Override
    public void setGhostLook(boolean wireframe) {
    }

    private void load3D() {
        this.detachAllChildren();
        for (int i = 0; i < vortexSegments.length; i++) {
            vortexSegments[i] = buildCylinder(1, 1, ColorRGBA.Red);
            vortexSegments[i].rotate(0f, (float) Math.PI / 2, 0f);
            this.acNode.staticNode.attachChild(vortexSegments[i]);
        }
        // util.attachCoordinateAxes(Vector3f.ZERO, new Vector3f(1.3f, 1.3f, 1.3f), this, am);
    }

    private Geometry buildCylinder(float l, float r, ColorRGBA c) {
        Cylinder t = new Cylinder(10, 30, r, l, false);
        Geometry geom = new Geometry("Cylinder", t);

        Material matRed = new Material(am,
                "Common/MatDefs/Misc/Unshaded.j3md");
        //cube2Mat.setTexture("ColorMap", am.loadTexture("Textures/ColoredTex/Monkey.png"));
        matRed.setColor("Color", new ColorRGBA(1, 0, 0, 0.8f)); // 0.5f is the alpha value
        matRed.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        geom.setQueueBucket(Bucket.Transparent);
        matRed.getAdditionalRenderState().setWireframe(false);
        matRed.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        geom.setMaterial(matRed);
        return geom;
    }

    @Override
    public void setControlSurfaces(float da, float de, float dr, float den, float df) {
        this.iVortexSegment++;
        if (this.iVortexSegment >= vortexSegments.length - 1) {
            this.iVortexSegment = 0;
        }
        // Set length and radius:
        // displacement since last update:
        Vector3f deltaP = this.getWorldTranslation().subtract(this.lastposition_world);
        this.lastposition_world = this.getWorldTranslation().clone();
        float l = deltaP.length();
        float r = de;
        // Shift vortex center into the wing:
        Vector3f dp = new Vector3f(-l / 2, 0f, -0.15f);
        dp = this.getWorldRotation().mult(dp);

        for (int i = 0; i < vortexSegments.length; i++) {
            vortexSegments[i].setLocalScale(r, r, l);
            // Adjust position:
            // this.attachChild(vortexSegments[i]);
            // vortexSegments[i].setLocalTranslation(dp[0]);
        }
        // Set transparency:
        float transparency = 1 - den;
        if (Math.abs(lasttransparency - transparency) > 0.01) {
            for (int i = 0; i < vortexSegments.length; i++) {
                Material m = vortexSegments[i].getMaterial();
                m.setColor("Color", new ColorRGBA(1, 1, 1, transparency));
                vortexSegments[i].setMaterial(m);
                lasttransparency = transparency;
            }
        }
        // Drop vortex segment at current position in NED frame to mark vortex trail:
        Vector3f pos_NED_before = vortexSegments[this.iVortexSegment].getWorldTranslation();
        // this.acNode.staticNode.attachChild(vortexSegments[this.iVortexSegment]);
        vortexSegments[this.iVortexSegment].setLocalTranslation(this.getWorldTranslation().add(dp));
        Quaternion rot = new Quaternion();
        rot = rot.fromAngles(0f, (float) Math.PI / 2, 0f);
        vortexSegments[this.iVortexSegment].setLocalRotation(this.getWorldRotation().mult(rot));
        //System.out.println(l);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        for (int i = 0; i < vortexSegments.length; i++) {
            vortexSegments[i].removeFromParent();
        }
    }
}
