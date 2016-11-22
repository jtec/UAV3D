package upside.IO;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Date;
import upside.State;

public class SimulationUDPServerThread extends Thread {

    protected DatagramSocket socket = null;
    protected BufferedReader in = null;
    boolean keepMoving = true;
    Date t0 = new Date();
    protected volatile boolean doSend = false;

    public SimulationUDPServerThread(int port) throws IOException {
        this("SimulationUDPServerThread");
        socket = new DatagramSocket();
    }

    public SimulationUDPServerThread(String name) throws IOException {
        super(name);
    }

    @Override
    public void run() {
        Date tMessage = new Date();
        while (keepMoving) {
            if(doSend){
            // Send fake aircraft state messages at 30Hz:
            Date now = new Date();
            float dt_s = ((float)(now.getTime() - tMessage.getTime())) / 1000f;
            if (dt_s > (long)(1f / 30f)) {
                tMessage = new Date();
                try {
                    //Compose packet:
                    DatagramPacket packet = composePacket();
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                    keepMoving = false;
                }
            }
        }
        }
        socket.close();
    }

    public void end() {
        this.keepMoving = false;
    }

    private DatagramPacket composePacket() {
        // The simulated aircraft circles (0, 0, -50) at a speed of 15 m/s:
        Date now = new Date();
        final float r = 50;
        final float v = 15;
        float omega = v/r;
        float dt_s = ((float)(now.getTime() - t0.getTime())) / 1000f;
        float x = r * FastMath.cos(dt_s * omega);
        float y = r * FastMath.sin(dt_s * omega);
        float z = -50f;
        // Align heading to flight path:
        float psi = FastMath.atan2(y, x);
        Quaternion q = new Quaternion();
        q = q.fromAngleAxis(psi+FastMath.PI/2, Vector3f.UNIT_Z);
        // Coordinated turn
        float phi = FastMath.asin(omega*v / 9.81f);
        Quaternion qPhi = new Quaternion();
        qPhi = qPhi.fromAngleAxis(phi, Vector3f.UNIT_X);
        q = q.mult(qPhi);
        
        AircraftStateMessage msg = new AircraftStateMessage((int) 20,  // ID
                x, y, -50f,                                             // NED position
                q.getW(), q.getX(), q.getY(), q.getZ(),                 // Attitude quaternion
                0f, 0f, 0f, 0.5f, 0f,                                      // Control surface deflections
                upside.State.Model3D.MAVION
                );
        ByteBuffer buffer = msg.pack();

        InetAddress address = null;
        try {
            address = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
        }
        int port = 4445;
        DatagramPacket dgp = new DatagramPacket(buffer.array(), buffer.array().length, address, port);
        return dgp;
    }
}
