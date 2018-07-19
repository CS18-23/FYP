package server;


import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_highgui;

import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;
import org.bytedeco.javacpp.opencv_videoio;
import org.bytedeco.javacpp.opencv_imgcodecs; 

 
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;



public class FaceDetection{

	public static final String XML_FILE = 
			"resources/haarcascade_frontalface_default.xml";
	
	public static String imagePath;
	
	static JFileChooser fc;
	static File testFile;
	static IplImage img;
	
//	static Rect[] rect_Crop;

	public static void main(String[] args){
//		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		fc = new JFileChooser();
        fc.setDialogTitle("Load a test file");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (fc.showOpenDialog(fc) == JFileChooser.APPROVE_OPTION) {
        	testFile = fc.getSelectedFile();
        	System.out.println(testFile.getName().split(".j")[0]);
//        	testImage = imread(testFile.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
		
		
			img = cvLoadImage(testFile.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
			cvShowImage("Before", img);
			cvWaitKey(0);
			detect(img);	
        }
	}
	
	public static ArrayList<String> callDetect(){
		fc = new JFileChooser();
        fc.setDialogTitle("Load a test file");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (fc.showOpenDialog(fc) == JFileChooser.APPROVE_OPTION) {
        	testFile = fc.getSelectedFile();
        	System.out.println(testFile.getName().split(".j")[0]);
//        	testImage = imread(testFile.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
			img = cvLoadImage(testFile.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
//			cvShowImage("Before", img);
//			cvWaitKey(0);
        }
		return detect(img);
	}

	public static ArrayList<String> detect(IplImage src){

		CvHaarClassifierCascade cascade = new 
				CvHaarClassifierCascade(cvLoad(XML_FILE));
		
		ArrayList<String> imageList = new ArrayList<String>();
		
//		CascadeClassifier faceDetector = new CascadeClassifier(FaceDetector.class.getResource("resources/haarcascade_frontalface_alt.xml").getPath());
		
		CvMemStorage storage = CvMemStorage.create();
		CvSeq sign = cvHaarDetectObjects(
				src,
				cascade,
				storage,
				1.5,
				3,
				CV_HAAR_DO_CANNY_PRUNING);

		cvClearMemStorage(storage);

		int total_Faces = sign.total();		
		
		

		for(int i = 0; i < total_Faces; i++){
//			CvRect r = new CvRect(cvGetSeqElem(sign, i));
			/*cvRectangle (
					src,
					cvPoint(r.x(), r.y()),
					cvPoint(r.width() + r.x(), r.height() + r.y()),
					CvScalar.RED,
					2,
					CV_AA,
					0);*/
			
			Rect rect = new Rect(cvGetSeqElem(sign, i));
			System.out.println("x: "+rect.x());
			System.out.println("y: "+rect.y());
			System.out.println("width: "+rect.width());
			System.out.println("height: "+rect.height());
			
//			Rect rects = new Rect(rect.x(), rect.y(), rect.width(), rect.height());
			org.opencv.core.Rect newRect = new org.opencv.core.Rect(rect.x(), rect.y(), rect.width(), rect.height());
			

			
//			rect_Crop[i] = new Rect(r.x(), r.y(), r.width() + r.x(), r.height() + r.y());
			
//			Mat image_roi = new Mat(image, rect_Crop[i]);
//			imwrite("C:\\cropimage_912.jpg",image_roi);
			
			org.opencv.core.Mat image_1;
			image_1 = org.opencv.imgcodecs.Imgcodecs.imread(testFile.getAbsolutePath());
			org.opencv.core.Mat images = new org.opencv.core.Mat(image_1,newRect);
//			image_1.apply(rect);
			
			imagePath = "resources/"+testFile.getName().split(".j")[0]+"_cropped_"+i+".jpg";
			org.opencv.imgcodecs.Imgcodecs.imwrite(imagePath, images);
			
			// reads input image
			File inputFile = new File(imagePath);
			
			try {
				BufferedImage inputImage = ImageIO.read(inputFile);
				BufferedImage resized = resize(inputImage, 100, 100);

				System.out.println("Resized width: "+resized.getWidth());
				System.out.println("Resized height: "+resized.getHeight());
				
				File output = new File("resources/resized_"+i+".jpg");
				ImageIO.write(resized, "jpg", output);
				
			} catch (IOException e) {
				System.out.println(e);
				e.printStackTrace();
			}
			
			imageList.add("resources/resized_"+i+".jpg");
			rect.close();
			
		}
		

//		cvShowImage("Result", src);
//		cvWaitKey(0);
		
		return imageList;

	}

	
	public static ArrayList<String> detect(IplImage src, File path){

		CvHaarClassifierCascade cascade = new 
				CvHaarClassifierCascade(cvLoad(XML_FILE));
		
		ArrayList<String> imageList = new ArrayList<String>();
		
//		CascadeClassifier faceDetector = new CascadeClassifier(FaceDetector.class.getResource("resources/haarcascade_frontalface_alt.xml").getPath());
		
		CvMemStorage storage = CvMemStorage.create();
		CvSeq sign = cvHaarDetectObjects(
				src,
				cascade,
				storage,
				1.5,
				3,
				CV_HAAR_DO_CANNY_PRUNING);

		cvClearMemStorage(storage);

		int total_Faces = sign.total();		
		
		

		for(int i = 0; i < total_Faces; i++){
//			CvRect r = new CvRect(cvGetSeqElem(sign, i));
			/*cvRectangle (
					src,
					cvPoint(r.x(), r.y()),
					cvPoint(r.width() + r.x(), r.height() + r.y()),
					CvScalar.RED,
					2,
					CV_AA,
					0);*/
			
			Rect rect = new Rect(cvGetSeqElem(sign, i));
			System.out.println("x: "+rect.x());
			System.out.println("y: "+rect.y());
			System.out.println("width: "+rect.width());
			System.out.println("height: "+rect.height());
			
//			Rect rects = new Rect(rect.x(), rect.y(), rect.width(), rect.height());
			org.opencv.core.Rect newRect = new org.opencv.core.Rect(rect.x(), rect.y(), rect.width(), rect.height());
			

			
//			rect_Crop[i] = new Rect(r.x(), r.y(), r.width() + r.x(), r.height() + r.y());
			
//			Mat image_roi = new Mat(image, rect_Crop[i]);
//			imwrite("C:\\cropimage_912.jpg",image_roi);
			
			org.opencv.core.Mat image_1;
			image_1 = org.opencv.imgcodecs.Imgcodecs.imread(path.toString());
			org.opencv.core.Mat images = new org.opencv.core.Mat(image_1,newRect);
//			image_1.apply(rect);
			
			imagePath = "src/server/toRecognize/cropped/"+path.getName().split(".j")[0]+"_cropped_"+i+".jpg";
			org.opencv.imgcodecs.Imgcodecs.imwrite(imagePath, images);
			
			// reads input image
			File inputFile = new File(imagePath);
			
			try {
				BufferedImage inputImage = ImageIO.read(inputFile);
				BufferedImage resized = resize(inputImage, 100, 100);

				System.out.println("Resized width: "+resized.getWidth());
				System.out.println("Resized height: "+resized.getHeight());
				
				File output = new File("src/server/toRecognize/resized/"+path.getName().split(".j")[0]+"resized_"+i+".jpg");
				ImageIO.write(resized, "jpg", output);
				
			} catch (IOException e) {
				System.out.println(e);
				e.printStackTrace();
			}
			
			imageList.add("src/server/toRecognize/resized/"+path.getName().split(".j")[0]+"resized_"+i+".jpg");
			rect.close();
			
		}
		

//		cvShowImage("Result", src);
//		cvWaitKey(0);
		
		return imageList;

	}
	
	
	public static BufferedImage resize(BufferedImage img, int height, int width) {
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);//SCALE_SMOOTH, SCALE_AREA_AVERAGING
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }
}