package com.example.bot.spring;

import com.example.bot.spring.model.*;
import lombok.extern.slf4j.Slf4j;

import com.example.bot.spring.model.Plan;

import java.math.BigDecimal;
import java.sql.*;
import java.net.URISyntaxException;
import java.net.URI;
import java.util.ArrayList;

@Slf4j
public class SQLDatabaseEngine extends DatabaseEngine {
	@Override
	String search(String text) throws Exception {
		String result = null;
		try {
			Connection connection = this.getConnection();
			PreparedStatement stmt = connection.prepareStatement("SELECT response FROM ChatLookup WHERE keyword=?;");
			stmt.setString(1, text);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				result = rs.getString(1);
			}
			rs.close();
			stmt.close();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (result!=null) {
			return result;
		}
		throw new Exception("NOT FOUND");
	}

	@Override
	ArrayList<Booking> getEnrolledTours(String customerId) {
		String query = String.format("SELECT * FROM Bookings WHERE customerId=%s;", customerId);
		return getResultsForQuery(query, SQLDatabaseEngine::bookingFromResultSet);
	}

	@Override
	BigDecimal getAmmountOwed(String customerId) {
		ArrayList<Booking> bookings = getEnrolledTours(customerId);
		return bookings.stream().map(booking -> (booking.fee.subtract(booking.paid))).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	@Override
	ArrayList<Plan> getPlans() {
		String query = "SELECT * FROM Plans;";
		return getResultsForQuery(query, SQLDatabaseEngine::planFromResultSet);
	}

	@Override
	ArrayList<FAQ> getFAQs() {
		String query = "SELECT question, answer FROM faq;";
		return getResultsForQuery(query, SQLDatabaseEngine::faqFromResultSet);
	}

	@FunctionalInterface
	public interface SQLModelReader<T> {
		T apply(ResultSet t) throws SQLException;
	}

	public <T> ArrayList<T> getResultsForQuery (String query, SQLModelReader<T> modelReader) {
		ArrayList<T> results = new ArrayList<>();
		try {
			Connection connection = this.getConnection();
			PreparedStatement stmt = connection.prepareStatement(query);
			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				results.add(modelReader.apply(resultSet));
			}
			resultSet.close();
			stmt.close();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	public static FAQ faqFromResultSet(ResultSet resultSet) throws SQLException {
		return new FAQ(resultSet.getString(1),
				resultSet.getString(2));
	}

	public static Booking bookingFromResultSet(ResultSet resultSet) throws SQLException {
		return new Booking(resultSet.getString(1),
				resultSet.getString(2),
				resultSet.getString(3),
				resultSet.getInt(4),
				resultSet.getInt(5),
				resultSet.getInt(6),
				resultSet.getBigDecimal(7),
				resultSet.getBigDecimal(8),
				resultSet.getString(9),
				resultSet.getString(10));
	}

	public static Tag tagFromResultSet(ResultSet resultSet)  throws SQLException{
		return new Tag(resultSet.getString(1),
				resultSet.getString(2));
	}
	
	void insertTag(String cid, String newTag) {
		try {
			Connection connection = this.getConnection();
			PreparedStatement stmt = connection.prepareStatement(
					"INSERT INTO Tags(name, customerID) VALUES(\'?\',\'?\')");
			stmt.setString(2, cid);
			stmt.setString(1, newTag);
			stmt.executeQuery();
			stmt.close();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static Message messageFromResultSet(ResultSet resultSet)  throws SQLException{
		Timestamp ts = resultSet.getTimestamp(2);
				//(Timestamp) resultSet.getObject("created");
		ZonedDateTime zonedDateTime = 
		    ZonedDateTime.ofInstant(ts.toInstant(), ZoneOffset.UTC);
		
		return new Message(resultSet.getString(1),
				zonedDateTime,
				resultSet.getString(3));
	}
	
	public static Customer customerFromResultSet(ResultSet resultSet)  throws SQLException{
		return new Customer(resultSet.getString(1),
				resultSet.getString(2),
				resultSet.getString(3),
				resultSet.getInt(4),
				resultSet.getString(5),
				resultSet.getString(6));
	}

	public static Plan planFromResultSet(ResultSet resultSet) throws SQLException {
		return new Plan(resultSet.getString(1),
				resultSet.getString(2),
				resultSet.getString(3),
				resultSet.getInt(4),
				resultSet.getString(5),
				resultSet.getBigDecimal(6));
	}

	public static Tour tourFromResultSet(ResultSet resultSet) throws SQLException {
		return new Tour(
				resultSet.getString(1),
				resultSet.getString(2),
				resultSet.getString(3),
				resultSet.getString(4),
				resultSet.getString(5),
				resultSet.getInt(6),
				resultSet.getInt(7)
		);
	}

	private Connection getConnection() throws URISyntaxException, SQLException {
		Connection connection;
		URI dbUri = new URI(System.getenv("DATABASE_URL"));

		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath()
				+ "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";

		log.info("Username: {} Password: {}", username, password);
		log.info("dbUrl: {}", dbUrl);

		connection = DriverManager.getConnection(dbUrl, username, password);

		return connection;
	}
}
