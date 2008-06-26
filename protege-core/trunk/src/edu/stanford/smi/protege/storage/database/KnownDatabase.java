package edu.stanford.smi.protege.storage.database;

public enum KnownDatabase {
    MYSQL(true, "mysql", 
          "VARCHAR(255) COLLATE UTF8_BIN", 255, "MEDIUMTEXT",
          "BIT", "SMALLINT", "INT"),
    POSTGRESQL(true, "postgresql",
               "VARCHAR(255)", 255, "TEXT",
               "BOOL", "INT2", "INT4"),
    SQLSERVER(true, "MsSqlServer",
              "VARCHAR(255) COLLATE SQL_Latin1_General_CP1_CS_AS", 255, "NTEXT",
              "BIT", "SMALLINT", "INT"),
    ORACLE(true, "oracle",
           "VARCHAR2(1900)", 1900, "LONG",
           "SMALLINT", "SMALLINT", "INTEGER")
    ;

    private boolean supported;
    private String shortName;
    private String stringType;
    private int maxStringSize;
    private String longStringType;
    private String bitType;
    private String smallIntType;
    private String intType;

    private KnownDatabase(boolean supported,
                          String shortName, 
                          String stringType, 
                          int maxStringSize,
                          String longStringType, 
                          String bitType,
                          String smallIntType, 
                          String intType) {
        this.supported = supported;
        this.shortName = shortName;
        this.stringType = stringType;
        this.maxStringSize = maxStringSize;
        this.longStringType = longStringType;
        this.bitType = bitType;
        this.smallIntType = smallIntType;
        this.intType = intType;
    }
    
    public boolean getSupported() {
        return supported;
    }

    public String getShortName() {
        return shortName;
    }

    public String getStringType() {
        return stringType;
    }
    
    public int getMaxStringSize() {
        return maxStringSize;
    }

    public String getLongStringType() {
        return longStringType;
    }

    public String getBitType() {
        return bitType;
    }

    public String getSmallIntType() {
        return smallIntType;
    }

    public String getIntType() {
        return intType;
    }


    public static void main(String [] args) {
        
    }
}
