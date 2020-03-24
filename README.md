# NEAT-Library
Attempting to create a NEAT library that I can use for future projects in Java. Will be based off YouTube and the original paper: http://nn.cs.utexas.edu/downloads/papers/stanley.ec02.pdf

To use this package, a few changes a required. In your code you must create the Evaluator instance but also override the evaluateGenome function to work with the project you are doing.

Also in the NeuralNetwork instance, you need to specify the number of inputs and outputs by creating a "genesis" genome. This should just have the input neurons and output neurons.

Main functions to call are:\
    1. evaluate in Evaluator\
    2. feedForward in NeuralNetwork