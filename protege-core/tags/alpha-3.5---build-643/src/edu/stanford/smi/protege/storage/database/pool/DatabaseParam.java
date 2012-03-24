/**
 * 
 */
package edu.stanford.smi.protege.storage.database.pool;

public class DatabaseParam {
    private String driver;
    private String url;
    private String username;
    private String password;
    
    public DatabaseParam(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }
    
    public String getDriver() {
        return driver;
    }
    public String getUrl() {
        return url;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof DatabaseParam)) {
            return false;
        }
        DatabaseParam other = (DatabaseParam) o;
        return stringEquals(driver, other.driver)
            && stringEquals(url, other.url)
            && stringEquals(username, other.username)
            && stringEquals(password, other.password);
    }

    public int hashCode() {
        return driver.hashCode() + 42 * url.hashCode();
    }

    public boolean stringEquals(String s1, String s2) {
        if (s1 == s2) {
            return true;
        }
        else if (s1 == null || s2 == null) {
            return false;
        }
        else return s1.equals(s2);
    }
}