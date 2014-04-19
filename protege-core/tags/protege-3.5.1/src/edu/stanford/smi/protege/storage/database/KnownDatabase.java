package edu.stanford.smi.protege.storage.database;

public enum KnownDatabase {
    MYSQL(true, "mysql",
          "VARBINARY(310)",
          "VARCHAR(310) COLLATE utf8_general_ci", 310,
          "MEDIUMTEXT",
          "BIT", "SMALLINT", "INT"),
    POSTGRESQL(true, "postgresql",
               "VARCHAR(500)",
               "VARCHAR(500)", 500,
               "TEXT",
               "BOOL", "INT2", "INT4"),
    SQLSERVER(true, "MsSqlServer",
              "VARCHAR(255) COLLATE SQL_Latin1_General_CP1_CS_AS",
              "VARCHAR(255) COLLATE SQL_Latin1_General_CP1_CS_AS", 255,
              "NTEXT",
              "BIT", "SMALLINT", "INT"),
    ORACLE(true, "oracle",
           "VARCHAR2(1900)",
           "VARCHAR2(1900)", 1900,
           "LONG",
           "SMALLINT", "SMALLINT", "INTEGER"),
    DERBY(false, "derby",
          "VARCHAR(500)",
          "VARCHAR(500)", 500,
          "LONG VARCHAR",
          "SMALLINT","SMALLINT","INTEGER")
    ;

    private boolean supported;
    private String  shortName;
    private String  frameNameType;
    private String  shortValueType;
    private int     maxShortValueSize;
    private String  longStringType;
    private String  bitType;
    private String  smallIntType;
    private String  intType;

    private KnownDatabase(boolean supported,
                          String shortName,
                          String frameNameType,
                          String shortValueType,
                          int maxShortValueSize,
                          String longStringType,
                          String bitType,
                          String smallIntType,
                          String intType) {
        this.supported = supported;
        this.shortName = shortName;
        this.frameNameType = frameNameType;
        this.shortValueType = shortValueType;
        this.maxShortValueSize = maxShortValueSize;
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

    public String getFrameNameType() {
        return frameNameType;
    }

    public String getShortValueType() {
        return shortValueType;
    }

    public int getMaxShortValueSize() {
        return maxShortValueSize;
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
