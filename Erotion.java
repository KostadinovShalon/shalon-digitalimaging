package pfvn47;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.Scalar;
import org.opencv.core.Point;

public class Erotion {
	
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
		
		System.out.println("Beginning erotion");
		long begin = System.currentTimeMillis();
		Mat dst = erode(image, element, anchorPoint);
		long end = System.currentTimeMillis();
		long elapsed = end - begin;
		Imgcodecs.imwrite(output, dst);
		System.out.println("Erotion finished. Time elapsed: " + elapsed + " ms");
	}

	public static Mat erode(Mat src, Mat structuralElement, Point anchorPoint) {
		Mat src_c = Erotion.complement(src);
		Mat eroded = Dilation.dilate(src_c, Dilation.getReflection(structuralElement), 
					Dilation.getReflectedPoint(structuralElement, anchorPoint));
		return Erotion.complement(eroded);
	}

	public static Mat complement(Mat src) {
		Mat dst = src.clone();
		src.convertTo(dst, CvType.CV_8UC1);
		
		Mat m255 = new Mat();
		Core.multiply(Mat.ones(src.rows(), src.cols(), CvType.CV_8UC1), new Scalar(255), m255);
		Core.subtract(m255, src, dst);
		return dst;
	}
}
