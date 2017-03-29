package com.neuralnetwork.sample.model;
/*
Copyright [2017] [Pi Jing]

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
public class ImageModel {
    private double[] grayMatrix = null;
    private int digit = -1;
    private double[] outputList = new double[10];

    public ImageModel(double[] grayMatrix,int digit){
        this.grayMatrix = grayMatrix;
        this.digit = digit;
        for(int i=0;i<10;i++){
            if(this.digit == i){
                outputList[i] = 1.0;
            }else{
                outputList[i] = 0.0;
            }
        }
    }

    public double[] getGrayMatrix() {
        return grayMatrix;
    }

    public void setGrayMatrix(double[] grayMatrix) {
        this.grayMatrix = grayMatrix;
    }

    public int getDigit() {
        return digit;
    }

    public void setDigit(int digit) {
        this.digit = digit;
    }

    public double[] getOutputList() {
        return outputList;
    }

    public void setOutputList(double[] outputList) {
        this.outputList = outputList;
    }
}
