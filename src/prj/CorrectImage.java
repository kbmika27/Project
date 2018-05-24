package prj;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class CorrectImage {

	Mat src;
	Mat dst;

	//ファイルの読み込み
	public CorrectImage(Mat src, int centerX, int centerY) {
		this.src = src;
		dst = getCI(centerX, centerY);
	}

	private Mat getCI(int centerX, int centerY) {
		int width;
		int height;
		int x, y = 0;
		double dst;

		width = src.cols();
		height = src.rows();

		Mat dst_mat = new Mat(height, width, CvType.CV_32FC1);
		dst_mat.put(centerX, centerY, 255);

		for (x = 0; x < height; x++) {
			for (y = 0; y < width; y++) {
				dst = 255 * (Math
						.exp((-1) * (((centerX - x) * (centerX - x) + (centerY - y) * (centerY - y)) / (2 * 2 * 2))));
				dst_mat.put(x, y, dst);
			}
		}
		return dst_mat;
	}
}
