package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;

/**
 * Reads a BSH file containing graphics from Anno 1602.
 * 
 * @author Dan Bryce
 */
public class AnnoReader {

    private static final String OUTPUT_DIR = "out";

    private static final String PALETTE_FILE = "TOOLGFX/STADTFLD.COL";

    private final String annoDir;
    
    private int[] palette = new int[256];

    /**
     * Creates an AnnoReader for the given Anno 1602 directory.
     * 
     * @param annoDir
     */
    public AnnoReader(String annoDir) {
        this.annoDir = annoDir;
        readPalette();
    }
    
    /**
     * Reads the game's palette.
     */
    private void readPalette() {
        
        System.out.println("Reading palette");
        
        File paletteFile = new File(annoDir + "/" + PALETTE_FILE);
        ByteBuffer buf = readBytes(paletteFile);
        
        // Skip 20 byte header
        for (int i = 0; i < 20; i++) {
            buf.get();            
        }
        
        for (int i = 0; i < palette.length; i++) {
            
            int r = unsignedByte(buf.get());
            int g = unsignedByte(buf.get());
            int b = unsignedByte(buf.get());
            
            buf.get(); // Skip 1 byte
            
            palette[i] = argbToInt(255, r, g, b);
        }
    }
    
    /**
     * Reads the images from the given BSH File and saves them in PNG format.
     * 
     * @param inputFile
     */
    private void read(File inputFile) {

        ByteBuffer buf = readBytes(inputFile);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        
        final int HEADER_LENGTH = 20;
        
        // Skip unknown bytes
        // 12 byte header: 42 53 48 00 A8 11 41 00 40 00 00 00
        // 8 unknown bytes
        buf.position(buf.position() + HEADER_LENGTH);
        
        // Load the offset of the first image
        int firstoffset = buf.getInt() + HEADER_LENGTH;
        
        // List with all image offsets
        List<Integer> offsets = new Vector<Integer>();
        
        offsets.add(firstoffset);
        
        // Load offsets of all images
        for (int offset = buf.getInt(); buf.position() <= firstoffset; offset = buf.getInt()) {
			offsets.add(offset + HEADER_LENGTH);
		}      
        
        // Ensure output directory exists
        new File(OUTPUT_DIR).mkdir();
        
        // Read (and save) each image in the file
        int i = 0;
        
        for (Integer offset : offsets) {
        	buf.position(offset);
        	
            BufferedImage image = readImage(buf);
            
            // Not every image is correct!
            if(image == null) {
            	continue;
            }
            
            String filename = OUTPUT_DIR + "/" +
                    inputFile.getName() + "_" +
                    String.valueOf(i) + ".png";
            saveImage(image, filename);
            i++;
            
            System.out.println("Saved image: " + filename);
            System.out.println(buf.remaining() + " bytes remaining");
        }
    }

    /**
	 * Reads a single image from the given data buffer.
	 * 
	 * @param buf
	 */
    private BufferedImage readImage(ByteBuffer buf) {
        
        int x = 0;
        int y = 0;
    
        // Read width and height
        int width =  buf.getInt();
        int height = buf.getInt();

        // Check if the image has a valid size
        if (width <= 0 || height <= 0) {
        	return null;
        }
        
        BufferedImage image = new BufferedImage(width, height, 
                BufferedImage.TYPE_INT_ARGB);
        
        // Skip 8 unknown bytes
        buf.position(buf.position() + 8);
        
        // Read until we reach the end marker
        while (true){
                       
            int numAlpha = unsignedByte(buf.get());

            // End marker
            if (numAlpha == 255){
                break;
            }

            // End of row
            if (numAlpha == 254){
                x = 0;
                y++;
                continue;
            }
                
            // Pixel data
            for (int i = 0; i < numAlpha; i++){
                image.setRGB(x, y, 0);
                x++;
            }
            int numPixels = unsignedByte(buf.get());
            for (int i = 0; i < numPixels; i++){
                int colourIndex = unsignedByte(buf.get());
                int colour = palette[colourIndex];
                image.setRGB(x, y, colour);
                x++;
            }
        }

        return image;
    }

    /**
     * Reads the given File's bytes fully.
     * 
     * @param inputFile
     * @return
     */
    private static ByteBuffer readBytes(File inputFile) {
        byte[] bytes = null;
        
        try (RandomAccessFile in = new RandomAccessFile(inputFile, "r")) {
            
            long filesize = in.length();
            bytes = new byte[(int) filesize];
            in.readFully(bytes);
            
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        return ByteBuffer.wrap(bytes);
    }

    /**
     * Converts the given byte to an unsigned value.
     * 
     * @param b
     * @return
     */
    private static int unsignedByte(byte b) {
        return b & 0xff;
    }

    /**
     * Creates an integer from the given ARGB colour values.
     * 
     * Based on java.awt.Color().
     * 
     * @param a
     * @param r
     * @param g
     * @param b
     * @return
     */
    private static int argbToInt(int a, int r, int g, int b) {
        return (a << 24) |
               (r << 16) |
               (g << 8)  |
               (b << 0);
    }

    /**
     * Saves the given image to file.
     * 
     * @param img
     * @param filename
     */
    private static void saveImage(BufferedImage img, String filename) {
        try {
            File file = new File(filename);
            file.createNewFile();
            ImageIO.write(img, "PNG", file);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Entry point for the application.
     * 
     * @param args
     */
    public static void main(String[] args) {        
        
        if (args.length < 2) {
            System.err.println("Expected: ANNO_DIR BSH_FILE");
            System.exit(1);
        }
        
        String annoDir = args[0];
        String filename = args[1];
        
        AnnoReader reader = new AnnoReader(annoDir);
        
        File inputFile = new File(annoDir + "/" + filename);
        if (!inputFile.exists()){
            System.err.println("File not found");
            System.exit(1);
        }

        reader.read(inputFile);
    }
    
}
