package markergenerator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import javax.imageio.ImageIO;

/**
 * Marker generator for EMB4 project: Treadmill controller.
 * Creating marker from terminal interface
 *
 * @author Stefan Larsen
 */
public class MarkerGenerator {

	private static final char WHITE_PIXEL = ' ';
	private static final char BLACK_PIXEL = '\u2588';
	private static final char BORDER_PIXEL = '#';

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws IOException {

		//Create scanner reading from terminal
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset()));
		boolean validMarker = false;
		//Marker parameters
		Integer height;
		Integer width;
		Boolean border;
		int[][] markerPattern;
		Integer imgScale = null;
		BufferedImage img = null;

		while (!validMarker) {
			//Marker parameters
			height = null;
			width = null;
			border = null;
			//parser variable 
			String tmp;

			//read and parse marker height
			System.out.print("Enter marker height: ");
			while (height == null) {
				tmp = br.readLine();
				try {
					height = Integer.parseInt(tmp);
					if (height < 1) {
						throw new IllegalArgumentException();
					}
				} catch (IllegalArgumentException e) {
					height = null;
					System.out.print("Height must be > 0. \nTry again: ");
				} catch (Exception e) {
					height = null;
					System.out.print("Could Not parse input. \nTry again: ");
				}
			}

			//read and parse marker width
			System.out.print("Enter marker width: ");
			while (width == null) {
				tmp = br.readLine();
				try {
					width = Integer.parseInt(tmp);
					if (width < 1) {
						throw new IllegalArgumentException();
					}
				} catch (IllegalArgumentException e) {
					width = null;
					System.out.print("Width must be > 0. \nTry again: ");
				} catch (Exception e) {
					width = null;
					System.out.print("Could Not parse input. \nTry again: ");
				}
			}

			//read and parse marker border
			System.out.print("Enter border settings [y,n]: ");
			while (border == null) {
				tmp = br.readLine();
				border = parseBooleanString(tmp);
				if (border == null) {
					System.out.print("Could Not parse input. \nTry again: ");
				}
			}

			//read and parse marker pattern
			markerPattern = /*(border) ? new int[height+2][width+2] :*/ new int[height][width];
			boolean validLength = false;
			System.out.println("Enter marker pattern (zero for black pixel, none-zero for white): ");
			for (int y = 0; y < height; y++) {
				while (!validLength) {
					System.out.print("Enter line " + (y + 1) + ": ");
					tmp = br.readLine();
					if (tmp.length() == width) {
						validLength = true;
						for (int x = 0; x < width; x++) {
							markerPattern[y][x] = (tmp.charAt(x) != '0') ? 1 : 0;
						}
					} else {
						System.out.print("Could Not parse input.\n");
					}
				}
				validLength = false;
			}

			//read and parse image scale factor
			System.out.print("Enter image scale factor: ");
			while (imgScale == null) {
				tmp = br.readLine();
				try {
					imgScale = Integer.parseInt(tmp);
					if (imgScale < 1) {
						throw new IllegalArgumentException();
					}
				} catch (IllegalArgumentException e) {
					imgScale = null;
					System.out.print("Scale factor must be > 0. \nTry again: ");
				} catch (Exception e) {
					imgScale = null;
					System.out.print("Could Not parse input. \nTry again: ");
				}
			}

			//create image
			img = (border)
							? new BufferedImage((width + 2) * imgScale, (height + 2) * imgScale, BufferedImage.TYPE_INT_ARGB)
							: new BufferedImage(width * imgScale, height * imgScale, BufferedImage.TYPE_INT_ARGB);
			Graphics2D imgGraphics = img.createGraphics();
			imgGraphics.setColor(Color.BLACK);
			imgGraphics.fillRect(0, 0, img.getWidth(), img.getHeight());
			imgGraphics.setColor(Color.WHITE);
			//crate char array for printing marker pattern
			char[][] markerChars = new char[(height + 2)][(width + 2)];
			for (int i = 0; i < markerChars.length; i++) {
				Arrays.fill(markerChars[i], (border) ? BLACK_PIXEL : BORDER_PIXEL);
			}
			//draw marker pattern
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					markerChars[y + 1][x + 1] = (markerPattern[y][x] == 0) ? BLACK_PIXEL : WHITE_PIXEL;
					if (markerPattern[y][x] != 0) {
						if (border) {
							imgGraphics.fillRect((x + 1) * imgScale, (y + 1) * imgScale, imgScale, imgScale);
						} else {
							imgGraphics.fillRect(x * imgScale, y * imgScale, imgScale, imgScale);
						}
					}
				}
			}

			//Print marker settings
			System.out.println("Marker Parameters:");
			System.out.println("Hegiht: " + height);
			System.out.println("Width: " + width);
			System.out.println("Border: " + ((border) ? "yes" : "no"));
			System.out.println("Image Scale: " + imgScale);
			System.out.println("Marker pattern:");
			StringBuilder pattern = new StringBuilder();
			for (int y = 0; y < markerChars.length; y++) {
				pattern.append("  ");
				for (int x = 0; x < markerChars[y].length; x++) {
					pattern.append(markerChars[y][x]);
				}
				pattern.append("\n");
			}
			System.out.print(pattern);

			//Read acceptance of marker pattern
			System.out.print("Generate image file from marker settings [y,n]: ");
			Boolean markerAcceptance = null;
			while (markerAcceptance == null) {
				tmp = br.readLine();
				markerAcceptance = parseBooleanString(tmp);
				if (markerAcceptance == null) {
					System.out.print("Could Not parse input. \nTry again: ");
				}
			}
			validMarker = markerAcceptance;
		}

		//writing marker to png file
		System.out.println("Generating marker image file");
		File outputfile = new File("marker.png");
		ImageIO.write(img, "png", outputfile);
		System.out.println("Done!");
	}

	private static Boolean parseBooleanString(String s) {
		Boolean result = null;
		if (s.equalsIgnoreCase("y") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true")) {
			result = Boolean.TRUE;
		} else if (s.equalsIgnoreCase("n") || s.equalsIgnoreCase("no") || s.equalsIgnoreCase("false")) {
			result = Boolean.FALSE;
		}
		return result;
	}
}
