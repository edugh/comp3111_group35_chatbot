package com.example.bot.spring;

import com.example.bot.spring.model.Booking;
import com.example.bot.spring.model.Customer;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.ImageMessage;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;

import static org.h2.engine.Constants.UTF8;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { KitchenSinkTester.class, SQLDatabaseEngine.class })
public class KitchenSinkTester {

	private DatabaseEngine databaseEngine;
	private MockKitchenSinkController kitchenSinkController;

	private static final String TESTDATA_FILE = "dataset.xml";
	private static final String JDBC_DRIVER = org.h2.Driver.class.getName();
	private static final String JDBC_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL";
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
		// only initialize once
		if (databaseEngine == null) {
			databaseEngine = new MockDatabaseEngine(dataSource());
			kitchenSinkController = new MockKitchenSinkController(databaseEngine);
		}
		kitchenSinkController.clearMessages();
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

	private MessageEvent<TextMessageContent> createMessageEvent(String replyToken, String userId, String messageId, String messageText) {
		Source source = new UserSource(userId);
		TextMessageContent messageContent = new TextMessageContent(messageId, messageText);
		return new MessageEvent<>(replyToken, source, messageContent, null);
	}

	@Test
	public void testBasicFAQResponse() throws Exception {
		MessageEvent<TextMessageContent> messageEvent;
		List<Message> responses;

		messageEvent = createMessageEvent("replyToken1", "userId1", "messageId1", "How to apply?");
		kitchenSinkController.handleTextMessageEvent(messageEvent);
		responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 1);
		Assert.assertEquals(responses.get(0), new TextMessage("FAQ answer 1"));

		messageEvent = createMessageEvent("replyToken2", "userId1", "messageId2", "Where is the gathering/assemble and dismiss spot?");
		kitchenSinkController.handleTextMessageEvent(messageEvent);
		responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 1);
		Assert.assertEquals(responses.get(0), new TextMessage("FAQ answer 2"));
	}

	@Test
	public void testJoinResponseAndTableUpdated() throws Exception {
		FollowEvent followEvent = createFollowEvent("replyToken1", "userId1");
		kitchenSinkController.handleFollowEvent(followEvent);
		List<Message> responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 3);

		Message[] expectedMessages = {
				new TextMessage("Welcome. This is travel chatbot No.35."),
				new ImageMessage(
						"https://kendrickuy.com/wp-content/uploads/2016/01/Travel-for-One-Year-Update-Kendrick-Uy.jpg",
						"https://kendrickuy.com/wp-content/uploads/2016/01/Travel-for-One-Year-Update-Kendrick-Uy.jpg"),
				new TextMessage("What can I do for you?")
		};
		Assert.assertArrayEquals(responses.toArray(), expectedMessages);

		Optional<Customer> customer = databaseEngine.getCustomer("userId1");
		Assert.assertTrue(customer.isPresent());
		Assert.assertEquals(customer.get(), new Customer("userId1", null, null, 0, null, "new"));
	}

	@Test
	public void testMultipleJoinedUsers() throws Exception {
		FollowEvent followEvent1 = createFollowEvent("replyToken1", "userId1");
		FollowEvent followEvent2 = createFollowEvent("replyToken2", "userId2");
		kitchenSinkController.handleFollowEvent(followEvent1);
		kitchenSinkController.handleFollowEvent(followEvent2);

		Optional<Customer> customer1 = databaseEngine.getCustomer("userId1");
		Assert.assertTrue(customer1.isPresent());
		Assert.assertEquals(customer1.get(), new Customer("userId1", null, null, 0, null, "new"));

		Optional<Customer> customer2 = databaseEngine.getCustomer("userId2");
		Assert.assertTrue(customer2.isPresent());
		Assert.assertEquals(customer2.get(), new Customer("userId2", null, null, 0, null, "new"));
	}

	@Test
	public void testQueryTours() throws Exception {
		MessageEvent<TextMessageContent> messageEvent;

		FollowEvent followEvent = createFollowEvent("replyToken1", "userId1");
		kitchenSinkController.handleFollowEvent(followEvent);

		messageEvent = createMessageEvent("replyToken2", "userId1", "messageId2", "Which tours are available?");
		kitchenSinkController.handleTextMessageEvent(messageEvent);

		List<Message> responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 5);
		Assert.assertEquals(responses.get(3), new TextMessage("Id1: Fake Tour 1 - Description1"));
		Assert.assertEquals(responses.get(4), new TextMessage("Id2: Fake Tour 2 - Description2"));
	}

	/*
	Since we arent really using postresql we need to do some weird comparison stuff between floats and bigdecimals
	 */
	private boolean closeEnough(BigDecimal num1, double num2) {
		return num1.intValue() == (int) num2;
	}

	@Test
	public void testBookingFlowBasic() throws Exception {
		MessageEvent<TextMessageContent> messageEvent;

		FollowEvent followEvent = createFollowEvent("replyToken1", "userId1");
		kitchenSinkController.handleFollowEvent(followEvent);

		Map<String, String> userResponses = new HashMap<>();
		userResponses.put("What can I do for you?", "Which tours are available?");
		userResponses.put("Id2: Fake Tour 2 - Description2", "Can I book Fake Tour 2?");
		userResponses.put("What's your name, please?", "Jason Zukewich");
		userResponses.put("Male or Female please?", "M");
		userResponses.put("How old are you please?", "20");
		userResponses.put("Phone number please?", "0123 4567");
		userResponses.put("When are you planing to set out? Please answer in YYYY/MM/DD.", "2017/11/08");
		userResponses.put("How many adults(Age>11) are planning to go?", "1");
		userResponses.put("How many children (Age 4 to 11) are planning to go?", "3");
		userResponses.put("How many children (Age 0 to 3) are planning to go?", "5");
		userResponses.put("Confirmed?", "yes");


		while (true) {
			List<Message> botResponses = kitchenSinkController.getLatestMessages();
			String lastBotMessage = ((TextMessage) botResponses.get(botResponses.size() - 1)).getText();
			log.info("bot message: {}", lastBotMessage);
			String userResponse = userResponses.get(lastBotMessage);
			messageEvent = createMessageEvent("replyToken2", "userId1", "messageId2", userResponse);
			kitchenSinkController.handleTextMessageEvent(messageEvent);
			log.info("user response: {}", userResponse);
			if (userResponse.equals("yes")) {
				//Confirmed
				break;
			}
		}
		List<Message> responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 1);
		Assert.assertEquals(responses.get(0), new TextMessage("Thank you. Please pay the tour fee by ATM to 123-345-432-211 of ABC Bank or by cash in our store. When you complete the ATM payment, please send the bank in slip to us. Our staff will validate it."));

		Assert.assertEquals(databaseEngine.getCustomer("userId1").get(), new Customer("userId1", "Jason", "M", 20, "01234567", "booked"));

		BigDecimal ammountOwed = databaseEngine.getAmmountOwed("userId1");
		Assert.assertTrue(closeEnough(ammountOwed, 1247.5));

		ArrayList<Booking> bookings = databaseEngine.getBookings("userId1");
		Assert.assertEquals(bookings.size(), 1);
		Booking booking = bookings.get(0);
		Assert.assertTrue(closeEnough(booking.fee, 1247.5));
		Assert.assertTrue(closeEnough(booking.paid, 0));
		Booking expectedBooking = new Booking("userId1", "Id1", Utils.getDateFromText("2017/11/08"), 1, 3, 5, booking.fee, booking.paid, null);
		Assert.assertEquals(booking, expectedBooking);
	}
}
