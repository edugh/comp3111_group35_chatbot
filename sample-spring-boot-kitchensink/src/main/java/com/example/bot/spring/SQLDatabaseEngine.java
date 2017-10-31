package com.example.bot.spring;

import com.example.bot.spring.model.FAQ;
import com.example.bot.spring.model.Tour;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
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
	ArrayList<Tour> getTours() {
		ArrayList<Tour> tours = new ArrayList<>();
		try {
			Connection connection = this.getConnection();
			PreparedStatement stmt = connection.prepareStatement("SELECT tourid, tourname, tourshortdescription, tourlength, tourdeparture, tourprice FROM tourlist;");
			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				Tour tour = Tour.fromResultSet(resultSet);
				tours.add(tour);
			}
			resultSet.close();
			stmt.close();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tours;
	}

	@Override
	ArrayList<FAQ> getFAQs() {
		ArrayList<FAQ> faqs = new ArrayList<>();
		try {
			Connection connection = this.getConnection();
			PreparedStatement stmt = connection.prepareStatement("SELECT question, answer FROM faq;");
			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				FAQ faq = FAQ.fromResultSet(resultSet);
				faqs.add(faq);
			}
			resultSet.close();
			stmt.close();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return faqs;
	}

	
	
	//TODO(Shuo): read/write order, customer, tour here
	@Override
	Ordering readOrdering(string cid) {
		try {
			Connection connection = this.getConnection();
			PreparedStatement stmt = connection.prepareStatement(
					"SELECT * FROM orderinglist WHERE custID = " + cid + ";");
			ResultSet resultSet = stmt.executeQuery();
			//TODO: construct an ordering
			
			stmt.close();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	void updateOrdering(Ordering ordering, String field, String value) {
		try {
			Connection connection = this.getConnection();
			PreparedStatement stmt = connection.prepareStatement(
					"UPDATE orderingList SET " + field + " = " + value
						+ "WHERE custid = " + ordering.cid + ";");
			stmt.executeQuery();
			stmt.close();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	void confirmOrder(Order order) {
		try {
			Connection connection = this.getConnection();
			if(order.spclRqst != null) {
				qString = "INSERT INTO orderlist (custid, tourid, nadult, nchildren, ntoodler, fee, paid, spclrqst) "
						+ "VALUES(\'" + order.custID + "\',\'" + order.tourID + "\'," + Integer.toString(order.nAdult)
						+ "," + Integer.toString(order.nChild) + "," + Integer.toString(order.nToddler)
						+ "," + Double.toString(order.fee) + "," + Double.toString(0) + ",\'" + order.spclRqst + "\');";
			}
			else {
				qString = "INSERT INTO orderlist (custid, tourid, nadult, nchildren, ntoodler, fee, paid) "
						+ "VALUES(\'" + order.custID + "\',\'" + order.tourID + "\'," + Integer.toString(order.nAdult)
						+ "," + Integer.toString(order.nChild) + "," + Integer.toString(order.nToddler)
						+ "," + Double.toString(order.fee) + "," + Double.toString(0) + ");";
			}
			PreparedStatement stmt = connection.prepareStatement(qString);
			stmt.executeQuery();
			stmt.close();
			//update tour(whose id with date), have not create table
			
			stmt = connection.prepareStatement(qString);
			stmt.executeQuery();
			stmt.close();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
