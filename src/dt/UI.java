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
    private JMenuItem clearScreenMenuItem, loadVertexMenuItem, saveVertexMenuItem, loadQuadMenuItem, saveQuadMenuItem;
    private JMenuItem newQuadMenuItem, deleteVertexMenuItem;
    private JMenuItem shortestPathMenuItem;
    private JCheckBoxMenuItem highlightPathMenuItem;
    private JCheckBoxMenuItem showDTMenuItem, showVDMenuItem, showCoordsMenuItem;
    private JCheckBoxMenuItem showB2SMenuItem, showOnlyChosenB2SMenuItem, showB3SMenuItem, showOnlyChosenB3SMenuItem, showB3SFGMenuItem; // sub-menu items for showVD
    
    private final DelaunayTriangulation delaunayTriangulation;
    
    public UI(Quadrilateral q, ArrayList<Vertex> vertexSet) {
        this.delaunayTriangulation = new DelaunayTriangulation(q, vertexSet);
        createFrame();
    }
    
    private void createFrame() {
        // Set up display window
        this.frame = new JFrame("Voronoi Diagram");
        
        // Mouse listener for handling adding vertexs when mouse clicks
        this.delaunayTriangulation.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                //Utility.debugPrintln(e.getX() + "," + e.getY());
                addVoronoiVertex(e.getX(), e.getY());
            }
        });
        
        // Mouse listener for showing mouse coordinates
        this.delaunayTriangulation.addMouseMotionListener(new MouseMotionAdapter() {
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
        contentPane.add(this.delaunayTriangulation, BorderLayout.CENTER);
        this.frame.setPreferredSize(new Dimension(800, 700));
        this.frame.setLocationRelativeTo(null);
        this.frame.pack();
        this.frame.setVisible(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent ev)
    {
        //Utility.debugPrintln(ev.getActionCommand());
        switch(ev.getActionCommand()) {
            // File menu
            case "Load Vertex Set":
                loadVertexSet();
                break;
            case "Save Vertex Set":
                try {
                    saveVertexSet();
                } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                    Utility.debugPrintln("Something went wrong while saving vertex set");
                }
                break;
            case "Load Quadrilateral":
                loadQuadrilateral();
                break;
            case "Save Quadrilateral":
                try{
                    saveQuadrilateral();
                } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                    Utility.debugPrintln("Something went wrong while saving Quadrilateral");
                }
                break;
            // Edit menu
            case "Clear Screen":
                this.delaunayTriangulation.reset();
                break;
            case "Remove Vertex":
                removeVertex();
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
                this.delaunayTriangulation.setShowCoordinates(showCoordsMenuItem.getState());
                break;
            // Voronoi Diagram menu
            case "Show Bisectors 2 Sites":
                this.delaunayTriangulation.setShowB2S(this.showB2SMenuItem.getState());
                break;
            case "Only Show Chosen Bisectors 2 Sites":
                this.delaunayTriangulation.setOnlyShowChosenB2S(this.showOnlyChosenB2SMenuItem.getState());
                break;
            case "Show Bisectors 3 Sites":
                this.delaunayTriangulation.setShowB3S(this.showB3SMenuItem.getState());
                break;
            case "Only Show Chosen Bisectors 3 Sites":
                this.delaunayTriangulation.setOnlyShowChosenB3S(this.showOnlyChosenB3SMenuItem.getState());
                break;
            /*case "Show FG For Bisectors 3 Sites":
                this.delaunayTriangulation.setShowFG(this.showB3SFGMenuItem.getState());
                break;*/
            // Delaunay Triangulation Menu
            case "Find Shortest Path":
                getShortestPath();
                break;
            case "Highlight Shortest Path":
                this.delaunayTriangulation.setHighlightShortestPath(this.highlightPathMenuItem.getState());
                break;
        }
    }
    
    /**
     * Allow user to load vertex set from a file containing vertexs and reconstruct VD
     */
    private void loadVertexSet() {
        JFileChooser jfcLoadPts = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                
        if (jfcLoadPts.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfcLoadPts.getSelectedFile();
            Utility.debugPrintln(selectedFile.getAbsolutePath());
            try {
                loadVertexSetFile(selectedFile);
            } catch (FileNotFoundException ex) {
                Utility.debugPrintln("File not found");
            }
        }
    }
    
    /**
     * Load new vertex set and reconstruct Voronoi Diagram
     * @param file File containing new vertex set
     * @throws FileNotFoundException Thrown if file not found
     */
    private void loadVertexSetFile(File file) throws FileNotFoundException {
        try (Scanner input = new Scanner(file)) {
            List<Vertex> newVertexSet = new ArrayList();
            while(input.hasNext()) {
                String nextLine = input.nextLine();
                try {
                    newVertexSet.add(new Vertex(Double.parseDouble(nextLine.split(",")[0]), Double.parseDouble(nextLine.split(",")[1])));
                } catch(NumberFormatException e) {
                    Utility.debugPrintln("Vertex set file not correct format.");
                    return;
                }
            }
            this.delaunayTriangulation.newVertexSet(newVertexSet);
        }
    }
    
    /**
     * Allow user to save the vertex set currently on screen to a file
     */
    private void saveVertexSet() throws FileNotFoundException, UnsupportedEncodingException {
        JFileChooser jfcSavePts = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                
        if (jfcSavePts.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfcSavePts.getSelectedFile();
            Utility.debugPrintln(selectedFile.getAbsolutePath());
            
            saveVertexSetFile(selectedFile);
        }
    }
    
    /**
     * Write the vertex set file
     * @param file File to write vertex set to
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException 
     */
    private void saveVertexSetFile(File file) throws FileNotFoundException, UnsupportedEncodingException {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            for (Vertex p : this.delaunayTriangulation.getVertices()) {
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
            Utility.debugPrintln(selectedFile.getAbsolutePath());
            saveQuadrilateralFile(selectedFile);
        }
    }
    
    private void saveQuadrilateralFile(File file) throws FileNotFoundException, UnsupportedEncodingException {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            for (Vertex p : this.delaunayTriangulation.quad.getVertices()) {
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
            Utility.debugPrintln(selectedFile.getAbsolutePath());
            try {
                loadQuadFile(selectedFile);
            } catch (FileNotFoundException ex) {
                Utility.debugPrintln("File not found");
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
            Vertex[] newQuad = new Vertex[4];
            int i = 0;
            while(i < 4) {
                String nextLine = input.nextLine();
                try {
                    newQuad[i] = new Vertex(Double.parseDouble(nextLine.split(",")[0]), Double.parseDouble(nextLine.split(",")[1]));
                } catch(NumberFormatException e) {
                    Utility.debugPrintln("Vertex set file not correct format.");
                    return;
                }
                i ++;
            }
            this.delaunayTriangulation.newQuad(newQuad);
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
                this.delaunayTriangulation.newQuad(new Vertex[]{new Vertex(Double.parseDouble(newQuadFieldx1.getText()), Double.parseDouble(newQuadFieldy1.getText())),
                        new Vertex(Double.parseDouble(newQuadFieldx2.getText()), Double.parseDouble(newQuadFieldy2.getText())),
                        new Vertex(Double.parseDouble(newQuadFieldx3.getText()), Double.parseDouble(newQuadFieldy3.getText())),
                        new Vertex(Double.parseDouble(newQuadFieldx4.getText()), Double.parseDouble(newQuadFieldy4.getText()))});
            } catch (NumberFormatException e) {
                Utility.debugPrintln("Invalid format for new quad vertex. X and Y coordinates must be numbers.");
            }
        } else if (newQuadResult == JOptionPane.OK_OPTION ) {
            Utility.debugPrintln("All fields must not be empty.");
        }
    }
    
    /**
     * Allow user to specify a vertex in the VD to remove and reconstruct VD
     */
    private void removeVertex() {
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
                this.delaunayTriangulation.removeVertex(new Vertex(Double.parseDouble(xField.getText()), Double.parseDouble(yField.getText())));
            } catch (NumberFormatException e) {
                Utility.debugPrintln("Invalid vertex format. X and Y coordinates must be numbers.");
            }
        }
    }
    
    private void addVoronoiVertex(int x, int y) {
        //Utility.debugPrintln("Adding vertex (" + x + ", " + (this.delaunayTriangulation.getBounds().getSize().height - y) + ")");
        this.delaunayTriangulation.addVertex(new Vertex(x, this.delaunayTriangulation.getBounds().getSize().height - y));
    }
    
    private void displayCoordinates(int x, int y) {
        this.delaunayTriangulation.setMouseCoordinates(x, y);
    }
    
    /**
     * Allow user to choose two vertices. The minimum length path between them is then displayed
     */
    private void getShortestPath() {
        JTextField v1Field = new JTextField(5);
        JTextField v2Field = new JTextField(5);

        JPanel getPathPanel = new JPanel();
        getPathPanel.add(new JLabel("V1:"));
        getPathPanel.add(v1Field);
        getPathPanel.add(Box.createHorizontalStrut(15));
        getPathPanel.add(new JLabel("V2:"));
        getPathPanel.add(v2Field);

        int getPathResult = JOptionPane.showConfirmDialog(null, getPathPanel, 
                 "Enter vertex indices", JOptionPane.OK_CANCEL_OPTION);
        if (getPathResult == JOptionPane.OK_OPTION) {
            try {
                String s = "Path: ";
                for (Vertex v : this.delaunayTriangulation.shortestPath(Integer.parseInt(v1Field.getText()), Integer.parseInt(v2Field.getText()))) {
                    s += v + " ";
                }
                s += "\nPath Length: " + this.delaunayTriangulation.getShortestPathLength(Integer.parseInt(v1Field.getText()), Integer.parseInt(v2Field.getText()));
                JOptionPane.showMessageDialog(null, s, "Minimum path V" + v1Field.getText() + " and V" + v2Field.getText(), JOptionPane.PLAIN_MESSAGE);
            } catch (NumberFormatException e) {
                Utility.debugPrintln("Invalid vertex index. Index must be an integer.");
            }
        }
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
     * Create File menu for opening and saving vertex sets and quadrilaterals
     */
    private void createFileMenu() {
        fileMenu = new JMenu("File");
        
        loadVertexMenuItem = new JMenuItem("Load Vertex Set");
        saveVertexMenuItem = new JMenuItem("Save Vertex Set");
        loadQuadMenuItem = new JMenuItem("Load Quadrilateral");
        saveQuadMenuItem = new JMenuItem("Save Quadrilateral");
        
        loadVertexMenuItem.addActionListener(this);
        saveVertexMenuItem.addActionListener(this);
        loadQuadMenuItem.addActionListener(this);
        saveQuadMenuItem.addActionListener(this);
        
        fileMenu.add(loadVertexMenuItem);
        fileMenu.add(saveVertexMenuItem);
        fileMenu.add(loadQuadMenuItem);
        fileMenu.add(saveQuadMenuItem);
        
        menuBar.add(fileMenu);
    }
    
    /**
     * Create Edit menu to remove vertexs or create a new quadrilateral
     */
    private void createEditMenu() {
        editMenu = new JMenu("Edit");
        
        clearScreenMenuItem = new JMenuItem("Clear Screen");
        deleteVertexMenuItem = new JMenuItem("Remove Vertex");
        newQuadMenuItem = new JMenuItem("New Quadrilateral");
        
        clearScreenMenuItem.addActionListener(this);
        deleteVertexMenuItem.addActionListener(this);
        newQuadMenuItem.addActionListener(this);
        
        editMenu.add(clearScreenMenuItem);
        editMenu.add(deleteVertexMenuItem);
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
        showB2SMenuItem.setState(this.delaunayTriangulation.getShowB2S());
        showOnlyChosenB2SMenuItem = new JCheckBoxMenuItem("Only Show Chosen Bisectors 2 Sites");
        showB3SMenuItem = new JCheckBoxMenuItem("Show Bisectors 3 Sites");
        showB3SMenuItem.setState(this.delaunayTriangulation.getShowB3S());
        showOnlyChosenB3SMenuItem = new JCheckBoxMenuItem("Only Show Chosen Bisectors 3 Sites");
        showOnlyChosenB3SMenuItem.setState(!this.delaunayTriangulation.getShowB3SHidden());
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
        
        shortestPathMenuItem = new JMenuItem("Find Shortest Path");
        highlightPathMenuItem = new JCheckBoxMenuItem("Highlight Shortest Path");
        highlightPathMenuItem.setState(this.delaunayTriangulation.getHighlightShortestPath());
                
        shortestPathMenuItem.addActionListener(this);
        highlightPathMenuItem.addActionListener(this);
        
        dtMenu.add(shortestPathMenuItem);
        dtMenu.add(highlightPathMenuItem);
        
        menuBar.add(dtMenu);
    }
    
}
