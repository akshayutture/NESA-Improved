package textClassificationVsmText;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class GenerateDataPoints {
	public static void main(String args[]) throws IOException{
		File[] folderNames = new File(Constants.dataToConvert).listFiles();
		String nextWord;
		ArrayList<String> wordArray;
		double[] documentVector;
		
		//Initialize the stop words
		Utils.initStopWords();
		
		//Open output file for writing
		File outfile = new File(Constants.classificationFile);
		if (!outfile.exists()) {
			outfile.createNewFile();
		}
		FileWriter fw = new FileWriter(outfile.getAbsoluteFile(),true);
		BufferedWriter bw = new BufferedWriter(fw);
		
		//Looping through all the class folders
		for (int folderIndex = 0; folderIndex<folderNames.length;folderIndex++){
			File[] fileNames = new File(folderNames[folderIndex].toString()).listFiles();
			//Looping through all the documents of 1 class
			for (File doc : fileNames){
				//Read the file and tokenize it
				wordArray = new ArrayList<String>();
				String str = Utils.readFile(doc.getAbsolutePath());
				StringTokenizer strTok = new StringTokenizer(str);
						
				//Looping through all the tokens in the wiki document
				while (strTok.hasMoreTokens()) {
					nextWord = strTok.nextToken();
					nextWord.toLowerCase();
					nextWord = nextWord.replaceAll("[^a-z0-9]", ""); //Remove punctutation marks
					if (!Utils.stopwords.contains(nextWord)){ //If the word is not a stop word - only then count its frequency
						wordArray.add(nextWord);
					}
				}
				
				//Call the getWikpediaVectorMethod
				documentVector = GetWikipediaVector.getWikipediaVector(wordArray);
				
				//Write the document vector, along with the class label
				for (int i=0;i<documentVector.length;i++){
					bw.write(documentVector[i] + ",");
				}
				bw.write(folderIndex + "\n");
			}
		}
		bw.close();
	}
}
