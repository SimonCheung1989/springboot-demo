package com.simon.demo.profileservice;

import org.junit.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CallbackTest {

    @Test
    public void testCallback(){

    }


    public static void main(String[] args) {
        JFrame jFrame = new JFrame("Test");
        jFrame.setSize(300, 300);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JButton jButton = new JButton("Click me");

        jButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getID());
            }
        });

        jFrame.add(jButton);


        jFrame.setVisible(true);
    }
}
