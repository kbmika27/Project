package prj;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;

public class Fourier {

	Mat src;//元
	Mat dst;//フーリエ後
    Mat real;//実部
	Mat img;//虚部
	Mat grayImage;

	//ファイルの読み込み
	public Fourier(Mat src, Mat grayImage) {
		this.src = src;
		this.grayImage = grayImage;
		dst = getDFT();
	}


	private Mat getDFT() {
		// Convert to gray image.
		int m = Core.getOptimalDFTSize(grayImage.rows());
		int n = Core.getOptimalDFTSize(grayImage.cols());
		Mat padded = new Mat(new Size(n, m), grayImage.type());

		Core.copyMakeBorder(grayImage, padded, 0, m - src.rows(), 0,n - src.cols(), Core.BORDER_CONSTANT);

		// Make complex matrix.
		List<Mat> planes = new ArrayList<Mat>();
		planes.add(padded);
		planes.add(Mat.zeros(padded.size(), padded.type()));
		Mat complexI = Mat.zeros(padded.size(), CvType.CV_32FC1);
		Mat complexI2 = Mat.zeros(padded.size(), CvType.CV_32F);
		Core.merge(planes, complexI);

		// Calculate DFT, and magnitude.
		Core.dft(complexI, complexI2);//complexI2がフーリエ後
		Core.split(complexI2, planes);//実部と虚部
		real=planes.get(0);
		img=planes.get(1);
		Mat mag = new Mat(planes.get(0).size(), planes.get(0).type());
		Core.magnitude(planes.get(0), planes.get(1), mag);
		
		
		Mat magI = mag;
		Mat magI2 = new Mat(magI.size(), magI.type());
		Mat magI3 = new Mat(magI.size(), magI.type());
		Mat magI4 = new Mat(magI.size(), magI.type());
		Mat magI5 = new Mat(magI.size(), magI.type());

		// Normalize.
		Core.add(magI, Mat.ones(padded.size(), CvType.CV_32F), magI2);
		Core.log(magI2, magI3);

		// Swap, swap, swap.
		Mat crop = new Mat(magI3,
				new Rect(0, 0, magI3.cols() & -2, magI3.rows() & -2));

		magI4 = crop.clone();

		int cx = magI4.cols() / 2;
		int cy = magI4.rows() / 2;

		Rect q0Rect = new Rect(0, 0, cx, cy);
		Rect q1Rect = new Rect(cx, 0, cx, cy);
		Rect q2Rect = new Rect(0, cy, cx, cy);
		Rect q3Rect = new Rect(cx, cy, cx, cy);

		Mat q0 = new Mat(magI4, q0Rect); // Top-Left
		Mat q1 = new Mat(magI4, q1Rect); // Top-Right
		Mat q2 = new Mat(magI4, q2Rect); // Bottom-Left
		Mat q3 = new Mat(magI4, q3Rect); // Bottom-Right

		Mat tmp = new Mat();
		q0.copyTo(tmp);
		q3.copyTo(q0);
		tmp.copyTo(q3);

		q1.copyTo(tmp);
		q2.copyTo(q1);
		tmp.copyTo(q2);

		Core.normalize(magI4, magI5, 0, 255, Core.NORM_MINMAX);

		// Convert image.
		Mat realResult = new Mat(magI5.size(), CvType.CV_32FC1);
		magI5.convertTo(realResult, CvType.CV_32FC1);

		return complexI2;
	}
}
