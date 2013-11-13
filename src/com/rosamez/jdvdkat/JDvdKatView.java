/*
 * JDvdKatView.java
 */
package com.rosamez.jdvdkat;

import java.io.IOException;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * The application's main frame.
 */
public class JDvdKatView extends FrameView {
    private static Logger log = Logger.getLogger(JDvdKatView.class);

    private JDvdKatApp app = JDvdKatApp.getApplication();
    private TableNodesController tableNodesController;
    private ListFilesController listFilesController;
    private DbListView dbListOpenView = new DbListOpenView(new JFrame(), true);
    private DbListView dbListDeleteView = new DbListDeleteView(new JFrame(), true);
    private AddAllFilesView addAllFilesView = new AddAllFilesView(new JFrame(), true);
    private DbCreateView dbCreateView = new DbCreateView(new JFrame(), true);
    private DbCreateView dbRenameView = new DbCreateView(new JFrame(), true);
    private String defaultTitle = null;
    private String goToNodeAbsPath = null;
    private String goToNodeName = null;
    private SearchView searchView = new SearchView();
    private boolean jListFilesMulti = false;

    public JDvdKatView(SingleFrameApplication app) {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            @Override
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

        // higher level controls
        tableNodesController = new TableNodesController(jTableNodes, jTextPaneAttrs);
        listFilesController = new ListFilesController(jListFiles);
        
        // dialogs
        FileExtensionFilter filter = new FileExtensionFilter("XML files", ".xml");
        jXmlFileChooser.setFileFilter(filter);

        jSaveDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jSaveDirChooser.setAcceptAllFileFilterUsed(false);
        jSaveDirChooser.setDialogTitle("Select destination directory");

        jAddAllDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jAddAllDirChooser.setAcceptAllFileFilterUsed(false);

        dbRenameView.setTitle("Rename DB"); // since it's made from the createdb...

        // mnemonics madness
        // http://wiki.netbeans.org/I18NMnemonics is nonsense?!
        // TODO: use & in the string and add a method to convert to mnemo? like with openide.awt
        jMenuFiles.setMnemonic(KeyEvent.VK_F);
        jMenuItemCreateDb.setMnemonic(KeyEvent.VK_C);
        jMenuItemOpenDb.setMnemonic(KeyEvent.VK_O);
        jMenuItemDeleteDb.setMnemonic(KeyEvent.VK_D);
        jMenuItemAddFile.setMnemonic(KeyEvent.VK_A);
        jMenuItemAddAll.setMnemonic(KeyEvent.VK_L);
        jMenuItemExport.setMnemonic(KeyEvent.VK_E);
        jMenuItemExportAll.setMnemonic(KeyEvent.VK_X);
        jMenuItemRemoveFile.setMnemonic(KeyEvent.VK_R);
        jMenuSearch.setMnemonic(KeyEvent.VK_S);
        jMenuItemSearchView.setMnemonic(KeyEvent.VK_N);
        jMenuItemRenameDb.setMnemonic(KeyEvent.VK_R);
        jMenuItemRefresh.setMnemonic(KeyEvent.VK_F);

        // misc startup stuff
        lockMenusForNoFile(listFilesController.isEmpty());

        // TEST
        //showSearch();
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = app.getMainFrame();
            aboutBox = new JDvdKatAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        app.show(aboutBox);
    }

    @Action
    public void populateListFiles() {
        String[] xmlNames = app.dvdKatModel.getFiles();
        Arrays.sort(xmlNames);
        listFilesController.clear();
        for (int i = 0; i < xmlNames.length; i++) {
            String actName = xmlNames[i];
            int lastSlash = actName.lastIndexOf("/");
            if (lastSlash > -1) {
                actName = actName.substring(lastSlash + 1);
            }
            // add only the .xml files, do not add fake nodes
            if (actName.indexOf(".xml") > -1) {
                listFilesController.add(actName);
            }
        }
        //jListFiles.setModel(listFilesModel);
        listFilesController.gotoFirst();
    }

    @Action
    public void closeCurrentDb() {
        noDbMode(true);
        app.dbManager.closeActiveDb();
        updateTitle();
    }

