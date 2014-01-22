package gr.iti.mklab.detector.smal;
import java.io.IOException;

import gr.iti.mklab.detector.utilIOmat.IOUtil;
import smal.smalDetector;

import com.mathworks.extern.java.MWStructArray;
import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
/**
 * 
 * 
 * Implementation of Incremental Learning of k-approximate eigenvectors.
 * SmalDetector computes the eigenfunctions and eigenvectors of test data by
 * interpolating them with the eigenvector of training data.
 * For efficient reasons, demo compute the eigenvectors from test data
 * in batches (1000 items/batch).

 * Demo also runs three different variant of learning model methods:
 * 1. Linear SVM.
 * 2. Smooth Function

 * The code is ready to run for flickr2013 (included ulr file) and twitter2013 datasets.
 * You can download the datasets from
 * 1. flickr2013: http://www.socialsensor.eu/datasets/mm-concept-detection-dataset-2013/mm-concept-detection-datasets.zip
 * 2. twitter2013: http://www.socialsensor.eu/datasets/mm-concept-detection-dataset-2013/mm-concept-detection-twitter2013-images.zip
 *  password: socialsensor 
 * 
 * @author lmantziou
 *
 */

public class Detector {
	
	static smalDetector smal = null;
	static Object[] testEigenvector = null;
	static Object[] classificationResult = null;

	static MWStructArray incrementalParams = null;
	static MWStructArray classifierParams = null;
	static MWNumericArray predictedScore;
	static MWNumericArray predictedConcept;
	
	static double[][] uutest;


	static double[][] concept;
	static double[][] score;
	public static double[][] ComputeConceptDetector(double[][] vlVector) throws IOException{
	
		try {
			smal = new smalDetector();
			
			IOUtil matStructure = IOUtil.readingMatlabFile();
			
			/**
			 * compute the test eigenvectors from vlad vector by interopolating in training set
			 */

			/*
			 * Initialize parameters
			 * import parameters in matlab methods
			 */
			String[] ParamsStructFieldsIncremental={"vtest","bins_out", "uu1", "jj"};
			incrementalParams= new MWStructArray(1,1, ParamsStructFieldsIncremental);
			incrementalParams.set("vtest",1,vlVector);
			incrementalParams.set("bins_out",1,matStructure.getbins_out());
			incrementalParams.set("uu1", 1,matStructure.getuu1());
			incrementalParams.set("jj", 1,matStructure.getjj());

			/*
			 * the final test eigenvector
			 */

			testEigenvector = smal.inremental(1, incrementalParams);
			
			MWNumericArray MWuutest = (MWNumericArray)testEigenvector[0];
			
			uutest = (double[][])MWuutest.toArray();

			/**
			 * classify the test set using the matlab classifier.
			 * CLASSIFICATION METHODS:
			 * 	1. We suggest to use as "method" parameter -->linear (linear SVM, liblinear implementation).
			 *  Optionally you can define the SVM trade-off parameter. Default is C=5.
			 *  2. in "method" parameter--> smooth to implement the smooth function.
			 *  	You have to define the training eigenvalues to compute the smooth function. For this reason
			 *  	you have to set another parameter in classifierParams. See bellow. 
			 *  	Also, you have to define the the weight of labeled samples. Default is �=100.
			 *  Thus the ParamsStructFieldsClassifier could be written as:
			 *  a. String[] ParamsStructFieldsClassifier={"uutrain","uutest", "trainLabels", "method",};
			 *  b. String[] ParamsStructFieldsClassifier={"uutrain","uutest", "trainLabels", "method","parameter"};
			 *  c. String[] ParamsStructFieldsClassifier={"uutrain","uutest", "trainLabels", "method","parameter","ddtrain"};
			 */
			
			
			/*
			 * Initialize parameters
			 * import parameters in matlab methods
			 */

			String[] ParamsStructFieldsClassifier={"uutrain","uutest", "trainLabels", "method"};
			classifierParams= new MWStructArray(1,1, ParamsStructFieldsClassifier);
			classifierParams.set("uutrain",1,matStructure.getuutrain());
			classifierParams.set("uutest",1,uutest);
			classifierParams.set("trainLabels", 1,matStructure.gettrainLabels());
			classifierParams.set("method", 1, "1"); // 1 for linear 2 for "smooth" instead of linear, for the smooth function 
//			classifierParams.set("parameter", 1, "5"); // optionally 
//			classifierParams.set("ddtrain", 1, "ddtrain"); // for the smooth function

			classificationResult = smal.classifier(2,classifierParams);

			/*
			 * the prediction scores for test set and the predicted concept
			 */
			predictedScore = (MWNumericArray)classificationResult[0];
			predictedConcept = (MWNumericArray)classificationResult[1];

			// convert to int to use it in java functions
			concept = (double[][])predictedConcept.toArray();
			
			//			System.out.println(concept);
			
			// if you need to return the score of predicted images, then you must return this variable
			score = (double[][]) predictedScore.toArray(); 

		} catch (MWException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			// free native resources
			smal.dispose();
			MWArray.disposeArray(incrementalParams);
			MWArray.disposeArray(predictedScore);
			MWArray.disposeArray(predictedConcept);
			MWArray.disposeArray(testEigenvector);
			MWArray.disposeArray(classificationResult);
			MWArray.disposeArray(classifierParams);
			MWArray.disposeArray(uutest);
		}

		return concept;
	}


}
