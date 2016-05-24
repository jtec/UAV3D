package upside.UI;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.jme3.util.JmeFormatter;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import javax.swing.*;
import upside.State;
import upside.StateChangeCommand;
import upside.StateChangeEvent;
import upside.App;
import java.net.URL;

public class UI {

    private JmeCanvasContext context;
    private Canvas canvas;
    private App app;
    private JFrame frame;
    private JPanel canvasPanel1, canvasPanel2;
    private JPanel currentPanel;
    private JTabbedPane tabbedPane;
    private EventBus eventBus;
    protected JTextArea textArea;
    protected String newline = "\n";
    static final private String PREVIOUS = "previous";
    static final private String UP = "up";
    static final private String NEXT = "next";

    private void createTabs() {
        tabbedPane = new JTabbedPane();

        canvasPanel1 = new JPanel();
        canvasPanel1.setLayout(new BorderLayout());
        
        JToolBar toolBar = new JToolBar("Still draggable");
        addButtons(toolBar);
        //Lay out the main panel.
        canvasPanel1.add(toolBar, BorderLayout.PAGE_START);
 
        tabbedPane.addTab("3D view", canvasPanel1);

        canvasPanel2 = new JPanel();
        canvasPanel2.setLayout(new BorderLayout());
        tabbedPane.addTab("Configuration", canvasPanel2);

        frame.getContentPane().add(tabbedPane);

        currentPanel = canvasPanel1;
    }
    
     protected void addButtons(JToolBar toolBar) {
        JButton buttoni = null;
        
        buttoni = new JButton("Clear All Tracks");
        toolBar.add(buttoni);
        buttoni.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                app.clearTracks();
            }
        });
        
        buttoni = new JButton("Clear All Aircraft");
        toolBar.add(buttoni);
        buttoni.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                app.clearAircraft();
            }
        });
        
        final JButton button = new JButton("Launch built-in Simulation");
        toolBar.add(button);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (button.getText().equals("Launch built-in Simulation")) {
                    app.startSimulation();
                    button.setText("Stop built-in Simulation");
                } else if (button.getText().equals("Stop built-in Simulation")) {
                    app.stopSimulation();
                    button.setText("Launch built-in Simulation");
                }
            }
        });

    }
   
