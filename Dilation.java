package pfvn47;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.Scalar;

import java.util.List;
import java.util.ArrayList;

/**
 * This class performs the dilation transformation in an image. 
 */
public class Dilation {
	
	protected Mat image;
	private String input;
	private String output;
	protected Mat structuralElement;
	protected Point anchorPoint;
	protected Mat dst;
	
	private int size;
	
	public static void main(String[] args) {	
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		Dilation dilation = new Dilation(args);
				
		System.out.println("Beginning dilation");
		long elapsed = dilation.perform();
		dilation.saveImage();
		System.out.println("Dilation finished. Time elapsed: " + elapsed + " ms");
	}
	
	public Dilation(String[] args) {
		this(args, null);
	}
	
	public Dilation(String[] args, Mat structuralElement)
	{
		this(args, structuralElement, null);
	}
	
	public Dilation(String[] args, Mat structuralElement, Point anchorPoint) {
		if(args == null || args.length < 2) {
			System.out.println("You must enter two arguments");
			return;
		}

		input = args[0];
		output = args[1];
		
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
		if(structuralElement == null) {
			this.structuralElement = Mat.ones(size, size, CvType.CV_8UC1);
		}
		else
		{
			this.structuralElement = structuralElement;
		}
		if(anchorPoint == null)
		{
			int width = this.structuralElement.cols();
			int height = this.structuralElement.rows();
			this.anchorPoint = new Point((width - 1) / 2, (height - 1) / 2);
		}
		else
		{
			this.anchorPoint = anchorPoint;
		}
		this.dst = null;
	}
	
	public Dilation(Mat src, Mat structuralElement, Point anchorPoint) {
		this.image = src;
		this.structuralElement = structuralElement;
		this.anchorPoint = anchorPoint;
	}
	
	public Mat getProcessedImage() {
		return this.dst;
	}
	
	public long perform()
	{
		long begin = System.currentTimeMillis();
		Mat reflectedElement = getReflection(structuralElement);
		Point point = getReflectedPoint(structuralElement, anchorPoint);

		dst = image.clone();
		
		int x = (int)point.x;
		int y = (int)point.y;
		
		for(int i = 0; i<dst.rows(); i++) {
			for(int j = 0; j<dst.cols(); j++) {
				byte[] max = new byte[image.channels()];
				
				for(int p = -y; p<reflectedElement.rows() - y; p++) {
					for(int q = -x; q<reflectedElement.cols() - x; q++) {
						if(i + p >= 0 && i + p < image.rows() &&
								j + q >= 0 && j + q < image.cols()) {
							byte[] sPixel = new byte[1];
							reflectedElement.get(p+y, q+x, sPixel);
							if(sPixel[0] != 0x00)
							{
								byte[] _pixel = new byte[image.channels()];
								image.get(i + p, j + q, _pixel);
								for(int k = 0; k<_pixel.length; k++) {
									int pixel = _pixel[k] & 0xFF;
									if(pixel > (max[k] & 0xFF)) {
										max[k] = (byte)pixel;
									}
								}
							}
						}						
					}
				}
				
				dst.put(i, j, max);
								
			}
		}
		long end = System.currentTimeMillis();
		return end - begin;
	}

	public void saveImage() {
		if(dst != null)
			Imgcodecs.imwrite(output, dst);
	}
	public static Mat getReflection(Mat src) {
		Mat reflected = src.clone();
		for(int i = 0; i<src.rows(); i++) {
			for(int j = 0; j<src.cols(); j++) {
				byte[] result = new byte[src.channels()];
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

	public static Mat complement(Mat src) {
		Mat dst = src.clone();
		List<Mat> channels = new ArrayList<>();
		Core.split(dst, channels);
		
		for(Mat channel : channels) {
			int type = channel.type();
			channel.convertTo(channel, CvType.CV_8UC1);
			Mat m255 = new Mat();
			Core.multiply(Mat.ones(src.rows(), src.cols(), CvType.CV_8UC1), new Scalar(255), m255);
			Core.subtract(m255, channel, channel);
			channel.convertTo(channel, type);
		}
		Core.merge(channels, dst);
		return dst;
	}
}
