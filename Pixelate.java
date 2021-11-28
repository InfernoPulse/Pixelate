import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

/**
 * Pixelate
 */
public class Pixelate{

    Node quadTree;
    BufferedImage image;
    String[] fileTypes = {"png", "gif", "jpg", "jpeg", "bmp"};

    public Pixelate(String path, int depth) {

        //check that the files extension is that of an image type
        try {
            // when the user chooses a file
            String[] splitFile = path.split("\\.|/|\\\\");
            //checking file is of the right type
            boolean match = false;
            for (String type : fileTypes) {
                if (splitFile[splitFile.length - 1].equals(type)){
                    match = true;
                }
            }
            if(match == false){
                throw new EOFException();
            }
            File folder = new File("./" + splitFile[splitFile.length - 2] + "/");
            File picture = new File(path);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File picturePixel = new File("./" + splitFile[splitFile.length - 2] + "/_depth" + depth + ".png");
            //https://alvinalexander.com/blog/post/java/open-read-image-file-java-imageio-class/
            image = ImageIO.read(picture);
            long pixelCount = image.getWidth() * image.getHeight();
            int treeHeight = (int)(Math.ceil(logBaseN(pixelCount, 4)));
            if (treeHeight < depth || depth < 0) {
                System.err.println("Error: depth out of bounds for image, depth must be between/at " + treeHeight + " and 0");
                throw new Exception();
            }
            quadTree = new Node(null, new int[][]{{0,0}, {image.getWidth() - 1, image.getHeight() - 1}});
            quadTree = constructQuadTree(quadTree);
            quadTree = colourQuadTree(quadTree, image);

            //get average colour for each layer
            //output bottom layer of quad tree
            //implement pixelate and depixelate functionality
            // printTree(quadTree, 0);
            outputLayer(0, treeHeight - depth, quadTree);
            ImageIO.write(image, "png", picturePixel);    
        } 
        catch (EOFException err) {
            System.err.println("Error: File is not an image");
        }
        catch (IOException err){
            System.err.println("Error: File was not read properly");
        }
        catch (Exception e){
            e.getStackTrace();
            System.err.println("ERROR " + e.getMessage());
        }
        
    }

    public Node constructQuadTree(Node parent){ 
        // bounds is the boundaries of the current node, e.g. for the original picture it would be the top left corner with bounds[0] being the x, y of the top left
        // and the x, y of the bottom right corner being bounds[1]
        int[][] bounds = parent.getBounds();
        // where the midpoint of the current bounds is
        int[] halfBounds = new int[]{ 
            (int) Math.ceil((bounds[1][0] + bounds[0][0]) / 2),
            (int) Math.ceil((bounds[1][1] + bounds[0][1]) / 2) 
        };
        Node[] children = parent.getChildren();

        // System.err.println(bounds[0][0] + " " + bounds[0][1] + "\t" + bounds[1][0] + " " + bounds[1][1]);

        //if we arrive at a quad of size 2 by 2 or smaller
        if(Math.abs((bounds[0][0] - bounds[1][0])) <= 1 || Math.abs((bounds[0][1] - bounds[1][1])) <= 1){
            parent.setChild(new Node(parent, bounds[0], image.getRGB(bounds[0][0], bounds[0][1])), 0);
            parent.setChild(new Node(parent, new int[]{bounds[0][0], bounds[1][1]}, image.getRGB(bounds[0][0], bounds[1][1])), 1);
            parent.setChild(new Node(parent, new int[]{bounds[1][0], bounds[0][1]}, image.getRGB(bounds[1][0], bounds[0][1])), 2);
            parent.setChild(new Node(parent, bounds[1], image.getRGB(bounds[1][0], bounds[1][1])), 3);
            return parent;
        }

        //the quadrants the children are in if drawn onto a square would be: 0 top left, 1 top right, 2 bottom left, 3 bottom right
        parent.setChild(new Node(parent, new int[][]{
            bounds[0], 
            halfBounds}), 
        0);
        parent.setChild(new Node(parent, new int[][]{
            {halfBounds[0], bounds[0][1]}, 
            {bounds[1][0], halfBounds[1]}}), 
        1);
        parent.setChild(new Node(parent, new int[][]{
            {bounds[0][0], halfBounds[1]}, 
            {halfBounds[0], bounds[1][1]}}), 
        2);
        parent.setChild(new Node(parent, new int[][]{
            halfBounds, 
            bounds[1]}), 
        3);

        for (Node child : children) {
            child = constructQuadTree(child);
        }
        
        return parent;
    }

    public Node colourQuadTree(Node parent, BufferedImage image) {
        Node[] children = parent.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i] != null) {
                children[i] = colourQuadTree(children[i], image);
                parent.setToAvgChildColor();
            }
            else {
                int[][] pixel = parent.getBounds();
                int rgb = image.getRGB(pixel[0][0], pixel[0][1]);
                parent.setRGB(rgb);
                // System.out.println(pixel[0][0] + " " + pixel[0][1] + ": " + rgb);
                // System.out.println(parent.getRGB());
            }
        }        
        return parent;
    }

    public void printTree(Node parent, int depth) {
        Node[] children = parent.getChildren();
        int[][] bounds = parent.getBounds();
        if (children[0] != null) {
            for (Node node : children) {
                printTree(node, depth + 1);
            }
        }
        for (int i = 0; i < depth; i++) {
            System.out.print("\t");
        }
        int[] wh = {bounds[0][0] - bounds[1][0], bounds[0][1] - bounds[1][1]};
        System.err.println(depth + " " + bounds[0][0] + "," + bounds[0][1] + " " + bounds[1][0] + "," + bounds[1][1] + " " + wh[0] + "," + wh[1] + " " + parent.printRGB());
    }

    public void outputLayer(int layer, int goalLayer, Node node){
        if(layer != goalLayer){
            Node[] children = node.getChildren();
            for (Node child : children) {
                if (child != null)
                    outputLayer(layer + 1, goalLayer, child);
            }
        }
        else{
            int[][] bounds = node.getBounds();
            int rgb = node.getRGB();
            for (int i = bounds[0][0]; i <= bounds[1][0]; i++) {
                for (int j = bounds[0][1]; j <= bounds[1][1]; j++) {
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
        Pixelate pixelate = new Pixelate(args[0], Integer.parseInt(args[1]));
        // Pixelate pixelate = new Pixelate("./test.png", 5);
    }
}