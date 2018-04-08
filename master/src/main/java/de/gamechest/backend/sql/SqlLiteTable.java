package de.gamechest.backend.sql;

/**
 * Created by ByteList on 08.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public interface SqlLiteTable {

    public String getName();

    public SqlLiteTableStructure getStructure();

    public int count();
}
