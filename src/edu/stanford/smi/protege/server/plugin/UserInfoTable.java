package edu.stanford.smi.protege.server.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import edu.stanford.smi.protege.server.RemoteSession;

public class UserInfoTable extends AbstractTableModel {

  public enum Column {  
    user("User"), ipAddr("IP Adress"), transaction("In Transaction?");
    
    private String name;   
    
    private Column(String name) {
      this.name = name;
    }
    
    public String getColumnName() {
      return name;
    }
  };
  
  Map<RemoteSession, Boolean> userInfo = new HashMap<RemoteSession, Boolean>();
  List<RemoteSession>  sessions = new ArrayList<RemoteSession>();

  public UserInfoTable() {
  }
    
  public void setUserInfo(Map<RemoteSession, Boolean> userInfo) {
    this.userInfo = userInfo;
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
    default:
      throw new UnsupportedOperationException("Programmer Error");
    }
  }

}
