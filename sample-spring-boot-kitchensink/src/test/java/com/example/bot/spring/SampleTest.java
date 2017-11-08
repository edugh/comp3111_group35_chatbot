package com.example.bot.spring;

import junit.framework.TestCase;
import org.dbunit.DBTestCase;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;

import java.io.FileInputStream;

//public class SampleTest extends TestCase
//{
//    private IDatabaseTester databaseTester;
//
//    public SampleTest(String name)
//    {
//        super(name);
//    }
//
//    protected void setUp() throws Exception
//    {
//        Class.forName("com.mckoi.JDBCDriver");
//        databaseTester = new JdbcDatabaseTester("com.mckoi.JDBCDriver", "jdbc:mckoi:sample", "sa", "");
//
//        // initialize your dataset here
//        IDataSet dataSet = null;
//        // ...
//
//        databaseTester.setDataSet( dataSet );
//        // will call default setUpOperation
//        databaseTester.onSetup();
//    }
//
//    protected void tearDown() throws Exception
//    {
//        // will call default tearDownOperation
//        databaseTester.onTearDown();
//    }
//
//    @Test
//    public void testExample() throws Exception
//    {
//        databaseTester.getConnection().getConnection();
//    }
//}

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.FileInputStream;

import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;

public class SampleTest extends DBTestCase {
    public SampleTest(String name) {
        super(name);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, "com.mysql.jdbc.Driver");
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, "jdbc:mysql://localhost:3306/user");
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, "root");
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, "");
    }

    private void cleanlyInsertDataset(IDataSet dataSet) throws Exception {
        IDatabaseTester databaseTester = new JdbcDatabaseTester(
                "org.h2.Driver", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "");
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.setDataSet(dataSet);
        databaseTester.onSetup();
    }

    protected IDataSet getDataSet() throws Exception {
        return new FlatXmlDataSetBuilder().build(new FileInputStream("user.xml"));
    }

    protected DatabaseOperation getSetUpOperation() throws Exception {
        return DatabaseOperation.REFRESH;
    }

    protected DatabaseOperation getTearDownOperation() throws Exception {
        return DatabaseOperation.NONE;
    }

    @Test
    public void testById() {

        int userId = 5;// get user id from database
        assertThat(1, is(userId));
    }
}
