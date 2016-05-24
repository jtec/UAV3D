package upside.IO;

import java.io.IOException;

/**
 * UDP Interface e.g. to receive data from a flight simulator.
 * @author j.bolting
 */
public class UDPServer {
    public UDPServerThread serverthread;
    public SimulationUDPServerThread simulationserverthread;
    
    
    public void launchServerthread(int port) {
        try{
            serverthread = new UDPServerThread(port);
            serverthread.start();
            
            simulationserverthread = new SimulationUDPServerThread(port);
            simulationserverthread.start();
        }catch(IOException ioe){
            System.err.println("Could not start UDP server thread (port already in use?), shutting down");
            System.err.println(ioe.getMessage());
            System.exit(-1);
        }
    }
    
    
    public void stop(){
        this.serverthread.end();
    }
    
    public void enableSimulation(){
        this.simulationserverthread.doSend = true;
    }
    
    public void disableSimulation(){
        this.simulationserverthread.doSend = false;
    }
}
