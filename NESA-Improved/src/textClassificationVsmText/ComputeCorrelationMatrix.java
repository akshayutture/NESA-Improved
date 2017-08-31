package textClassificationVsmText;

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
		double [][] correlationMatrix = new double [numberOfWikiDocuments][numberOfWikiDocuments];
		
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
						transformationMatrix[i][wordToWordIndex.get(nextWord)] += 1;					}				
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
				if (transformationMatrix[i][wordToWordIndex.get(word)]!=0){
					numberOfDocumentsWordAppearsIn += 1;
				}
			}
			double inverseDocumentFrequency = Math.log((double)numberOfWikiDocuments/(double)numberOfDocumentsWordAppearsIn);
			
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
		
		//Now multiplying these 2 to get the correlation matrix -standard multiplication code
		for (int i=0;i<numberOfWikiDocuments;i++){
			for (int j=0;j<numberOfWikiDocuments;j++){
				for (int k=0;k<wordToWordIndex.size();k++){
					correlationMatrix[i][j] += transformationMatrix[i][k]*transformationMatrixTranspose[k][j];
				}
			}
		}

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
