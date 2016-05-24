/*
 * Captures the state of an aircraft displayed on screen.
 */
package upside.IO;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import upside.State;

/**
 *
 * @author j.bolting
 */
public class AircraftStateMessage {

    public int id = 0;
    ;
    public Date date = new Date();
    public float[] p_NED_m = new float[3];
    public float[] quaternion_NED2body = new float[4];
    // Control surface deflections
    public float da, de, dr, den, df;
    public State.Model3D visual;

    public AircraftStateMessage() {
    }

    public AircraftStateMessage(int id,
            float x_NED_m, float y_NED_m, float z_NED_m,
            float qw, float qx, float qy, float qz,
            float da, float de, float dr, float den, float df,
            State.Model3D visual) {
        this.p_NED_m[0] = x_NED_m;
        this.p_NED_m[1] = y_NED_m;
        this.p_NED_m[2] = z_NED_m;
        this.quaternion_NED2body[0] = qw;
        this.quaternion_NED2body[1] = qx;
        this.quaternion_NED2body[2] = qy;
        this.quaternion_NED2body[3] = qz;
        this.id = id;
        this.date = new Date();

        this.da = da;
        this.de = de;
        this.dr = dr;
        this.den = den;
        this.df = df;
        this.visual = visual;
    }

    @Override
    public String toString() {
        return "AircraftStateMessage{" + "id=" + id + ", date=" + date + ", p_NED_m=" + p_NED_m + ", quaternion_NED2body=" + quaternion_NED2body + ", da=" + da + ", de=" + de + ", dr=" + dr + ", den=" + den + ", df=" + df + ", visual=" + visual + '}';
    }

    public ByteBuffer pack() {
        ByteBuffer buffer = ByteBuffer.allocate(14 * 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putFloat(this.id);
        buffer.putFloat(this.p_NED_m[0]);
        buffer.putFloat(this.p_NED_m[1]);
        buffer.putFloat(this.p_NED_m[2]);
        buffer.putFloat(this.quaternion_NED2body[0]);
        buffer.putFloat(this.quaternion_NED2body[1]);
        buffer.putFloat(this.quaternion_NED2body[2]);
        buffer.putFloat(this.quaternion_NED2body[3]);
        buffer.putFloat(this.da);
        buffer.putFloat(this.de);
        buffer.putFloat(this.dr);
        buffer.putFloat(this.den);
        buffer.putFloat(this.df);
        buffer.putFloat((float) this.visual.ordinal());
        return buffer;
    }

    public void unpack(byte[] buffer) {
        ByteBuffer bbuffer = ByteBuffer.wrap(buffer);
        bbuffer.order(ByteOrder.LITTLE_ENDIAN);

        try {
            AircraftStateMessage msg = new AircraftStateMessage((int) bbuffer.getFloat(), // ID
                    bbuffer.getFloat(), bbuffer.getFloat(), bbuffer.getFloat(), // NED position
                    bbuffer.getFloat(), bbuffer.getFloat(), bbuffer.getFloat(), bbuffer.getFloat(), // Attitude quaternion
                    bbuffer.getFloat(), bbuffer.getFloat(), bbuffer.getFloat(), bbuffer.getFloat(), bbuffer.getFloat(), // Control surface deflections
                    State.Model3D.values()[(int) bbuffer.getFloat()]); // 3D model
            this.p_NED_m[0] = msg.p_NED_m[0];
            this.p_NED_m[1] = msg.p_NED_m[1];
            this.p_NED_m[2] = msg.p_NED_m[2];
            this.quaternion_NED2body[0] = msg.quaternion_NED2body[0];
            this.quaternion_NED2body[1] = msg.quaternion_NED2body[1];
            this.quaternion_NED2body[2] = msg.quaternion_NED2body[2];
            this.quaternion_NED2body[3] = msg.quaternion_NED2body[3];
            this.id = msg.id;
            this.date = new Date();

            this.da = msg.da;
            this.de = msg.de;
            this.dr = msg.dr;
            this.den = msg.den;
            this.df = msg.df;
            this.visual = msg.visual;
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            System.out.println(e);
        }
    }
}
