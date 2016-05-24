package upside;

public class StateChangeCommand{
    public State state;
    public StateChangeCommand(State newState){
        this.state = newState;
    }
}