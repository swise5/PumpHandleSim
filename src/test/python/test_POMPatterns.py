
import unittest
import os
import sys
import numpy as np
import pandas as pd
from scipy.stats import shapiro
#from numpy.random import randn
import matplotlib.pyplot as plt


class TestPomPatterns(unittest.TestCase):
    
    def util_exportPropertiesFile(self, filename, varDict):
        with open(filename, 'w') as f:
            for x in varDict:
                f.write("{}={}\n".format(x, varDict[x]))
    
    def util_runWrappedJarFile(self, pathToTarget, seed, duration, paramFile, outputFile):
        command = " ".join(["java -classpath", pathToTarget, "sim.SimWrapper ", str(seed), duration, paramFile, outputFile])
        os.system(command)
        res = pd.read_table(outputFile, sep=',', header=None)
        return {"cases": res.loc[0], "deaths": res.loc[1]}

    def util_exportHistograms(self, data, labels, outputFilename, xmax=10000, alpha=.5):
        numRows = int(np.ceil(len(labels)/2))
        fig, axes = plt.subplots(nrows=numRows, ncols=2)

        axesIndex = 0
        for label in labels:
            ax = axes[axesIndex]
            
            #bins = np.linspace(0,  max(data[label]))
            ax.hist(data[label], alpha=alpha, label=label, histtype='step')# bins=bins, )
            ax.set_title(label)
            axesIndex += 1
    
        #plt.show()
        plt.savefig(outputFilename)  
    
    def util_exportMultiTraces(self, data, labels, outputFilename, xmax=10000, alpha=.5):
        numRows = int(np.ceil(len(labels)/2))
        fig, axes = plt.subplots(nrows=numRows, ncols=2)

        axesIndex = 0
        for label in labels:
            ax = axes[axesIndex]
            
            runs = data[label]
            for run in runs:
                ax.plot(run, alpha=alpha)# bins=bins, )
            ax.set_title(label)
            axesIndex += 1
    
        #plt.show()
        plt.savefig(outputFilename)      
    
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
        self.util_exportPropertiesFile(paramFile, {"percPeople": 80, "gridWidth": 50, "gridHeight": 50, "numInitialCases":1})

        print(paramFile)
        # metrics to track quality
        normalityOfCases = 0
        normalityOfDeaths = 0
        recOfCases = []
        recOfDeaths = []

        # controlling the instances                
        numIterations = 10
        duration = str(50)
        for seed in range(numIterations):
            outputFile = outputDir + "testingNormality_" + str(seed) + ".csv"
            res = self.util_runWrappedJarFile(pathToTarget, seed, duration, paramFile, outputFile)
            fit_cases = shapiro(res["cases"]) # cases
            fit_deaths = shapiro(res["deaths"]) # deaths
            
            normalityOfCases += fit_cases.pvalue < alpha
            normalityOfDeaths += fit_deaths.pvalue < alpha
            recOfCases.append(res["cases"])
            recOfDeaths.append(res["deaths"])
            
            print(".")

        normalityOfCases /= numIterations
        normalityOfDeaths /= numIterations

        # export image
        dataHolder = {"Cases": recOfCases, "Deaths": recOfDeaths}
        #self.util_exportHistograms(dataHolder, dataHolder.keys(), outputDir + "hist.png")
        #self.util_exportMultiTraces(dataHolder, dataHolder.keys(), outputDir + "epidemicCurveVisualisation.png", alpha=.1)
                
        failedErrorMessage = "Failed EPIDEMIC CURVE pattern for {} - Outcome: {}% versus Target: {}% of instantiations have normality at level {}"
        self.assertGreater(normalityOfCases, percentPassing, failedErrorMessage.format("CASES", normalityOfCases * 100, percentPassing * 100, alpha))
        self.assertGreater(normalityOfDeaths, percentPassing, failedErrorMessage.format("DEATHS", normalityOfDeaths * 100, percentPassing * 100, alpha))