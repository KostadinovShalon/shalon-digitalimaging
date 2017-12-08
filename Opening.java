package pfvn47;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.Point;

public class Opening {
	
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
		Point anchorPoint = new Point(2, 2);
		
		System.out.println("Beginning opening");
		long begin = System.currentTimeMillis();
		Mat dst = open(image, element, anchorPoint);
		long end = System.currentTimeMillis();
		long elapsed = end - begin;
		Imgcodecs.imwrite(output, dst);
		System.out.println("Opening finished. Time elapsed: " + elapsed + " ms");
	}

	public static Mat open(Mat src, Mat structuralElement, Point anchorPoint) {
		Mat eroded = Erotion.erode(src, structuralElement, anchorPoint);
		return Dilation.dilate(eroded, structuralElement, anchorPoint);
	}
}

