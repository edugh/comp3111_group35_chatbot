package com.example.bot.spring;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Test;

import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class KitchenSinkTester extends DatabaseTestCase {

	private static final String TESTDATA_FILE = "dataset.xml";
	
	@Test
	public void testDummy() throws Exception {
		DatabaseEngine databaseEngine = new SQLDatabaseEngine();
		System.out.print(databaseEngine.getFAQs().size());
	}

	@Override
	protected IDatabaseConnection getConnection() throws Exception {
		Class driverClass = Class.forName("com.mckoi.JDBCDriver");
		String url = "jdbc:mckoi://localhost/";
		String usr = "admin_user";
		String pwd = "aupass00";
		Connection jdbcConnection = DriverManager.getConnection(url, usr, pwd);
		return new DatabaseConnection(jdbcConnection);
	}

	@Override
	protected IDataSet getDataSet() throws Exception {
		return new FlatXmlDataSet(new FileInputStream(TESTDATA_FILE));
	}
}
