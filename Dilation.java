import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.Scalar;

import java.util.List;
import java.util.ArrayList;

/**
 * Performs dilation transformation of an image. This class loads an image and process the dilation transformation
 * with a specific kernel. If no kernel information is given, a default will be used (a 5x5 square). When the
 * the transformation is finished, the image will be saved in the location of the user preference. 
 * This class is the parent of all the other morphological transformations, since all of them can be derived from this 
 * transformation.
 * This and all derived transformations can be applied to grayscale or color images, where the transformation is applied
 * to the RGB channels.
 *
 * @author Brian Kostadinov Shalon Isaac Medina
 */
public class Dilation {

	protected Mat image;
	protected Mat structuralElement;
	protected Point anchorPoint;
	protected Mat dst;

	private String output;
	private int size;

	public static void main(String[] args) {	
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		Dilation dilation = new Dilation(args);

		System.out.println("Beginning dilation");
		long elapsed = dilation.perform();
		dilation.saveImage();
		System.out.println("Dilation finished. Time elapsed: " + elapsed + " ms");
	}

	/**
	 * Creates a dilation object with a square flat structural element. The anchor point of this transformation will be the center of the structural element.
	 *
	 * @param args the arguments of the main method. This array must have two elements at least, indicating first the filename of the image to process and then the filename of the resulting image.
	 *             An optional third element can be used for giving the size of the structural element. If not, a 5x5 square will be used.
	 */
	public Dilation(String[] args) {
		this(args, null);
	}

	/**
	 * Creates a dilation object with a given structural element. The anchor point of this transformation will be the center of the structural element.
	 *
	 * @param args the arguments of the main method. This array must have two elements at least, indicating first the filename of the image to process and then the filename of the resulting image.
	 *             An optional third element can be used for giving the size of the structural element. This will be only used if the structural element is null.
	 * @param structuralElement a Mat object indicating the structural element to be used. All structural elements in this class are flat, so values different from 0 are taken as 1.
	 *                                If this parameter is null, a 5x5 square full with ones  will be used.
	 */
	public Dilation(String[] args, Mat structuralElement)
	{
		this(args, structuralElement, null);
	}

	/**
	 * Creates a dilation object with a given structural element and an anchor point (the center of the structural element).
	 * If the anchor point is null, the geometric center of the structural element will be used.
	 * If the structural element is null, a 5x5 square will be used.
	 *
	 * @param args the arguments of the main method. This array must have two elements at least, indicating first the filename of the image to process and then the filename of the resulting image.
	 *             An optional third element can be used for giving the size of the structural element. This will be only used if the structural element is null.
	 * @param structuralElement a Mat object indicating the structural element to be used. All structural elements in this class are flat, so values different from 0 are taken as 1.
	 *                                If this parameter is null, a 5x5 square full with ones  will be used.
	 * @param anchorPoint the origin of the structural element. This point will be used as a reference when performing morphological transformations. 
	 *                    If this parameter is null, the anchor point will be the geometrical center of the structural element.
	 */
	public Dilation(String[] args, Mat structuralElement, Point anchorPoint) {
		if(args == null || args.length < 2) {
			System.out.println("You must enter two arguments");
			return;
		}

		String input = args[0];
		output = args[1];

		//If more than two arguments are given, uses the third element as the size of the structural element.
		if(args.length > 2) {
			try {
				size = Integer.parseInt(args[2]);
			} catch(NumberFormatException e) {
				size = 5;
			}
		}
		else {
			size = 5;
		}
		image = Imgcodecs.imread(input);

		//If the structuralElement param is null, uses a 5x5 square.
		if(structuralElement == null) {
			this.structuralElement = Mat.ones(size, size, CvType.CV_8UC1);
		}
		else {
			this.structuralElement = structuralElement;
		}

		//If anchorPoint is null, uses the center of the image.
		if(anchorPoint == null)	    {
			int width = this.structuralElement.cols();
			int height = this.structuralElement.rows();
			this.anchorPoint = new Point((width - 1) / 2, (height - 1) / 2);
		}
		else	    {
			this.anchorPoint = anchorPoint;
		}
		this.dst = null;
	}

	/**
	 * Creates a dilation object with a given structural element and an anchor point (the center of the structural element).
	 * If the anchor point is null, the geometric center of the structural element will be used.
	 * If the structural element is null, a 5x5 square will be used.
	 *
	 * @param src a Mat object containing the image.
	 * @param structuralElement a Mat object indicating the structural element to be used. All structural elements in this class are flat, so values different from 0 are taken as 1.
	 *                                If this parameter is null, a 5x5 square full with ones  will be used.
	 * @param anchorPoint the origin of the structural element. This point will be used as a reference when performing morphological transformations. 
	 *                    If this parameter is null, the anchor point will be the geometrical center of the structural element.
	 */
	public Dilation(Mat src, Mat structuralElement, Point anchorPoint) {
		this.image = src;
		this.structuralElement = structuralElement;
		this.anchorPoint = anchorPoint;
	}

