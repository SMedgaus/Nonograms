/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nonograms;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.Timer;

/**
 *
 * @author Sergey
 */
public class ProcessDisplayWindow extends javax.swing.JFrame {

    private final Nonogram nonogram;
    private Timer timer;
    private int stepNum = 1;

    /**
     * Creates new form ProcessDisplayWindow
     *
     * @throws java.io.IOException
     */
    public ProcessDisplayWindow() throws IOException {
        initComponents();

        File folder = new File("result");
        if (!folder.exists()) {
            Files.createDirectory(folder.toPath());
        } else {
            File[] files = folder.listFiles();
            for (File file : files) {
                Files.delete(file.toPath());
            }
        }

        nonogram = new Nonogram(new File("dinosaur.txt"));
        timer = new Timer(500, (ActionEvent evt) -> {
            if (!analyzeNonogram()) {
                timer.stop();
            }
        });
    }

    /**
     *
     * @return true, while there's analysis
     */
    private boolean analyzeNonogram() {
        String description;
        do {
            description = nonogram.oneStepAnalysis();
        } while (!description.contains("Метод") && !description.contains("Анализ окончен"));

        BufferedImage nonogramState = nonogram.getCurrentState();

        currentStepLabel.setText(description);
        nonogramDisplay.setIcon(new ImageIcon(nonogramState));

        try {
            ImageIO.write(getScreenComponent(this), "png", new File("result/result-" + stepNum++ + ".png"));
        } catch (IOException ex) {
            Logger.getLogger(ProcessDisplayWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        return !description.contains("Анализ окончен");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        nonogramDisplay = new javax.swing.JLabel();
        currentStepLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.9;
        getContentPane().add(nonogramDisplay, gridBagConstraints);

        currentStepLabel.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weighty = 0.1;
        getContentPane().add(currentStepLabel, gridBagConstraints);

        setSize(new java.awt.Dimension(593, 432));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void formKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
        if (evt.getExtendedKeyCode() == KeyEvent.VK_SPACE) {
            timer.start();
        }
    }//GEN-LAST:event_formKeyReleased

    public static BufferedImage getScreenComponent(Component c) {
        BufferedImage image = null;
        try {
            image = new Robot().createScreenCapture(
                    new Rectangle(
                            c.getLocationOnScreen().x,
                            c.getLocationOnScreen().y,
                            c.getWidth(),
                            c.getHeight()
                    )
            );
        } catch (AWTException e) {
        }
        return image;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ProcessDisplayWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ProcessDisplayWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ProcessDisplayWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ProcessDisplayWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            try {
                new ProcessDisplayWindow().setVisible(true);
            } catch (IOException ex) {
                Logger.getLogger(ProcessDisplayWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel currentStepLabel;
    private javax.swing.JLabel nonogramDisplay;
    // End of variables declaration//GEN-END:variables
}
