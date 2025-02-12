/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Frontend;

import Backend.Analysis.AnalysisCompare;
import Backend.Analysis.AnalysisCompare.CompareResult;
import Backend.Analysis.SoundAnalysis;
import Backend.Analysis.SpotifyAnalysis;
import Backend.Spotify.SpotifyAPI;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Arnav
 */
public class MainSearch extends javax.swing.JFrame {

    // Values above 50 will not work for now.
    private int numberOfComparisonSongs = 50;
    private String id = "";
    private DecimalFormat percentFormat = new DecimalFormat("0.00%");
    private String[] resultURLs = new String[numberOfComparisonSongs];
    //private javax.swing.JScrollPane scrollPanel;
    //private javax.swing.JList<String> songList;
    /**
     * Creates new form MainSearch
     */
    public MainSearch() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        idInput = new javax.swing.JTextField();
        titleLabel = new javax.swing.JLabel();
        backButton = new javax.swing.JButton();
        suggestButton = new javax.swing.JButton();
        idLabel = new javax.swing.JLabel();
        errorLabel = new javax.swing.JLabel();
        instructions = new javax.swing.JLabel();
        scrollPanel = new javax.swing.JScrollPane();
        songList = new javax.swing.JList<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        idInput.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                idInputKeyReleased(evt);
            }
        });

        titleLabel.setFont(new java.awt.Font("Helvetica Neue", 0, 36)); // NOI18N
        titleLabel.setText("ASMR");

        backButton.setText("Back");
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });

        suggestButton.setText("Suggest Songs");
        suggestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                suggestButtonActionPerformed(evt);
            }
        });

        idLabel.setText("Track ID:");

        errorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        errorLabel.setText("Invalid ID");
        errorLabel.setVisible(false);

        instructions.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        instructions.setText("<html>\nType in the \"track ID\" of a Spotify song. This can be found in<br>\nthe song's \"share\" link after \"/track/\". For example, the track ID of<br>\n <a href=\"https://open.spotify.com/track/17lrs2l9qXEuFybi7hSsid?si=37b141e7c99649c7\">\nhttps://open.spotify.com/track/17lrs2l9qXEuFybi7hSsid?si=37b141e7c99649c7</a><br>\nis \"17lrs2l9qXEuFybi7hSsid\". Do not include the \"?si=\" value.\n</html>");

        songList.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        scrollPanel.setViewportView(songList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(instructions, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 816, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(156, 156, 156)
                        .addComponent(errorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(idLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(idInput, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(backButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(suggestButton, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(18, 18, 18)
                .addComponent(scrollPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 485, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(349, 349, 349)
                .addComponent(titleLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(titleLabel)
                .addGap(18, 18, 18)
                .addComponent(instructions, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(idLabel)
                            .addComponent(idInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(errorLabel)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(backButton)
                            .addComponent(suggestButton)))
                    .addComponent(scrollPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(73, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Simply moves back a frame, to the Login frame. The current Search frame is disposed.
     * @param evt
     */
    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        this.toBack();
        this.dispose();
        Login newLogin = new Login();
        newLogin.setVisible(true);
        newLogin.toFront();
    }//GEN-LAST:event_backButtonActionPerformed

    /**
     * Uses the id variable to analyze however many songs we have set it to
     * After analyzing the songs and finding matches, update songList within ScrollPanel, and display matches to user ordered by highest match.
     * @param evt
     */
    private void suggestButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_suggestButtonActionPerformed
        suggestButton.setText("Working...");
        try {
            // Get user analysis.
            List<SoundAnalysis> userAnalysis = new ArrayList<>();
            userAnalysis.add(SpotifyAPI.getTrackFeatures(id));

            // Get N random songs to compare with.
            List<SoundAnalysis> comparisonAnalyses = new ArrayList<>(numberOfComparisonSongs);
            String[] comparisonIds = SpotifyAPI.randomSong(numberOfComparisonSongs);
            for (String id : comparisonIds)
                comparisonAnalyses.add(SpotifyAPI.getTrackFeatures(id));

            // Compare songs and print results.
            List<CompareResult> results = AnalysisCompare.compareAnalyses(userAnalysis, comparisonAnalyses);

            String[] resultIds = new String[results.size()];
            for (int i = 0; i < results.size(); i++)
                resultIds[i] = ((SpotifyAnalysis) results.get(i).b).getTrackId();
            resultURLs= SpotifyAPI.getTrackURLs(resultIds);

            for (int i = 0; i < results.size(); i++) {
                String matchPercent = percentFormat.format(results.get(i).result);
                System.out.println(resultURLs[i] + " = " + matchPercent);
            }

            // DefaultListModel object to update the list representing songs
            DefaultListModel lm = new DefaultListModel();
            // iterate through each url result and append the match percentage
            for (int i = 0; i < resultURLs.length; i++) {
                String matchPercent = percentFormat.format(results.get(i).result);
                String combined = resultURLs[i] +  ", " + matchPercent;
                // add appended string to our DefaultListModel
                lm.add(i, combined);
            }
            // set the current ListModel as our updated DefaultListModel
            songList.setModel(lm);
            // update the frame
            this.revalidate();
            this.repaint();
            
        } catch (RuntimeException e) {
            errorLabel.setVisible(true);
            e.printStackTrace();
            System.out.println("MainSearch: Invalid ID - " + e.getMessage());
        }
        suggestButton.setText("Suggest Songs");
        
    }//GEN-LAST:event_suggestButtonActionPerformed

    /**
     * Updates the trackId to the text inputted in idInput field
     * @param evt
     */
    private void idInputKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_idInputKeyReleased
        id = idInput.getText();
    }//GEN-LAST:event_idInputKeyReleased

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
            java.util.logging.Logger.getLogger(MainSearch.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainSearch.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainSearch.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainSearch.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainSearch().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JTextField idInput;
    private javax.swing.JLabel idLabel;
    private javax.swing.JLabel instructions;
    private javax.swing.JScrollPane scrollPanel;
    private javax.swing.JList<String> songList;
    private javax.swing.JButton suggestButton;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
}