    @Action
    public void addFile() {
        jXmlFileChooser.showOpenDialog(mainPanel);
        File selFile = jXmlFileChooser.getSelectedFile();
        if (selFile == null) {
            return;
        }
        if (!app.dbManager.addFile(selFile)) {
            JOptionPane.showMessageDialog(mainPanel, "Could not add the specified file!\nIf it already exists in the db, please delete it first!", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            populateListFiles();
            listFilesController.gotoFileName(selFile.getAbsolutePath());
        }
    }

    @Action
    public void addAllFiles() {
        jAddAllDirChooser.showOpenDialog(mainPanel);
        File selFile = jAddAllDirChooser.getSelectedFile();
        if (selFile == null) {
            return;
        }
        if (!selFile.exists()) {
            JOptionPane.showMessageDialog(mainPanel, "Directory doesn't exist!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        addAllFilesView.doImportShow(mainPanel, selFile);
    }

    protected void onAddAllFilesFinished() {
        if (addAllFilesView.getLastSuccAdds() > 0) {
            log.info("import finished with files.");
            populateListFiles();
            listFilesController.gotoFirst();
        }
    }

    public String getSelectedFile() {
        return (String) jListFiles.getSelectedValue();
    }

    /**
     * deletes the selected file(s)
     * (TODO: handle multiselect in gui)
     */
    @Action
    public void removeFile() {
        // nothing to remove
        if (listFilesController.isEmpty()) {
            return;
        }

        Object[] selFileNames = jListFiles.getSelectedValues();
        String message;
        if (selFileNames.length > 1) {
            message = "Delete " + selFileNames.length + " files?";
        } else {
            message = "Are you sure you want to remove the \"" + (String)selFileNames[0] + "\" file?";
        }

        // are you sure?
        int btnNum = JOptionPane.showConfirmDialog(
                mainPanel,
                message,
                "Remove XML file",
                JOptionPane.YES_NO_OPTION);

        // yes, let's remove
        if (btnNum == 0) {
            for (int i = 0; i < selFileNames.length; i++) {
                String selFileName = (String)selFileNames[i];
                app.dbManager.removeFile(selFileName); // has no throw!
            }
            populateListFiles();
            listFilesController.gotoFirst();
        }

        if (listFilesController.isEmpty()) {
            tableNodesController.dePopulate();
        }
    }

    @Action
    public void exportAllFiles() {
        jSaveDirChooser.showOpenDialog(mainPanel);
        File selFile = jSaveDirChooser.getSelectedFile();
        if (selFile == null) {
            return;
        }
        if (!selFile.exists()) {
            JOptionPane.showMessageDialog(mainPanel, "Directory doesn't exist!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // we can still have a L&F thread exception, I can't catch that here
        String path = selFile.getAbsolutePath();
        app.dbManager.exportDb(path);
        JOptionPane.showMessageDialog(mainPanel, "Export operation finished.", "Done", JOptionPane.INFORMATION_MESSAGE);
    }

    @Action
    public void exportFile() {
        jXmlFileChooser.showOpenDialog(mainPanel);
        File selFile = jXmlFileChooser.getSelectedFile();
        if (selFile == null) {
            return;
        }
        String name = listFilesController.getCurrent();
        String xmlString = app.dvdKatModel.fetchFileAsSaveable(name);
        try {
            FileUtils.writeStringToFile(selFile, xmlString, "UTF-8");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(mainPanel, "Could not export the file.", "Error", JOptionPane.ERROR_MESSAGE);
            log.error(ex.getMessage());
            return;
        }
        JOptionPane.showMessageDialog(mainPanel, "File exported.", "Done", JOptionPane.INFORMATION_MESSAGE);
    }

    @Action
    public void showDbListOpen() {
        dbListOpenView.setLocationRelativeTo(mainPanel);
        dbListOpenView.initShow();
        openDb(dbListOpenView.getSelectedDb());
    }

    @Action
    public void reOpenDb() {
        String dbName = app.dbManager.getActiveDbNameShort();
        //String act = listFilesController.getCurrent();
        closeCurrentDb();
        openDb(dbName);
        //listFilesController.gotoItem(act);
    }

    private void openDb(String dbName) {
        if ((dbName != null) && (!"".equals(dbName))) {
            app.dbManager.closeActiveDb();
            tableNodesController.dePopulate();
            listFilesController.dePopulate();
            app.dbManager.openDb(dbName);
            updateTitle();
            noDbMode(false);
            populateListFiles();
        }
    }

    @Action
    public void showDbListDelete() {
        dbListDeleteView.setLocationRelativeTo(mainPanel);
        dbListDeleteView.initShow();
        String dbName = dbListDeleteView.getSelectedDb();
        String actDbName = app.dbManager.getActiveDbNameShort();
        boolean mustReopen = false;
        if (dbName != null) {
            if (actDbName != null && actDbName.equals(dbName)) {
                closeCurrentDb();
                mustReopen = true;
            }
            if (app.dbManager.deleteDb(dbName)) {
                if (mustReopen) {
                    app.dbManager.openDb(actDbName);
                }
                JOptionPane.showMessageDialog(mainPanel, "Database deleted.", "Done", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(mainPanel, "Could not delete the database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Action
    public void showDbCreate() {
        dbCreateView.setLocationRelativeTo(mainPanel);
        dbCreateView.initShow();
        String dbName = dbCreateView.newDbName.trim();
        if ((dbName != null) && (!"".equals(dbName))) {
            if (app.dbManager.createDb(dbName)) {
                openDb(dbName);
            }
        }
    }

    @Action
    public void showDbRename() {
        dbRenameView.setLocationRelativeTo(mainPanel);
        dbRenameView.defaultText = app.dbManager.getActiveDbNameShort();
        dbRenameView.initShow();
        String dbName = dbRenameView.newDbName.trim();
        if ((dbName != null) && (!"".equals(dbName))) {
            if (app.dbManager.renameDbTo(dbName)) {
                openDb(dbName);
            }
        }
    }

    @Action
    public void showSearch() {
        searchView.initShow();
    }

    protected void noDbMode(boolean state) {
        if (state) {
            jMenuItemAddFile.setEnabled(false);
            jMenuItemAddAll.setEnabled(false);
            jMenuItemSearchView.setEnabled(false);
            jMenuItemRefresh.setEnabled(false);
            jMenuItemRenameDb.setEnabled(false);
            tableNodesController.dePopulate();
            tableNodesController.disableAll();
            listFilesController.dePopulate();
            listFilesController.disable();
        } else {
            jMenuItemAddFile.setEnabled(true);
            jMenuItemAddAll.setEnabled(true);
            jMenuItemSearchView.setEnabled(true);
            jMenuItemRefresh.setEnabled(true);
            jMenuItemRenameDb.setEnabled(true);
            tableNodesController.enableAll();
            listFilesController.enable();
        }
    }

    protected void lockMenusForNoFile(boolean state) {
        if (state) {
            jMenuItemExportAll.setEnabled(false);
            jMenuItemExport.setEnabled(false);
            jMenuItemRemoveFile.setEnabled(false);
        } else {
            jMenuItemExportAll.setEnabled(true);
            jMenuItemExport.setEnabled(true);
            jMenuItemRemoveFile.setEnabled(true);
        }
    }

    /**
     *
     * @param doc
     * @param absPath
     * @param name  we need this to speed up things!
     */
    public void goToDocPath(String doc, String absPath, String name) {
        goToNodeAbsPath = absPath;
        goToNodeName = name;
        // if the doc is the same as the selected item in the file list,
        // then we will not have an onchange event, hence the node table
        // fill must be triggered automatically
        if (!doc.equals(listFilesController.getCurrent())) {
            listFilesController.gotoFileName(doc);
        } else {
            populatePendingAbsPath();
        }
    }

    public void populatePendingAbsPath() {
        if (goToNodeAbsPath != null) {
            tableNodesController.populateForAbsPath(goToNodeAbsPath, goToNodeName);
            goToNodeAbsPath = null;
            goToNodeName = null;
        }
    }

    /*
    @Action
    public void setButtonText(String text) {
    jButton1.setText(text + JDvdKatApp.getApplication().myTestString);
    }
     */
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jSplitPaneMain = new javax.swing.JSplitPane();
        jSplitPaneRight = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableNodes = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jPanelNoWrap = new javax.swing.JPanel();
        jTextPaneAttrs = new javax.swing.JTextPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListFiles = new javax.swing.JList();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu jMenuDatabase = new javax.swing.JMenu();
        jMenuItemOpenDb = new javax.swing.JMenuItem();
        jMenuItemRefresh = new javax.swing.JMenuItem();
        jMenuItemCreateDb = new javax.swing.JMenuItem();
        jMenuItemRenameDb = new javax.swing.JMenuItem();
        jMenuItemDeleteDb = new javax.swing.JMenuItem();
        javax.swing.JMenuItem jMenuItemExit = new javax.swing.JMenuItem();
        jMenuFiles = new javax.swing.JMenu();
        jMenuItemAddFile = new javax.swing.JMenuItem();
        jMenuItemAddAll = new javax.swing.JMenuItem();
        jMenuItemRemoveFile = new javax.swing.JMenuItem();
        jMenuItemExportAll = new javax.swing.JMenuItem();
        jMenuItemExport = new javax.swing.JMenuItem();
        jMenuSearch = new javax.swing.JMenu();
        jMenuItemSearchView = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        jXmlFileChooser = new javax.swing.JFileChooser();
        jSaveDirChooser = new javax.swing.JFileChooser();
        jAddAllDirChooser = new javax.swing.JFileChooser();

        mainPanel.setName("mainPanel"); // NOI18N

        jSplitPaneMain.setBorder(null);
        jSplitPaneMain.setName("jSplitPaneMain"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.rosamez.jdvdkat.JDvdKatApp.class).getContext().getResourceMap(JDvdKatView.class);
        jSplitPaneRight.setBackground(resourceMap.getColor("jSplitPaneRight.background")); // NOI18N
        jSplitPaneRight.setBorder(null);
        jSplitPaneRight.setName("jSplitPaneRight"); // NOI18N
        jSplitPaneRight.setPreferredSize(new java.awt.Dimension(25, 183));
        jSplitPaneRight.setRequestFocusEnabled(false);

        jScrollPane2.setBackground(resourceMap.getColor("jScrollPane2.background")); // NOI18N
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTableNodes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Size", "Type", "Date"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableNodes.setFillsViewportHeight(true);
        jTableNodes.setIntercellSpacing(new java.awt.Dimension(0, 0));
        jTableNodes.setName("jTableNodes"); // NOI18N
        jTableNodes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTableNodes.getTableHeader().setReorderingAllowed(false);
        jTableNodes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableNodesMouseClicked(evt);
            }
        });
        jTableNodes.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTableNodesKeyPressed(evt);
            }
        });
        jScrollPane2.setViewportView(jTableNodes);
        jTableNodes.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTableNodes.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTableNodes.columnModel.title0")); // NOI18N
        jTableNodes.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTableNodes.columnModel.title1")); // NOI18N
        jTableNodes.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("jTableNodes.columnModel.title2")); // NOI18N
        jTableNodes.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("jTableNodes.columnModel.title3")); // NOI18N

        jSplitPaneRight.setLeftComponent(jScrollPane2);

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jPanelNoWrap.setName("jPanelNoWrap"); // NOI18N
        jPanelNoWrap.setLayout(new java.awt.BorderLayout());

        jTextPaneAttrs.setBackground(resourceMap.getColor("jTextPaneAttrs.background")); // NOI18N
        jTextPaneAttrs.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jTextPaneAttrs.setEditable(false);
        jTextPaneAttrs.setMinimumSize(new java.awt.Dimension(800, 50));
        jTextPaneAttrs.setName("jTextPaneAttrs"); // NOI18N
        jPanelNoWrap.add(jTextPaneAttrs, java.awt.BorderLayout.CENTER);

        jScrollPane3.setViewportView(jPanelNoWrap);

        jSplitPaneRight.setRightComponent(jScrollPane3);

        jSplitPaneMain.setRightComponent(jSplitPaneRight);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jListFiles.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jListFiles.setName("jListFiles"); // NOI18N
        jListFiles.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListFilesValueChanged(evt);
            }
        });
        jListFiles.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jListFilesKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(jListFiles);

        jSplitPaneMain.setLeftComponent(jScrollPane1);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneMain, javax.swing.GroupLayout.DEFAULT_SIZE, 925, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneMain, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        jMenuDatabase.setMnemonic('d');
        jMenuDatabase.setText(resourceMap.getString("jMenuDatabase.text")); // NOI18N
        jMenuDatabase.setName("jMenuDatabase"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.rosamez.jdvdkat.JDvdKatApp.class).getContext().getActionMap(JDvdKatView.class, this);
        jMenuItemOpenDb.setAction(actionMap.get("showDbListOpen")); // NOI18N
        jMenuItemOpenDb.setName("jMenuItemOpenDb"); // NOI18N
        jMenuDatabase.add(jMenuItemOpenDb);

        jMenuItemRefresh.setAction(actionMap.get("reOpenDb")); // NOI18N
        jMenuItemRefresh.setText(resourceMap.getString("jMenuItemRefresh.text")); // NOI18N
        jMenuItemRefresh.setName("jMenuItemRefresh"); // NOI18N
        jMenuDatabase.add(jMenuItemRefresh);

        jMenuItemCreateDb.setAction(actionMap.get("showDbCreate")); // NOI18N
        jMenuItemCreateDb.setText(resourceMap.getString("jMenuItemCreateDb.text")); // NOI18N
        jMenuItemCreateDb.setName("jMenuItemCreateDb"); // NOI18N
        jMenuDatabase.add(jMenuItemCreateDb);

        jMenuItemRenameDb.setAction(actionMap.get("showDbRename")); // NOI18N
        jMenuItemRenameDb.setText(resourceMap.getString("jMenuItemRenameDb.text")); // NOI18N
        jMenuItemRenameDb.setName("jMenuItemRenameDb"); // NOI18N
        jMenuDatabase.add(jMenuItemRenameDb);

        jMenuItemDeleteDb.setAction(actionMap.get("showDbListDelete")); // NOI18N
        jMenuItemDeleteDb.setName("jMenuItemDeleteDb"); // NOI18N
        jMenuDatabase.add(jMenuItemDeleteDb);

        jMenuItemExit.setAction(actionMap.get("quit")); // NOI18N
        jMenuItemExit.setName("jMenuItemExit"); // NOI18N
        jMenuDatabase.add(jMenuItemExit);

        menuBar.add(jMenuDatabase);

        jMenuFiles.setAction(actionMap.get("addFile")); // NOI18N
        jMenuFiles.setText(resourceMap.getString("jMenuFiles.text")); // NOI18N
        jMenuFiles.setName("jMenuFiles"); // NOI18N

        jMenuItemAddFile.setAction(actionMap.get("addFile")); // NOI18N
        jMenuItemAddFile.setText(resourceMap.getString("jMenuItemAddFile.text")); // NOI18N
        jMenuItemAddFile.setName("jMenuItemAddFile"); // NOI18N
        jMenuFiles.add(jMenuItemAddFile);

        jMenuItemAddAll.setAction(actionMap.get("addAllFiles")); // NOI18N
        jMenuItemAddAll.setText(resourceMap.getString("jMenuItemAddAll.text")); // NOI18N
        jMenuItemAddAll.setName("jMenuItemAddAll"); // NOI18N
        jMenuFiles.add(jMenuItemAddAll);

        jMenuItemRemoveFile.setAction(actionMap.get("removeFile")); // NOI18N
        jMenuItemRemoveFile.setText(resourceMap.getString("jMenuItemRemoveFile.text")); // NOI18N
        jMenuItemRemoveFile.setName("jMenuItemRemoveFile"); // NOI18N
        jMenuFiles.add(jMenuItemRemoveFile);

        jMenuItemExportAll.setAction(actionMap.get("exportAllFiles")); // NOI18N
        jMenuItemExportAll.setName("jMenuItemExportAll"); // NOI18N
        jMenuFiles.add(jMenuItemExportAll);

        jMenuItemExport.setAction(actionMap.get("exportFile")); // NOI18N
        jMenuItemExport.setName("jMenuItemExport"); // NOI18N
        jMenuFiles.add(jMenuItemExport);

        menuBar.add(jMenuFiles);

        jMenuSearch.setMnemonic('s');
        jMenuSearch.setText(resourceMap.getString("jMenuSearch.text")); // NOI18N
        jMenuSearch.setName("jMenuSearch"); // NOI18N

        jMenuItemSearchView.setAction(actionMap.get("showSearch")); // NOI18N
        jMenuItemSearchView.setName("jMenuItemSearchView"); // NOI18N
        jMenuSearch.add(jMenuItemSearchView);

        menuBar.add(jMenuSearch);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 925, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 755, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        jXmlFileChooser.setName("jXmlFileChooser"); // NOI18N

        jSaveDirChooser.setName("jSaveDirChooser"); // NOI18N

        jAddAllDirChooser.setName("jAddAllDirChooser"); // NOI18N

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jListFilesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListFilesValueChanged
        if (evt.getValueIsAdjusting()) {
            return;
        }
        lockMenusForNoFile(listFilesController.isEmpty());
        Object selected = jListFiles.getSelectedValue();
        if (selected == null) {
            return;
        }
        String actValue = selected.toString();
        app.dvdKatModel.setActiveDoc(actValue);
        if (goToNodeAbsPath == null) {
            tableNodesController.populateForRoot();
        } else {
            populatePendingAbsPath();
        }

        // scroll to item: will not work for multiselect, but it's okay for now
        jListFiles.ensureIndexIsVisible(jListFiles.getSelectedIndex());
    }//GEN-LAST:event_jListFilesValueChanged

    private void jTableNodesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTableNodesKeyPressed
        int keyCode = evt.getKeyCode();
        boolean hasShift = evt.isShiftDown();
        if ((keyCode == KeyEvent.VK_ENTER) || (keyCode == KeyEvent.VK_RIGHT)) {
            tableNodesController.enterCurrentNode();
            evt.consume();
        } else if (keyCode == KeyEvent.VK_LEFT) {
            tableNodesController.gotoParentNode();
            evt.consume();
        } else if (keyCode == KeyEvent.VK_HOME) {
            tableNodesController.gotoFirstNode();
            evt.consume();
        } else if (keyCode == KeyEvent.VK_END) {
            tableNodesController.gotoLastNode();
            evt.consume();
        } else if (keyCode == KeyEvent.VK_TAB && !hasShift) {
            jTextPaneAttrs.requestFocusInWindow();
            evt.consume();
        } else if (keyCode == KeyEvent.VK_TAB && hasShift) {
            jListFiles.requestFocusInWindow();
            evt.consume();
        }
    }//GEN-LAST:event_jTableNodesKeyPressed

    /**
     * key pressed on the file lister:
     * - delete = remove file
     * - insert = add new
     * - alt+m = toggle selection mode (debug)
     * @param evt
     */
    private void jListFilesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jListFilesKeyPressed
        int keyCode = evt.getKeyCode();
        boolean hasAlt = evt.isAltDown();
        if (keyCode == KeyEvent.VK_DELETE) {
            removeFile();
            evt.consume();
        } else if (keyCode == KeyEvent.VK_INSERT) {
            addFile();
            evt.consume();
        } else if (keyCode == KeyEvent.VK_M && hasAlt) {
            int selMode;
            if (jListFilesMulti) {
                selMode = ListSelectionModel.SINGLE_SELECTION;
            } else {
                selMode = ListSelectionModel.SINGLE_INTERVAL_SELECTION;
            }
            jListFilesMulti = !jListFilesMulti;
            jListFiles.setSelectionMode(selMode);
            evt.consume();
        }
    }//GEN-LAST:event_jListFilesKeyPressed

    private void jTableNodesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableNodesMouseClicked
        if (evt.getClickCount() == 2) {
            tableNodesController.enterCurrentNode();
        }
    }//GEN-LAST:event_jTableNodesMouseClicked

    public void updateTitle() {
        String dn = app.dbManager.getActiveDbNameShort();
        if (dn == null || "".equals(dn)) {
            dn = "no database";
        }
        if (defaultTitle == null) {
            defaultTitle = this.getFrame().getTitle();
        }
        this.getFrame().setTitle(defaultTitle + " - " + dn);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFileChooser jAddAllDirChooser;
    private javax.swing.JList jListFiles;
    private javax.swing.JMenu jMenuFiles;
    private javax.swing.JMenuItem jMenuItemAddAll;
    private javax.swing.JMenuItem jMenuItemAddFile;
    private javax.swing.JMenuItem jMenuItemCreateDb;
    private javax.swing.JMenuItem jMenuItemDeleteDb;
    private javax.swing.JMenuItem jMenuItemExport;
    private javax.swing.JMenuItem jMenuItemExportAll;
    private javax.swing.JMenuItem jMenuItemOpenDb;
    private javax.swing.JMenuItem jMenuItemRefresh;
    private javax.swing.JMenuItem jMenuItemRemoveFile;
    private javax.swing.JMenuItem jMenuItemRenameDb;
    private javax.swing.JMenuItem jMenuItemSearchView;
    private javax.swing.JMenu jMenuSearch;
    private javax.swing.JPanel jPanelNoWrap;
    private javax.swing.JFileChooser jSaveDirChooser;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPaneMain;
    private javax.swing.JSplitPane jSplitPaneRight;
    private javax.swing.JTable jTableNodes;
    private javax.swing.JTextPane jTextPaneAttrs;
    private javax.swing.JFileChooser jXmlFileChooser;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
}
