/*
 * Node that carries an aircraft 3D model.
 */
package upside;

import upside.models.Aircraft;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Box;
import java.awt.Color;
import java.util.Date;
import upside.IO.AircraftStateMessage;
import upside.models.*;

/**
 *
 * @author j.bolting
 */
public class ACNode extends Node {

    private Date updateTime = new Date();
    Geometry[] breadcrumbs = new Geometry[30];
    int breadcrumbCounter = 0;
    ColorRGBA color;
    public Node staticNode;
    AssetManager am;
    Mesh trail;
    Vector3f lastTrailPoint = Vector3f.ZERO;
    Aircraft model;
    public Node trailNode;
    int id;
    boolean leaveTrail;
    State.Model3D visual;
    long lasttraildroptime;
            
    public ACNode(AssetManager assetManager, int id, Node staticNode) {
        super();
        
        trailNode = new Node();
        staticNode.attachChild(trailNode);
        this.am = assetManager;
        this.staticNode = staticNode;

        Color acColor = generateColor(id);
        color = new ColorRGBA(acColor.getRed(), acColor.getGreen(), acColor.getBlue(), acColor.getAlpha());

        // util.attachCoordinateAxes(Vector3f.ZERO, new Vector3f(1f, 1f, 1f), this, assetManager);
        // util.attachWireBox(Vector3f.ZERO, 1, color, this, assetManager);
        // Create breadcrumbs that mark the aircraft's trajectory:
        for (int i = 0; i < breadcrumbs.length; i++) {
            breadcrumbs[i] = buildBreadcrumb(assetManager);
            staticNode.attachChild(breadcrumbs[i]);
            breadcrumbs[i].setLocalTranslation(Vector3f.ZERO);
        }
        trail = new Mesh();
        trail.setBuffer(VertexBuffer.Type.Position, 3, new float[]{0, 0, 0, 0, 0, 0});
        trail.setBuffer(VertexBuffer.Type.Index, 2, new short[]{0, 1});
        addToTrail(Vector3f.ZERO);
        this.id = id;
        leaveTrail = true;

        this.model = new Zagi(this.am, this.color, id, this);
        this.attachChild(this.model);
        this.setVisual(State.Model3D.ZAGI);
    }

    public void setLeaveTrail(boolean leaveTrail) {
        this.leaveTrail = leaveTrail;
    }

    public void cleanup() {
        this.model.cleanup();
    }

    protected void clearTracks() {
        this.trailNode.detachAllChildren();
        for (int i = 0; i < breadcrumbs.length; i++) {
            breadcrumbs[i].setLocalTranslation(Vector3f.ZERO);
        }
    }

    public void setGhostLook(boolean isghost) {
        model.setGhostLook(isghost);
    }

    private Geometry buildBreadcrumb(AssetManager am) {
        Box box = new Box(0.03f, 0.03f, 0.03f);

        // Creating a geometry, and apply a single color material to it
        Geometry geom = new Geometry("OurMesh", box);
        Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        geom.setMaterial(mat);
        return geom;
    }

    private void addToTrail(Vector3f where) {
        Mesh trail = new Mesh();
        trail.setMode(Mesh.Mode.Lines);
        trail.setBuffer(VertexBuffer.Type.Position, 3, new float[]{lastTrailPoint.x, lastTrailPoint.y, lastTrailPoint.z, where.x, where.y, where.z});
        trail.setBuffer(VertexBuffer.Type.Index, 2, new short[]{0, 1});
        trail.updateBound();
        trail.updateCounts();
        Geometry lineGeometry = new Geometry("line", trail);
        Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", color);
        lineGeometry.setMaterial(mat);
        trailNode.attachChild(lineGeometry);
    }

    public void setState(AircraftStateMessage msg) {
        // Set position:
        this.setLocalTranslation(msg.p_NED_m[0], msg.p_NED_m[1], msg.p_NED_m[2]);
        // Set attitude:
        float qw = msg.quaternion_NED2body[0];
        float qx = msg.quaternion_NED2body[1];
        float qy = msg.quaternion_NED2body[2];
        float qz = msg.quaternion_NED2body[3];

        this.setLocalRotation(new Quaternion(qx, qy, qz, qw));

        this.updateTime = new Date();

        // Figure out whether to emit a new breadcrumb:
        float d = this.getLocalTranslation().subtract(lastTrailPoint).length();
        float dt_ms = System.currentTimeMillis() - this.lasttraildroptime;
        if ((d > 1) && leaveTrail) {
            // System.out.print(d);
            breadcrumbCounter++;
            if (breadcrumbCounter >= breadcrumbs.length) {
                breadcrumbCounter = 0;
            }
            // Avoid contuínuing the trail if the simulation has been interrupted:
            if (dt_ms < 500) {
            //if (d < 200) {
                addToTrail(this.getLocalTranslation());
            }
            lasttraildroptime = System.currentTimeMillis();
            lastTrailPoint = this.getLocalTranslation().clone();
            // breadcrumbs[breadcrumbCounter].setLocalTranslation(this.getLocalTranslation());
        }
        // Update control surface deflections:
        this.model.setControlSurfaces(msg.da, msg.de, msg.dr, msg.den, msg.df);
        // Update 3D model
        this.setVisual(msg.visual);
        if(this.id > 100){
            this.model.setGhostLook(true);
        }
    }

    public Color generateColor(int id) {
        Color[] cols = new Color[10];
        cols[0] = Color.LIGHT_GRAY;
        cols[1] = Color.GREEN;
        cols[2] = Color.RED;
        cols[3] = Color.BLUE;
        cols[4] = Color.ORANGE;
        cols[5] = Color.BLACK;
        cols[6] = Color.YELLOW;
        cols[7] = Color.CYAN;
        cols[8] = Color.MAGENTA;
        cols[9] = Color.PINK;

        if (id > 9) {
            id = 0;
        }

        return cols[id];
    }

    public void setVisual(State.Model3D v) {
        if (v != this.visual) {
            this.detachChild(this.model);
            boolean isghost = model.hasGhostLook();
            if (v == State.Model3D.DLG) {
                this.model = new DLG(this.am, this.color, this.id, this);
            } else if (v == State.Model3D.SAILPLANE) {
                this.model = new Sailplane(this.am, this.color, id, this);
            } else if (v == State.Model3D.ZAGI) {
                this.model = new Zagi(this.am, this.color, id, this);
            } else if (v == State.Model3D.QUAD) {
                this.model = new Quad(this.am, this.color, id, this);
            } else if (v == State.Model3D.BALL) {
                this.model = new Ball(this.am, this.color, this.id, this);
            } else if (v == State.Model3D.CLOUD) {
                this.model = new Cloud(this.am, this.color, id, this);
            } else if (v == State.Model3D.CUBE) {
                this.model = new Cube(this.am, this.color, id, this);
            } else if (v == State.Model3D.VORTEX) {
                this.model = new Vortex(this.am, this.color, id, this);
                this.setLeaveTrail(false);
            }
            this.model.setGhostLook(isghost);
            this.attachChild(this.model);
            this.visual = v;
        }
    }
}
