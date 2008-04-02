package edu.stanford.smi.protege.storage.database;

import java.sql.SQLException;
import java.util.EnumSet;


public class SelectStatement<X extends Enum<X>> {
  private final EnumSet<X> cols;
  private String stmt;
  
  public SelectStatement(EnumSet<X> cols) {
    this.cols = cols;
  }
  
  public String toString() {
    if (stmt == null) {
      stmt = "Select ";
      boolean firstTime = true;
      for (X col : cols) {
        if (firstTime) {
          firstTime = false;
        } else {
          stmt += ", ";
        }
        stmt += col;
      }
      stmt += " ";
    }
    return stmt;
  }
  
  public int rsIndex(X col) throws SQLException {
    int i = 1;
    for (X foundCol : cols) {
      if (foundCol == col) {
        return i;
      }
      i++;
    }
    throw new SQLException("Missing index - probably implementation Error");
  }

}
