//Overall Main class: 30 marks ***********************************************
//Correctness (10 marks) ********************************
import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class Main {
	/**
     * Extracts 8x8 grayscale patches from an image.
     * @param image Input BufferedImage.
     * @return PositionList of 8x8 patches.
     */
    public static PositionList<Patch> extractPatches(BufferedImage image) {
        PositionList<Patch> patches = new PositionList<>();
        for (int y = 0; y <= image.getHeight() - 8; y += 8) {
            for (int x = 0; x <= image.getWidth() - 8; x += 8) {
                double[][] patchData = new double[8][8];
                for (int dy = 0; dy < 8; dy++) {
                    for (int dx = 0; dx < 8; dx++) {
                        int rgb = image.getRGB(x + dx, y + dy);
                        int gray = ((rgb >> 16) & 0xff + (rgb >> 8) & 0xff + rgb & 0xff) / 3;
                        patchData[dy][dx] = gray;
                    }
                }
                patches.addLast(new Patch(patchData, x, y));
            }
        }
        return patches;
    }

    /**
     * Reconstructs an image from a list of patches.
     * @param patches PositionList of selected patches.
     * @param width Width of the final image.
     * @param height Height of the final image.
     * @return Reconstructed BufferedImage.
     * 10 marks ***********************************************
     */
    public static BufferedImage renderScene(PositionList<Patch> patches, int width, int height) {
    	
    	BufferedImage Result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (Position<Patch> pos = patches.first(); pos != null; pos = patches.next(pos)) {
            Patch patch = pos.element();
            if (patch != null) {
                int x = patch.getX();
                int y = patch.getY();
                double[][] data = patch.getData();

                for (int dy = 0; dy < 8; dy++) {
                    for (int dx = 0; dx < 8; dx++) {
                        int gray = (int) data[dy][dx];
                        int rgb = (gray << 16) | (gray << 8) | gray;
                        Result.setRGB(x + dx, y + dy, rgb);
                    }
                }
            }
        }

        return Result;
    }
    
    public static long computeHash(double[][] patchData) 
    {
    	
    	//my helper for computing the average hash in the  8x8 patch
        double total = 0;
        for (double[] row : patchData) {
            for (double val : row) {
                total += val;
            }
        }
        double avg = total / 64.0;

        long hash = 0L;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                hash <<= 1;
                if (patchData[i][j] >= avg) {
                    hash |= 1;
                }
            }
        }
        return hash;
    }


	public static void main(String[] args) throws IOException {
		// Load multiple images from the scene
        File[] imageFiles = new File("scenes/").listFiles((dir, name) -> name.endsWith(".jpg"));
        
        PositionList<Patch> allPatches = new PositionList<>();

        for (File file : imageFiles) {
            BufferedImage img = ImageIO.read(file);
            PositionList<Patch> patches = extractPatches(img);
            for (Position<Patch> pos = patches.first(); pos != null; pos = patches.next(pos)) {
                allPatches.addLast(pos.element());
            }
        }
         
        // Heap insertion  - Use the custom Heap class with key = Hamming distance and value = Patch
        //5 marks ***********************************************
        //TODO: Complete
        
        

        // Select top patches
        //5 marks ***********************************************
        Heap<Long, Patch> patchHeap = new Heap<>();
        for (Position<Patch> pos = allPatches.first(); pos != null; pos = allPatches.next(pos)) {
            Patch patch = pos.element();
            /*long hash = computeAHash(patch.getData());
            patchHeap.insert(hash, patch);*/

            if(patch != null)
            {
            	long hash = computeHash(patch.getData());
            	 patchHeap.insert(hash, patch);
            }
        }
        
        PositionList<Patch> bestPatches = new PositionList<>();
        int patchCount = 100;
        //TODO: Complete
        for (int i = 0; i < patchCount && !patchHeap.isEmpty(); i++) {
            Entry<Long, Patch> entry = patchHeap.removeMin();
            
            //checking for exceptions when adding the entry
            if (entry != null && entry.getValue() != null) {
                bestPatches.addLast(entry.getValue());
            }
        }
        //TODO: Complete

     // Reconstruct and save image
        BufferedImage result = renderScene(bestPatches, 800, 800); // assume 800x800 output
        File output = new File("completed_scene.png");
        if (ImageIO.write(result, "png", output)) {
            System.out.println("Image saved successfully to: " + output.getAbsolutePath());
        } else {
            System.out.println("Failed to save image.");
        }
    }
}