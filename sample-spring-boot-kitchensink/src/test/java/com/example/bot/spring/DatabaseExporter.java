package com.example.bot.spring;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility to pull our database into an xml file to help with testing
 */
public class DatabaseExporter
{
    public static void main(String[] args) throws Exception
    {
        Connection jdbcConnection = getConnection();
        IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);

        IDataSet fullDataSet = connection.createDataSet();
        FlatXmlDataSet.write(fullDataSet, new FileOutputStream("dataset.xml"));
    }

    private static Connection getConnection() throws URISyntaxException, SQLException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath()
                + "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";

        Connection connection = DriverManager.getConnection(dbUrl, username, password);

        return connection;
    }
}
