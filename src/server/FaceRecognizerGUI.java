package server;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.nio.IntBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacpp.opencv_face.FisherFaceRecognizer;
import org.opencv.core.Core;

import client.Menu;

public class FaceRecognizerGUI extends JFrame {

	private JPanel contentPane;
	private JTextArea textArea;
	JPanel panelButton, testImagePanel, statusPanel;
	JFileChooser fc;
	
	int position = 0;
	
	FaceRecognizer faceRecognizer;
	File[] folders;
	FileFilter jpgFilter;
	ArrayList<File> trainingSet;
	ArrayList<String> predictedName;
	Mat testImage;
	
	PreparedStatement pst;
	Connection con = null;
	ResultSet rs = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FaceRecognizerGUI frame = new FaceRecognizerGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FaceRecognizerGUI() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(2, 1, 0, 0));
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1);
		panel_1.setLayout(new GridLayout(1, 0, 0, 0));
		
		statusPanel = new JPanel();
		panel_1.add(statusPanel);
		statusPanel.setLayout(null);
		
		textArea = new JTextArea();
		textArea.setBounds(10, 5, 192, 109);
		statusPanel.add(textArea);
		
		testImagePanel = new JPanel();
		panel_1.add(testImagePanel);
		
		panelButton = new JPanel();
		contentPane.add(panelButton);
		panelButton.setLayout(new GridLayout(1, 3, 0, 0));
		
		JButton loadImageButton = new JButton("Load Images");
		loadImageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadImage();
			}
		});
		panelButton.add(loadImageButton);
		
		JButton trainButton = new JButton("Train");
		trainButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				train();
			}
		});
		panelButton.add(trainButton);
		
		JButton testImageButton = new JButton("Test Image");
		testImageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				testImage();
			}
		});
		panelButton.add(testImageButton);
	}
	
	public void loadImage() {
		fc = new JFileChooser();
        fc.setDialogTitle("Load a file");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        trainingSet = new ArrayList<File>();
        predictedName = new ArrayList<String>();

        if (fc.showOpenDialog(fc) == JFileChooser.APPROVE_OPTION) {
	        File folder = fc.getSelectedFile();
	        //System.out.println("1	"+folder);
	        
	        FileFilter dirFilter = new FileFilter() {
	            public boolean accept(File pathname) {
	                return pathname.exists() && pathname.isDirectory();
	            }
	        };
	        jpgFilter = new FileFilter() {
	            public boolean accept(File pathname) {
	                String filename = pathname.getName();
	                boolean jpgFile = (filename.toUpperCase().endsWith("JPG")
	                        || filename.toUpperCase().endsWith("JPEG"));
	                return pathname.exists() && pathname.isFile() && jpgFile;
	            }
	        };
	
	        folders = folder.listFiles(dirFilter);
	        //System.out.println("2	"+folders);
	        trainingSet.clear();
	        
	        textArea.insert("\nFinished Loading\n", 0);
	        
        }
	}
	
	public void train() {
		
		for (int i = 0; i < folders.length; i++) {				//For each folder in the training set directory
            File[] files = folders[i].listFiles(jpgFilter);
//            System.out.println("3	" + files);
            for (int j = 0; j < files.length; j++) {
                trainingSet.add(files[j]);
            }
            
            //connect to database and add the name from student
            String query = "SELECT firstname, lastname FROM student WHERE studentNo = "+files[i].getParentFile().getName();
			 try {
					con = DriverManager.getConnection("jdbc:mysql://localhost/registration?useSSL=false","root", "");
					pst = con.prepareStatement(query);
					rs=pst.executeQuery(query);
					
					while(rs.next()) {
						 predictedName.add(rs.getString("firstname")+" "+rs.getString("lastname"));
					}
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
           
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
        	faceRecognizer.save("save/FaceDB.yml");
        
        
        textArea.insert("\nFinished Training\n", 0);
	}
	
	public void testImage() {
		testImagePanel.removeAll();
		textArea.insert("\n\n\n",0);
		
        IntPointer label = new IntPointer(1);
        DoublePointer confidence = new DoublePointer(1);
        
        String name = "";
        
        //choose a test face
        ArrayList<String> imageList = FaceDetection.callDetect();
        for(int i=0; i<imageList.size(); i++) {
        	JLabel picLabel = new JLabel(new ImageIcon(imageList.get(i)));
		    //picLabel.setSize(250, 220);
        	
		    testImagePanel.add(picLabel);
	        System.out.println("Image List: "+imageList.get(i));
	        testImage = imread(imageList.get(i), CV_LOAD_IMAGE_GRAYSCALE);
	        faceRecognizer.predict(testImage, label, confidence);
	        int predictedLabel = label.get(0);
	
	        
	        try{
				Statement stmt = null;	
				String query = "SELECT FirstName, LastName FROM student WHERE StudentNo = "+predictedLabel;
				con = DriverManager.getConnection("jdbc:mysql://localhost/registration?useSSL=false","root", "");
				stmt = con.createStatement();
				rs = stmt.executeQuery(query);
				while(rs.next()) {
					name = rs.getString("FirstName")+" "+rs.getString("LastName");
				}
				
			}
			catch(Exception e) {
				e.printStackTrace();
			}
	        
	        textArea.removeAll();
	        textArea.insert("\nName: " +name,0);
	        
        }
	}

}
