package upside;

public class StateChangeEvent{
    public State state;
    public StateChangeEvent(State newState){
        this.state = newState;
    }
}