	/**
	 * Gets the image after processing. Since the transform is not performing at the beginning (i.e., it is performed after the use of the perform method), this method can return null.
	 *
	 * @return the image after processing. Null if it has not been processed yet.
	 */
	public Mat getProcessedImage() {
		return this.dst;
	}

	/**
	 * Performs the transformation. 
	 *
	 * @return the time to do the processing, in milliseconds.
	 */
	public long perform()
	{
		//The dilation can be defined as: [f + b](x, y) = max{f(x - s, y - t)} for all (s, t) in b.
		long begin = System.currentTimeMillis();

		//First, the dilation of the structural element is obtained.
		Mat reflectedElement = getReflection(structuralElement);
		//Because the coordinates of the anchor point is changed after reflection, it is needed to obtain the reflected anchor point.
		Point point = getReflectedPoint(structuralElement, anchorPoint);

		dst = image.clone();

		int x = (int)point.x;
		int y = (int)point.y;

		//The following loops are for applying the transformation.
		//The first two for loops are used to access the pixels of the image.
		for(int i = 0; i<dst.rows(); i++) {
			for(int j = 0; j<dst.cols(); j++) {

				//A byte array is created for storing the max values.
				byte[] max = new byte[image.channels()];

				//The following two loops are for accesing the structural element pixels and checking the source image.
				for(int p = -y; p<reflectedElement.rows() - y; p++) {
					for(int q = -x; q<reflectedElement.cols() - x; q++) {

						//In order to avoid an IndexOutOfBorderException, it is needed to check if the index are within the limits of the image.
						if(i + p >= 0 && i + p < image.rows() &&
								j + q >= 0 && j + q < image.cols()) {

							//The value of the pixel in the structural element at the given index is gotten.
							byte[] sPixel = new byte[1];
							reflectedElement.get(p+y, q+x, sPixel);

							//The value will be processed only if the value of the pixel in the structural element is different from 0 (i.e., the structural element is flat).
							if(sPixel[0] != 0x00)
							{
								//The pixel from the image is obtained.
								byte[] _pixel = new byte[image.channels()];
								image.get(i + p, j + q, _pixel);
								//This loop is for checking the max value at every channel.
								for(int k = 0; k<_pixel.length; k++) {
									int pixel = _pixel[k] & 0xFF; //Converting from byte to integer.
									if(pixel > (max[k] & 0xFF)) { //If the current pixel is greater than the already stored maximum value, a new maximum is assigned.
										max[k] = (byte)pixel;
									}
								}
							}
						}						
					}
				}
				//Writing the resulting image in the dst Mat
				dst.put(i, j, max);

			}
		}
		long end = System.currentTimeMillis();
		//The processing time is calculated and returned
		return end - begin;
	}

	/**
	 * Writes the image using the given output filename.
	 */
	public void saveImage() {
		if(dst != null)
			Imgcodecs.imwrite(output, dst);
	}

	/**
	 * Gets the reflection of a given Mat.
	 *
	 * @param src the image to get the reflection
	 * @return the reflected image.
	 */
	public static Mat getReflection(Mat src) {
		Mat reflected = src.clone();
		for(int i = 0; i<src.rows(); i++) {
			for(int j = 0; j<src.cols(); j++) {
				byte[] result = new byte[src.channels()];
				//Getting and assigning the pixel value in the reflected coordinates.
				src.get(src.rows() - 1 - i, src.cols() - 1 - j, result);
				reflected.put(i, j, result);
			}
		}
		return reflected;
	}

	/**
	 * Gets the reflection of a given point.
	 *
	 * @param src the image to which the point belongs. It is used for calculating the size and the absolute coordinates of the point.
	 * @param anchorPoint the point to process.
	 * @return the reflected point.
	 */
	public static Point getReflectedPoint(Mat src, Point anchorPoint)
	{
		Point newAnchorPoint = new Point(src.rows() - 1 - anchorPoint.x, src.cols() - 1 - anchorPoint.y);
		return newAnchorPoint;
	}

	/**
	 * Gets the complement of the image (MaximumValue - Image).
	 *
	 * @param src the image to process.
	 * @return the complement of the image.
	 */
	public static Mat complement(Mat src) {
		Mat dst = src.clone();
		//Getting the channels of the image in a List.
		List<Mat> channels = new ArrayList<>();
		Core.split(dst, channels);

		for(Mat channel : channels) {
			//Converting each channel in a 8-bit channel.
			int type = channel.type();
			channel.convertTo(channel, CvType.CV_8UC1);

			//Creating a constant Mat object with only 255-value pixels.
			Mat m255 = new Mat();
			Core.multiply(Mat.ones(src.rows(), src.cols(), CvType.CV_8UC1), new Scalar(255), m255);

			//Substracting 255 - image channel
			Core.subtract(m255, channel, channel);
			//Converting to the original value.
			channel.convertTo(channel, type);
		}
		Core.merge(channels, dst);
		return dst;
	}
}
