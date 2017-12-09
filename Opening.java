package pfvn47;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;

public class Opening extends Dilation{
	
	public static void main(String[] args) {
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		Opening opening = new Opening(args);
		
		System.out.println("Beginning opening");
		long elapsed = opening.perform();
		opening.saveImage();
		System.out.println("Opening finished. Time elapsed: " + elapsed + " ms");
	}
	
	public Opening(String[] args) {
		super(args);
	}
	
	public Opening(String[] args, Mat structuralElement)
	{
		super(args, structuralElement);
	}
	
	public Opening(String[] args, Mat structuralElement, Point anchorPoint) {
		super(args, structuralElement, anchorPoint);
	}
	
	public Opening(Mat src, Mat structuralElement, Point anchorPoint) {
		super(src, structuralElement, anchorPoint);
	}

	@Override
	public long perform () {
		long begin = System.currentTimeMillis();
		Erotion erotion = new Erotion(image, structuralElement, anchorPoint);
		erotion.perform();
		Mat eroded = erotion.getProcessedImage();
		Dilation dilation = new Dilation(eroded, structuralElement, anchorPoint);
		dilation.perform();
		this.dst = dilation.getProcessedImage();
		long end = System.currentTimeMillis();
		return end - begin;
	}
}

