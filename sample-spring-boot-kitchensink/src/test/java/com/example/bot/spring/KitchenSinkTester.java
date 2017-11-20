package com.example.bot.spring;

import com.example.bot.spring.model.Booking;
import com.example.bot.spring.model.Customer;
import com.example.bot.spring.model.Dialogue;
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
import java.sql.Date;
import java.sql.*;
import java.util.*;

import static org.h2.engine.Constants.UTF8;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { KitchenSinkTester.class })
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
			databaseEngine = DatabaseEngine.connectToTest(dataSource());
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
				new ImageMessage("https://i.imgur.com/RpIsqnC.jpg",	"https://i.imgur.com/kQNwgcK.jpg"),
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
		kitchenSinkController.clearMessages();

		messageEvent = createMessageEvent("replyToken2", "userId1", "messageId2", "Which tours are available?");
		kitchenSinkController.handleTextMessageEvent(messageEvent);

		List<Message> responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 4);
		Assert.assertEquals(responses.get(0), new TextMessage("Id1: Shimen National Forest Tour - Description1"));
		Assert.assertEquals(responses.get(1), new TextMessage("Id2: Yangshan Hot Spring Tour - Description2"));
		Assert.assertEquals(responses.get(2), new TextMessage("Id3: National Park Tour - Description3"));
	}

	@Test
	public void testQueryToursWithParameters() throws Exception {
		MessageEvent<TextMessageContent> messageEvent;

		FollowEvent followEvent = createFollowEvent("replyToken1", "userId1");
		kitchenSinkController.handleFollowEvent(followEvent);
		kitchenSinkController.clearMessages();

		messageEvent = createMessageEvent("replyToken2", "userId1", "messageId2", "Are there tours going to Shimen National Forest?");
		kitchenSinkController.handleTextMessageEvent(messageEvent);
		List<Message> responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 3);
		Assert.assertEquals(responses.get(0), new TextMessage("Id1: Shimen National Forest Tour - Description1"));
		Assert.assertEquals(responses.get(1), new TextMessage("Id3: National Park Tour - Description3"));

		messageEvent = createMessageEvent("replyToken3", "userId1", "messageId3", "Any Hot Spring Tours on Tuesday?");
		kitchenSinkController.handleTextMessageEvent(messageEvent);
		responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 2);
		Assert.assertEquals(responses.get(0), new TextMessage("Id2: Yangshan Hot Spring Tour - Description2"));

		messageEvent = createMessageEvent("replyToken4", "userId1", "messageId4", "Are there tours going to a Hot Spring on Wednesday?");
		kitchenSinkController.handleTextMessageEvent(messageEvent);
		responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 2);
		Assert.assertEquals(responses.get(0), new TextMessage("Id2: Yangshan Hot Spring Tour - Description2"));

		messageEvent = createMessageEvent("replyToken4", "userId1", "messageId4", "What tours are available on Tuesday?");
		kitchenSinkController.handleTextMessageEvent(messageEvent);
		responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 4);
		Assert.assertEquals(responses.get(0), new TextMessage("Id2: Yangshan Hot Spring Tour - Description2"));
		Assert.assertEquals(responses.get(1), new TextMessage("Id3: National Park Tour - Description3"));
		Assert.assertEquals(responses.get(2), new TextMessage("Id1: Shimen National Forest Tour - Description1"));
	}

	@Test
	public void testQueryToursWithPastBookings() throws Exception {
		MessageEvent<TextMessageContent> messageEvent;

		FollowEvent followEvent = createFollowEvent("replyToken1", "userId1");
		kitchenSinkController.handleFollowEvent(followEvent);
		kitchenSinkController.clearMessages();

		databaseEngine.insertBooking("userId1", "Id1", new Date(0), 1, 3, 5, new BigDecimal(100), new BigDecimal(0));
		messageEvent = createMessageEvent("replyToken2", "userId1", "messageId2", "Which tours are available?");
		kitchenSinkController.handleTextMessageEvent(messageEvent);
		List<Message> responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 3);
		Assert.assertEquals(responses.get(0), new TextMessage("Id2: Yangshan Hot Spring Tour - Description2"));
		Assert.assertEquals(responses.get(1), new TextMessage("Id3: National Park Tour - Description3"));

		databaseEngine.insertBooking("userId1", "Id2", new Date(0), 1, 3, 5, new BigDecimal(100), new BigDecimal(0));
		messageEvent = createMessageEvent("replyToken2", "userId1", "messageId2", "Which tours are available?");
		kitchenSinkController.handleTextMessageEvent(messageEvent);
		responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 2);
		Assert.assertEquals(responses.get(0), new TextMessage("Id3: National Park Tour - Description3"));
	}

	@Test
	public void testUnhandledUserText() throws Exception {
		MessageEvent<TextMessageContent> messageEvent;

		FollowEvent followEvent = createFollowEvent("replyToken1", "userId1");
		kitchenSinkController.handleFollowEvent(followEvent);
		kitchenSinkController.clearMessages();

		ArrayList<Dialogue> dialogueRecord = databaseEngine.getDialogues("userId1");
		Assert.assertTrue(dialogueRecord.isEmpty());

		messageEvent = createMessageEvent("replyToken2", "userId1", "messageId2", "Elephants fly inward from the sky");
		kitchenSinkController.handleTextMessageEvent(messageEvent);

		List<Message> responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 1);
		Assert.assertEquals(responses.get(0), new TextMessage("I don't understand your question, try rephrasing"));
		ArrayList<Dialogue> dialogueRecordAfter = databaseEngine.getDialogues("userId1");
		Assert.assertTrue(!dialogueRecordAfter.isEmpty());
		Assert.assertEquals(dialogueRecordAfter.get(0).customerId, "userId1");
		Assert.assertEquals(dialogueRecordAfter.get(0).content, "Elephants fly inward from the sky");
	}

	@Test
	public void testUnhandledUserTextReport() throws Exception {
		MessageEvent<TextMessageContent> messageEvent;

		FollowEvent followEvent = createFollowEvent("replyToken1", "userId1");
		kitchenSinkController.handleFollowEvent(followEvent);
		kitchenSinkController.clearMessages();

		ArrayList<Dialogue> dialogueRecord = databaseEngine.getDialogues("userId1");
		Assert.assertTrue(dialogueRecord.isEmpty());

		messageEvent = createMessageEvent("replyToken2", "userId1", "messageId2", "Elephants fly inward from the sky");
		kitchenSinkController.handleTextMessageEvent(messageEvent);
		messageEvent = createMessageEvent("replyToken2", "userId1", "messageId3", "Allllllll not is watch");
		kitchenSinkController.handleTextMessageEvent(messageEvent);
		kitchenSinkController.clearMessages();

		messageEvent = createMessageEvent("replyToken2", "userId1", "messageId4", "admin:question_report");
		kitchenSinkController.handleTextMessageEvent(messageEvent);

		List<Message> responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 2);
		Assert.assertEquals(responses.get(0), new TextMessage("Elephants fly inward from the sky"));
		Assert.assertEquals(responses.get(1), new TextMessage("Allllllll not is watch"));
	}

	public void goThroughDialogflow(Map<String, String> userResponses, String breakString) throws Exception {
		while (true) {
			List<Message> botResponses = kitchenSinkController.getLatestMessages();
			String lastBotMessage = botResponses.isEmpty()? null : ((TextMessage) botResponses.get(botResponses.size() - 1)).getText();
			log.info("bot message: {}", lastBotMessage);
			String userResponse = userResponses.get(lastBotMessage);
			MessageEvent<TextMessageContent> messageEvent = createMessageEvent("replyToken2", "userId1", "messageId2", userResponse);
			kitchenSinkController.handleTextMessageEvent(messageEvent);
			log.info("user response: {}", userResponse);
			if (userResponse.equals(breakString)) {
				break;
			}
		}
	}

	@Test
	public void testBookingFlowWithUserCreation() throws Exception {
		MessageEvent<TextMessageContent> messageEvent;

		FollowEvent followEvent = createFollowEvent("replyToken1", "userId1");
		kitchenSinkController.handleFollowEvent(followEvent);
        kitchenSinkController.clearMessages();

		Map<String, String> userResponses = new HashMap<>();
		userResponses.put(null, "Which tours are available?");
		userResponses.put("Here are some tours that may interest you, please respond which one you would like to book", "Can I book the Shimen National Forest Tour?");
		userResponses.put("What's your name, please?", "Jason Zukewich");
		userResponses.put("Male or Female please?", "M");
		userResponses.put("How old are you please?", "20");
		userResponses.put("Phone number please?", "0123 4567");
		userResponses.put("When are you planing to set out? Please answer in YYYY/MM/DD.", "2017/11/08");
		userResponses.put("How many adults(Age>11) are planning to go?", "1");
		userResponses.put("How many children (Age 4 to 11) are planning to go?", "3");
		userResponses.put("How many children (Age 0 to 3) are planning to go?", "5");
		userResponses.put("Confirmed?", "yes");
		goThroughDialogflow(userResponses, "yes");

		List<Message> responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 1);
		Assert.assertEquals(responses.get(0), new TextMessage("Thank you. Please pay the tour fee by ATM to 123-345-432-211 of ABC Bank or by cash in our store. When you complete the ATM payment, please send the bank in slip to us. Our staff will validate it."));

		Assert.assertEquals(databaseEngine.getCustomer("userId1").get(), new Customer("userId1", "Jason", "M", 20, "01234567", "booked"));

		BigDecimal amountOwed = databaseEngine.getAmountOwed("userId1");
		Assert.assertEquals(amountOwed.doubleValue(), 1247.5, 1);

		ArrayList<Booking> bookings = databaseEngine.getBookings("userId1");
		Assert.assertEquals(bookings.size(), 1);
		Booking booking = bookings.get(0);
		Assert.assertEquals(booking.fee.doubleValue(), 1247.5, 1);
		Assert.assertEquals(booking.paid.doubleValue(), 0, 1);
		Booking expectedBooking = new Booking("userId1", "Id1", Utils.getDateFromText("2017/11/08"), 1, 3, 5, booking.fee, booking.paid, null);
		Assert.assertEquals(booking, expectedBooking);
	}

	@Test
	public void testBookingFlowBasic() throws Exception {
		databaseEngine.insertCustomer("userId1", "Jason", 20, "M", "01234567");

		Map<String, String> userResponses = new HashMap<>();
		userResponses.put(null, "Which tours are available?");
		userResponses.put("Here are some tours that may interest you, please respond which one you would like to book", "Can I book the Shimen National Forest Tour?");
		userResponses.put("When are you planing to set out? Please answer in YYYY/MM/DD.", "2017/11/08");
		userResponses.put("How many adults(Age>11) are planning to go?", "1");
		userResponses.put("How many children (Age 4 to 11) are planning to go?", "3");
		userResponses.put("How many children (Age 0 to 3) are planning to go?", "5");
		userResponses.put("Confirmed?", "yes");
		goThroughDialogflow(userResponses, "yes");

		List<Message> responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 1);
		Assert.assertEquals(responses.get(0), new TextMessage("Thank you. Please pay the tour fee by ATM to 123-345-432-211 of ABC Bank or by cash in our store. When you complete the ATM payment, please send the bank in slip to us. Our staff will validate it."));

		BigDecimal amountOwed = databaseEngine.getAmountOwed("userId1");
		Assert.assertEquals(amountOwed.doubleValue(), 1247.5, 1);

		ArrayList<Booking> bookings = databaseEngine.getBookings("userId1");
		Assert.assertEquals(bookings.size(), 1);
		Booking booking = bookings.get(0);
		Assert.assertEquals(booking.fee.doubleValue(), 1247.5, 1);
		Assert.assertEquals(booking.paid.doubleValue(), 0, 1);
		Booking expectedBooking = new Booking("userId1", "Id1", Utils.getDateFromText("2017/11/08"), 1, 3, 5, booking.fee, booking.paid, null);
		Assert.assertEquals(booking, expectedBooking);
	}

    @Test
    public void testAmountOwed() throws Exception {
		databaseEngine.insertBooking("userId1", "Id1", new Date(0), 1, 3, 5, new BigDecimal(100), new BigDecimal(0));
		databaseEngine.insertBooking("userId1", "Id2", new Date(0), 1, 3, 5, new BigDecimal(100), new BigDecimal(0));
		databaseEngine.insertBooking("userId2", "Id3", new Date(0), 1, 3, 5, new BigDecimal(100), new BigDecimal(0));

		Assert.assertEquals(databaseEngine.getAmountOwed("userId1").doubleValue(), 200, 1);
		Assert.assertEquals(databaseEngine.getAmountOwed("userId2").doubleValue(), 100, 1);
	}

    @Test
    public void testEnrolledTours() throws Exception {
		databaseEngine.insertBooking("userId1", "Id1", new Date(0), 2, 4, 6, new BigDecimal(100), new BigDecimal(0));
		databaseEngine.insertBooking("userId1", "Id2", new Date(0), 1, 3, 5, new BigDecimal(100), new BigDecimal(0));
		databaseEngine.insertBooking("userId2", "Id3", new Date(0), 0, 2, 4, new BigDecimal(100), new BigDecimal(0));

        ArrayList<Booking> bookings = databaseEngine.getBookings("userId1");
        Assert.assertEquals(bookings.size(), 2);
        Booking booking = bookings.get(0);
        Booking expectedBooking = new Booking("userId1", "Id1", booking.tourDate, 2, 4, 6, booking.fee, booking.paid, null);
        Assert.assertEquals(booking, expectedBooking);

		bookings = databaseEngine.getBookings("userId2");
		Assert.assertEquals(bookings.size(), 1);
		booking = bookings.get(0);
		expectedBooking = new Booking("userId2", "Id3", booking.tourDate, 0, 2, 4, booking.fee, booking.paid, null);
		Assert.assertEquals(booking, expectedBooking);
    }

	@Test
	public void testTourFull() throws Exception {
		String pid = "Id2";
		Date date = Date.valueOf("2017-11-14");
		Assert.assertFalse(databaseEngine.isTourFull(pid, date));

		databaseEngine.insertCustomer("userId1", "Elliot", 22, "M", "01234567");
		databaseEngine.insertBooking("userId1", pid, date, 2, 3, 2, BigDecimal.ZERO, BigDecimal.ZERO);
        Assert.assertFalse(databaseEngine.isTourFull(pid, date));

        databaseEngine.insertCustomer("userId2", "Keith", 22, "M", "12345800");
        databaseEngine.insertBooking("userId2", pid, date, 1, 1, 1, BigDecimal.ZERO, BigDecimal.ZERO);
        Assert.assertTrue(databaseEngine.isTourFull(pid, date));
	}

	// NEGATIVE TEST CASES
	@Test
	public void testUnknownQuestions() throws Exception {
		MessageEvent<TextMessageContent> messageEvent;
		FollowEvent followEvent = createFollowEvent("replyToken1", "userId1");
		kitchenSinkController.handleFollowEvent(followEvent);
		kitchenSinkController.clearMessages();

		messageEvent = createMessageEvent("replyToken2", "userId1", "messageId2", "wubalubadubdub");
		kitchenSinkController.handleTextMessageEvent(messageEvent);

		List<Message> responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 1);
		Assert.assertEquals(responses.get(0), new TextMessage("I don't understand your question, try rephrasing"));
	}

	@Test
	public void testCancelBookings() throws Exception {
		databaseEngine.insertCustomer("userId1", "Jason", 20, "M", "01234567");

		Map<String, String> userResponses = new HashMap<>();
		userResponses.put(null, "Which tours are available?");
		userResponses.put("Here are some tours that may interest you, please respond which one you would like to book", "Can I book the Shimen National Forest Tour?");
		userResponses.put("When are you planing to set out? Please answer in YYYY/MM/DD.", "2017/11/08");
		userResponses.put("How many adults(Age>11) are planning to go?", "1");
		userResponses.put("How many children (Age 4 to 11) are planning to go?", "3");
		userResponses.put("How many children (Age 0 to 3) are planning to go?", "5");
		userResponses.put("Confirmed?", "no");
		goThroughDialogflow(userResponses, "no");

		List<Message> responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 1);
		Assert.assertEquals(responses.get(0), new TextMessage("Booking Cancelled"));

		BigDecimal amountOwed = databaseEngine.getAmountOwed("userId1");
		Assert.assertEquals(amountOwed.doubleValue(), 0, 1);

		ArrayList<Booking> bookings = databaseEngine.getBookings("userId1");
		Assert.assertEquals(bookings.size(), 0);
	}

	@Test
	public void testFullBooking() throws Exception {
		databaseEngine.insertCustomer("userId1", "Jason", 20, "M", "01234567");

		Map<String, String> userResponses = new HashMap<>();
		userResponses.put(null, "Which tours are available?");
		userResponses.put("Here are some tours that may interest you, please respond which one you would like to book", "Can I book the Shimen National Forest Tour?");
		userResponses.put("When are you planing to set out? Please answer in YYYY/MM/DD.", "2017/11/06");
		userResponses.put("Are you interested in changing to any of these trips?", "How about the Shimen National Forest Tour?");
		userResponses.put("When are you planing to set out? Please answer in YYYY/MM/DD.", "2017/11/08");
		userResponses.put("How many adults(Age>11) are planning to go?", "1");
		userResponses.put("How many children (Age 4 to 11) are planning to go?", "3");
		userResponses.put("How many children (Age 0 to 3) are planning to go?", "5");
		userResponses.put("Confirmed?", "yes");
		goThroughDialogflow(userResponses, "yes");

		List<Message> responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 1);
		Assert.assertEquals(responses.get(0), new TextMessage("Thank you. Please pay the tour fee by ATM to 123-345-432-211 of ABC Bank or by cash in our store. When you complete the ATM payment, please send the bank in slip to us. Our staff will validate it."));

		BigDecimal amountOwed = databaseEngine.getAmountOwed("userId1");
		Assert.assertEquals(amountOwed.doubleValue(), 1247.5, 1);

		ArrayList<Booking> bookings = databaseEngine.getBookings("userId1");
		Assert.assertEquals(bookings.size(), 1);
		Booking booking = bookings.get(0);
		Assert.assertEquals(booking.fee.doubleValue(), 1247.5, 1);
		Assert.assertEquals(booking.paid.doubleValue(), 0, 1);
		Booking expectedBooking = new Booking("userId1", "Id1", Utils.getDateFromText("2017/11/08"), 1, 3, 5, booking.fee, booking.paid, null);
		Assert.assertEquals(booking, expectedBooking);
	}
	public void testCancelPartialBooking() throws Exception {
		databaseEngine.insertCustomer("userId1", "Jason", 20, "M", "01234567");

		Map<String, String> userResponses = new HashMap<>();
		userResponses.put(null, "Which tours are available?");
		userResponses.put("Here are some tours that may interest you, please respond which one you would like to book", "Can I book the Shimen National Forest Tour?");
		userResponses.put("When are you planing to set out? Please answer in YYYY/MM/DD.", "2017/11/08");
		userResponses.put("How many adults(Age>11) are planning to go?", "1");
		userResponses.put("How many children (Age 4 to 11) are planning to go?", "Cancel Booking");
		goThroughDialogflow(userResponses, "Cancel Booking");

		List<Message> responses = kitchenSinkController.getLatestMessages();
		Assert.assertEquals(responses.size(), 1);
		Assert.assertEquals(responses.get(0), new TextMessage("Booking Cancelled"));

		BigDecimal amountOwed = databaseEngine.getAmountOwed("userId1");
		Assert.assertEquals(amountOwed.doubleValue(), 0, 1);

		ArrayList<Booking> bookings = databaseEngine.getBookings("userId1");
		Assert.assertEquals(bookings.size(), 0);
	}
}
