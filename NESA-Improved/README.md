# NLP PROJECT README (Non-Orthogonal ESA)

Read the Design Document to understand the theory for the Project

A] LIBRARIES REQUIRED FOR RUNNING
	1. The JAMA library (Java), used for SVD in the NESA-LSA code is provided as a JAR file in the 'lib' folder
	2. The scikit-learn library (Python) used for the classification task, can be installed using 'pip' or 'conda'. The installation link is provided in the 'lib' folder
	
	
B] DATA REQUIRED FOR RUNNING
	1. The dataset for text classification is the 20 NewsGroup dataset. It can be downloaded from 'http://qwone.com/~jason/20Newsgroups/'
	2. A sample set of Wikipedia articles is provided in the 'data' folder. The code will run with this data, but accuracy on the text classification will be bad obviously. The actual set of articles used is very large. The set of articles used is given in 'data/actualData' 
	
	
C] COMPILING

	1. Run 'make clean' to clear any old version of .class files, from the folder you are currently in
	1. Run the command 'make' from the folder you are currently in.

D] EXPLANATION OF CODE STRUCTURE

- Code is located in the 'src' folder

	1. The 'classifiers' folder contains 2 python files - each of them does the classification of the features extracted from the test. (1 for KNN classifier and the other is a Random Forest Classifier)
		
	2. Each of the folders starting with 'textClassification' inside the 'src' folder represent one of the NESA/ESA methods for extracting features from the text files of the 20 NewsGroup dataset. Each folder is for a seperate method.
	
E] RUNNING THE CODE

- Run steps 1,2 and 3, once for the train data, and once for the test data

	1. Set the folder names of the text classification data set and wikipedia articles in the 'Constants' class.
	
	2. Next, for 'NESA-LSA' and 'VSM-text' run the main functions in the 'ComputeInvertedIndex' and 'ComputeCorrelationMatrix' in that order. For the other methods do nothing.
	
	3. Run the main function located in the 'GenerateDataPoints' class in any of the 'textClassification' folders. This extracts the features into a file. 
	
	4. Now that we have the test and train data, copy them into the 'classifiers' folder. Now run 'python randomForestClassifier.py' from the terminal, after moving into the 'classifiers' folder. The accuracy will be printed on the screen.
