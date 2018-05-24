package prj;

import java.io.File;
import java.text.DecimalFormat;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Main {

	public static Mat getGray(Mat src) {
		Mat grayImage = Mat.zeros(src.size(), CvType.CV_64F);
		Imgproc.cvtColor(src, grayImage, Imgproc.COLOR_RGB2GRAY);
		grayImage.convertTo(grayImage, CvType.CV_32FC1);
		Imgcodecs.imwrite("gray.jpg", grayImage);
		return grayImage;
	}

	public static void main(String[] args) throws Exception {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		DecimalFormat dformat = new DecimalFormat("000");
		Mat[] F_DST = new Mat[770];  //フーリエ変換させた画像の配列
		Mat[] CI_DST = new Mat[770]; //正解画像をフーリエ変換させた画像の配列
		File file;
		Mat src;

		for(int i=0; i<7; i++) {
			String filename = "David/img/0"+dformat.format(i+1)+".jpg";

			//入力画像を読み込んでMat型にする
			file = new File(filename);
			src = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_COLOR);
			if (src == null) {
				throw new RuntimeException("Can't load image.");
			}

			//フーリエ変換させる
			Fourier fft = new Fourier(src, getGray(src));
			F_DST[i] = fft.dst;  //配列に読み込む

			//正解画像を作ってフーリエ変換させる
			CorrectImage ci = new CorrectImage(src,130,130);
			Fourier fci = new Fourier(ci.dst,  ci.dst);
			CI_DST[i] = fci.dst; //配列に読み込む
			Imgcodecs.imwrite("CIdst"+i+".jpg", fci.dst); //デバッグ用
		 }

		//画像のサイズを取得
		int width = F_DST[0].cols();
		int height = F_DST[0].rows();

		double [][] numer = new double[height][width]; //分子
		double [][] denom = new double[height][width]; //分母

		for (int x = 0; x < height; x++) {
			for (int y = 0; y < width; y++) {
				for(int k=0; k<770; k++) {
					numer[x][y] += F_DST[k].get(x, y, data)*CI_DST
						
				}
			}
		}
	}

}
