import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;

public class Dilation {
	
	public static void main(String[] args) {
		if(args == null || args.length != 2) {
			System.out.println("You must enter two arguments");
			return;
		}
		
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		String input = args[0];
		String output = args[1];
		
		Mat image = Imgcodecs.imread(input, Imgcodecs.IMREAD_GRAYSCALE);
		Mat structuralElement = Mat.ones(5,5,CvType.CV_8UC1);
		Point anchorPoint = new Point(2, 2);
		//StructuralElement element = new StructuralElement(Mat.ones(5, 5, CvType.CV_8UC1));
		
		System.out.println("Beginning dilation");
		long begin = System.currentTimeMillis();
		Mat dst = dilate(image, structuralElement, anchorPoint);
		long end = System.currentTimeMillis();
		long elapsed = end - begin;
		Imgcodecs.imwrite(output, dst);
		System.out.println("Dilation finished. Time elapsed: " + elapsed + " ms");
	}
	
	public static Mat dilate(Mat src, Mat structuralElement, Point anchorPoint)
	{
		Mat reflectedElement = getReflection(structuralElement);
		Point point = getReflectedPoint(structuralElement, anchorPoint);

		Mat dst = src.clone();
		Mat aux = new Mat();
		src.convertTo(aux, CvType.CV_8UC1);
		int x = (int)point.x;
		int y = (int)point.y;
		
		for(int i = 0; i<dst.rows(); i++) {
			for(int j = 0; j<dst.cols(); j++) {
				int max = 0;
				
				for(int p = -y; p<reflectedElement.rows() - y; p++) {
					for(int q = -x; q<reflectedElement.cols() - x; q++) {
						if(i + p >= 0 && i + p < aux.rows() &&
								j + q >= 0 && j + q < aux.cols()) {
							byte[] sPixel = new byte[1];
							reflectedElement.get(p+y, q+x, sPixel);
							if(sPixel[0] != 0x00)
							{
								byte[] _pixel = new byte[1];
								aux.get(i + p, j + q, _pixel);
								int pixel = _pixel[0] & 0xFF;
								if(pixel > max)
									max = pixel;
							}
						}						
					}
				}
				
				dst.put(i, j, new byte[] {(byte)max});
								
			}
		}
		
		return dst;
	}

	public static Mat getReflection(Mat src) {
		Mat reflected = src.clone();
		for(int i = 0; i<src.rows(); i++) {
			for(int j = 0; j<src.cols(); j++) {
				byte[] result = new byte[1];
				src.get(src.rows() - 1 - i, src.cols() - 1 - j, result);
				reflected.put(i, j, result);
			}
		}
		return reflected;
	}

	public static Point getReflectedPoint(Mat src, Point anchorPoint)
	{
		Point newAnchorPoint = new Point(src.rows() - 1 - anchorPoint.x, src.cols() - 1 - anchorPoint.y);
		return newAnchorPoint;
	}
}
