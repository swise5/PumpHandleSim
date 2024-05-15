
import unittest
import os
import sys
import numpy as np
import pandas as pd
from scipy.stats import shapiro
#from numpy.random import randn



class TestPomPatterns(unittest.TestCase):
    
    def util_exportPropertiesFile(self, filename, varDict):
        with open(filename, 'w') as f:
            for x in varDict:
                f.write("{}={}\n".format(x, varDict[x]))
    
    def test_modelGeneratesEpidemicCurve(self):
        
        # standards at which we're testing the pattern
        alpha = .05
        percentPassing = .8

        # paths to important data
        baseDir = os.getcwd().removesuffix("src/test/python")
        pathToTarget = baseDir + "target/PumpHandleSim-0.1.0.jar"
        outputDir = baseDir + "scratch/"
        paramFile = baseDir + "src/test/python/resources/epiCurvePDDTest.properties"

        # export the properties file
        self.util_exportPropertiesFile(paramFile, {"numPeople": 80, "gridWidth": 10, "gridHeight": 10, "numInitialCases":1})

        print(paramFile)
        # metrics to track quality
        normalityOfCases = 0
        normalityOfDeaths = 0

        # controlling the instances                
        numIterations = 4
        duration = str(50)
        for seed in range(numIterations):
            outputFile = outputDir + "testingNormality_" + str(seed) + ".csv"
            command = " ".join(["java -classpath", pathToTarget, "sim.SimWrapper ", str(seed), duration, paramFile, outputFile])
            os.system(command)
            res = pd.read_table(outputFile, sep=',', header=None)
            fit_cases = shapiro(res.loc[0]) # cases
            fit_deaths = shapiro(res.loc[1]) # deaths
            
            normalityOfCases += fit_cases.pvalue < alpha
            normalityOfDeaths += fit_deaths.pvalue < alpha
        
        normalityOfCases /= numIterations
        normalityOfDeaths /= numIterations
                
        failedErrorMessage = "Failed EPIDEMIC CURVE pattern for {} - Outcome: {}% versus Target: {}% of instantiations have normality at level {}"
        self.assertGreater(normalityOfCases, percentPassing, failedErrorMessage.format("CASES", normalityOfCases * 100, percentPassing * 100, alpha))
        self.assertGreater(normalityOfDeaths, percentPassing, failedErrorMessage.format("DEATHS", normalityOfDeaths * 100, percentPassing * 100, alpha))