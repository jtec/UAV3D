/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package upside.models;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;

/**
 *
 * @author j.bolting
 */
public class Cloud extends Aircraft {

    public Cloud(AssetManager am, ColorRGBA color, int id) {
        super(am, color, id);
        this.load3D();
    }

    @Override
    public void setGhostLook(boolean wireframe) {
        
    }

    private void load3D() {
        this.detachAllChildren();
      Material material = new Material(am, "Common/MatDefs/Misc/Particle.j3md");
        material.setTexture("Texture", am.loadTexture("Textures/flamme.png"));
        material.setFloat("Softness", 30f); // 
        // material.setTexture("Texture", assetManager.loadTexture("Particles/cloud.png"));
        material.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Node cloudNode = new Node();
        ParticleEmitter smoke = new ParticleEmitter("Smoke", ParticleMesh.Type.Triangle, 1);
        smoke.setMaterial(material);
        smoke.setShape(new EmitterSphereShape(Vector3f.ZERO, 5));
        smoke.setImagesX(1);
        smoke.setImagesY(1); // 2x2 texture animation
        smoke.setStartColor(new ColorRGBA(0.99f, 0.99f, 0.99f, 0.4f)); // dark gray
        smoke.setEndColor(new ColorRGBA(0.99f, 0.99f, 0.99f, 0.3f)); // gray      
        smoke.setStartSize(5f);
        smoke.setEndSize(5f);
        smoke.setGravity(0, -0.00f, 0);
        smoke.setLowLife(1000);
        smoke.setHighLife(1000);
        smoke.setLocalTranslation(10, 10, -10);
        smoke.emitAllParticles();

        // cloudNode.attachChild(smoke);
        
        /**
         * A bumpy rock with a shiny light effect.
         */
        Sphere sphereMesh = new Sphere(32, 32, 2f);
        Geometry sphereGeo = new Geometry("Shiny rock", sphereMesh);
        sphereMesh.setTextureMode(Sphere.TextureMode.Projected); // better quality on spheres
        TangentBinormalGenerator.generate(sphereMesh);           // for lighting effect
        Material sphereMat = new Material(am,
                "Common/MatDefs/Light/Lighting.j3md");
        sphereMat.setTexture("DiffuseMap",
                am.loadTexture("Textures/cloudy.png"));
        sphereMat.setTexture("NormalMap",
                am.loadTexture("Textures/cloudy.png"));
        sphereMat.setBoolean("UseMaterialColors", true);
        sphereMat.setColor("Diffuse", ColorRGBA.White);
        sphereMat.setColor("Specular", ColorRGBA.White);
        sphereMat.setFloat("Shininess", 0f);  // [0,128]
        sphereMat.getAdditionalRenderState().setWireframe(false);
        sphereMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        sphereMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        
        Material mat = new Material(am, "Common/MatDefs/Light/Lighting.j3md");
        mat.getAdditionalRenderState().setWireframe(false);
        Texture tex = am.loadTexture("Textures/cloudy.png");
        mat.setTexture("NormalMap", tex);
        mat.setTexture("DiffuseMap", tex);
        mat.setFloat("Shininess", 1f);
        mat.getTextureParam("DiffuseMap").getTextureValue().setWrap(Texture.WrapMode.Clamp);
        
        sphereGeo.setMaterial(sphereMat);
        sphereGeo.setLocalTranslation(0, 2, -2); // Move it a bit
        sphereGeo.rotate(1.6f, 0, 0);          // Rotate it a bit
        // cloudNode.attachChild(sphereGeo);

        Spatial cloud = am.loadModel("Models/cloud.j3o");
        // Fix orientation:
        Quaternion q = new Quaternion();
        q = q.fromAngles(FastMath.DEG_TO_RAD * (-90), FastMath.DEG_TO_RAD * 0, FastMath.DEG_TO_RAD * (-90));
        cloud.setLocalRotation(q);

        cloud.setMaterial(sphereMat);
        cloud.setLocalTranslation(-100, 0, -100);
        cloud.setLocalScale(30f);
        cloudNode.attachChild(cloud);
       
        this.attachChild(cloudNode);
    }

    @Override
    public void setControlSurfaces(float da, float de, float dr, float den, float df) {
        
    }
}
