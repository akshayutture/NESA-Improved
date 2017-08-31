package textClassificationNesaLsa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import Jama.*;

public class ComputeCorrelationMatrix {
	
	public static void main (String args[]) throws IOException{
	
		//INITIALIZATIONS
	
		//Initialize the stop words
		Utils.initStopWords();
		
		//Getting the names of all the documents
		File[] documentNames = new File(Constants.wikiDocumentsFolder).listFiles();
		int numberOfWikiDocuments = documentNames.length;
	
		//First get the inverted index by deserialization
		HashMap<String, double[]> invertedIndex = deserializeInvertedIndex(Constants.invertedIndexSerFile);
		
		//Create a word to index mapping, so that in all arrays we can identify which column stands for which word
		HashMap<String, Integer> wordToWordIndex = new HashMap<String, Integer>();
		int nextWordIndex = 0;
		for (String word : invertedIndex.keySet()){
			wordToWordIndex.put(word, nextWordIndex);
			nextWordIndex += 1;
		}
		
		//Declarations
		double [][] transformationMatrix = new double [numberOfWikiDocuments][wordToWordIndex.size()];
		double [][] transformationMatrixTranspose = new double [wordToWordIndex.size()][numberOfWikiDocuments];
		double [][] correlationMatrix;
		
		//READING ONE DOCUMENT AT A TIME
		
		for (int i=0;i<documentNames.length;i++){
			//Read the file and tokenize it
			String str = Utils.readFile(documentNames[i].getAbsolutePath());
			StringTokenizer strTok = new StringTokenizer(str);
			
			//Create a new document object for this document
			String nextWord;
			
			//Looping through all the tokens in the text
			while (strTok.hasMoreTokens()) {
				nextWord = strTok.nextToken();
				nextWord = nextWord.toLowerCase();
				nextWord = nextWord.replaceAll("[^a-z0-9A-Z]", ""); //Remove punctutation marks
				if (!Utils.stopwords.contains(nextWord)){ //If the word is not a stop word - only then count its frequency	
					if (wordToWordIndex.containsKey(nextWord)){
						transformationMatrix[i][wordToWordIndex.get(nextWord)] += 1;
					}				
				}
			}
		}
		
		//MULTIPLYING THE INVERSE DOCUMENT FREQUENCY (IDF) TERM FOR EVERY CELL OF THE TRANSFORMATION MATRIX
		//Looping through all the words in the inverted index
		for (Map.Entry<String, double[]> entry : invertedIndex.entrySet()){
			//Compute the inverse document frequency for that word
			String word = entry.getKey();
			double[] invertedIndexForWord = entry.getValue();
			//Calculating the IDF for this word
			int numberOfDocumentsWordAppearsIn = 0;
			for (int i=0;i<numberOfWikiDocuments;i++){
				if (invertedIndexForWord[i]!=0){
					numberOfDocumentsWordAppearsIn += 1;
				}
			}
			double inverseDocumentFrequency = Math.log((double)numberOfWikiDocuments/(double)numberOfDocumentsWordAppearsIn);
			if (numberOfDocumentsWordAppearsIn==0){
				System.out.println();
			}
			//Go and update all the entries for this word in the transformation matrix
			for (int i=0;i<numberOfWikiDocuments;i++){
				if (wordToWordIndex.containsKey(word)){
					transformationMatrix[i][wordToWordIndex.get(word)] *= inverseDocumentFrequency;
				}
			}
		}
		
		//COMPUTING THE CORRELATION MATRIX
		//first compute the transpose of the transformation matrix
		for (int i=0;i<numberOfWikiDocuments;i++){
			for (int j=0;j<wordToWordIndex.size();j++){
				transformationMatrixTranspose [j][i] = transformationMatrix[i][j];
			}
		}
		//call the lsa function to compute the correlation
		correlationMatrix = computeCorrelationMatrixUsingLSA(transformationMatrixTranspose);

		//SERIALIZING AND WRITING THE INVERTED INDEX OBJECT TO A FILE
		try {
	         FileOutputStream fileOut = new FileOutputStream(Constants.correlationMatrix);
	         ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         out.writeObject(correlationMatrix);
	         out.close();
	         fileOut.close();
	         System.out.printf("Transformation Matrix is saved in " + Constants.correlationMatrix);
	      }catch(IOException i) {
	         i.printStackTrace();
	      }
	}
	//DOING AN LSA ON THE TERM-WIKIPEDIA DOCUMENT MATRIX
	private static double[][] computeCorrelationMatrixUsingLSA(double[][] termDocumentMatrix) {
		//Doing an SVD on the termDocumentMatrix
		Matrix originalMatrix = new Matrix(termDocumentMatrix); // convert to the jama matrix format
		SingularValueDecomposition svd =new SingularValueDecomposition(originalMatrix) ; 
		Matrix U = svd.getU();
		Matrix V = svd.getV(); 
		Matrix S = svd.getS();

		//Reducing the number of dimensions
		int reduceNumberOfDimensions = 10;
		double[][] reducedU = chooseKcolumns(reduceNumberOfDimensions,U.getArrayCopy());
		double[][] reducedS = chooseKcolumns(reduceNumberOfDimensions,S.getArrayCopy());
		reducedS = chooseKRows(reduceNumberOfDimensions,reducedS);
		double[][] reducedV = chooseKRows(reduceNumberOfDimensions,V.getArrayCopy());
		
		//Multiply U*S*V to recover the approximation of the original matrix
		double[][] reducedVtranspose = ((new Matrix(reducedV)).transpose()).getArrayCopy();
		double[][] reducedTermDocumentMatrix = matrixMultiply(matrixMultiply(reducedU,reducedS),reducedV);
		double[][] reducedTermDocumentMatrixTranspose = ((new Matrix(reducedTermDocumentMatrix)).transpose()).getArrayCopy();
		
		//Now multiplying reducedTermDocumentMatrix with its transpose to get document-document similarity
		double[][] correlationMatrix = matrixMultiply(reducedTermDocumentMatrixTranspose, reducedTermDocumentMatrix);
		return correlationMatrix;
	}

	private static double[][] matrixMultiply(double[][] matrix1,double[][] matrix2) {
		double[][] answer = new double [matrix1.length][matrix2[0].length];
		for (int i=0;i<matrix1.length;i++){
			for (int j=0;j<matrix2[0].length;j++){
				for (int k=0;k<matrix1[0].length;k++){
					answer[i][j] += matrix1[i][k]*matrix2[k][j];
				}
			}
		}
		return answer;
	}
	private static double[][] chooseKcolumns(int reducedNumberOfDimensions,double[][] array) {
		double[][] answer = new double [array.length][reducedNumberOfDimensions];
		for (int i=0;i<array.length;i++){
			for (int j=0;j<reducedNumberOfDimensions;j++){
				answer[i][j] = array[i][j];
			}
		}
		return answer;
	}
	
	
	private static double[][] chooseKRows(int reducedNumberOfDimensions,double[][] array) {
		double[][] answer = new double [reducedNumberOfDimensions][array[0].length];
		for (int i=0;i<reducedNumberOfDimensions;i++){
			for (int j=0;j<array[0].length;j++){
				answer[i][j] = array[i][j];
			}
		}
		return answer;
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
}
