package textClassificationNesaLsa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.StringTokenizer;

public class ComputeInvertedIndex {
	public static void main (String [] args) throws IOException{
		//Declarations
		/*inverted index - is a hashmap where the keys are the words and the values are vectors representing the
		 * the frequency of words in the wikipedia articles. This vector itself is implemented as a hashmap, where
		 * the key is the index number of the wikipedia article, and the value is the frequency of that word in that 
		 * wikipedia article
		 */
		HashMap<String, double[]> invertedIndex = new HashMap<String, double[]>();
		File[] documentNames = new File(Constants.wikiDocumentsFolder).listFiles();
		int numberOfWikiArticles = documentNames.length;
		String nextWord;
		
		//Initialize the stop words
		Utils.initStopWords();
		
		//READING ONE DOCUMENT AT A TIME
		for (int i=0;i<documentNames.length;i++){
			//Read the file and tokenize it
			String str = Utils.readFile(documentNames[i].getAbsolutePath());
			StringTokenizer strTok = new StringTokenizer(str);
					
			//Looping through all the tokens in the wiki document
			while (strTok.hasMoreTokens()) {
				nextWord = strTok.nextToken();
				nextWord.toLowerCase();
				nextWord = nextWord.replaceAll("[^a-z0-9]", ""); //Remove punctutation marks
				if (!Utils.stopwords.contains(nextWord)){ //If the word is not a stop word - only then count its frequency
					//If the word doesn't have an entry in the inverted index list, add it
					if (!invertedIndex.containsKey(nextWord)){
						invertedIndex.put(nextWord, new double[numberOfWikiArticles]);
					}
					//Increment the frequency of this document in the the inverted index entry for this word
					double [] invertedIndexEntryForThisWord = invertedIndex.get(nextWord);
					invertedIndexEntryForThisWord[i] += 1;
				}
			}
		}
		
		//SERIALIZING AND WRITING THE INVERTED INDEX OBJECT TO A FILE
		try {
	         FileOutputStream fileOut = new FileOutputStream(Constants.invertedIndexSerFile);
	         ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         out.writeObject(invertedIndex);
	         out.close();
	         fileOut.close();
	         System.out.printf("Serialized inverted index is saved in " + Constants.invertedIndexSerFile);
	      }catch(IOException i) {
	         i.printStackTrace();
	      }
	}
}
