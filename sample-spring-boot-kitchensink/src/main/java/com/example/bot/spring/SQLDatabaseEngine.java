package com.example.bot.spring;

import com.example.bot.spring.model.FAQ;
import com.example.bot.spring.model.Tour;
import com.sun.jmx.remote.util.OrderClassLoaders;
import com.sun.org.apache.xml.internal.resolver.readers.OASISXMLCatalogReader;

import lombok.extern.slf4j.Slf4j;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.net.URISyntaxException;
import java.beans.Customizer;
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
	void newOrdering(String cid) {
		try {
			Connection connection = this.getConnection();
			PreparedStatement stmt = connection.prepareStatement(
					"INSERT INTO orderinglist(custID, state) VALUES(\'?\', 'new');");
			stmt.setString(1, cid);
			stmt.executeQuery();
			stmt.close();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
	
	@Override
	Ordering readOrdering(String cid) {
		Order order = null;
		try {
			Connection connection = this.getConnection();
			PreparedStatement stmt = connection.prepareStatement(
					"SELECT * FROM orderinglist WHERE custID = \'?\';");
			stmt.setString(1, cid);
			ResultSet resultSet = stmt.executeQuery();
			if (rs.next()) {
				Order order = new Order(
						cid,
						rs.getString(2),					//tid
						Integer.parseInt(rs.getString(3)),	//nA
						Integer.parseInt(rs.getString(4)),	//nC
						Integer.parseInt(rs.getString(5)),	//nT
						Double.parseDouble(rs.getString(6)),//fee
						Double.parseDouble(rs.getString(7)),//paid
						rs.getString(8),					//state
						rs.getString(9)						//spclRqst
						//not sure what if db return null
						);
			}
			stmt.close();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return order;
	}
	
	@Override
	void updateOrdering(Ordering ordering, String field, String value, String state) {
		try {
			Connection connection = this.getConnection();
			PreparedStatement stmt = connection.prepareStatement(
					"UPDATE orderingList SET ? = \'?\', state = \'?\' WHERE custid = \'?\';");
			stmt.setString(4, ordering.cid);
			stmt.setString(parameterIndex, x);
			stmt.setString(2, value);
			stmt.setString(1, field);
			stmt.executeQuery();
			stmt.close();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	void updateOrdering(Ordering ordering, String field, int value, double price, String state) {
		try {
			Connection connection = this.getConnection();
			PreparedStatement stmt = connection.prepareStatement(
					"UPDATE orderingList SET ? = ?, state = \'?\' WHERE custid = \'?\';");
			stmt.setString(4, ordering.cid);
			stmt.setString(3, state);
			stmt.setInt(2, value);
			stmt.setString(1, field);
			stmt.executeQuery();
			PreparedStatement stmt = connection.prepareStatement(
					"UPDATE orderingList SET fee = ? WHERE custid = \'?\';");
			stmt.setString(2, cid);
			stmt.setString(1, Double.toString(ordering.calFee(price)));
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
				qString = "INSERT INTO orderlist (custid, tourid, nadult, nchildren, ntoodler, fee, paid, spclrqst, state) "
						+ "VALUES(\'" + order.custID + "\',\'" + order.tourID + "\'," + Integer.toString(order.nAdult)
						+ "," + Integer.toString(order.nChild) + "," + Integer.toString(order.nToddler)
						+ "," + Double.toString(order.fee) + "," + Double.toString(0) + ",\'" 
						+ order.spclRqst + "\',\\'ordered\\');";
			}
			else {
				qString = "INSERT INTO orderlist (custid, tourid, nadult, nchildren, ntoodler, fee, paid, state) "
						+ "VALUES(\'" + order.custID + "\',\'" + order.tourID + "\'," + Integer.toString(order.nAdult)
						+ "," + Integer.toString(order.nChild) + "," + Integer.toString(order.nToddler)
						+ "," + Double.toString(order.fee) + "," + Double.toString(0) + ",\'ordered\');";
			}
			PreparedStatement stmt = connection.prepareStatement(qString);
			stmt.executeQuery();
			// delete ordering
			stmt = connection.prepareStatement(
					"DELETE FROM orderingList WHERE custid = \'?\';");
			stmt.setString(1, cid);
			stmt.executeQuery();
			//update tour(whose id with date), have not create table
			//update customer order history
			
			stmt = connection.prepareStatement(qString);
			stmt.executeQuery();
			stmt.close();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	void newCustomer(String cid) {
		return;
	}
	
	@Override
	Customer readCustomer(String cid) {
		return null;
	}
	
	@Override
	void updateCustomer(String cid) {
		return;
	}
	
	double getTourPrice(String tid) {
		//shuo
		retun 0;
	}
	
	String promoteTour() {
		String pro = new String("Here is the a recomended tour:");
		String result = new String();
		ArrayList<String> tourList = new ArrayList<String>();
		try {
			Connection connection = this.getConnection();
			PreparedStatement stmt = connection.prepareStatement(
					"SELECT tourname FROM tourlist;");
			ResultSet rs = stmt.executeQuery();
			while(rs.next()) {
				result = rs.getString(1);
				tourList.add(result)
			}
			pro = pro + "\n" + tourList.get((int)(Math.random()*tourList.size()));
			rs.close();
			stmt.close();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		return pro;
	}
	
	String promoteTour(String tag) {
		//milestone3
		return null;
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
