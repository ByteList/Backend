package de.gamechest.backend.sql;

/**
 * Created by ByteList on 08.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class SqlLiteTableStructure {

    private String asString;
    private StringBuilder appended;

    public SqlLiteTableStructure() {
        this.asString = "tableStructure[]";
        this.appended = new StringBuilder();
    }

    public SqlLiteTableStructure append(String key) {
        this.appended.append("'").append(key).append("'").append(" string,");
        return this;
    }

    public SqlLiteTableStructure create() {
        String string = "tableStructure[" + this.appended.toString() + "#$#]";
        this.asString = string.replace(",#$#", "");
        return this;
    }

    @Override
    public String toString() {
        return this.asString;
    }

    public String toStatementFormattedString() {
        return this.asString.replace("tableStructure[", "(").replace("]", ")").replaceAll("'", "");
    }

    public String toValuesFormattedString() {
        return this.asString.replace("tableStructure[", "(").replace("]", ")");
    }

}
