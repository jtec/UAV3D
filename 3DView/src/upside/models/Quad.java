/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package upside.models;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

/**
 *
 * @author j.bolting
 */
public class Quad extends Aircraft {

    public Quad(AssetManager am, ColorRGBA color, int id) {
        super(am, color, id);
        this.load3D();
    }

    @Override
    public void setGhostLook(boolean wireframe) {
        super.setGhostLook(wireframe);
        if (wireframe) {
            Spatial model = am.loadModel("Models/quad.j3o");
            this.attachChild(model);
            model.setLocalScale(0.1f);
            Quaternion q = new Quaternion();
            q = q.fromAngles(FastMath.DEG_TO_RAD * (-90), FastMath.DEG_TO_RAD * -90, FastMath.DEG_TO_RAD * (-90));
            model.setLocalRotation(q);

            Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.getAdditionalRenderState().setWireframe(true);
            mat.setColor("Color", color);
            model.setMaterial(mat);
        } else {
            this.load3D();
        }

    }

    private void load3D() {
        Spatial model = am.loadModel("Models/quad.j3o");
        this.attachChild(model);
        model.setLocalScale(0.1f);
    }

    @Override
    public void setControlSurfaces(float da, float de, float dr, float den, float df) {
        Quaternion elev = new Quaternion();
        elev = elev.fromAngles(0, de, 0);

        Quaternion ail = new Quaternion();
        ail = ail.fromAngles(0, da, 0);
        leftaileron.setLocalRotation(ail.mult(elev));
        rightaileron.setLocalRotation(ail.inverse().mult(elev));
        movingmarker.setLocalRotation(leftaileron.getLocalRotation());

        // Festfahrung Propeller:
        float rps = den * 1000 / 60;
        // One update about once every 20ms:
        float rotationPerFrame = 0.02f * rps * 2f * (float) Math.PI;
        ail = ail.fromAngleAxis(rotationPerFrame, Vector3f.UNIT_X);
        propeller.setLocalRotation(propeller.getLocalRotation().mult(ail));
    }

    private Geometry buildDeflectionFrame(float r, float angle) {
        Mesh m = new Mesh();
        m.setMode(Mesh.Mode.Lines);
        // Vertex positions in space
        int sections = 10;
        Vector3f[] vertices = new Vector3f[sections + 1];
        Vector3f radius = new Vector3f(-r, 0, 0);
        Matrix3f rm = new Matrix3f();
        rm.fromAngleAxis(-angle / 2, Vector3f.UNIT_Y);
        // Set radius vector to where the arc starts:
        radius = rm.mult(radius);
        float dAngle = angle / sections;
        for (int i = 0; i < sections; i++) {
            rm.fromAngleAxis(dAngle, Vector3f.UNIT_Y);
            radius = rm.mult(radius);
            vertices[i] = radius;
            rm.fromAngleAxis(dAngle / 10, Vector3f.UNIT_Y);
            radius = rm.mult(radius);
        }
        vertices[sections] = Vector3f.ZERO;
        // Indexes. We define the order in which mesh should be constructed
        short[] indexes = new short[sections * 2];
        indexes[0] = 0;
        indexes[indexes.length - 1] = (short) (sections - 1);
        int ind = 1;
        for (int i = 1; i < indexes.length - 1; i = i + 2) {
            indexes[i] = (short) (ind);
            indexes[i + 1] = (short) ind;
            ind++;
        }

        // Setting buffers
        m.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        m.setBuffer(VertexBuffer.Type.Index, 1, BufferUtils.createShortBuffer(indexes));
        m.updateBound();
        Geometry geom = new Geometry("OurMesh", m);
        Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", color);
        geom.setMaterial(mat);

        return geom;
    }

    private Geometry buildDeflectionMarker(float r) {
        Mesh m = new Mesh();
        m.setMode(Mesh.Mode.Lines);
        // Vertex positions in space
        Vector3f[] vertices = new Vector3f[2];
        vertices[0] = new Vector3f(0, 0, 0);
        vertices[0] = new Vector3f(-r, 0, 0);

        // Indexes. We define the order in which mesh should be constructed
        short[] indexes = {0, 1};
        // Setting buffers
        m.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        m.setBuffer(VertexBuffer.Type.Index, 1, BufferUtils.createShortBuffer(indexes));
        m.updateBound();
        Geometry geom = new Geometry("OurMesh", m);
        Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", color);
        geom.setMaterial(mat);

        return geom;
    }
}
