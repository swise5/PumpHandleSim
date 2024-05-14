
import unittest
import os
import sys
import numpy as np
import pandas as pd
from scipy.stats import shapiro
#from numpy.random import randn



class TestIfNormal(unittest.TestCase):
    
    def test_modelGeneratesEpidemicCurve(self):

        alpha = .05
        percentPassing = .8

        wd = os.getcwd()
        pythonDirPath = "src/test/python"
        pathToTarget = wd.removesuffix(pythonDirPath) + "target/PumpHandleSim-0.1.0.jar"
        print(pathToTarget)
        

        outputDir = "/Users/swise/workspace/PumpHandleSim/scratch/"
        duration = str(50)
        gridWidth = str(10)
        gridHeight = str(10)
        numPeople = str(80)

        # metrics to track quality
        normalityOfCases = 0
        normalityOfDeaths = 0
                
        numIterations = 4
        for seed in range(numIterations):
            outputFile = outputDir + "testingNormality_" + str(seed) + ".csv"
            command = " ".join(["java -classpath", pathToTarget, "sim.SimWrapper ", duration, outputFile, str(seed), gridWidth, gridHeight, numPeople])
            os.system(command)
            res = pd.read_table(outputFile, sep=',', header=None)
            fit_cases = shapiro(res.loc[0]) # cases
            fit_deaths = shapiro(res.loc[1]) # deaths
            
            normalityOfCases += fit_cases.pvalue < alpha
            normalityOfDeaths += fit_deaths.pvalue < alpha
            #print(np.array(res[0]))
        
        normalityOfCases /= numIterations
        normalityOfDeaths /= numIterations
        
        self.assertGreater(normalityOfCases, percentPassing, "Target is: at least {} of instances have normality at level {}".format(percentPassing * 100, alpha))
        self.assertGreater(normalityOfDeaths, percentPassing, "Target is: at least {} of instances have normality at level {}".format(percentPassing * 100, alpha))