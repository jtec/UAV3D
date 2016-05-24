/*
 * Builds all kinds of terrains ans scene backgrounds.
 */
package upside.Terrain;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;

/**
 *
 * @author j.bolting
 */
public class TerrainBuilder {

    private AssetManager assetManager;
    private Camera camera;
    private float grassScale = 64;
    private float dirtScale = 16;
    private float rockScale = 128;

    public TerrainBuilder(AssetManager assetManager, Camera cam) {
        this.assetManager = assetManager;
        this.camera = cam;
    }

    public TerrainQuad buildTerrain_semirealistic() {
        TerrainQuad terrain;
        Material matRock;
        Material matWire;
        boolean wireframe = false;
        float grassScale = 64;
        float dirtScale = 16;
        float rockScale = 128;
        // First, we load up our textures and the heightmap texture for the terrain

        // TERRAIN TEXTURE material
        matRock = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        matRock.setBoolean("useTriPlanarMapping", false);

        // ALPHA map (for splat textures)
        matRock.setTexture("Alpha", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"));

        // HEIGHTMAP image (for the terrain heightmap)
        Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");

        // GRASS texture
        Texture forest = assetManager.loadTexture("Textures/Terrain/splat/forest.png");
        matRock.setTexture("Tex1", forest);
        matRock.setFloat("Tex1Scale", grassScale);

        // DIRT texture
        Texture sahara = assetManager.loadTexture("Textures/Terrain/splat/sand.png");
        sahara.setWrap(Texture.WrapMode.Repeat);
        matRock.setTexture("Tex2", sahara);
        matRock.setFloat("Tex2Scale", dirtScale);

        // ROCK texture
        Texture rock = assetManager.loadTexture("Textures/Terrain/splat/acker.png");
        matRock.setTexture("Tex3", rock);
        matRock.setFloat("Tex3Scale", rockScale);

        // WIREFRAME material
        matWire = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matWire.getAdditionalRenderState().setWireframe(true);
        matWire.setColor("Color", ColorRGBA.Green);

        // CREATE HEIGHTMAP
        AbstractHeightMap heightmap = null;
        try {
            //heightmap = new HillHeightMap(1025, 1000, 50, 100, (byte) 3);

            heightmap = new ImageBasedHeightMap(heightMapImage.getImage(), 1f);
            heightmap.load();

        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * Here we create the actual terrain. The tiles will be 65x65, and the total size of the
         * terrain will be 513x513. It uses the heightmap we created to generate the height values.
         */
        /**
         * Optimal terrain patch size is 65 (64x64). The total size is up to
         * you. At 1025 it ran fine for me (200+FPS), however at size=2049, it
         * got really slow. But that is a jump from 2 million to 8 million
         * triangles...
         */
        terrain = new TerrainQuad("terrain", 65, 1025, heightmap.getHeightMap());
        TerrainLodControl control = new TerrainLodControl(terrain, this.camera);
        control.setLodCalculator(new DistanceLodCalculator(65, 2.7f)); // patch size, and a multiplier
        terrain.addControl(control);
        // TERRAIN TEXTURE material
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        // DIRT texture
        Texture aerialPhotoTex = assetManager.loadTexture("Textures/aerialPhoto.png");
        mat.setTexture("ColorMap", sahara);

        // Simple material:
        Material sMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        sMat.setColor("Color",ColorRGBA.DarkGray);  // minimum material color
        // DIRT texture
        Texture sTex = assetManager.loadTexture("Textures/aerialPhoto.png");
        // sMat.setTexture("ColorMap", aerialPhotoTex);

        terrain.setMaterial(mat);
        terrain.setLocalTranslation(0, -100, 0);
        terrain.setLocalScale(1f, 0.5f, 1f);
        return terrain;
    }

    public Geometry buildTerrain_extralarge(float dx, float dy) {
        Material matRock;
        Box box = new Box(dx, dy, 1);

        // Creating a geometry, and apply a single color material to it
        Geometry geom = new Geometry("OurMesh", box);
        // TERRAIN TEXTURE material
        matRock = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
        matRock.setBoolean("useTriPlanarMapping", false);

        // ROCK texture
        Texture rock = assetManager.loadTexture("Textures/Terrain/splat/sand.png");
        // rock.setWrap(Texture.WrapMode.Clamp);
        matRock.setTexture("Tex3", rock);
        matRock.setFloat("Tex3Scale", rockScale);
        geom.setMaterial(matRock);

        return geom;
    }

    public Geometry buildTerrain_aerialPhoto(float dx, float dy) {
        Box box = new Box(dx, dy, 1);

        // Creating a geometry, and apply a single color material to it
        Geometry geom = new Geometry("OurMesh", box);
        // TERRAIN TEXTURE material
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        // DIRT texture
        Texture aerialPhotoTex = assetManager.loadTexture("Textures/aerialPhoto.png");
        mat.setTexture("ColorMap", aerialPhotoTex);

        geom.setMaterial(mat);

        return geom;
    }
    
        public Geometry buildTerrain_unicolor(float dx, float dy) {
        Box box = new Box(dx, dy, 1);

        // Creating a geometry, and apply a single color material to it
        Geometry geom = new Geometry("OurMesh", box);
        // TERRAIN TEXTURE material
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        // DIRT texture
        //Texture aerialPhotoTex = assetManager.loadTexture("Textures/aerialPhoto.png");
        //mat.setTexture("ColorMap", aerialPhotoTex);

        geom.setMaterial(mat);

        return geom;
    }

}
