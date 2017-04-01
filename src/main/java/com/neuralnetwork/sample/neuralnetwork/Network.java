package com.neuralnetwork.sample.neuralnetwork;
import cern.colt.function.DoubleDoubleFunction;
import cern.colt.function.DoubleFunction;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import com.neuralnetwork.sample.model.ImageModel;
import java.util.ArrayList;
import java.util.List;
/**
 * Neural Network
 */
public class Network {
    private int[] sizes = null;
    private int layerNum = -1;
    private List<ImageModel> trainData = null;
    private int iterNums = 0;
    private double learningRatio = 0.0;
    private List<DenseDoubleMatrix2D> weightMatrixList = null;
    private List<DenseDoubleMatrix2D> biasMatrixList = null;
    private boolean train = false;

    public Network(int[] sizes){
        this.sizes = sizes;
        this.layerNum = sizes.length;

        this.initWeightAndBais();
    }

    public void initWeightAndBais(){
        this.weightMatrixList = new ArrayList<DenseDoubleMatrix2D>();
        this.biasMatrixList = new ArrayList<DenseDoubleMatrix2D>();

        //initialize weight and bias
        for(int i=0;i<this.layerNum-1;i++){
            int colNumber = this.sizes[i];
            int rowNumber = this.sizes[i+1];
            DenseDoubleMatrix2D weightMatrix = new DenseDoubleMatrix2D(rowNumber,colNumber);
            double[][] randomMatrix = new double[rowNumber][colNumber];

            for(int m=0;m<rowNumber;m++){
                for(int n=0;n<colNumber;n++){
                    //[0,1) double
                    randomMatrix[m][n] = Math.random()-0.5;
                }
            }
            weightMatrix.assign(randomMatrix);
            weightMatrixList.add(weightMatrix);
        }

        for(int i=1;i<this.layerNum;i++){
            int colNumber = 1;
            int rowNumber = this.sizes[i];
            DenseDoubleMatrix2D biasMatrix = new DenseDoubleMatrix2D(rowNumber,colNumber);
            double[][] randomMatrix = new double[rowNumber][colNumber];

            for(int m=0;m<rowNumber;m++){
                for(int n=0;n<colNumber;n++){
                    //[0,1) double
                    randomMatrix[m][n] = Math.random()-0.5;
                }
            }
            biasMatrix.assign(randomMatrix);
            biasMatrixList.add(biasMatrix);
        }
    }

    DoubleDoubleFunction plus = new DoubleDoubleFunction() {
        public double apply(double a, double b) { return a+b; }
    };
    DoubleFunction sigmoid = new DoubleFunction(){
        public double apply(double v) {
            return 1.0/(1.0+Math.exp(-1*v));
        }
    };
    DoubleDoubleFunction errFunction1 = new DoubleDoubleFunction(){
        public double apply(double v, double v1) {
            return v*(1-v)*(v1-v);
        }
    };
    DoubleFunction errFunction2 = new DoubleFunction(){
        public double apply(double v) {
            return v*(1-v);
        }
    };
    DoubleFunction learnRatioFunction1 = new DoubleFunction(){
        public double apply(double v) {
            return v*learningRatio;
        }
    };

    public int predict(double[] input){
        //calculate input and output for each hidden level and output level
        DenseDoubleMatrix2D inputMatrix = new DenseDoubleMatrix2D(input.length,1);
        double[][] transInput = new double[input.length][1];
        for(int k=0;k<input.length;k++){
            transInput[k][0] = input[k];
        }
        inputMatrix.assign(transInput);
        DenseDoubleMatrix2D[] pureInput = new DenseDoubleMatrix2D[layerNum];
        DenseDoubleMatrix2D[] pureOutput = new DenseDoubleMatrix2D[layerNum];

        //input level output equals input
        pureOutput[0] = inputMatrix;

        for(int m=1;m<layerNum;m++){
            pureInput[m] = (DenseDoubleMatrix2D)Algebra.DEFAULT.mult(weightMatrixList.get(m-1),pureOutput[m-1]);
            pureInput[m] = (DenseDoubleMatrix2D)pureInput[m].assign(biasMatrixList.get(m - 1), plus);

            pureOutput[m] = (DenseDoubleMatrix2D)pureInput[m].assign(sigmoid);
        }

        double rtn = Double.MIN_VALUE;
        int record = -1;
        double[][] resultOut = pureOutput[layerNum-1].toArray();
        for(int i=0;i<resultOut.length;i++){
            if(Double.compare(resultOut[i][0],rtn) > 0){
                rtn = resultOut[i][0];
                record = i;
            }
        }

        return record;
    }

