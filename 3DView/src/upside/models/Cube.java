package upside.models;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import upside.ACNode;

/**
 *
 * @author j.bolting
 */
public class Cube extends Aircraft {

    private Node bbox;
    private Geometry cube2Geo;
    float lasttransparency;

    public Cube(AssetManager am, ColorRGBA color, int id, ACNode staticNode) {
        super(am, color, id, staticNode);
        this.load3D();
    }

    @Override
    public void setGhostLook(boolean wireframe) {
    }

    private void load3D() {
        this.detachAllChildren();

        Box cube2Mesh = new Box(1f, 1f, 1f);
        cube2Geo = new Geometry("window frame", cube2Mesh);
        Material cube2Mat = new Material(am,
                "Common/MatDefs/Misc/Unshaded.j3md");
        //cube2Mat.setTexture("ColorMap", am.loadTexture("Textures/ColoredTex/Monkey.png"));
        cube2Mat.setColor("Color", new ColorRGBA(1, 0, 0, 0.5f)); // 0.5f is the alpha value
        cube2Mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        cube2Geo.setQueueBucket(Bucket.Transparent);
        cube2Mat.getAdditionalRenderState().setWireframe(false);
        cube2Geo.setMaterial(cube2Mat);

        Box outerbox = new Box(1f, 1f, 1f);
        Geometry geo = new Geometry("window frame", outerbox);
        Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        //cube2Mat.setTexture("ColorMap", am.loadTexture("Textures/ColoredTex/Monkey.png"));
        mat.setColor("Color", new ColorRGBA(1, 0, 0, 1.0f)); // 0.5f is the alpha value
        mat.getAdditionalRenderState().setWireframe(true);
        geo.setMaterial(mat);

        // geom.setMaterial(mat);
        bbox = new Node();
        bbox.attachChild(cube2Geo);
        bbox.attachChild(geo);
        this.attachChild(bbox);
    }

    @Override
    // This methos is (ab)used here to set the cube's dimensions and transparency:
    public void setControlSurfaces(float da, float de, float dr, float den, float df) {
        bbox.setLocalScale(da/2, de/2, dr/2);
        // The position of the box is its 3D center, adapt z position to make it the bottom of the box:
        bbox.setLocalTranslation(0, 0, -dr/2);
        // Set transparency:
        float transparency = 1-den;
        if (Math.abs(lasttransparency - transparency) > 0.01) {
            Material m = cube2Geo.getMaterial();
            m.setColor("Color", new ColorRGBA(1, 0, 0, transparency));
            cube2Geo.setMaterial(m);
            lasttransparency = transparency;
        }
    }
}
