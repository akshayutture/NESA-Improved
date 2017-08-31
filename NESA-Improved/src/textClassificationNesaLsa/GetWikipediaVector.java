package textClassificationNesaLsa;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class GetWikipediaVector {
	//declarations
	public static boolean firstTimeComputation = true;
	public static HashMap<String, double[]> newInvertedIndex;
	public static double [][] correlationMatrix;
	public static double[] getWikipediaVector (ArrayList<String> words){
		
		//If it is the first time you are using the function, you need to deserialize the inverted index and correlation matrix, and multiply them
		if (firstTimeComputation==true){
			double[] invertedIndexForWord;
			newInvertedIndex = deserializeInvertedIndex(Constants.invertedIndexSerFile);
			correlationMatrix = deserializeCorrelationMatrix(Constants.correlationMatrix);
			for (String word : newInvertedIndex.keySet()){
				invertedIndexForWord = newInvertedIndex.get(word);
				invertedIndexForWord = vectorMatrixMultiplication(invertedIndexForWord,correlationMatrix);
			}
			firstTimeComputation = false;
		}
	
		//Declarations
		double[] documentWikipediaRepresentation = new double[correlationMatrix.length];
		double[] wordWikipediaRepresentation = new double[correlationMatrix.length];
		
		//Computing the wikipedia vector for the whole input -by adding the wikipedia vector for every word
		for (String word : words){
			if (newInvertedIndex.containsKey(word)){
				wordWikipediaRepresentation = newInvertedIndex.get(word);
				for (int i=0;i<documentWikipediaRepresentation.length;i++){
					documentWikipediaRepresentation[i] += wordWikipediaRepresentation[i];
				}
			}
		}
		//Return the document wikipedia vector
		return documentWikipediaRepresentation;
	}
	
	private static double[] vectorMatrixMultiplication(double[] vector,double[][] matrix) {
		int answerLength = matrix[0].length;
		double[] answer = new double[answerLength];
		for (int i=0;i<answerLength;i++){
			for (int j=0;j<vector.length;j++){
				answer[i] += vector[j]*matrix[j][i];
			}
		}
		return answer;
	}

	/*HELPER FUNCTIONS*/
	public static void main(String args[]){
		String[] article = new String[2];
		article[0] = "machine";
		article[0] = "learning";
		//getWikipediaVector(article);
	}
	
	//This function deserializes the inverted index
	public static HashMap<String, double[]> deserializeInvertedIndex(String serializedFileName){
		HashMap<String, double[]> deserializedObject = null;
		try {
			FileInputStream fileIn = new FileInputStream(serializedFileName);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			deserializedObject = (HashMap<String, double[]>)in.readObject();
			in.close();
			fileIn.close();
			}catch(IOException i) {
				i.printStackTrace();
			}catch(ClassNotFoundException c) {
				System.out.println("Employee class not found");
				c.printStackTrace();
			}
		return deserializedObject;
	}
	
	//This function deserializes any object in the filename specified in the argument
	public static double[][] deserializeCorrelationMatrix(String serializedFileName){
		double[][] deserializedObject = null;
		try {
			FileInputStream fileIn = new FileInputStream(serializedFileName);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			deserializedObject = (double[][])in.readObject();
			in.close();
			fileIn.close();
			}catch(IOException i) {
				i.printStackTrace();
			}catch(ClassNotFoundException c) {
				System.out.println("Employee class not found");
				c.printStackTrace();
			}
		return deserializedObject;		
	}
}
