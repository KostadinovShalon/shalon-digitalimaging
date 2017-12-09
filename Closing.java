package pfvn47;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;

public class Closing extends Dilation{
	
	public static void main(String[] args) {
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		Closing closing = new Closing(args);
		
		System.out.println("Beginning closing");
		long elapsed = closing.perform();
		closing.saveImage();
		System.out.println("Closing finished. Time elapsed: " + elapsed + " ms");
	}
	
	public Closing(String[] args) {
		super(args);
	}
	
	public Closing(String[] args, Mat structuralElement)
	{
		super(args, structuralElement);
	}
	
	public Closing(String[] args, Mat structuralElement, Point anchorPoint) {
		super(args, structuralElement, anchorPoint);
	}
	
	public Closing(Mat src, Mat structuralElement, Point anchorPoint) {
		super(src, structuralElement, anchorPoint);
	}

	@Override
	public long perform () {
		long begin = System.currentTimeMillis();
		Dilation dilation = new Dilation(image, structuralElement, anchorPoint);
		dilation.perform();
		Mat dilated = dilation.getProcessedImage();
		Erotion erotion = new Erotion(dilated, structuralElement, anchorPoint);
		erotion.perform();
		this.dst = erotion.getProcessedImage();
		long end = System.currentTimeMillis();
		return end - begin;
	}
}
