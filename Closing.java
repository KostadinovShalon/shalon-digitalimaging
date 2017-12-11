import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
/**
 * Performs closing transformation of an image. This class loads an image and process the opening transformation
 * with a specific kernel. If no kernel information is given, a default will be used (a 5x5 square). When the
 * the transformation is finished, the image will be saved in the location of the user preference. 
 * 
 * This transformation can be applied to grayscale or color images, where the transformation is applied
 * to the RGB channels.
 *
 * @author Brian Kostadinov Shalon Isaac Medina
 */
public class Closing extends Dilation{

	public static void main(String[] args) {
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		Closing closing = new Closing(args);

		System.out.println("Beginning closing");
		long elapsed = closing.perform();
		closing.saveImage();
		System.out.println("Closing finished. Time elapsed: " + elapsed + " ms");
	}

	/**
	 * Creates a closing object with a square flat structural element. The anchor point of this transformation will be the center of the structural element.
	 *
	 * @param args the arguments of the main method. This array must have two elements at least, indicating first the filename of the image to process and then the filename of the resulting image.
	 *             An optional third element can be used for giving the size of the structural element. If not, a 5x5 square will be used.
	 */
	public Closing(String[] args) {
		super(args);
	}

	/**
	 * Creates a closing object with a given structural element. The anchor point of this transformation will be the center of the structural element.
	 *
	 * @param args the arguments of the main method. This array must have two elements at least, indicating first the filename of the image to process and then the filename of the resulting image.
	 *             An optional third element can be used for giving the size of the structural element. This will be only used if the structural element is null.
	 * @param structuralElement a Mat object indicating the structural element to be used. All structural elements in this class are flat, so values different from 0 are taken as 1.
	 *                                If this parameter is null, a 5x5 square full with ones  will be used.
	 */
	public Closing(String[] args, Mat structuralElement)
	{
		super(args, structuralElement);
	}

	/**
	 * Creates a closing object with a given structural element and an anchor point (the center of the structural element).
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
	public Closing(String[] args, Mat structuralElement, Point anchorPoint) {
		super(args, structuralElement, anchorPoint);
	}

	/**
	 * Creates a closing object with a given structural element and an anchor point (the center of the structural element).
	 * If the anchor point is null, the geometric center of the structural element will be used.
	 * If the structural element is null, a 5x5 square will be used.
	 *
	 * @param src a Mat object containing the image.
	 * @param structuralElement a Mat object indicating the structural element to be used. All structural elements in this class are flat, so values different from 0 are taken as 1.
	 *                                If this parameter is null, a 5x5 square full with ones  will be used.
	 * @param anchorPoint the origin of the structural element. This point will be used as a reference when performing morphological transformations. 
	 *                    If this parameter is null, the anchor point will be the geometrical center of the structural element.
	 */
	public Closing(Mat src, Mat structuralElement, Point anchorPoint) {
		super(src, structuralElement, anchorPoint);
	}

	/**
	 * Performs the transformation. 
	 *
	 * @return the time to do the processing, in milliseconds.
	 */
	@Override
	public long perform () {
		//The closing can be defined as: f . b = (f + b) - b   (The closing is obtained dilating the image and then eroding it with the same structural element).
		long begin = System.currentTimeMillis();
		//First, the dilation is obtained
		Dilation dilation = new Dilation(image, structuralElement, anchorPoint);
		dilation.perform();
		Mat dilated = dilation.getProcessedImage();
		//Then, the erotion is performed over the previously dilated image.
		Erotion erotion = new Erotion(dilated, structuralElement, anchorPoint);
		erotion.perform();
		this.dst = erotion.getProcessedImage();
		long end = System.currentTimeMillis();
		return end - begin;
	}
}
