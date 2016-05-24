package upside;

/*
 * FIXME Memory consumption increases over 
 * time to more than 1 Gb; is there a memory leak?
 * FIXME Clicking on the window's exit button sometimes does not stop the jmonkey thread 
 * and the UDP server thread.
 */
// jm3 packages:
import upside.Terrain.TerrainBuilder;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.font.BitmapFont;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.noise.ShaderUtils;
import com.jme3.terrain.noise.basis.FilteredBasis;
import com.jme3.terrain.noise.filter.IterativeFilter;
import com.jme3.terrain.noise.filter.OptimizedErode;
import com.jme3.terrain.noise.filter.PerturbFilter;
import com.jme3.terrain.noise.filter.SmoothFilter;
import com.jme3.terrain.noise.fractal.FractalSum;
import com.jme3.terrain.noise.modulator.NoiseModulator;
import com.jme3.texture.Texture;
import java.awt.MouseInfo;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import jme3utilities.sky.SkyControl;
import upside.IO.AircraftStateMessage;
import upside.IO.UDPServer;
import upside.UI.PopupEvent;
import worldtest.world.NoiseBasedWorld;
import worldtest.world.World;

public class App extends SimpleApplication {

    private TerrainQuad terrain;
    Material mat_terrain;
    UPCameraNode camNode;
    Node lookAtNode = new Node();
    Node cameraAxisNode = new Node();
    Node terrainNode = new Node();
    Node endlessTerrainNode = new Node();
    private UDPServer udpserver = new UDPServer();
    private Vector3f swarmCenter = new Vector3f(Vector3f.ZERO);
    Vector3f masterposition = new Vector3f(Vector3f.ZERO);
    public EventBus eventBus;
    public State state = new State();
    private boolean isRecording;
    private VideoRecorderAppState videoRecorderAppState;
    // Endless terrain:
    private World world;
    float maxWorldHeight = 260;
    int tileSize = 65;
    int blockSize = 129;
    private BulletAppState bulletAppState;
    private static App app;

    public App(EventBus ebus) {
        super();
        this.eventBus = ebus;
        ebus.register(this);
        this.showSettings = false;
        setDisplayStatView(false);
        this.updateUI();
        isRecording = false;
        app = this;
    }

    private void updateUI() {
        eventBus.post(new StateChangeEvent(state));
    }

    public Node getTerrainNode() {
        return this.endlessTerrainNode;
    }

