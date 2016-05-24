package upside.IO;

import java.io.*;
import java.net.*;
import java.util.HashMap;

public class UDPServerThread extends Thread {

    protected DatagramSocket socket = null;
    protected BufferedReader in = null;
    // Holds the latest state messages:
    protected HashMap<Integer, AircraftStateMessage> inboxes = new HashMap<Integer, AircraftStateMessage>(10);
    private boolean receptionEnabled = true;
    boolean keepMoving = true;

    public UDPServerThread(int port) throws IOException {
	this("ServerThread");
        socket = new DatagramSocket(port);
    }

    public UDPServerThread(String name) throws IOException {
        super(name);
    }

    public void run() {
        byte[] buf = new byte[14*4];
        while(keepMoving){
        if(this.receptionEnabled){
            try {
                // Receive packet
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.setSoTimeout(100);
                try{
                    socket.receive(packet);
                    handleUDPPacket(packet);
                }catch(java.net.SocketTimeoutException e){
                    
                }
            } catch (IOException e) {
                e.printStackTrace();
		keepMoving = false;
            }
        }
    }
        socket.close();
        System.out.println("UDP Server thread: shutting down");
    }
    
    public void end(){
        this.keepMoving = false;
    }

    private void handleUDPPacket(DatagramPacket p){
        AircraftStateMessage msg = new AircraftStateMessage();
        msg.unpack(p.getData());
        inboxes.put(msg.id, msg);
    }

    public  HashMap<Integer, AircraftStateMessage> getInboxes() {
        return inboxes;
    }
    
}
