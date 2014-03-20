package com.fyp.authenticator.face;

import com.googlecode.javacv.cpp.opencv_core;

import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_contrib.*;

import android.util.Log;

public class FaceDAO {

	FaceRecognizer recognizer = null;

	public FaceDAO() {
		this.recognizer = createEigenFaceRecognizer();
//		this.trainFaceRecognizer();
	}

	public void trainFaceRecognizer() {
		int labels[] = new int[1];
		MatVector images = new MatVector(1);
		
		IplImage img;
    IplImage grayImg;
    
		{
			img = cvLoadImage("absolute path");
      
      // Convert image to grayscale:
      grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1);
      cvCvtColor(img, grayImg, CV_BGR2GRAY);
      
      // Append it in the image list:
      images.put(0, grayImg);
      
      // And in the labels list:
      labels[0] = 1;
		}
		
		this.recognizer.train(images, labels);
	}

	public double getMatch(Object faceObject) {
		; // TODO:
		return 0;
	}

}