    @Override
    public void simpleInitApp() {
        TerrainBuilder tb = new TerrainBuilder(assetManager, getCamera());
        terrain = tb.buildTerrain_semirealistic();
        terrainNode.attachChild(terrain);
        // terrainNode.attachChild(terrain2D);
        // terrain2D.move(0, 0, 10);
        // jmonkey convention: y axis point up, z axis points to the user -> terrain
        // has to be rotated by -90 deg around the world's x-axis for a NED perspective.
        Quaternion rotation = new Quaternion();
        rotation.fromAngleAxis(FastMath.DEG_TO_RAD * (-90), Vector3f.UNIT_X);
        terrain.rotate(rotation);
        rootNode.attachChild(terrainNode);
        rotation = new Quaternion();
        rotation.fromAngleAxis(FastMath.DEG_TO_RAD * (-45), Vector3f.UNIT_X);
        endlessTerrainNode.rotate(rotation);
        rootNode.attachChild(endlessTerrainNode);

        bulletAppState = new BulletAppState();
        // bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);
        //createWorldWithNoise();
        // OR
        // createWorldWithImages();
        // attach to state manager so we can monitor movement.
        // this.stateManager.attach(world);

        // Add more sophisticated sky:
        SkyControl sc = new SkyControl(assetManager, cam, 0.8f, false, true);
        Node skyNode = new Node();
        skyNode.addControl(sc);
        rootNode.attachChild(skyNode);
        rotation.fromAngleAxis(FastMath.DEG_TO_RAD * (-90), Vector3f.UNIT_X);
        skyNode.rotate(rotation);
        sc.getSunAndStars().setHour(13f);
        sc.getSunAndStars().setObserverLatitude(54.0f * FastMath.DEG_TO_RAD);
        sc.getSunAndStars().setSolarLongitude(Calendar.JUNE, 10);
        sc.setCloudiness(0.0f);
        sc.setEnabled(true);

        // Add directional light source + a little diffuse light representing 
        // reflections from the ground:
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(0f, 0f, 1f).normalizeLocal());
        rootNode.addLight(sun);
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(100f));
        rootNode.addLight(al);

        // The LOD (level of detail) depends on were the camera is:
        TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
        terrain.addControl(control);
        initKeys();
        flyCam.setEnabled(false);

        // Populate the scene:
        camNode = new UPCameraNode("camMode", cam);
        lookAtNode.attachChild(camNode);
        rootNode.attachChild(lookAtNode);

        // Disable clipping of objects that are too close or too far from the camera:
        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.01f, 10000f);

        // Launch UDP Server to receive aircraft state messages:
        this.udpserver.launchServerthread(4445);

        // Add compass at origin of NED frame:
        Spatial compass = assetManager.loadModel("Models/compassrose.j3o");
        rootNode.attachChild(compass);
        compass.setLocalTranslation(0, 0, -10);
        compass.setLocalScale(50);

        Pylon pylon = new Pylon(100f, 1f, assetManager);
        rootNode.attachChild(pylon);
        
        rootNode.attachChild(guiNode);
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");

    }

    private void createWorldWithNoise() {
        NoiseBasedWorld newWorld = new NoiseBasedWorld(app, bulletAppState.getPhysicsSpace(), tileSize, blockSize);
        newWorld.setWorldHeight(192f);
        newWorld.setViewDistance(2);
        // newWorld.setViewDistance(14, 1, 2, 1);
        newWorld.setCacheTime(5000);

        Material terrainMaterial = createTerrainMaterial();
        newWorld.setMaterial(terrainMaterial);

        // create a noise generator
        FractalSum base = new FractalSum();

        base.setRoughness(0.7f);
        base.setFrequency(1.0f);
        base.setAmplitude(1.0f);
        base.setLacunarity(3.12f);
        base.setOctaves(8);
        base.setScale(0.02125f);
        base.addModulator(new NoiseModulator() {
            @Override
            public float value(float... in) {
                return ShaderUtils.clamp(in[0] * 0.5f + 0.5f, 0, 1);
            }
        });

        FilteredBasis ground = new FilteredBasis(base);

        PerturbFilter perturb = new PerturbFilter();
        perturb.setMagnitude(0.119f);

        OptimizedErode therm = new OptimizedErode();
        therm.setRadius(5);
        therm.setTalus(0.011f);

        SmoothFilter smooth = new SmoothFilter();
        smooth.setRadius(1);
        smooth.setEffect(0.7f);

        IterativeFilter iterate = new IterativeFilter();
        iterate.addPreFilter(perturb);
        iterate.addPostFilter(smooth);
        iterate.setFilter(therm);
        iterate.setIterations(1);

        ground.addPreFilter(iterate);

        newWorld.setFilteredBasis(ground);

        this.world = newWorld;
    }

    private Material createTerrainMaterial() {
        Material terrainMaterial = new Material(this.assetManager, "Common/MatDefs/Terrain/HeightBasedTerrain.j3md");

        float grassScale = 16;
        float dirtScale = 16;
        float rockScale = 16;

        // GRASS texture
        Texture grass = this.assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(Texture.WrapMode.Repeat);
        terrainMaterial.setTexture("region1ColorMap", grass);
        terrainMaterial.setVector3("region1", new Vector3f(88, 200, grassScale));

        // DIRT texture
        Texture dirt = this.assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(Texture.WrapMode.Repeat);
        terrainMaterial.setTexture("region2ColorMap", dirt);
        terrainMaterial.setVector3("region2", new Vector3f(0, 90, dirtScale));

        // ROCK texture

        Texture rock = this.assetManager.loadTexture("Textures/Terrain/Rock/Rock.PNG");
        rock.setWrap(Texture.WrapMode.Repeat);
        terrainMaterial.setTexture("region3ColorMap", rock);
        terrainMaterial.setVector3("region3", new Vector3f(198, 260, rockScale));

        terrainMaterial.setTexture("region4ColorMap", rock);
        terrainMaterial.setVector3("region4", new Vector3f(198, 260, rockScale));

        Texture rock2 = this.assetManager.loadTexture("Textures/Terrain/Rock2/rock.jpg");
        rock2.setWrap(Texture.WrapMode.Repeat);

        terrainMaterial.setTexture("slopeColorMap", rock2);
        terrainMaterial.setFloat("slopeTileFactor", 32);

        terrainMaterial.setFloat("terrainSize", blockSize);

        return terrainMaterial;
    }

    @Subscribe
    public void handleSettingsChangeCommand(StateChangeCommand e) {
        // Should we start recording:
        if (!this.isRecording && e.state.isRecording) {
            videoRecorderAppState = new VideoRecorderAppState(1, 30);
            stateManager.attach(videoRecorderAppState); // start recording
            this.isRecording = true;
        } else if (this.isRecording && !e.state.isRecording) {
            stateManager.detach(videoRecorderAppState); //stop recording
            this.isRecording = false;
        }

        this.state = e.state;
        // Update UI
        eventBus.post(new StateChangeEvent(e.state));
        // Update all aircraft 3D models if necessary:
        Iterator iter = state.aircraft.keySet().iterator();
        while (iter.hasNext()) {
            state.aircraft.get(iter.next()).setVisual(state.model);
        }
    }

    /**
     * Custom Keybinding: Map named actions to inputs.
     */
    private void initKeys() {
        // Clear the default listeners of SimpleApplication and add the new ones:
        inputManager.clearMappings();

        // You can map one or several inputs to one named action
        inputManager.addMapping("Up", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping("Down", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("Left", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("Right", new MouseAxisTrigger(MouseInput.AXIS_X, false));

        inputManager.addMapping("Exit", new KeyTrigger(KeyInput.KEY_ESCAPE));

        inputManager.addMapping("Approach", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping("Retreat", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
        inputManager.addMapping("Approach", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Retreat", new KeyTrigger(KeyInput.KEY_DOWN));

        inputManager.addMapping("RotationEnable", new MouseButtonTrigger(MouseInput.AXIS_WHEEL));
        inputManager.addMapping("Rightclick", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("Leftclick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        inputManager.addListener(analogListener, "Left", "Right", "Up", "Down", "Exit", "Approach", "Retreat");
        inputManager.addListener(clickListener, "Rightclick", "Leftclick");
        inputManager.addListener(rotationListener, "RotationEnable");
    }
    private ActionListener rotationListener = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {

            if (name.equals("RotationEnable") && keyPressed) {
                camNode.rotationEnabled = true;
            } else {
                camNode.rotationEnabled = false;
            }
        }
    };
    private ActionListener clickListener = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {

            if (name.equals("Rightclick") && keyPressed) {
                eventBus.post(new PopupEvent(MouseInfo.getPointerInfo().getLocation(), PopupEvent.Flags.RISE));
            }

            if (name.equals("Leftclick") && keyPressed) {
                eventBus.post(new PopupEvent(MouseInfo.getPointerInfo().getLocation(), PopupEvent.Flags.HIDE));
            }
        }
    };
    private AnalogListener analogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            float frameratescaling = 0.01f;
            if (state.viewmode == State.ViewModes.VIEW3D || state.viewmode == State.ViewModes.CHASEVIEW || state.viewmode == State.ViewModes.FORMATIONVIEW) {
                // Check whether rotation is disabled:
                if (camNode.rotationEnabled) {

                    if (name.equals("Right")) {
                        camNode.azimate(-frameratescaling * camNode.azimuthSpeed);
                    } else if (name.equals("Left")) {
                        camNode.azimate(frameratescaling * camNode.azimuthSpeed);
                    } else if (name.equals("Up")) {
                        camNode.elevate(-frameratescaling * camNode.elevationSpeed);
                    } else if (name.equals("Down")) {
                        camNode.elevate(frameratescaling * camNode.elevationSpeed);
                    }
                }
            }

            float zoomStep = (frameratescaling * camNode.zoomSpeed);
            if (name.equals("Approach")) {
                // airplane.move(-zoomStep, -zoomStep, -zoomStep);
                camNode.approachTarget(-zoomStep);
            }
            if (name.equals("Retreat")) {
                // airplane.move(zoomStep, zoomStep, zoomStep);
                camNode.approachTarget(zoomStep);
            }

            // Evaluate application control keys:
            if (name.equals("Exit")) {
                stop();
            }
        }
    };

    @Override
    public void update() {
        try {
            super.update();
        } catch (java.lang.NullPointerException ne) {
        }
        // Iterate over inboxes, read latest aircraft state messages
        Set keys = this.udpserver.serverthread.getInboxes().keySet();
        Iterator iter = keys.iterator();
        while (iter.hasNext()) {
            Integer msgKey = (Integer) iter.next();
            AircraftStateMessage msg = this.udpserver.serverthread.getInboxes().get(msgKey);
            if (msg != null) {
                handleMessage(msg);
            }
            this.udpserver.serverthread.getInboxes().put(msgKey, null);
        }
        // Set view:
        if (state.viewmode == State.ViewModes.VIEW3D) {
            threeDView(state.viewTargetID);
        } else if (state.viewmode == State.ViewModes.CHASEVIEW) {
            chaseView(state.viewTargetID);
        } else if (state.viewmode == State.ViewModes.FORMATIONVIEW) {
            formationCenterView();
        } else if (state.viewmode == State.ViewModes.MAPVIEW) {
            mapView(state.viewTargetID);
        }

        checkTerrain();
        camNode.updateCamState();
    }

    private void threeDView(int ID) {
        ACNode target = this.state.aircraft.get(ID);
        if (target != null) {
            rootNode.attachChild(lookAtNode);
            lookAtNode.setLocalTranslation(target.getLocalTranslation());
        } else {
            lookAtNode.setLocalTranslation(Vector3f.ZERO);
        }
    }

    private void chaseView(int ID) {
        ACNode target = this.state.aircraft.get(ID);
        if (target != null) {
            target.attachChild(lookAtNode);
            lookAtNode.setLocalTranslation(Vector3f.ZERO);
        } else {
            lookAtNode.setLocalTranslation(Vector3f.ZERO);
        }
    }

    private void mapView(int id) {
        ACNode target = this.state.aircraft.get(id);
        if (target != null) {
            lookAtNode.setLocalTranslation(target.getLocalTranslation());
        } else {
            lookAtNode.setLocalTranslation(Vector3f.ZERO);
        }
        camNode.set(FastMath.DEG_TO_RAD * 90, FastMath.DEG_TO_RAD * 0);
    }

    private void formationCenterView() {
        swarmCenter = new Vector3f(Vector3f.ZERO);
        masterposition = new Vector3f(Vector3f.ZERO);
        Iterator iter = this.state.aircraft.keySet().iterator();
        int nAircraft = this.state.aircraft.keySet().size();
        while (iter.hasNext()) {
            Vector3f pos = this.state.aircraft.get(iter.next()).getLocalTranslation();
            swarmCenter = swarmCenter.add(pos);
        }
        if (nAircraft > 0) {
            swarmCenter = swarmCenter.divide(nAircraft);
        } else {
            swarmCenter = Vector3f.ZERO;
        }
        lookAtNode.setLocalTranslation(swarmCenter);
        if (masterposition.z != 0) {
            lookAtNode.setLocalTranslation(masterposition);
        }
    }

    private void handleMessage(AircraftStateMessage msg) {
        // Check if message is fairly recent, otherwise remove aircraft:
        long timeout_ms = 3600 * 1000;
        Date now = new Date();
        long dt = Math.abs(now.getTime() - msg.date.getTime());
        if (dt < timeout_ms) {
            // For valid messages, proceed as follows:
            if (!this.state.aircraft.containsKey(msg.id)) {
                this.addAircraft(msg.id);
            }
            this.state.aircraft.get(msg.id).setState(msg);
        } else {
            // If message has timed out, remove aircraft node if it exists:
            if (this.state.aircraft.containsKey(msg.id)) {
                this.state.aircraft.get(msg.id).clearTracks();
                this.state.aircraft.get(msg.id).removeFromParent();
                this.state.aircraft.remove(msg.id);
                System.out.println("Removed aircraft, id= " + msg.id);
            }
        }

        if (state.aircraft.size() == 2) {
        }
    }

    private void addAircraft(int id) {
        ACNode newaircraft = new ACNode(assetManager, id, rootNode);
        if ((id >= 100) && (id < 200)){
            newaircraft.setGhostLook(true);
        } else {
            newaircraft.setGhostLook(false);
        }

        rootNode.attachChild(newaircraft);
        this.state.aircraft.put(id, newaircraft);
        // If there is only one aircraft, there is nothing else interesting 
        // enough to look at:
        if (state.aircraft.size() == 1) {
            state.viewTargetID = id;
        }

        System.out.println("Added new aircraft, id= " + id);
        this.eventBus.post(new StateChangeEvent(this.state));
    }

    public void startSimulation() {
        this.udpserver.enableSimulation();
    }

    public void stopSimulation() {
        this.udpserver.disableSimulation();
    }

    @Override
    public void stop() {
        this.udpserver.stop();
        super.stop();
    }

    private void checkTerrain() {
        Vector3f dv = lookAtNode.getLocalTranslation().subtract(this.terrainNode.getLocalTranslation());
        float dx = java.lang.Math.abs(dv.getX());
        float dy = java.lang.Math.abs(dv.getY());
        if ((dx > this.terrain.getTotalSize() / 2) || (dy > this.terrain.getTotalSize() / 2)) {
            this.terrainNode.setLocalTranslation(lookAtNode.getLocalTranslation().x, lookAtNode.getLocalTranslation().y, 0);
        }
    }

    /*
     * Removes all aircraft tracks.
     */
    public void clearTracks() {
        Iterator iter = state.aircraft.keySet().iterator();
        while (iter.hasNext()) {
            state.aircraft.get(iter.next()).clearTracks();
        }
    }

    private void removeAircraft(int id) {
        this.state.aircraft.get(id).clearTracks();
        this.state.aircraft.get(id).removeFromParent();
        this.state.aircraft.remove(id);
        System.out.println("Removed aircraft, id= " + id);
    }

    /*
     * Removes all aircraft.
     */
    public void clearAircraft() {
        Iterator iter = this.state.aircraft.keySet().iterator();
        while (iter.hasNext()) {
            int id = this.state.aircraft.get(iter.next()).id;
            this.state.aircraft.get(id).clearTracks();
            this.state.aircraft.get(id).removeFromParent();
            // This is the way to remove elements from a map while iterating over the map:
            iter.remove();
            System.out.println("Removed aircraft, id= " + id);
        }
        updateUI();
    }
}
