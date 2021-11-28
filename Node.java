public class Node {
    
    private Node parent;
    private Node[] children = new Node[4];
    private int alpha = 0;
    private int red = 0;
    private int blue = 0;
    private int green = 0;
    private int[][] bounds = new int[2][2];

    public Node(Node parent, int[][] bounds){
        this.parent = parent;
        this.bounds = bounds;
        this.alpha = 0;
        this.red = 0;
        this.blue = 0;
        this.green = 0;
    }

    public Node(Node parent, int[] bounds, int rgb){
        this.parent = parent;
        this.bounds[0] = bounds;
        this.bounds[1] = bounds;
        this.setRGB(rgb);
    }

    public Node getParent(){
        return this.parent;
    }

    public Node[] getChildren(){
        return this.children;
    }

    public int[][] getBounds(){
        return this.bounds;
    }
    
    public int getAlpha(){
        return this.alpha;
    }

    public int getRed(){
        return this.red;
    }

    public int getBlue(){
        return this.blue;
    }

    public int getGreen(){
        return this.green;
    }
    public void setRGB(int rgb){
        //https://dyclassroom.com/image-processing-project/how-to-get-and-set-pixel-value-in-java
        //learned about bitwise AND from a friend along with other bitwise operations
        this.alpha = (rgb >> 24) & 0xff;
        this.red = (rgb >> 16) & 0xff;
        this.green = (rgb >> 8) & 0xff;
        this.blue = rgb & 0xff;
    }

    public int getRGB(){
        int rgb = (this.alpha << 24) | (this.red << 16) | (this.green << 8) | this.blue;
        return rgb;
    }

    public String printRGB() {
        return (this.red + " " + this.green + " " + this.blue);
    }

    public void setChild(Node child, int index){
        this.children[index] = child;
    }

    public void setToAvgChildColor(){
        //add clause to only average for the number of non transparent pixels

        double tracker = 0;
        double r = 0;
        double g = 0;
        double b = 0;

        for (Node child : children) {
            int alpha = child.getAlpha();
            tracker += alpha;
            this.alpha += alpha;
            r += gammaExpansion(child.getRed()) * alpha;
            g += gammaExpansion(child.getGreen()) * alpha;
            b += gammaExpansion(child.getBlue()) * alpha;
        }
        if (tracker > 0) {
            this.alpha = (int)(tracker / 4.0);
            r /= tracker;
            g /= tracker;
            b /= tracker;

            this.red = gammaCompression(r);
            this.green = gammaCompression(g);
            this.blue = gammaCompression(b);
        }
    }

    //https://youtu.be/LKnqECcg6Gw Gamma Correction
    //https://en.wikipedia.org/wiki/SRGB sRGB
    public int gammaCompression(double input){
        if(input <= 0.0031308){
            return (int) (255 * 25 * input / 323.0);
        }
        return (int) (255 * (211 * Math.pow(input, 5.0 / 12.0) - 11) / 200);
    }

    public double gammaExpansion(int color){
        double input = color / 255.0;
        if (input <= 0.04045) {
            return (25 * input / 323.0);
        }
        return Math.pow(((double)(200 * input + 11) / 211.0), 12.0 / 5.0);
    }
}