    public void SGD(List<ImageModel> trainData, int iterNums, final double learningRatio){
        this.trainData = trainData;
        this.iterNums = iterNums;
        this.learningRatio = learningRatio;
        this.initWeightAndBais();

        for(int i=0;i<iterNums;i++){
            for(int j=0;j<trainData.size();j++){
                double[] input = trainData.get(j).getGrayMatrix();
                double[] output = trainData.get(j).getOutputList();

                //calculate input and output for each hidden level and output level
                DenseDoubleMatrix2D inputMatrix = new DenseDoubleMatrix2D(input.length,1);
                DenseDoubleMatrix2D outputMatrix = new DenseDoubleMatrix2D(output.length,1);
                double[][] transInput = new double[input.length][1];
                double[][] transOutput = new double[output.length][1];
                for(int k=0;k<input.length;k++){
                    transInput[k][0] = input[k];
                }
                inputMatrix.assign(transInput);
                for(int k=0;k<output.length;k++){
                    transOutput[k][0] = output[k];
                }
                outputMatrix.assign(transOutput);

                DenseDoubleMatrix2D[] pureInput = new DenseDoubleMatrix2D[layerNum];
                DenseDoubleMatrix2D[] pureOutput = new DenseDoubleMatrix2D[layerNum];

                //input level output equals input
                pureOutput[0] = inputMatrix;

                for(int m=1;m<layerNum;m++){
                    pureInput[m] = (DenseDoubleMatrix2D)Algebra.DEFAULT.mult(weightMatrixList.get(m-1),pureOutput[m-1]);
                    pureInput[m] = (DenseDoubleMatrix2D)pureInput[m].assign(biasMatrixList.get(m - 1), plus);

                    pureOutput[m] = (DenseDoubleMatrix2D)pureInput[m].assign(sigmoid);
                }

                //backdrop
                DenseDoubleMatrix2D[] errMatrix = new DenseDoubleMatrix2D[layerNum];
                DenseDoubleMatrix2D copyPureOutput = new DenseDoubleMatrix2D(pureOutput[layerNum-1].toArray().length,
                        pureOutput[layerNum-1].toArray()[0].length);
                copyPureOutput.assign(pureOutput[layerNum - 1]);
                errMatrix[layerNum-1] = (DenseDoubleMatrix2D)copyPureOutput.assign(outputMatrix, errFunction1);
                for(int m=layerNum-2;m>=0;m--){
                    errMatrix[m] = (DenseDoubleMatrix2D)Algebra.DEFAULT.mult(Algebra.DEFAULT.transpose(weightMatrixList.get(m)),errMatrix[m+1]);
                    DenseDoubleMatrix2D copyPureOutput1 = new DenseDoubleMatrix2D(pureOutput[m].toArray().length,
                            pureOutput[m].toArray()[0].length);
                    copyPureOutput1.assign(pureOutput[m]);
                    DenseDoubleMatrix2D tempOut = (DenseDoubleMatrix2D)copyPureOutput1.assign(errFunction2);
                    double[][] errTemp = errMatrix[m].toArray();
                    double[][] outTemp = tempOut.toArray();
                    double[][] rtnTemp = new double[errTemp.length][1];
                    for(int u=0;u<errTemp.length;u++){
                        rtnTemp[u][0] = errTemp[u][0]*outTemp[u][0];
                    }
                    errMatrix[m] = (DenseDoubleMatrix2D) new DenseDoubleMatrix2D(errTemp.length,1).assign(rtnTemp);
                }

                for(int m=layerNum-2;m>=0;m--){
                    DenseDoubleMatrix2D deltaWeight = (DenseDoubleMatrix2D)Algebra.DEFAULT.mult(errMatrix[m + 1], Algebra.DEFAULT.transpose(pureOutput[m]));
                    deltaWeight = (DenseDoubleMatrix2D)deltaWeight.assign(learnRatioFunction1);
                    DenseDoubleMatrix2D copyWeight = new DenseDoubleMatrix2D(weightMatrixList.get(m).toArray().length,
                            weightMatrixList.get(m).toArray()[0].length);
                    copyWeight.assign(weightMatrixList.get(m));
                    weightMatrixList.set(m,(DenseDoubleMatrix2D)copyWeight.assign(deltaWeight,plus));

                    DenseDoubleMatrix2D copyErrorMatrix = new DenseDoubleMatrix2D(errMatrix[m+1].toArray().length,
                            errMatrix[m+1].toArray()[0].length);
                    copyErrorMatrix.assign(errMatrix[m+1]);
                    DenseDoubleMatrix2D deltaBias = (DenseDoubleMatrix2D)copyErrorMatrix.assign(learnRatioFunction1);
                    DenseDoubleMatrix2D copyBiasMatrix = new DenseDoubleMatrix2D(biasMatrixList.get(m).toArray().length,
                            biasMatrixList.get(m).toArray()[0].length);
                    copyBiasMatrix.assign(biasMatrixList.get(m));
                    biasMatrixList.set(m, (DenseDoubleMatrix2D) copyBiasMatrix.assign(deltaBias, plus));
                }
            }

            System.out.println("complete train epoch:"+i);
            double ratio = printCorrentRatio(trainData);
            if(Double.compare(ratio,0.95) > 0){
                break;
            }
        }
        train = true;
        System.out.println("train complete");
    }

    public double printCorrentRatio(List<ImageModel> list){
        int correctNum = 0;
        for(int i=0;i<list.size();i++){
            if(this.predict(list.get(i).getGrayMatrix()) == list.get(i).getDigit()){
                correctNum ++;
            }
        }
        System.out.println("correct ratio:"+correctNum+"/"+list.size());

        return correctNum/Double.valueOf(list.size());
    }

    public boolean isTrain() {
        return train;
    }
}
