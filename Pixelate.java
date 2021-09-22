import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Pixelate
 */
public class Pixelate extends JFrame{

    String[] fileTypes = {"png", "gif", "jpg", "jpeg", "bmp"};
    Node quadTree;
    BufferedImage image;

    public Pixelate() {

        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("File");

        JMenuItem open = new JMenuItem("Open File...");
        JMenuItem save = new JMenuItem("Save");
        JMenuItem saveAs = new JMenuItem("Save As...");
        JMenuItem pixelate = new JMenuItem("Pixelate");
        JMenuItem dePixelate = new JMenuItem("De-Pixelate");

        mb.add(file);
        mb.add(pixelate);
        mb.add(dePixelate);
        file.add(open);
        file.add(save);
        file.add(saveAs);

        this.add(mb, BorderLayout.NORTH);

        setSize(400, 400);;
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //getting the user to choose an image
                JFileChooser fileChooser = new JFileChooser();
                int i = fileChooser.showOpenDialog(null);
                
                // when the user chooses a file
                if (i == JFileChooser.APPROVE_OPTION){
                    File picture = fileChooser.getSelectedFile();
                    String picturePath = picture.getPath();
                    //https://stackoverflow.com/questions/14833008/java-string-split-with-dot
                    String[] extension = picturePath.split("\\.", -1);

                    //check that the files extension is that of an image type
                    try {
                        boolean match = false;
                        for (String type : fileTypes) {
                            if (extension[extension.length - 1].equals(type)){
                                match = true;
                            }
                        }
                        if(match == false){
                            throw new EOFException();
                        }
                        //https://alvinalexander.com/blog/post/java/open-read-image-file-java-imageio-class/
                        image = ImageIO.read(picture);
                        quadTree = new Node(null, new int[][]{{0,0}, {image.getWidth(), image.getHeight()}});

                        long pixelCount = image.getWidth() * image.getHeight();
                        int treeHeight = (int)(Math.ceil(logBaseN(pixelCount, 4)));
                        quadTree = constructQuadTree(quadTree);

                        
                        
                    } 
                    catch (EOFException err) {
                        System.err.println("Error: File is not an image");
                    }
                    catch (IOException err){
                        System.err.println("Error: File was not read properly");
                    }
                } 
                

            }
        });
    }

    public Node constructQuadTree(Node parent){ 
        // bounds is the boundaries of the current node, e.g. for the original picture it would be the top left corner with bounds[0] being the x, y of the top left
        // and the x, y of the bottom right corner being bounds[1]
        int[][] bounds = parent.getBounds();
        int[] halfBounds = new int[]{ (int) (Math.ceil(bounds[1][0] + bounds[0][0]) / 2), (int) Math.ceil((bounds[1][1] + bounds[0][1]) / 2) };
        Node[] children = parent.getChildren();

        System.err.println(bounds[0][0] + " " + bounds[0][1] + "\t" + bounds[1][0] + " " + bounds[1][1]);

        //if we arrive at a quad of size 2 by 2 or smaller
        if(((bounds[0][0]) + 1) == bounds[1][0] || ((bounds[0][1]) + 1) == bounds[1][1]){

            parent.setChild(new Node(parent, bounds[0], image.getRGB(bounds[0][0], bounds[0][1])), 0);
            parent.setChild(new Node(parent, new int[]{bounds[0][0], bounds[1][1]}, image.getRGB(bounds[0][0], bounds[1][1])), 1);
            parent.setChild(new Node(parent, new int[]{bounds[1][0], bounds[0][1]}, image.getRGB(bounds[1][0], bounds[0][1])), 2);
            parent.setChild(new Node(parent, bounds[1], image.getRGB(bounds[1][0], bounds[1][1])), 3);
            return parent;
        }

        //the quadrants the children are in if drawn onto a square would be: 0 top left, 1 top right, 2 bottom left, 3 bottom right
        parent.setChild(new Node(parent, new int[][]{bounds[0], halfBounds}), 0);
        parent.setChild(new Node(parent, new int[][]{{halfBounds[0], bounds[0][1]}, {bounds[1][0], halfBounds[1]}}), 1);
        parent.setChild(new Node(parent, new int[][]{{bounds[0][0], halfBounds[1]}, {halfBounds[0], bounds[1][1]}}), 2);
        parent.setChild(new Node(parent, new int[][]{halfBounds, bounds[1]}), 3);

        for (Node child : children) {
            child = constructQuadTree(child);
            System.err.println("\t" + bounds[0][0] + " " + bounds[0][1] + "\t" + bounds[1][0] + " " + bounds[1][1]);
            child.setToAvgChildColor();
        }
        
        return parent;
    }

    public void outputLayer(int layer, int goalLayer, Node node){
        if(layer != goalLayer){
            Node[] children = node.getChildren();
            for (Node child : children) {
                outputLayer(++layer, goalLayer, child);
            }
        }
        else{
            int[][] bounds = node.getBounds();
            int rgb = node.getRGB();
            for (int i = 0; i < bounds.length; i++) {
                for (int j = 0; j < bounds[i].length; j++) {
                    image.setRGB(i, j, rgb);
                }
            }
        }
    }

    //https://www.baeldung.com/java-logarithms
    public double logBaseN(double input, double n){
        return Math.log(input) / Math.log(n);
    }

    public static void main(String[] args) {
        Pixelate pixelate = new Pixelate();
    }
}