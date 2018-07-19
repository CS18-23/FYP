package server;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacpp.opencv_face.FisherFaceRecognizer;
import org.opencv.core.Core;

public class Starter {
	
	FaceRecognizer faceRecognizer;
	
	FileFilter jpgFilter, dirFilter;
	ArrayList<File> trainingSet;
	ArrayList<String> predictedName, membersPresent;
	
	ServerSocket serverSocket;
	Socket socket;
	DataInputStream in;
    InputStream inputStream;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Starter();
	}
	
	public Starter() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		trainingSet = new ArrayList<File>();
		
		
		jpgFilter = new FileFilter() {
            public boolean accept(File pathname) {
                String filename = pathname.getName();
                boolean jpgFile = (filename.toUpperCase().endsWith("JPG")
                        || filename.toUpperCase().endsWith("JPEG"));
                return pathname.exists() && pathname.isFile() && jpgFile;
            }
        };
        
        dirFilter = new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.exists() && pathname.isDirectory();
            }
        };
		
		
        //first train picures in the students' pictures folder
		train();
		
		//start waiting for instructions from the clients
		waitForInstruction();
	}
	
	public void train() {
		predictedName = new ArrayList<String>();
		File folder = new File("src/server/Students");
		File folders[] = folder.listFiles(dirFilter);
		for (int i = 0; i < folders.length; i++) {				//For each folder in the training set directory
            File[] files = folders[i].listFiles(jpgFilter);
            
            for (int j = 0; j < files.length; j++) {			//for each of the files
                trainingSet.add(files[j]);
            }
            
            predictedName.add(folders[i].getName());
			  
        }
		
		File[] files = trainingSet.toArray(new File[1]);
        
        MatVector images = new MatVector(files.length);
        Mat labels = new Mat(files.length, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.createBuffer();
        
        
        for (int i = 0; i < files.length; i++) {
        	Mat img = imread(files[i].getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
        	int label = Integer.parseInt(files[i].getParentFile().getName());
        	
        	
        	images.put(i, img);
            labelsBuf.put(i, label);
        }
        
        faceRecognizer = FisherFaceRecognizer.create();
        faceRecognizer.train(images, labels);
        faceRecognizer.save("src/server/save/FaceDB.yml");
        
        System.out.println("Finished training.....");
        
		
	}
	
	public void addStudent(int number, int id){
		for(int x=0; x<number; x++){
			try{

				byte[] sizeAr = new byte[4];
	        	inputStream.read(sizeAr);
	        	int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();
		
		        byte[] imageAr = new byte[size]; //size
			    inputStream.read(imageAr);

			    BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageAr));
			
	        	System.out.println("Received " + image.getHeight() + "x" + image.getWidth() + ": " + System.currentTimeMillis());
	        	//first make the directory
	        	String path = "src/server/Students/"+id+"/";
				File folder = new File(path);
				folder.mkdirs();
	       		ImageIO.write(image, "jpg", new File("src/server/Students/"+id+"/"+(x+1)+".jpg"));
				
//				serverSocket.close();
//				serverSocket = new ServerSocket(13085);
		       	socket = serverSocket.accept();
		       	System.out.println("Server accepted");
				inputStream = socket.getInputStream();

			}catch(IOException e) {
				System.out.println("IO:"+e.getMessage());
			}
		}
		//train the images after words
		train();
	}
	
	public void recognizeStudents(int number) {
		
		membersPresent = new ArrayList<String>();
		
		for(int x=0; x<number; x++){
			try{
				in = new DataInputStream( socket.getInputStream());
				
				byte[] sizeAr = new byte[1000];
//	        	inputStream.read(sizeAr);
				String input = in.readUTF();
				
	        	int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();
	        	System.out.println("Received: "+Integer.parseInt(input)+ "bytes ");
		        byte[] imageAr = new byte[Integer.parseInt(input)];//size
			    inputStream.read(imageAr);

			    BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageAr));
			
	        	System.out.println("Received: "+ image.getHeight() + "x" + image.getWidth() + ": " + System.currentTimeMillis());
	       		ImageIO.write(image, "jpg", new File("src/server/toRecognize/"+(x)+".jpg"));
			
				
				
				//begin to recognize each image
				//start with face deetction
				
				IplImage src = cvLoadImage("src/server/toRecognize/"+(x)+".jpg", CV_LOAD_IMAGE_GRAYSCALE);
				File path = new File("src/server/toRecognize/"+(x)+".jpg");
				ArrayList<String> imageList = FaceDetection.detect(src, path);
				

		        IntPointer label = new IntPointer(1);
		        DoublePointer confidence = new DoublePointer(1);
		        
		        
		        for(int i=0; i<imageList.size(); i++) {
		        	JLabel picLabel = new JLabel(new ImageIcon(imageList.get(i)));
				    //picLabel.setSize(250, 220);
		        
			        System.out.println("Image List: "+imageList.get(i));
			        Mat testImage = imread(imageList.get(i), CV_LOAD_IMAGE_GRAYSCALE);
			        faceRecognizer.predict(testImage, label, confidence);
			        int predictedLabel = label.get(0);
			        System.out.println("Found: "+predictedLabel);
			        membersPresent.add(""+predictedLabel);
			        System.out.println(membersPresent);
			        
		        }
		        
//				System.out.println("Socket closed");
//				serverSocket = new ServerSocket(13085+x);
		       	socket = serverSocket.accept();
		       	System.out.println("Server Socket Accepted");
		       	inputStream = socket.getInputStream();

			}catch(IOException e) {
				System.out.println("IO:"+e.getMessage());
			}
		}
		System.out.println("Present: "+membersPresent);
		
		//now send back the list of present students
		//first send the number of students present
		try {
			ServerSocket serverSocket = new ServerSocket(13084); //mind the port
	        Socket socket = serverSocket.accept();
	        System.out.println("Socket Accepted");
	        DataOutputStream out = new DataOutputStream( socket.getOutputStream());
	        out.writeUTF(""+membersPresent.size());
	        System.out.println(""+membersPresent.size());
	        
	        for(int i=0; i<membersPresent.size(); i++) {
				out.writeUTF(membersPresent.get(i));
				serverSocket.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		        
		
		
		
		
	}
	
	public void waitForInstruction() {
		try {
			serverSocket = new ServerSocket(13085);
		    socket = serverSocket.accept();
		
			in = new DataInputStream( socket.getInputStream());
			inputStream = socket.getInputStream();
	        
	        System.out.println("Reading: " + System.currentTimeMillis());

			
			String data = in.readUTF();
			if(data.split(",")[0].equals("add student")){
				addStudent(Integer.parseInt(data.split(",")[1]), Integer.parseInt(data.split(",")[2]));
			}
			else if(data.split(",")[0].equals("recognize students")){
				recognizeStudents(Integer.parseInt(data.split(",")[1]));
			}	
				
		}catch(EOFException e) {
			System.out.println("EOF:"+e.getMessage());
		}catch(IOException e) {
			System.out.println("IO:"+e.getMessage());
		}finally { 
			try {
				serverSocket.close();
			}catch (IOException e){
				/*close failed*/
			}
		}
		
		waitForInstruction();
	}

}
