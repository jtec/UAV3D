package worldtest.world;

import com.jme3.asset.AssetNotFoundException;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import upside.App;

public class ImageBasedWorld extends World
{
    private Material terrainMaterial;

    public ImageBasedWorld(App app, PhysicsSpace physicsSpace, int tileSize, int blockSize)
    {
        super(app, physicsSpace, tileSize, blockSize);
    }

    public final Material getMaterial() { return this.terrainMaterial; }
    public final void setMaterial(Material material) { this.terrainMaterial = material; }

    @Override
    public TerrainChunk getTerrainChunk(TerrainLocation location)
    {
        TerrainChunk tq = this.worldTiles.get(location);

        if (tq != null)
            return tq;

        tq = this.worldTilesCache.get(location);

        if (tq != null)
            return tq;

        String tqName = "TerrainChunk_" + location.getX() + "_" + location.getZ();

        float[] heightmap = null;

        File savedFile = new File("./world/" + tqName + ".chunk");

        if (savedFile.exists())
        {
            try
            {
                FileInputStream door = new FileInputStream(savedFile);
                ObjectInputStream reader = new ObjectInputStream(door);

                heightmap = (float[])reader.readObject();
            }
            catch(Exception ex)
            {
                Logger.getLogger(NoiseBasedWorld.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            // fire the imageHeightmapRequired event to obtain the image path
            String imagePath = tileListener.imageHeightmapRequired(location.getX(), location.getZ());

            try
            {
                Texture hmapImage = app.getAssetManager().loadTexture(imagePath);
                AbstractHeightMap map = new ImageBasedHeightMap(hmapImage.getImage());
                map.load();

                heightmap = map.getHeightMap();
            }
            catch (AssetNotFoundException ex)
            {
                Logger.getLogger("com.jme").log(Level.INFO, "Image not found: {0}", imagePath);
                // The assetManager already logs null assets. don't re-iterate the point.
                heightmap = new float[this.blockSize * this.blockSize];
                Arrays.fill(heightmap, 0f);
            }
        }

        tq = new TerrainChunk(tqName, this.tileSize, this.blockSize, heightmap);
        // tq.setLocalScale(new Vector3f(1f, this.worldHeight, 1f));

        // set position
        int tqLocX = location.getX() << this.bitshift;
        int tqLoxZ = location.getZ() << this.bitshift;
        tq.setLocalTranslation(new Vector3f(tqLocX, 0, tqLoxZ));

        // add LOD
        // TerrainLodControl control = new TerrainLodControl(tq, app.getCamera());
        // control.setLodCalculator( new DistanceLodCalculator(this.tileSize, 2.7f));
        // tq.addControl(control);


        // add rigidity
        tq.addControl(new RigidBodyControl(new HeightfieldCollisionShape(heightmap, tq.getLocalScale()), 0));

        tq.setMaterial(terrainMaterial);
        return tq;

    }

}
