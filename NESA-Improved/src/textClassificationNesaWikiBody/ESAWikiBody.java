package textClassificationNesaWikiBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class ESAWikiBody {

	private HashMap<String, HashMap<String, Double>> invertedIndex;

	private double[][] transformationMatrix;

	private double[][] correlationMatrix;
	
	private File[] concepts;

	public ESAWikiBody(HashMap<String, HashMap<String, Double>> invertedIndex,
			File[] concepts) {
		this.invertedIndex = invertedIndex;
		this.concepts = concepts;
		transformationMatrix = new double[concepts.length][concepts.length];
		correlationMatrix = new double[concepts.length][concepts.length];
	}

	public HashMap<String, HashMap<String, Double>> wikiBodyIndex = new HashMap<String, HashMap<String, Double>>();

	public void initWikiBody() throws IOException, ClassNotFoundException {
		File wikiBodyIndexFile = new File("./resources/wikiBodyTransformation.ser");
		if (!wikiBodyIndexFile.exists()) {
			for (File concept : concepts) {
				String documentPath = concept.getAbsolutePath();
				StringTokenizer strTok = new StringTokenizer(
						Utils.readFile(documentPath));
				HashMap<String, Double> conceptVector = new HashMap<String, Double>();
				for (int i = 0; i < concepts.length; i++) {
					conceptVector.put(concepts[i].getAbsolutePath(), 0.0);
				}
				while (strTok.hasMoreTokens()) {
					String token = strTok.nextToken().toLowerCase();
					token = token.replaceAll("[^a-z0-9A-Z]", "");
					if (!invertedIndex.containsKey(token))
						continue;
					else {
						double idf = concepts.length/invertedIndex.get(token).size();
						for (Entry<String, Double> entry : invertedIndex.get(
								token).entrySet()) {
							double tfidf = conceptVector.get(entry.getKey());
							tfidf += entry.getValue() * idf;
							conceptVector.put(entry.getKey(), tfidf);
						}
					}
				}
				wikiBodyIndex.put(concept.getAbsolutePath(), conceptVector);
			}
			initTransformationMatrix();
			FileOutputStream fileOut = new FileOutputStream(
					"./resources/wikiBodyTransformation.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(transformationMatrix);
			out.close();
			fileOut.close();
		}else{
			FileInputStream fileIn = new FileInputStream("./resources/wikiBodyTransformation.ser");
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         transformationMatrix = (double[][]) in.readObject();
	         correlationMatrix = squareMatrixMultiplication(transformationMatrix, squareTranspose(transformationMatrix));
	         in.close();
	         fileIn.close();
		}
	}
	
	private static double[][] squareMatrixMultiplication(double[][] matrix1,double[][] matrix2){
		int size = matrix1.length;
		double[][] resultMatrix = new double[size][size];
		
		for(int i = 0;i < size;i++){
			for(int j = 0;j < size;j++){
				resultMatrix[i][j] = 0.0;
			}
		}
		
		for(int i = 0;i < size;i++){
			for(int j = 0;j < size;j++){
				for(int k = 0;k < size;k++){
					resultMatrix[i][j] += matrix1[i][k]*matrix2[k][j];
				}
			}
		}
		return resultMatrix;
	}
	
	private double[][] squareTranspose(double[][] matrix){
		int size = matrix.length;
		double[][] resultMatrix = new double[size][size];
		for(int i = 0;i < size;i++){
			for(int j = 0;j < size;j++){
				resultMatrix[i][j] = matrix[j][i];
			}
		}
		return resultMatrix;
	}

	private void initTransformationMatrix() {
		int i = 0, j = 0;
		for (Entry<String, HashMap<String, Double>> conceptEntry : wikiBodyIndex
				.entrySet()) {
			j = 0;
			for (Entry<String, Double> conceptVectorEntry : conceptEntry
					.getValue().entrySet()) {
				transformationMatrix[i][j] = conceptVectorEntry.getValue();
				j++;
			}
			i++;
		}
		correlationMatrix = squareMatrixMultiplication(transformationMatrix, squareTranspose(transformationMatrix));
	}

	public ArrayList<Double> getVector(String str) {
		StringTokenizer strTok = new StringTokenizer(str);
		ArrayList<Double> vector = new ArrayList<Double>();
		for (int i = 0; i < concepts.length; i++) {
			vector.add(0.0);
		}
		while (strTok.hasMoreTokens()) {
			String token = strTok.nextToken();
			HashMap<String, Double> tfMap = invertedIndex.get(token
					.toLowerCase());
			if (tfMap == null)
				continue;
			double idf = (double)concepts.length / (double)invertedIndex.get(token).size();
			for (int i = 0; i < concepts.length; i++) {
				if (tfMap.containsKey(concepts[i].getAbsolutePath()))
					vector.set(
							i,
							vector.get(i)
									+ tfMap.get(concepts[i].getAbsolutePath())
									* idf);
			}
		}
		ArrayList<Double> transformedVector = new ArrayList<Double>();
		for (int i = 0; i < correlationMatrix.length; i++) {
			double temp = 0.0;
			for (int j = 0; j < vector.size(); j++) {
				temp += vector.get(j) * correlationMatrix[j][i];
			}
			transformedVector.add(temp);
		}
		return transformedVector;
	}
	
//	public double getSimilarity(String str1, String str2) {
//		ArrayList<Double> vector1 = getVector(str1);
//		ArrayList<Double> vector2 = getVector(str2);
//		if (Utils.getDotProduct(vector1, vector2) == 0)
//			return 0.0;
//		else {
//			double similarity = Utils.getDotProduct(vector1,
//					vector2)
//					/ Math.sqrt(Utils.getDotProduct(vector1,
//							vector1)
//							* Utils.getDotProduct(vector2,
//									vector2));
//			return similarity;
//		}
//	}
}
