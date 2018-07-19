package server;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class Capture {
	
	public static void main() {
		
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
			    VideoCapture webSource = null;
			    Mat frame = new Mat();
			    int count = 0;
			    
			    synchronized (this) {
	                while (count < 10) {
	                    if (webSource.grab()) {
	                        try {
	                            webSource.retrieve(frame);
	                            count++;
	                        }catch(Exception e) {
	                        	
	                        }
	                    }
	                }
			    }
				
			}
		});
		
		thread.start();
	}

}
