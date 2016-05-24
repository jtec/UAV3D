package upside;

import java.util.HashMap;

/**
 *
 * @author j.bolting
 * Captures the jmonkey thread's state, such as how many aircraft are there etc. 
 * The state is displayed and modified by the UI thread.
 */
public class State {
    public HashMap<Integer, ACNode> aircraft = new HashMap<Integer, ACNode>(10);
    public int viewTargetID = 0;
    public enum ViewModes{
        MAPVIEW,
        VIEW3D,
        CHASEVIEW,
        FORMATIONVIEW
    }
    public enum Model3D{
        ZAGI,
        SAILPLANE,
        DLG,
        QUAD,
        BALL,
        CLOUD,
        CUBE
    }
    
    public ViewModes viewmode;
    public Model3D model;
    public boolean leaveTracks;
    public boolean isRecording;
    
    public State(){
        this.viewTargetID = 1;
        viewmode = ViewModes.VIEW3D;
        leaveTracks = false;
        isRecording = false;
    }
}
