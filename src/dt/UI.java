package dt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileSystemView;

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
    
    private final VoronoiDiagram voronoiDiagram;
    
    public UI(Quadrilateral q, ArrayList<Point> pointSet) {
        this.voronoiDiagram = new VoronoiDiagram(q, pointSet);
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
        //System.out.println(ev.getActionCommand());
        switch(ev.getActionCommand()) {
            // File menu
            case "Load Point Set":
                loadPointSet();
                break;
            case "Save Point Set":
                try {
                    savePointSet();
                } catch (FileNotFoundException ex) {
                    System.out.println("Something went wrong while saving point set");
                } catch (UnsupportedEncodingException ex) {
                    System.out.println("Something went wrong while saving point set");
                }
                break;
            case "Load Quadrilateral":
                loadQuadrilateral();
                break;
            case "Save Quadrilateral":
                try{
                    saveQuadrilateral();
                } catch (FileNotFoundException ex) {
                    System.out.println("Something went wrong while saving Quadrilateral");
                } catch (UnsupportedEncodingException ex) {
                    System.out.println("Something went wrong while saving Quadrilateral");
                }
                break;
            // Edit menu
            case "Clear Screen":
                this.voronoiDiagram.reset();
                break;
            case "Remove Point":
                removePoint();
                break;
            case "New Quadrilateral":
                newQuadrilateral();
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
    
    /**
     * Allow user to load point set from a file containing points and reconstruct VD
     */
    private void loadPointSet() {
        JFileChooser jfcLoadPts = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                
        if (jfcLoadPts.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfcLoadPts.getSelectedFile();
            System.out.println(selectedFile.getAbsolutePath());
            try {
                loadPointSetFile(selectedFile);
            } catch (FileNotFoundException ex) {
                System.out.println("File not found");
            }
        }
    }
    
    /**
     * Load new point set and reconstruct Voronoi Diagram
     * @param file File containing new point set
     * @throws FileNotFoundException Thrown if file not found
     */
    private void loadPointSetFile(File file) throws FileNotFoundException {
        try (Scanner input = new Scanner(file)) {
            List<Point> newPointSet = new ArrayList();
            while(input.hasNext()) {
                String nextLine = input.nextLine();
                try {
                    newPointSet.add(new Point(Double.parseDouble(nextLine.split(",")[0]), Double.parseDouble(nextLine.split(",")[1])));
                } catch(NumberFormatException e) {
                    System.out.println("Point set file not correct format.");
                    return;
                }
            }
            this.voronoiDiagram.newPointSet(newPointSet);
        }
    }
    
    /**
     * Allow user to save the point set currently on screen to a file
     */
    private void savePointSet() throws FileNotFoundException, UnsupportedEncodingException {
        JFileChooser jfcSavePts = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                
        if (jfcSavePts.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfcSavePts.getSelectedFile();
            System.out.println(selectedFile.getAbsolutePath());
            
            savePointSetFile(selectedFile);
        }
    }
    
    /**
     * Write the point set file
     * @param file File to write point set to
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException 
     */
    private void savePointSetFile(File file) throws FileNotFoundException, UnsupportedEncodingException {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            for (Point p : this.voronoiDiagram.points) {
                writer.println(p.x + "," + p.y);
            }
        }
    }
    
    /**
     * Allow user to save the current quadrilateral to a file
     */
    private void saveQuadrilateral() throws FileNotFoundException, UnsupportedEncodingException {
        JFileChooser jfcSaveQuad = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                
        if (jfcSaveQuad.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfcSaveQuad.getSelectedFile();
            System.out.println(selectedFile.getAbsolutePath());
            saveQuadrilateralFile(selectedFile);
        }
    }
    
    private void saveQuadrilateralFile(File file) throws FileNotFoundException, UnsupportedEncodingException {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            for (Point p : this.voronoiDiagram.quad.getVertices()) {
                writer.println(p.x + "," + p.y);
            }
        }
    }
    
    /**
     * Allow user to choose a file containing quadrilateral vertices and reconstruct VD
     */
    private void loadQuadrilateral() {
        JFileChooser jfcLoadQuad = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                
        if (jfcLoadQuad.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfcLoadQuad.getSelectedFile();
            System.out.println(selectedFile.getAbsolutePath());
            try {
                loadQuadFile(selectedFile);
            } catch (FileNotFoundException ex) {
                System.out.println("File not found");
            }
        }
    }
    
    /**
     * Load new Quadrilateral and reconstruct Voronoi Diagram
     * @param file File containing new Quadrilateral vertices
     * @throws FileNotFoundException Thrown if file not found
     */
    private void loadQuadFile(File file) throws FileNotFoundException {
        try (Scanner input = new Scanner(file)) {
            Point[] newQuad = new Point[4];
            int i = 0;
            while(i < 4) {
                String nextLine = input.nextLine();
                try {
                    newQuad[i] = new Point(Double.parseDouble(nextLine.split(",")[0]), Double.parseDouble(nextLine.split(",")[1]));
                } catch(NumberFormatException e) {
                    System.out.println("Point set file not correct format.");
                    return;
                }
                i ++;
            }
            this.voronoiDiagram.newQuad(newQuad);
        }
    }
    
    /**
     * Allow user to manually input new quadrilateral vertices and reconstruct VD
     */
    private void newQuadrilateral() {
        JTextField newQuadFieldx1 = new JTextField(5);
        JTextField newQuadFieldx2 = new JTextField(5);
        JTextField newQuadFieldx3 = new JTextField(5);
        JTextField newQuadFieldx4 = new JTextField(5);

        JTextField newQuadFieldy1 = new JTextField(5);
        JTextField newQuadFieldy2 = new JTextField(5);
        JTextField newQuadFieldy3 = new JTextField(5);
        JTextField newQuadFieldy4 = new JTextField(5);

        JPanel newQuadPanel = new JPanel();
        newQuadPanel.setLayout(new GridLayout(4,2));
        newQuadPanel.add(new JLabel("x1:"));
        newQuadPanel.add(newQuadFieldx1);
        newQuadPanel.add(Box.createHorizontalStrut(15));
        newQuadPanel.add(new JLabel("y1:"));
        newQuadPanel.add(newQuadFieldy1);

        newQuadPanel.add(new JLabel("x2:"));
        newQuadPanel.add(newQuadFieldx2);
        newQuadPanel.add(Box.createHorizontalStrut(15));
        newQuadPanel.add(new JLabel("y2:"));
        newQuadPanel.add(newQuadFieldy2);

        newQuadPanel.add(new JLabel("x3:"));
        newQuadPanel.add(newQuadFieldx3);
        newQuadPanel.add(Box.createHorizontalStrut(15));
        newQuadPanel.add(new JLabel("y3:"));
        newQuadPanel.add(newQuadFieldy3);

        newQuadPanel.add(new JLabel("x4:"));
        newQuadPanel.add(newQuadFieldx4);
        newQuadPanel.add(Box.createHorizontalStrut(15));
        newQuadPanel.add(new JLabel("y4:"));
        newQuadPanel.add(newQuadFieldy4);

        int newQuadResult = JOptionPane.showConfirmDialog(null, newQuadPanel, 
                 "Enter Vertices in Clockwise Order", JOptionPane.OK_CANCEL_OPTION);
        if (newQuadResult == JOptionPane.OK_OPTION &&
                !newQuadFieldx1.getText().isEmpty() && !newQuadFieldy1.getText().isEmpty() &&
                !newQuadFieldx2.getText().isEmpty() && !newQuadFieldy2.getText().isEmpty() &&
                !newQuadFieldx3.getText().isEmpty() && !newQuadFieldy3.getText().isEmpty() &&
                !newQuadFieldx4.getText().isEmpty() && !newQuadFieldy4.getText().isEmpty()) {
            try {
                this.voronoiDiagram.newQuad(new Point[]{new Point(Double.parseDouble(newQuadFieldx1.getText()), Double.parseDouble(newQuadFieldy1.getText())),
                        new Point(Double.parseDouble(newQuadFieldx2.getText()), Double.parseDouble(newQuadFieldy2.getText())),
                        new Point(Double.parseDouble(newQuadFieldx3.getText()), Double.parseDouble(newQuadFieldy3.getText())),
                        new Point(Double.parseDouble(newQuadFieldx4.getText()), Double.parseDouble(newQuadFieldy4.getText()))});
            } catch (NumberFormatException e) {
                System.out.println("Invalid format for new quad point. X and Y coordinates must be numbers.");
            }
        } else if (newQuadResult == JOptionPane.OK_OPTION ) {
            System.out.println("All fields must not be empty.");
        }
    }
    
    /**
     * Allow user to specify a point in the VD to remove and reconstruct VD
     */
    private void removePoint() {
        JTextField xField = new JTextField(5);
        JTextField yField = new JTextField(5);

        JPanel rmPtPanel = new JPanel();
        rmPtPanel.add(new JLabel("x:"));
        rmPtPanel.add(xField);
        rmPtPanel.add(Box.createHorizontalStrut(15));
        rmPtPanel.add(new JLabel("y:"));
        rmPtPanel.add(yField);

        int rmPtResult = JOptionPane.showConfirmDialog(null, rmPtPanel, 
                 "Enter X and Y Coordinates", JOptionPane.OK_CANCEL_OPTION);
        if (rmPtResult == JOptionPane.OK_OPTION) {
            try {
                this.voronoiDiagram.removePoint(new Point(Double.parseDouble(xField.getText()), Double.parseDouble(yField.getText())));
            } catch (NumberFormatException e) {
                System.out.println("Invalid point format. X and Y coordinates must be numbers.");
            }
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
