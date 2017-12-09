package pfvn47;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;

public class Erotion extends Dilation{
	
	public static void main(String[] args) {
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		Erotion erotion = new Erotion(args);
		
		System.out.println("Beginning erotion");
		long elapsed = erotion.perform();
		erotion.saveImage();
		System.out.println("Erotion finished. Time elapsed: " + elapsed + " ms");
	}
	
	public Erotion(String[] args) {
		super(args);
	}
	
	public Erotion(String[] args, Mat structuralElement)
	{
		super(args, structuralElement);
	}
	
	public Erotion(String[] args, Mat structuralElement, Point anchorPoint) {
		super(args, structuralElement, anchorPoint);
	}
	
	public Erotion(Mat src, Mat structuralElement, Point anchorPoint) {
		super(src, structuralElement, anchorPoint);
	}
	

	@Override
	public long perform() {
		long begin = System.currentTimeMillis();
		Mat src_c = Erotion.complement(image);
		Dilation dilation = new Dilation(src_c, getReflection(structuralElement), 
				getReflectedPoint(structuralElement, anchorPoint));
		dilation.perform();
		this.dst = complement(dilation.getProcessedImage());
		long end = System.currentTimeMillis();
		return end - begin;
	}

}