protected JButton makeNavigationButton(String imageName,
                                       String actionCommand,
                                       String toolTipText,
                                       String altText) {
    //Look for the image.
    String imgLocation = "images/"
                         + imageName
                         + ".gif";
    URL imageURL = UI.class.getResource(imgLocation);

    //Create and initialize the button.
    JButton button = new JButton();
    button.setActionCommand(actionCommand);
    button.setToolTipText(toolTipText);
    // button.addActionListener(this);

    if (imageURL != null) {                      //image found
        button.setIcon(new ImageIcon(imageURL, altText));
    } else {                                     //no image found
        button.setText(altText);
        System.err.println("Resource not found: " + imgLocation);
    }

    return button;
}

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu menuTortureMethods = new JMenu("Make Stuff Happen");
        menuBar.add(menuTortureMethods);

        menuBar.add(new ConfigMenu());

        JMenuItem itemExit = new JMenuItem("Exit");
        menuTortureMethods.add(itemExit);
        itemExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                frame.dispose();
                app.stop();
                System.exit(1);
            }
        });
        
        final JMenuItem itemRecord = new JMenuItem("Start recording");
        menuTortureMethods.add(itemRecord);
        itemRecord.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.toString());
                // Change item label:
                State state = app.state;
                state.isRecording = !state.isRecording;
                if(state.isRecording){
                    itemRecord.setText("Stop recording screen capture");
                }else{
                    itemRecord.setText("Start recording screen capture");                    
                }
                eventBus.post(new StateChangeCommand(state));
            }
        });
    }

    private void createFrame() {
        frame = new JFrame("UAV3D");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                app.stop();
                System.exit(1);
            }
        });

        createTabs();
        createMenu();
    }

    public void createCanvas() {
        AppSettings settings = new AppSettings(true);
        settings.setFrameRate(30);
        settings.setWidth(640);
        settings.setHeight(480);
        eventBus = new EventBus();
        eventBus.register(this);

        app = new App(eventBus);

        app.setPauseOnLostFocus(false);
        app.setSettings(settings);
        app.createCanvas();
        app.startCanvas();

        context = (JmeCanvasContext) app.getContext();
        canvas = context.getCanvas();
        canvas.setSize(settings.getWidth(), settings.getHeight());
    }

    public void startApp() {
        app.startCanvas();
        app.enqueue(new Callable<Void>() {
            public Void call() {
                if (app instanceof SimpleApplication) {
                    SimpleApplication simpleApp = (SimpleApplication) app;
                }
                return null;
            }
        });
    }

    public static void main(String[] args) {
        JmeFormatter formatter = new JmeFormatter();
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatter);

        Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
        Logger.getLogger("").addHandler(consoleHandler);
        final UI testcanvas = new UI();
        testcanvas.createCanvas();

        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JPopupMenu.setDefaultLightWeightPopupEnabled(false);

                testcanvas.createFrame();
                testcanvas.currentPanel.add(testcanvas.canvas, BorderLayout.CENTER);

                testcanvas.frame.pack();
                testcanvas.startApp();
                testcanvas.frame.setLocationRelativeTo(null);
                testcanvas.frame.setVisible(true);
            }
        });
    }

    @Subscribe
    public void handleMouseEvent(PopupEvent e) {
    }

    class ConfigMenu extends JMenu implements ActionListener {

        JCheckBoxMenuItem ViewsMenu_3Dview;
        JCheckBoxMenuItem ViewsMenu_2Dview;
        JCheckBoxMenuItem ViewsMenu_Chaseview;
        JMenu ViewsMenu_lookat;
        JMenu ViewsMenu_models;
        ArrayList<JCheckBoxMenuItem> targets = new ArrayList<JCheckBoxMenuItem>(10);
        ArrayList<JCheckBoxMenuItem> models3D = new ArrayList<JCheckBoxMenuItem>(10);
        UASChooserListener uasvchooserlistener = new UASChooserListener();
        ModelChooserListener modelchooserlistener = new ModelChooserListener();
        
        public ConfigMenu() {
            super("Views");
            this.addActionListener(this);
            ViewsMenu_3Dview = new JCheckBoxMenuItem("3D View");
            ViewsMenu_3Dview.addActionListener(this);
            ViewsMenu_3Dview.setActionCommand("3D View");

            ViewsMenu_2Dview = new JCheckBoxMenuItem("2D View");
            ViewsMenu_2Dview.addActionListener(this);
            ViewsMenu_2Dview.setActionCommand("2D View");
            
            ViewsMenu_Chaseview = new JCheckBoxMenuItem("Chase View");
            ViewsMenu_Chaseview.addActionListener(this);
            ViewsMenu_Chaseview.setActionCommand("Chase View");
            
            ViewsMenu_lookat = new JMenu("Look at");
            ViewsMenu_models = new JMenu("UAS 3D models");

            for (int i = 0; i < 10; i++) {
                targets.add(new JCheckBoxMenuItem("UAS " + i));
                ViewsMenu_lookat.add(targets.get(i));
                targets.get(i).setVisible(false);
                targets.get(i).addActionListener(uasvchooserlistener);
            }

            for (int i = 0; i < 10; i++) {
                models3D.add(new JCheckBoxMenuItem("Set 3D model name..."));
                ViewsMenu_models.add(models3D.get(i));
                models3D.get(i).setVisible(false);
                models3D.get(i).addActionListener(modelchooserlistener);
            }

            models3D.get(0).setVisible(true);
            models3D.get(0).setText("Zagi");
            models3D.get(1).setVisible(true);
            models3D.get(1).setText("Sailplane");
            models3D.get(2).setVisible(true);
            models3D.get(2).setText("DLG");

            this.add(ViewsMenu_3Dview);
            this.add(ViewsMenu_2Dview);
            this.add(ViewsMenu_Chaseview);
            this.add(ViewsMenu_lookat);
            //this.add(ViewsMenu_models);

            eventBus.register(this);
        }

        @Subscribe
        public void handleSettingsChangeEvent(StateChangeEvent e) {
            // Update UI
            ViewsMenu_2Dview.setSelected(e.state.viewmode == State.ViewModes.MAPVIEW);
            ViewsMenu_3Dview.setSelected(e.state.viewmode == State.ViewModes.VIEW3D);
            ViewsMenu_Chaseview.setSelected(e.state.viewmode == State.ViewModes.CHASEVIEW);

            models3D.get(0).setSelected(e.state.model == State.Model3D.ZAGI);
            models3D.get(1).setSelected(e.state.model == State.Model3D.SAILPLANE);
            models3D.get(2).setSelected(e.state.model == State.Model3D.DLG);

            // First hide all elements:
            for (int i = 0; i < targets.size(); i++) {
                targets.get(i).setVisible(false);
            }
            // Iterate over aircraft, make appropriate UI elements visible: 
            Iterator<Integer> iter = e.state.aircraft.keySet().iterator();
            int uasCounter = 0;
            int targetID = e.state.viewTargetID;
            while (iter.hasNext()) {
                int id = iter.next();
                targets.get(uasCounter).setVisible(true);
                targets.get(uasCounter).setActionCommand(String.valueOf(id));
                if (id == targetID) {
                    targets.get(uasCounter).setSelected(true);
                } else {
                    targets.get(uasCounter).setSelected(false);
                }
                if (id >= 100) {
                    targets.get(uasCounter).setText("Virtual UAS " + id);
                }
                if (id < 100) {
                    targets.get(uasCounter).setText("UAS " + id);
                }

                if (id >= 200) {
                    targets.get(uasCounter).setText("Built-In Simulated UAS " + id);
                }
                if (id >= 300) {
                    targets.get(uasCounter).setText("Formation head " + id);
                }
                if (id >= 400) {
                    targets.get(uasCounter).setText("Cloud " + id);
                }
                uasCounter++;
            }
        }

        public void actionPerformed(ActionEvent e) {
            // System.out.println(e.toString());
            State setts = app.state;
            if (e.getActionCommand().equalsIgnoreCase("2D View")) {
                // Get and modify 3D view settings:
                setts.viewmode = State.ViewModes.MAPVIEW;
            } else if (e.getActionCommand().equalsIgnoreCase("3D View")) {
                // Get and modify 3D view settings:
                setts.viewmode = State.ViewModes.VIEW3D;
            }else if (e.getActionCommand().equalsIgnoreCase("Chase View")) {
                // Get and modify 3D view settings:
                setts.viewmode = State.ViewModes.CHASEVIEW;
            }
            eventBus.post(new StateChangeCommand(setts));
        }

        private class UASChooserListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                // System.out.println(e.toString());
                int id = Integer.parseInt(e.getActionCommand());
                State state = app.state;
                state.viewTargetID = id;
                eventBus.post(new StateChangeCommand(state));
            }
        }

        private class ModelChooserListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                //System.out.println(e.toString());
                State setts = app.state;

                if (e.getActionCommand().equalsIgnoreCase("Zagi")) {
                    // Get and modify 3D model choice view:
                    setts.model = State.Model3D.ZAGI;
                } else if (e.getActionCommand().equalsIgnoreCase("Sailplane")) {
                    // Get and modify 3D model choice view:
                    setts.model = State.Model3D.SAILPLANE;
                } else if (e.getActionCommand().equalsIgnoreCase("DLG")) {
                    // Get and modify 3D model choice view:
                    setts.model = State.Model3D.DLG;
                }
                eventBus.post(new StateChangeCommand(setts));
            }
        }
    }
}
