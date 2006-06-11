package edu.stanford.smi.protege.server.plugin;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.resource.Icons;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.server.framestore.RemoteClientStats;
import edu.stanford.smi.protege.widget.AbstractTabWidget;

// an example tab
public class ServerStatsPlugin extends AbstractTabWidget {
  
  private UserInfoTable userInfo;
  private JTable userInfoTable;
  private JTextField cacheStatsField;
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
    setLayout(new FlowLayout());
    userInfo = new UserInfoTable();
    userInfoTable = new JTable(userInfo);
    add(userInfoTable);
    add(createRemoteClientStats());
    add(refreshButton);
  }
  
  private JPanel createRemoteClientStats() {
    JPanel clientStats = new JPanel(new GridLayout(2,2));
    clientStats.add(new JLabel("Client Cache Hit rate: "));
    cacheStatsField = createOutputTextField();
    clientStats.add(cacheStatsField);
    clientStats.add(new JLabel("Client Closure Cache Hit rate: "));
    closureCacheStatsField = createOutputTextField();
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
    RemoteClientStats stats = client.getStats();
    
    float rate = ((float) 100) * ((float) stats.getCacheHits()) / ((float) (stats.getCacheHits() + stats.getCacheMisses()));
    cacheStatsField.setText("" + rate);
    
    rate = ((float) 100) * ((float) stats.getClosureCacheHits()) 
                / ((float) (stats.getClosureCacheHits() + stats.getClosureCacheMisses()));
    closureCacheStatsField.setText("" + rate);
    
    userInfo.setUserInfo(client.getUserInfo());
  }
  
  private JTextField createOutputTextField() {
    JTextField field = new JTextField(10);
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
