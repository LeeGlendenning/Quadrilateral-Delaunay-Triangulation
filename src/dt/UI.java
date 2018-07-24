package dt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 *
 * @author Lee Glendenning
 */
public class UI implements ActionListener{
    
    private JFrame frame;
    private JMenuBar menuBar;
    private JMenu fileMenu, editMenu, viewMenu, vdMenu, dtMenu;
    private JMenuItem clearScreenMenuItem, loadPointMenuItem, savePointMenuItem, loadQuadMenuItem, saveQuadMenuItem;
    private JMenuItem newQuadMenuItem, deletePointMenuItem;
    private JCheckBoxMenuItem showDTMenuItem, showVDMenuItem, showCoordsMenuItem;
    private JCheckBoxMenuItem showB2SMenuItem, showOnlyChosenB2SMenuItem, showB3SMenuItem, showOnlyChosenB3SMenuItem, showB3SFGMenuItem; // sub-menu items for showVD
    
    private VoronoiDiagram voronoiDiagram;
    private Quadrilateral quad;
    
    public UI(Quadrilateral q, ArrayList<Point> pointSet) {
        this.quad = q;
        this.voronoiDiagram = new VoronoiDiagram(this.quad, pointSet);
        createFrame();
    }
    
    private void createFrame() {
        // Set up display window
        this.frame = new JFrame("Voronoi Diagram");
        
        // Mouse listener for handling adding points when mouse clicks
        this.voronoiDiagram.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                //System.out.println(e.getX() + "," + e.getY());
                addVoronoiPoint(e.getX(), e.getY());
            }
        });
        
        // Mouse listener for showing mouse coordinates
        this.voronoiDiagram.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent me)
            {
                displayCoordinates(me.getX(), me.getY());
            }
        });
        
        addMenuBar();
        
        this.frame.setSize(800, 700);
        this.frame.setResizable(false);
        this.frame.setLocation(375, 25);
        this.frame.getContentPane().setBackground(Color.BLACK);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container contentPane = this.frame.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this.voronoiDiagram, BorderLayout.CENTER);
        this.frame.setPreferredSize(new Dimension(800, 700));
        this.frame.setLocationRelativeTo(null);
        this.frame.pack();
        this.frame.setVisible(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent ev)
    {
        System.out.println(ev.getActionCommand());
        switch(ev.getActionCommand()) {
            // File menu
            case "Load Point Set":
                
                break;
            case "Save Point Set":
                
                break;
            case "Load Quadrilateral":
                
                break;
            case "Save Quadrilateral":
                
                break;
            // Edit menu
            case "Clear Screen":
                this.voronoiDiagram.reset();
                break;
            case "Remove Point":
                String rmPt = JOptionPane.showInputDialog(frame.getContentPane(), "Expecting \"x,y\"");
                try {
                    this.voronoiDiagram.removePoint(new Point(Double.parseDouble(rmPt.split(",")[0]), Double.parseDouble(rmPt.split(",")[1])));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid format for point removal. Expecting \"x,y\"");
                }
                break;
            case "New Quadrilateral":
                
                break;
            // View menu
            case "Delaunay Triangulation":
                showVDMenuItem.setState(false);
                break;
            case "Voronoi Diagram":
                showDTMenuItem.setState(false);
                break;
            case "Show Coordinates":
                this.voronoiDiagram.setShowCoordinates(showCoordsMenuItem.getState());
                break;
            // Voronoi Diagram menu
            case "Show Bisectors 2 Sites":
                this.voronoiDiagram.setShowB2S(this.showB2SMenuItem.getState());
                break;
            case "Only Show Chosen Bisectors 2 Sites":
                this.voronoiDiagram.setOnlyShowChosenB2S(this.showOnlyChosenB2SMenuItem.getState());
                break;
            case "Show Bisectors 3 Sites":
                this.voronoiDiagram.setShowB3S(this.showB3SMenuItem.getState());
                break;
            case "Only Show Chosen Bisectors 3 Sites":
                this.voronoiDiagram.setOnlyShowChosenB3S(this.showOnlyChosenB3SMenuItem.getState());
                break;
            /*case "Show FG For Bisectors 3 Sites":
                this.voronoiDiagram.setShowFG(this.showB3SFGMenuItem.getState());
                break;*/
        }
    }
    
    private void addVoronoiPoint(int x, int y) {
        //System.out.println("Adding point (" + x + ", " + (this.voronoiDiagram.getBounds().getSize().height - y) + ")");
        this.voronoiDiagram.addPoint(new Point(x, this.voronoiDiagram.getBounds().getSize().height - y));
    }
    
    private void displayCoordinates(int x, int y) {
        this.voronoiDiagram.setMouseCoordinates(x, y);
    }
    
    /**
     * Add menu bar to the JFrame
     */
    private void addMenuBar() {
        menuBar = new JMenuBar();
   
        createFileMenu();
        createEditMenu();
        createViewMenu();
        createVDMenu();
        createDTMenu();
        
        // Add menubar to JFrame
        frame.setJMenuBar(menuBar);
    }
    
    /**
     * Create File menu for opening and saving point sets and quadrilaterals
     */
    private void createFileMenu() {
        fileMenu = new JMenu("File");
        
        loadPointMenuItem = new JMenuItem("Load Point Set");
        savePointMenuItem = new JMenuItem("Save Point Set");
        loadQuadMenuItem = new JMenuItem("Load Quadrilateral");
        saveQuadMenuItem = new JMenuItem("Save Quadrilateral");
        
        loadPointMenuItem.addActionListener(this);
        savePointMenuItem.addActionListener(this);
        loadQuadMenuItem.addActionListener(this);
        saveQuadMenuItem.addActionListener(this);
        
        fileMenu.add(loadPointMenuItem);
        fileMenu.add(savePointMenuItem);
        fileMenu.add(loadQuadMenuItem);
        fileMenu.add(saveQuadMenuItem);
        
        menuBar.add(fileMenu);
    }
    
    /**
     * Create Edit menu to remove points or create a new quadrilateral
     */
    private void createEditMenu() {
        editMenu = new JMenu("Edit");
        
        clearScreenMenuItem = new JMenuItem("Clear Screen");
        deletePointMenuItem = new JMenuItem("Remove Point");
        newQuadMenuItem = new JMenuItem("New Quadrilateral");
        
        clearScreenMenuItem.addActionListener(this);
        deletePointMenuItem.addActionListener(this);
        newQuadMenuItem.addActionListener(this);
        
        editMenu.add(clearScreenMenuItem);
        editMenu.add(deletePointMenuItem);
        editMenu.add(newQuadMenuItem);
        
        menuBar.add(editMenu);
    }
    
    /**
     * Create View menu for showing either Delaunay Triangulation or Voronoi Diagram and various other related things
     */
    private void createViewMenu() {
        viewMenu = new JMenu("View");
        
        showDTMenuItem = new JCheckBoxMenuItem("Delaunay Triangulation");
        showVDMenuItem = new JCheckBoxMenuItem("Voronoi Diagram");
        showVDMenuItem.setState(true);
        showCoordsMenuItem = new JCheckBoxMenuItem("Show Coordinates");
        showCoordsMenuItem.setState(true);
        
        showDTMenuItem.addActionListener(this);
        showVDMenuItem.addActionListener(this);
        showCoordsMenuItem.addActionListener(this);
        
        viewMenu.add(showDTMenuItem);
        viewMenu.add(showVDMenuItem);
        viewMenu.add(showCoordsMenuItem);
        
        menuBar.add(viewMenu);
    }
    
    /**
     * Create sub menu for Voronoi Diagram menu item under View menu for showing various bisectors
     */
    private void createVDMenu() {
        vdMenu = new JMenu("Voronoi Diagram");
        
        showB2SMenuItem = new JCheckBoxMenuItem("Show Bisectors 2 Sites");
        showB2SMenuItem.setState(true);
        showOnlyChosenB2SMenuItem = new JCheckBoxMenuItem("Only Show Chosen Bisectors 2 Sites");
        showB3SMenuItem = new JCheckBoxMenuItem("Show Bisectors 3 Sites");
        showB3SMenuItem.setState(true);
        showOnlyChosenB3SMenuItem = new JCheckBoxMenuItem("Only Show Chosen Bisectors 3 Sites");
        showOnlyChosenB3SMenuItem.setState(true);
        //showB3SFGMenuItem = new JCheckBoxMenuItem("Show FG For Bisectors 3 Sites");
        
        showB2SMenuItem.addActionListener(this);
        showOnlyChosenB2SMenuItem.addActionListener(this);
        showB3SMenuItem.addActionListener(this);
        showOnlyChosenB3SMenuItem.addActionListener(this);
        //showB3SFGMenuItem.addActionListener(this);
        
        //vdMenu.add(showVDMenuItem);
        vdMenu.add(showB2SMenuItem);
        vdMenu.add(showOnlyChosenB2SMenuItem);
        vdMenu.add(showB3SMenuItem);
        vdMenu.add(showOnlyChosenB3SMenuItem);
        //showVDMenu.add(showB3SFGMenuItem);
        
        menuBar.add(vdMenu);
    }
    
    private void createDTMenu() {
        dtMenu = new JMenu("Delaunay Triangulation");
        
        menuBar.add(dtMenu);
    }
    
}
