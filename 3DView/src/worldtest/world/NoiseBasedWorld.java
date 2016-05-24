package worldtest.world;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.HeightfieldCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.noise.basis.FilteredBasis;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import upside.App;


public class NoiseBasedWorld extends World
{
    private Material terrainMaterial;
    private FilteredBasis filteredBasis;

    public NoiseBasedWorld(App app, PhysicsSpace physicsSpace, int tileSize, int blockSize)
    {
        super(app, physicsSpace, tileSize, blockSize);
    }

    public final Material getMaterial() { return this.terrainMaterial; }
    public final void setMaterial(Material material) { this.terrainMaterial = material; }

    public final FilteredBasis getFilteredBasis() { return this.filteredBasis; }
    public final void setFilteredBasis(FilteredBasis basis) { this.filteredBasis = basis; }

    @Override
    public TerrainChunk getTerrainChunk(TerrainLocation location)
    {
        TerrainChunk tq = this.worldTiles.get(location);

        if (tq != null){
            return tq;
        }
        
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
            heightmap = getHeightmap(location);
        }

        tq = new TerrainChunk(tqName, this.tileSize, this.blockSize, heightmap);
        tq.setLocalScale(new Vector3f(1f, this.worldHeight, 1f));

        // set position
        int tqLocX = location.getX() << this.bitshift;
        int tqLoxZ = location.getZ() << this.bitshift;
        // tqLocX = 0;
        // tqLoxZ = 0;
        tq.setLocalTranslation(new Vector3f(tqLocX, 0, tqLoxZ));
        Quaternion rotation = new Quaternion();
        rotation.fromAngleAxis(FastMath.DEG_TO_RAD * (-90), Vector3f.UNIT_X);
        // tq.rotate(rotation);

        // add LOD
        TerrainLodControl control = new TerrainLodControl(tq, app.getCamera());
        control.setLodCalculator( new DistanceLodCalculator(this.tileSize, 2.7f));
        tq.addControl(control);

        // add rigidity
        tq.addControl(new RigidBodyControl(new HeightfieldCollisionShape(heightmap, tq.getLocalScale()), 0));

        tq.setMaterial(terrainMaterial);
        return tq;
    }

    private float[] getHeightmap(TerrainLocation tl) { return getHeightmap(tl.getX(), tl.getZ()); }
    private float[] getHeightmap(int x, int z)
    {
        FloatBuffer buffer = this.filteredBasis.getBuffer(x * (this.blockSize - 1), z * (this.blockSize - 1), 0, this.blockSize);
        return buffer.array();
    }
}
