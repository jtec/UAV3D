/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package upside.models;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.BillboardControl;

public class Aircraft extends Node {

    Spatial leftaileron;
    Spatial rightaileron;
    Node righthinge;
    Node lefthinge;
    Spatial propeller;
    Geometry movingmarker;
    AssetManager am;
    ColorRGBA color;
    boolean ghostlook = false;
    int id;
    protected Node labelNode;

    public Aircraft(AssetManager am, ColorRGBA color, int id) {
        super();
        this.id = id;
        this.am = am;
        this.color = color;
        this.load3D();
        // util.attachWireBox(Vector3f.ZERO, 1, ColorRGBA.Brown, this, am);
    }

    private void load3D() {
        Spatial model = am.loadModel("Models/zagi_noelevons.j3o");
        this.attachChild(model);
        model.setLocalScale(1f);

        // Load control surface 3D models and attach them to the airframe:
        leftaileron = am.loadModel("Models/zagi_leftaileron.j3o");
        rightaileron = am.loadModel("Models/zagi_rightaileron.j3o");
        righthinge = new Node();
        lefthinge = new Node();
        lefthinge.attachChild(leftaileron);
        righthinge.attachChild(rightaileron);
        this.attachChild(lefthinge);
        lefthinge.setLocalTranslation(-0.585f, -0.4f, 0.005f);
        Quaternion hingealignment = new Quaternion().fromAngleAxis(-FastMath.DEG_TO_RAD * 17, Vector3f.UNIT_Z);
        hingealignment = hingealignment.mult(new Quaternion().fromAngleAxis(FastMath.DEG_TO_RAD * 5, Vector3f.UNIT_X));
        lefthinge.setLocalRotation(hingealignment);
        // Add deflection markers
        float rDeflections = 0.2f;
        Geometry fixedmarker = buildDeflectionFrame(rDeflections, FastMath.DEG_TO_RAD * 90);
        movingmarker = buildDeflectionMarker(1.0f * rDeflections);
        lefthinge.attachChild(fixedmarker);
        lefthinge.attachChild(movingmarker);
        // Add Propeller
        propeller = am.loadModel("Models/propeller1.j3o");
        this.attachChild(propeller);
        // Shift it into the right place:
        propeller.move(-0.5f, 0, 0);
    }

    public void setGhostLook(boolean wireframe) {
            this.ghostlook = wireframe;
            this.detachAllChildren();
            // util.attachWireBox(Vector3f.ZERO, 1, ColorRGBA.Brown, this, am);
    }

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

    public boolean hasGhostLook() {
        return this.ghostlook;
    }
    
    protected Node attachTextLabel(String text){
        Node bb = new Node("textlabel");
        BillboardControl control=new BillboardControl();
        bb.addControl(control);
        BitmapFont font = am.loadFont("Interface/Fonts/Default.fnt");
        BitmapText hud = new BitmapText(font, false);
        hud.setSize(font.getCharSet().getRenderedSize());
        hud.setColor(ColorRGBA.Yellow);
        hud.setText(text);
        hud.setSize(0.09f);
        hud.setLocalTranslation(0,0.3f,0);
        bb.attachChild(hud);
        return bb;
    }
}