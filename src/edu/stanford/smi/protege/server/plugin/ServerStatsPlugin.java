package edu.stanford.smi.protege.server.plugin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.server.framestore.RemoteClientStats;
import edu.stanford.smi.protege.server.framestore.background.FrameCalculatorStats;
import edu.stanford.smi.protege.widget.AbstractTabWidget;

// an example tab
public class ServerStatsPlugin extends AbstractTabWidget {
  
  private UserInfoTable userInfo;
  private JTable userInfoTable;
  private JTextField cacheStatsField;
  private JTextField serverCalcTimeField;
  private JTextField closureCacheStatsField;
  private JButton refreshButton;
  
  public void initialize() {
    setLabel("Server Stats");
    setIcon(Icons.getInstanceIcon());
    
    createRefreshButton();
    layoutUI();
    refresh();
  }
  
  private void layoutUI() {
    setLayout(new BorderLayout());
    userInfo = new UserInfoTable();
    userInfoTable = new JTable();
    userInfoTable.setModel(userInfo);
    add(new JScrollPane(userInfoTable), BorderLayout.EAST);
    add(createRemoteClientStats(), BorderLayout.NORTH);
    serverCalcTimeField = createOutputTextField(60);
    add(serverCalcTimeField, BorderLayout.CENTER);
    add(refreshButton, BorderLayout.SOUTH);
  }
  
  private JPanel createRemoteClientStats() {
    JPanel clientStats = new JPanel(new GridLayout(2,2));
    clientStats.add(new JLabel("Client Cache Hit rate: "));
    cacheStatsField = createOutputTextField(10);
    clientStats.add(cacheStatsField);
    clientStats.add(new JLabel("Client Closure Cache Hit rate: "));
    closureCacheStatsField = createOutputTextField(10);
    clientStats.add(closureCacheStatsField);
    return clientStats;
  }
  
  private void createRefreshButton() {
    refreshButton = new JButton("Refresh Server Stats");
    refreshButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        refresh();
      }
    });
  }
  
  private void refresh() {
    DefaultKnowledgeBase kb = (DefaultKnowledgeBase) getProject().getKnowledgeBase();
    RemoteClientFrameStore client = (RemoteClientFrameStore) kb.getTerminalFrameStore();
    RemoteClientStats clientStats = client.getClientStats();
    FrameCalculatorStats serverStats = client.getServerStats();
    
    float rate = ((float) 100) * ((float) clientStats.getCacheHits()) / ((float) (clientStats.getCacheHits() + clientStats.getCacheMisses()));
    cacheStatsField.setText("" + rate);
    
    rate = ((float) 100) * ((float) clientStats.getClosureCacheHits()) 
                / ((float) (clientStats.getClosureCacheHits() + clientStats.getClosureCacheMisses()));
    closureCacheStatsField.setText("" + rate);
    
    userInfo.setUserInfo(client.getUserInfo(), serverStats);
    
    serverCalcTimeField.setText("Server is taking " + serverStats.getPrecalculateTime() + "ms to pre-cache a frame");
  }
  
  private JTextField createOutputTextField(int size) {
    JTextField field = new JTextField(size);
    field.setEnabled(false);
    field.setHorizontalAlignment(SwingConstants.LEFT);
    return field;
  }
  

  
  public static boolean isSuitable(Project project, Collection errors) {
    KnowledgeBase kb = project.getKnowledgeBase();
    if (!(kb instanceof DefaultKnowledgeBase)) {
      errors.add("Knowledge base is not recognized");
      return false;
    }
    DefaultKnowledgeBase dkb = (DefaultKnowledgeBase) kb;
    boolean ret = dkb.getTerminalFrameStore() instanceof RemoteClientFrameStore;
    if (!ret) {
      errors.add("Not a client");
    }
    return ret;
  }
}
