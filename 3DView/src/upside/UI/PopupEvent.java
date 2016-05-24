package upside.UI;

import java.awt.Point;

/**
 *
 * @author j.bolting
 */
public class PopupEvent {
    Point xy_onscreen;
    public enum Flags{
    RISE, 
    HIDE
    }
    Flags flag;
    
    public PopupEvent(Point xy, PopupEvent.Flags flag){
        xy_onscreen = xy;
        this.flag = flag;
    }
}
