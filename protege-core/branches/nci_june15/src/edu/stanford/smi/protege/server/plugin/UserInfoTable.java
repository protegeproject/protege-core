package edu.stanford.smi.protege.server.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.background.FrameCalculatorStats;

public class UserInfoTable extends AbstractTableModel {

  public enum Column {  
    user("User"), ipAddr("IP Adress"), transaction("In Transaction?"), backlog("Server Backlog");
    
    private String name;   
    
    private Column(String name) {
      this.name = name;
    }
    
    public String getColumnName() {
      return name;
    }
  };
  
  Map<RemoteSession, Boolean> userInfo = new HashMap<RemoteSession, Boolean>();
  FrameCalculatorStats stats = null;
  List<RemoteSession>  sessions = new ArrayList<RemoteSession>();

  public UserInfoTable() {
  }
    
  public void setUserInfo(Map<RemoteSession, Boolean> userInfo, FrameCalculatorStats  stats) {
    this.userInfo = userInfo;
    this.stats    = stats;
    sessions  = new ArrayList<RemoteSession>();
    for (RemoteSession session :  userInfo.keySet()) {
      sessions.add(session);
    }
    fireTableDataChanged();
  }

  public int getRowCount() {
    return userInfo.size();
  }

  public int getColumnCount() {
    return Column.values().length;
  }

  public Object getValueAt(int rowIndex, int colIndex) {
    Column col = Column.values()[colIndex];
    switch (col) {
    case user:
      return sessions.get(rowIndex).getUserName();
    case ipAddr:
      return sessions.get(rowIndex).getUserIpAddress();
    case transaction:
      return userInfo.get(sessions.get(rowIndex));
    case backlog:
      if (stats == null) {
        return new Integer(-1);
      } else {
        RemoteSession session = sessions.get(rowIndex);
        Integer result = stats.getPreCacheBacklog().get(session);
        if (result == null) {
          return 0;
        } else {
          return new Integer(result);
        }
      }
    default:
      throw new UnsupportedOperationException("Programmer Error");  
    }
  }
  
  public String getColumnName(int colIndex) {
    Column col = Column.values()[colIndex];
    return col.getColumnName();
  }
  
  public Class getColumnClass(int colIndex) {
    Column col = Column.values()[colIndex];
    switch (col) {
    case user:
    case ipAddr:
      return String.class;
    case transaction:
      return Boolean.class;
    case backlog:
      return Integer.class;
    default:
      throw new UnsupportedOperationException("Programmer Error");
    }
  }

}
