package com.example.bot.spring;

import com.example.bot.spring.model.Customer;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.h2.engine.Constants.UTF8;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { KitchenSinkTester.class, SQLDatabaseEngine.class })
public class KitchenSinkTester {

	@Autowired
	private DatabaseEngine databaseEngine;

	private MockKitchenSinkController kitchenSinkController;

	private static final String TESTDATA_FILE = "dataset.xml";

	private static final String JDBC_DRIVER = org.h2.Driver.class.getName();
	private static final String JDBC_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
	private static final String USER = "sa";
	private static final String PASSWORD = "";

	@BeforeClass
	public static void createSchema() throws Exception {
		RunScript.execute(JDBC_URL, USER, PASSWORD, "schema.sql", UTF8, false);
	}

	@Before
	public void beforeEachTest() throws Exception {
		IDataSet dataSet = readDataSet();
		cleanlyInsert(dataSet);
		Connection connection = dataSource().getConnection();
		databaseEngine = new MockDatabaseEngine(connection);
		kitchenSinkController = new MockKitchenSinkController(databaseEngine);
	}

	private DataSource dataSource() {
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setURL(JDBC_URL);
		dataSource.setUser(USER);
		dataSource.setPassword(PASSWORD);
		return dataSource;
	}

	private IDataSet readDataSet() throws Exception {
		return new FlatXmlDataSetBuilder().build(new File(TESTDATA_FILE));
	}

	private void cleanlyInsert(IDataSet dataSet) throws Exception {
		IDatabaseTester databaseTester = new JdbcDatabaseTester(JDBC_DRIVER, JDBC_URL, USER, PASSWORD);
		databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
		databaseTester.setDataSet(dataSet);
		databaseTester.onSetup();
	}

	private FollowEvent createFollowEvent(String replyToken, String userId) {
		Source source = new UserSource(userId);
		return new FollowEvent(replyToken, source, null);
	}

	@Test
	public void sanityCheckFAQs() throws Exception {
		Assert.assertEquals(databaseEngine.getFAQs().size(), 13);
	}

	@Test
	public void testFAQResponse() throws Exception {
		FollowEvent followEvent = createFollowEvent("replyToken0", "userId0");
		kitchenSinkController.handleFollowEvent(followEvent);
		List<Message> messages = kitchenSinkController.getLatestMessages();
		Assert.assertTrue(messages.size() == 2);

		Message[] expectedMessages = {
				new TextMessage("Welcome. This is travel chatbot No.35. What can I do for you?"),
				new TextMessage("We don't have promotion image...")
		};
		Assert.assertArrayEquals(messages.toArray(), expectedMessages);

		Customer customer = databaseEngine.getCustomer("userId0");
		Assert.assertEquals(customer, new Customer("userId0", null, null, 0, null, null));
	}

/*
-Test new user, need to mock objects passed into kitchen sink controller, make sure row is inserted into database properly
also test that response is correct, maybe override reply method to send back?
-Test multiple users adding
-Test faq
-Test complete flow then ask enrollelent and ammount owed
-Test complete most of flow, cancel and check
*/
}
