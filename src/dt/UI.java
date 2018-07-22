package dt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 *
 * @author Lee Glendenning
 */
public class UI implements ActionListener{
    
    private JFrame frame;
    private JMenuBar menuBar;
    private JMenu fileMenu, editMenu, viewMenu;
    private JMenuItem loadPointMenuItem, savePointMenuItem, loadQuadMenuItem, saveQuadMenuItem;
    private JMenuItem newQuadMenuItem, deletePointMenuItem;
    private JMenuItem showDTMenuItem, showVDMenuItem;
    private JMenu showVDMenu;
    private JMenuItem showB2SMenuItem, showOnlyChosenB2SMenuItem, showB3SMenuItem, showOnlyChosenB3SMenuItem, showB3SFGMenuItem; // sub-menu items for showVD
    
    private Quadrilateral quad;
    private ArrayList<Point> pointSet;
    
    public UI(Quadrilateral q, ArrayList<Point> pts) {
        
        this.quad = q;
        this.pointSet = pts;
        createFrame(new VoronoiDiagram(q, pts));
    }
    
    private void createFrame(VoronoiDiagram vd) {
        // Set up display window
        this.frame = new JFrame("Voronoi Diagram");
        
        addMenuBar(this.frame);
        
        this.frame.setSize(800, 700);
        this.frame.setResizable(false);
        this.frame.setLocation(375, 25);
        this.frame.getContentPane().setBackground(Color.BLACK);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container contentPane = this.frame.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(vd, BorderLayout.CENTER);
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
            case "Load Point Set":
                
                break;
            case "Save Point Set":
                
                break;
            case "Load Quadrilateral":
                
                break;
            case "Save Quadrilateral":
                
                break;
            case "Remove Point":
                
                break;
            case "New Quadrilateral":
                
                break;
            case "Delaunay Triangulation":
                
                break;
            case "Voronoi Diagram":
                
                break;
            case "Show Bisectors 2 Sites":
                
                break;
            case "Only Show Chosen Bisectors 2 Sites":
                
                break;
            case "Show Bisectors 3 Sites":
                
                break;
            case "Only Show Chosen Bisectors 3 Sites":
                
                break;
            case "Show FG For Bisectors 3 Sites":
                
                break;
        }
    }
    
    private void addMenuBar(JFrame frame) {
        menuBar = new JMenuBar();
   
        createFileMenu();
        createEditMenu();
        createViewMenu();
        
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
        
        deletePointMenuItem = new JMenuItem("Remove Point");
        newQuadMenuItem = new JMenuItem("New Quadrilateral");
        
        deletePointMenuItem.addActionListener(this);
        newQuadMenuItem.addActionListener(this);
        
        editMenu.add(deletePointMenuItem);
        editMenu.add(newQuadMenuItem);
        
        menuBar.add(editMenu);
    }
    
    /**
     * Create View menu for showing either Delaunay Triangulation or Voronoi Diagram and various other related things
     */
    private void createViewMenu() {
        viewMenu = new JMenu("View");
        
        showDTMenuItem = new JMenuItem("Delaunay Triangulation");
        showVDMenu = new JMenu("Voronoi Diagram");
        
        showDTMenuItem.addActionListener(this);
        //showVDMenuItem.addActionListener(this);
        
        viewMenu.add(showDTMenuItem);
        viewMenu.add(showVDMenu);
        
        menuBar.add(viewMenu);
        
        createVDSubMenu();
    }
    
    /**
     * Create sub menu for Voronoi Diagram menu item under View menu for showing various bisectors
     */
    private void createVDSubMenu() {
        showVDMenuItem = new JMenuItem("Voronoi Diagram");
        showB2SMenuItem = new JMenuItem("Show Bisectors 2 Sites");
        showOnlyChosenB2SMenuItem = new JMenuItem("Only Show Chosen Bisectors 2 Sites");
        showB3SMenuItem = new JMenuItem("Show Bisectors 3 Sites");
        showOnlyChosenB3SMenuItem = new JMenuItem("Only Show Chosen Bisectors 3 Sites");
        showB3SFGMenuItem = new JMenuItem("Show FG For Bisectors 3 Sites");
        
        showVDMenuItem.addActionListener(this);
        showB2SMenuItem.addActionListener(this);
        showOnlyChosenB2SMenuItem.addActionListener(this);
        showB3SMenuItem.addActionListener(this);
        showOnlyChosenB3SMenuItem.addActionListener(this);
        showB3SFGMenuItem.addActionListener(this);
        
        showVDMenu.add(showVDMenuItem);
        showVDMenu.add(showB2SMenuItem);
        showVDMenu.add(showOnlyChosenB2SMenuItem);
        showVDMenu.add(showB3SMenuItem);
        showVDMenu.add(showOnlyChosenB3SMenuItem);
        showVDMenu.add(showB3SFGMenuItem);
    }
    
}
