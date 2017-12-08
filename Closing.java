package pfvn47;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.Point;

public class Closing {
	
	public static void main(String[] args) {
		if(args == null || args.length != 2) {
			System.out.println("You must enter two arguments");
			return;
		}
		
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		String input = args[0];
		String output = args[1];
		
		Mat image = Imgcodecs.imread(input, Imgcodecs.IMREAD_GRAYSCALE);
		Mat element = Mat.ones(5, 5, CvType.CV_8UC1);
		Point anchorPoint = new Point(2,2);
		
		System.out.println("Beginning closing");
		long begin = System.currentTimeMillis();
		Mat dst = close(image, element, anchorPoint);
		long end = System.currentTimeMillis();
		long elapsed = end - begin;
		Imgcodecs.imwrite(output, dst);
		System.out.println("Closing finished. Time elapsed: " + elapsed + " ms");
	}

	public static Mat close(Mat src, Mat structuralElement, Point anchorPoint) {
		Mat dilated = Dilation.dilate(src, structuralElement, anchorPoint);
		return Erotion.erode(dilated, structuralElement, anchorPoint);
	}
}
