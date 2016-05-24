package upside;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

/**
 *
 * @author Jan
 * @brief Use as follows: create UPCameraNode and attach it to the node that is
 * supposed to be observed by the camera attached to this node. There are three
 * nodes: this, attached to the target, the elevation node and the actual camera
 * node. To change the camera's azimuth, this node is rotated about the target
 * node's z axis. To change its elevation, the elevation mode is rotated around
 * this node's x axis.
 *
 */
public class UPCameraNode extends Node {

    float zoomSpeed = 100;
    float elevationSpeed = 5;
    float azimuthSpeed = 15;
    Node camNode = new Node("camNode");
    Node elevationNode = new Node("elevationNode");
    boolean rotationEnabled = false;
    float maxElevation_deg = 1000;
    Camera cam;

    public UPCameraNode(String name, Camera cam) {
        super(name);
        this.cam = cam;
        elevationNode.attachChild(camNode);
        this.attachChild(elevationNode);
        camNode.setLocalTranslation(0, 10, 0);
        elevationNode.setLocalTranslation(0, 0, 0);

        this.setLocalRotation(Quaternion.IDENTITY);
        elevationNode.setLocalRotation(Quaternion.IDENTITY);
        elevationNode.setLocalRotation(Quaternion.IDENTITY);
        this.set(FastMath.DEG_TO_RAD * 10, FastMath.DEG_TO_RAD * 10);
        updateCamState();
    }

    public void elevate(float angle) {
        //if (camNode.getWorldTranslation().z < 0 || angle > 0) {
            Quaternion rotation = new Quaternion();
            rotation.fromAngleAxis(-angle, Vector3f.UNIT_X);
            elevationNode.rotate(rotation);
            updateCamState();
        //}
    }

    public void azimate(float angle) {
        // System.out.println("Azimating");
        Quaternion rotation = new Quaternion();
        rotation.fromAngleAxis(angle, Vector3f.UNIT_Z);
        this.rotate(rotation);
        updateCamState();
    }

    public void set(float elevation_rad, float azimuth_rad) {
        Quaternion qElev = new Quaternion();
        qElev.fromAngleAxis(elevation_rad, Vector3f.UNIT_X.negate());
        elevationNode.setLocalRotation(qElev);

        Quaternion qAz = new Quaternion();
        qAz.fromAngleAxis(azimuth_rad, Vector3f.UNIT_Z);
        this.setLocalRotation(qAz);
        updateCamState();
    }

    public float getDistance() {
        return camNode.getLocalTranslation().length();
    }

    public void approachTarget(float byMeters) {
        //if (camNode.getWorldTranslation().z < 0 || byMeters < 0) {
            float distanceToTarget = upside.UAVMath.saturate((camNode.getLocalTranslation().length() + byMeters), 1f, 20000);
            zoomSpeed = 10 * distanceToTarget;
            camNode.setLocalTranslation(camNode.getLocalTranslation().normalize().mult(distanceToTarget));
            updateCamState();
        //}
    }

    public void updateCamState() {
        cam.setLocation(camNode.getWorldTranslation());
        // cam.setRotation(camNode.getWorldRotation());
        cam.lookAt(this.getWorldTranslation(), Vector3f.UNIT_Z.mult(-1));        
    }
}
