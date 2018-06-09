package prj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

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

	public static List<Integer> loadText() {

		FileReader fr = null;
		BufferedReader br = null;
		List<Integer> list = new ArrayList<>();

		try {
			fr = new FileReader("David/groundtruth_rect.txt");
			br = new BufferedReader(fr);

			String line;
			while ((line = br.readLine()) != null) {//1行ずつ読み込み
				String[] array = line.split(",", 0);
				for (String elem : array)
					list.add(Integer.parseInt(elem));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public static void main(String[] args) throws Exception {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		DecimalFormat dformat = new DecimalFormat("000");
		Mat[] F_DST = new Mat[471]; //フーリエ変換させた画像の配列
		Mat[] CI_DST = new Mat[471]; //正解画像をフーリエ変換させた画像の配列
		List<Integer> list = new ArrayList<>();  //画像の座標テキスト
		File file;
		Mat src;
    	list = loadText();
    	Mat[] real1 = new Mat[471]; //入力の実部
		Mat[] img1 = new Mat[471]; //入力の虚部
		Mat[]real2=new Mat[471];
		Mat []img2=new Mat[471];
		int width=0,height=0;

		for (int i = 0; i <471; i++) {
			String filename = "David/img/0" + dformat.format(i + 1) + ".jpg";

			//入力画像を読み込んでMat型にする
			file = new File(filename);
			src = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_COLOR);
			if (src == null) {
				throw new RuntimeException("Can't load image.");
			}
			//フーリエ変換させる
			Fourier fft= new Fourier(src, getGray(src));
			F_DST[i] = fft.dst; //配列に読み込む
			real1[i]= Mat.zeros(F_DST[0].size(), CvType.CV_16UC1);
			img1[i]=Mat.zeros(F_DST[0].size(), CvType.CV_16UC1);
			//画像のサイズを求める
			width = F_DST[0].cols();
		    height = F_DST[0].rows();
			real1[i]=fft.real;//入力の実部
			img1[i]=fft.img;//入力の虚部
			//正解画像を作ってフーリエ変換させる
			CorrectImage ci = new CorrectImage(src, list.get(i*4)+list.get(i*4+2)/2, list.get(i*4+1)+list.get(i*4+3)/2);
			Fourier fci = new Fourier(ci.dst, ci.dst);
			CI_DST[i] = fci.dst; //配列に読み込む
			real2[i]= Mat.zeros(CI_DST[0].size(), CvType.CV_16UC1);
			img2[i]=Mat.zeros(CI_DST[0].size(), CvType.CV_16UC1);
			real2[i]=fci.real;//正解の実部
			img2[i]=fci.img;//正解の虚部
			//Imgcodecs.imwrite("CIdst" + i + ".jpg", fci.dst); //デバッグ用
		}

		double[][] numer1 = new double[height][width]; //分子実部
		double[][] numer2 = new double[height][width]; //分子虚部
		double[][] denom = new double[height][width]; //分母
		Mat filter1=new Mat(height, width, CvType.CV_32FC1);
		Mat filter2=new Mat(height, width, CvType.CV_32FC1);
		Mat filter=new Mat(height, width, CvType.CV_32FC1);
		double ans1,ans2,Numer1=0,Numer2=0,Denom=0;
		
		for (int x = 0; x <height; x++) {
			for (int y = 0; y <width; y++) {//xy座標で画素を取得し、
				for (int k = 0; k <471; k++) {
					numer1[x][y] +=real1[k].get(x, y)[0]*real2[k].get(x, y)[0]+img1[k].get(x, y)[0]*img2[k].get(x, y)[0];
					numer2[x][y]+=img2[k].get(x, y)[0]*real1[k].get(x, y)[0]-real2[k].get(x, y)[0]*img1[k].get(x, y)[0];
					denom[x][y]+=real1[k].get(x, y)[0]*real1[k].get(x, y)[0]+img1[k].get(x, y)[0]*img1[k].get(x, y)[0];			
				Numer1=numer1[x][y];
				Numer2=numer2[x][y];
				Denom=denom[x][y];
				}
			}
		}
		ans1=Numer1/Denom;//フィルタの実部
		ans2=Numer2/Denom*-1;//フィルタの虚部
		for(int x=0;x<height;x++) {//x,y座標に実部、虚部をput
			for(int y=0;y<width;y++) {
				filter1.put(x, y, ans1);
				filter2.put(x, y, ans2);
			}
		}
		//filter1とfilter2をマージする(ここができない)
		List<Mat> listMat = new ArrayList<Mat>();
		listMat.add(filter1);//32fc1
		listMat.add(filter2);//32fc1
		Core.merge(listMat, filter);//32fc1
		Imgcodecs.imwrite("filter.jpg", filter);
		Imgcodecs.imwrite("real.jpg", filter1);
		Imgcodecs.imwrite("img.jpg", filter2);
		System.out.println("きた");
	}
}