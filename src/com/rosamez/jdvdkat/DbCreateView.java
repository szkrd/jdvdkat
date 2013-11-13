/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DbCreateView.java
 *
 * Created on 2010.06.08., 21:55:43
 */

package com.rosamez.jdvdkat;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.application.Action;

/**
 *
 * @author Szabolcs Kurdi
 */
public class DbCreateView extends javax.swing.JDialog {
    private final DbCreateView self = this;
    private JDvdKatApp app;
    private String[] dbs; // cache here
    public String newDbName = "";
    public String defaultText = "";

    /** Creates new form DbCreateView */
    public DbCreateView(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        jButtonCancel.setMnemonic(KeyEvent.VK_C);
        jButtonOk.setMnemonic(KeyEvent.VK_O);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelForFieldDbName = new javax.swing.JLabel();
        jTextFieldDbName = new javax.swing.JTextField();
        jLabelErrorMsg = new javax.swing.JLabel();
        jButtonOk = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.rosamez.jdvdkat.JDvdKatApp.class).getContext().getResourceMap(DbCreateView.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(245, 142));
        setName("Form"); // NOI18N

        jLabelForFieldDbName.setText(resourceMap.getString("jLabelForFieldDbName.text")); // NOI18N
        jLabelForFieldDbName.setName("jLabelForFieldDbName"); // NOI18N

        jTextFieldDbName.setText(resourceMap.getString("jTextFieldDbName.text")); // NOI18N
        jTextFieldDbName.setName("jTextFieldDbName"); // NOI18N
        jTextFieldDbName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextFieldDbNameKeyPressed(evt);
            }
        });

        jLabelErrorMsg.setText(resourceMap.getString("jLabelErrorMsg.text")); // NOI18N
        jLabelErrorMsg.setName("jLabelErrorMsg"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.rosamez.jdvdkat.JDvdKatApp.class).getContext().getActionMap(DbCreateView.class, this);
        jButtonOk.setAction(actionMap.get("accept")); // NOI18N
        jButtonOk.setText(resourceMap.getString("jButtonOk.text")); // NOI18N
        jButtonOk.setName("jButtonOk"); // NOI18N

        jButtonCancel.setAction(actionMap.get("cancel")); // NOI18N
        jButtonCancel.setText(resourceMap.getString("jButtonCancel.text")); // NOI18N
        jButtonCancel.setName("jButtonCancel"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelErrorMsg, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                    .addComponent(jTextFieldDbName, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                    .addComponent(jLabelForFieldDbName)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButtonCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonOk)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelForFieldDbName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldDbName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelErrorMsg)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonOk)
                    .addComponent(jButtonCancel))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextFieldDbNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldDbNameKeyPressed
        int keyCode = evt.getKeyCode();
        if (keyCode == KeyEvent.VK_ENTER) {
            if (jButtonOk.isEnabled()) {
                accept();
            }
            evt.consume();
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            cancel();
        }
    }//GEN-LAST:event_jTextFieldDbNameKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                DbCreateView dialog = new DbCreateView(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    
    public void init() {
        app = JDvdKatApp.getApplication();
        dbs = app.dbManager.getDbNames();
        jLabelErrorMsg.setText("");
        jTextFieldDbName.setText(defaultText);
        onJTextFieldDbNameChange();
        newDbName = "";
        jTextFieldDbName.requestFocusInWindow();
        jButtonOk.setEnabled(false);
        jTextFieldDbName.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                self.onJTextFieldDbNameChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                self.onJTextFieldDbNameChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                self.onJTextFieldDbNameChange();
            }
        });
    }

    private void onJTextFieldDbNameChange() {
        String text = jTextFieldDbName.getText();
        String msg = "";
        boolean exists = (Arrays.asList(dbs).indexOf(text) > -1);
        boolean valid = text.matches("^[\\w]*$");
        boolean editable = true;

        if (text.equals("")) {
            editable = false;
        } else {
            if (exists) {
                msg = "Database already exists!";
                editable = false;
            } else {
                if (!valid) {
                    msg = "Invalid characters";
                    editable = false;
                }
            }
        }

        jButtonOk.setEnabled(editable);
        jLabelErrorMsg.setText(msg);
    }

    public void initShow() {
        init();
        this.setVisible(true);
    }

    @Action
    public void accept() {
        newDbName = jTextFieldDbName.getText();
        this.setVisible(false);
    }

    @Action
    public void cancel() {
        newDbName = "";
        this.setVisible(false);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JLabel jLabelErrorMsg;
    private javax.swing.JLabel jLabelForFieldDbName;
    private javax.swing.JTextField jTextFieldDbName;
    // End of variables declaration//GEN-END:variables

}