import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * This class provides functionality to apply a mean filter to an image.
 * The mean filter is used to smooth images by averaging the pixel values
 * in a neighborhood defined by a kernel size.
 * 
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * ImageMeanFilter.applyMeanFilter("input.jpg", "output.jpg", 3);
 * }
 * </pre>
 * 
 * <p>Supported image formats: JPG, PNG</p>
 * 
 * <p>Author: temmanuel@comptuacao.ufcg.edu.br</p>
 */
public class ImageMeanFilter {
    /**
     * Applies mean filter to an image
     * 
     * @param inputPath  Path to input image
     * @param outputPath Path to output image 
     * @param kernelSize Size of mean kernel
     * @throws IOException If there is an error reading/writing
     */
    public static void applyMeanFilter(String inputPath, String outputPath, int kernelSize, int threadAmount) throws IOException {
        // Load image
        BufferedImage originalImage = ImageIO.read(new File(inputPath));
        
        Thread[] threads = new Thread[threadAmount];
        
        // Create result image
        BufferedImage filteredImage = new BufferedImage(
            originalImage.getWidth(), 
            originalImage.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );
        
        // Image processing
        int height = originalImage.getHeight();

        for (int i = 0; i < threadAmount; i++) {
            if (i <= height) {
                ImageMeanFilterThread imageMeanFilterThread = new ImageMeanFilterThread(originalImage, filteredImage, i, kernelSize, height, threadAmount);
                Thread thread = new Thread(imageMeanFilterThread);
                threads[i] = thread;
                thread.start();
            }
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        
        // Save filtered image
        ImageIO.write(filteredImage, "jpg", new File(outputPath));
    }
    
    /**
     * Calculates average colors in a pixel's neighborhood
     * 
     * @param image      Source image
     * @param centerX    X coordinate of center pixel
     * @param centerY    Y coordinate of center pixel
     * @param kernelSize Kernel size
     * @return Array with R, G, B averages
     */
    private static int[] calculateNeighborhoodAverage(BufferedImage image, int centerX, int centerY, int kernelSize) {
        int width = image.getWidth();
        int height = image.getHeight();
        int pad = kernelSize / 2;
        
        // Arrays for color sums
        long redSum = 0, greenSum = 0, blueSum = 0;
        int pixelCount = 0;
        
        // Process neighborhood
        for (int dy = -pad; dy <= pad; dy++) {
            for (int dx = -pad; dx <= pad; dx++) {
                int x = centerX + dx;
                int y = centerY + dy;
                
                // Check image bounds
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    // Get pixel color
                    int rgb = image.getRGB(x, y);
                    
                    // Extract color components
                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;
                    
                    // Sum colors
                    redSum += red;
                    greenSum += green;
                    blueSum += blue;
                    pixelCount++;
                }
            }
        }
        
        // Calculate average
        return new int[] {
            (int)(redSum / pixelCount),
            (int)(greenSum / pixelCount),
            (int)(blueSum / pixelCount)
        };
    }
    
    /**
     * Main method for demonstration
     * 
     * Usage: java ImageMeanFilter <input_file>
     * 
     * Arguments:
     *   input_file - Path to the input image file to be processed
     *                Supported formats: JPG, PNG
     * 
     * Example:
     *   java ImageMeanFilter input.jpg
     * 
     * The program will generate a filtered output image named "filtered_output.jpg"
     * using a 7x7 mean filter kernel
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java ImageMeanFilter <input_file> <thread_amount>");
            System.exit(1);
        }

        String inputFile = args[0];
        Integer threadAmount = Integer.parseInt(args[1]);

        try {
            applyMeanFilter(inputFile, "filtered_output.jpg", 7, threadAmount);
        } catch (IOException e) {
            System.err.println("Error processing image: " + e.getMessage());
        }
    }

    static class ImageMeanFilterThread implements Runnable {

        private BufferedImage originalImage;
        private BufferedImage filteredImage;
        private int y;
        private int kernelSize;
        private int height;
        private int threadAmount;

        public ImageMeanFilterThread(BufferedImage originalImage, BufferedImage filteredImage, int y, int kernelSize, int height, int threadAmount) {
            this.originalImage = originalImage;
            this.filteredImage = filteredImage;
            this.y = y;
            this.kernelSize = kernelSize;
            this.height = height;
            this.threadAmount = threadAmount;
        }

        @Override
        public void run() {
            applyMeanFilterToLine(originalImage, filteredImage, y, kernelSize);
        }

        private void applyMeanFilterToLine(BufferedImage originalImage, BufferedImage filteredImage, int y, int kernelSize) {
            int width = originalImage.getWidth();
    
            for (int line = y; line < height; line += threadAmount) {
                for (int x = 0; x < width; x++) {
                    // Calculate neighborhood average
                    int[] avgColor = calculateNeighborhoodAverage(originalImage, x, line, kernelSize);
                    
                    // Set filtered pixel
                    filteredImage.setRGB(x, line, 
                        (avgColor[0] << 16) | 
                        (avgColor[1] << 8)  | 
                        avgColor[2]
                    );
                }
            }
        }
    }
}