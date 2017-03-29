package com.neuralnetwork.sample.ui;
import com.neuralnetwork.sample.constant.Constant;
import com.neuralnetwork.sample.model.ImageModel;
import com.neuralnetwork.sample.neuralnetwork.Network;
import com.neuralnetwork.sample.util.ImageUtil;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
public class MainFrame extends JFrame{
    private int width = 450;
    private int height = 450;
    private Canvas canvas = null;

    //four buttons: clear,tell num,train,test
    private JButton jbClear = null;
    private JButton jbNum = null;
    private JButton jbTrain = null;
    private JButton jbTest = null;

    private Network network = null;

    public MainFrame(){
        super();
        this.setTitle("Digital Recognizer");
        this.setSize(width, height);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocation(300, 300);
        this.setLayout(null);

        this.canvas = new Canvas(280,280);
        this.canvas.setBounds(new Rectangle(85, 30, 280, 280));
        this.add(this.canvas);

        this.network = new Network(new int[]{28*28,30,10});

        this.jbClear = new JButton();
        this.jbClear.setText("clear");
        this.jbClear.setBounds(40, 360, 80, 30);
        this.add(jbClear);
        this.jbClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                canvas.clear();
                Constant.digit = -1;
            }
        });

        this.jbNum = new JButton();
        this.jbNum.setText("tell num");
        this.jbNum.setBounds(140, 360, 80, 30);
        this.add(jbNum);
        this.jbNum.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String str = (String) JOptionPane.showInputDialog(null, "Please input the number you write：\n", "Tell Me Number", JOptionPane.PLAIN_MESSAGE, null, null,
                        "");
                try {
                    int digit = Integer.parseInt(str);
                    if (digit < 0 || digit > 9) {
                        canvas.clear();
                        JOptionPane.showMessageDialog(null, "I can only learn number 0~9");
                        Constant.digit = -1;
                    } else {
                        Constant.digit = digit;
                        //save image and digit
                        String fileName = saveJPanel();
                        canvas.clear();
                        JOptionPane.showMessageDialog(null, "I have remember the number:" + digit + ". Image file path:" + fileName);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    canvas.clear();
                    Constant.digit = -1;
                    JOptionPane.showMessageDialog(null, "I can only learn number 0~9");
                }
            }
        });

        this.jbTrain = new JButton();
        this.jbTrain.setText("train");
        this.jbTrain.setBounds(240, 360, 80, 30);
        this.add(jbTrain);
        this.jbTrain.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                java.util.List<String> fileList = ImageUtil.getInstance().getImageList();
                if (fileList.size() < 10) {
                    JOptionPane.showMessageDialog(null, "You should create at least 10 train jpg. Try to use \"tell num\".");
                } else {
                    java.util.List<ImageModel> modelList = ImageUtil.getInstance().getImageModel(fileList);
                    //use modelList to train neural network
                    network.SGD(modelList, 10000, 0.1);
                }
            }
        });

        this.jbTest = new JButton();
        this.jbTest.setText("test");
        this.jbTest.setBounds(340, 360, 80, 30);
        this.add(jbTest);
        this.jbTest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!network.isTrain()){
                    JOptionPane.showMessageDialog(null,"You should train neural network first");
                }else{
                    int digit = network.predict(ImageUtil.getInstance().getGrayMatrixFromPanel(canvas));
                    if(digit == -1){
                        JOptionPane.showMessageDialog(null,"I can not recognize this number");
                    }else{
                        JOptionPane.showMessageDialog(null,"I guess this number is:"+digit);
                    }
                }
            }
        });

        this.setVisible(true);
    }

    public String saveJPanel(){
        Dimension imageSize = this.canvas.getSize();
        BufferedImage image = new BufferedImage(imageSize.width,imageSize.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        this.canvas.paint(graphics);
        graphics.dispose();
        try {
            String fileName = String.format("%s/%d_%s.jpg",Constant.trainFolder,Constant.digit,java.util.UUID.randomUUID());
            ImageIO.write(image, "jpg", new File(fileName));
            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
