#Importing modules
import numpy as np
import sklearn
from sklearn.neighbors import KNeighborsClassifier
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import cross_val_score
from sklearn.metrics import accuracy_score, make_scorer


#Reading in the data
train_data = []
with open('trainData.txt', 'r') as f:
    for temp in f:
    	problem = False
    	tempSplit = temp.split(",")
    	b = []
    	for entry in tempSplit:
    		if (len(entry)>3):
	    		if (entry[1]=='.' and entry[3]=='.'):
	    			problem = True
	    			entry = entry[2:]
    		b.append (float(entry))
    	if (problem == False):
        	train_data.append(b)
print "Finished Reading train data..."

test_data = []
with open('testData.txt', 'r') as f:
    for temp in f:
        test_data.append([float(x) for x in temp.split(',')])
print "Finished Reading test data..."

#converting to numpy array
train_data = np.array(train_data)
test_data = np.array(test_data)

#Splitting into input and labels
train_data_x = train_data[:,range(0,train_data.shape[1]-1)]
train_data_y = train_data[:,[train_data.shape[1]-1]]

test_data_x = test_data[:,range(0,test_data.shape[1]-1)]
test_data_y = test_data[:,[test_data.shape[1]-1]]

scaling = StandardScaler()
scaling.fit_transform(train_data_x)
scaling.fit_transform(test_data_x)

#making the prediction
gnb = KNeighborsClassifier()
gnb.fit(train_data_x,train_data_y)
print "Finished fitting to train data..."
predictedOutput = gnb.predict(test_data_x)
print "Finished predicting test data..."
#Computing the accuracy
totalCorrect = 0.0
for i in range(len(predictedOutput)):
	if (predictedOutput[i]==test_data_y[i]):
		totalCorrect+=1
print "Accuracy =",totalCorrect/len(predictedOutput)