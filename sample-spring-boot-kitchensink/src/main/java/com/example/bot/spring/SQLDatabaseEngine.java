package com.example.bot.spring;

import com.example.bot.spring.model.*;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URI;
import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;

import static com.example.bot.spring.Utils.params;

@Slf4j
public class SQLDatabaseEngine {

	private Connection connection;

	public SQLDatabaseEngine(Connection c) {
		this.connection = c;
	}

    public static SQLDatabaseEngine connectToProduction() {
		try {
			URI dbUri = new URI(System.getenv("DATABASE_URL"));
			String username = dbUri.getUserInfo().split(":")[0];
			String password = dbUri.getUserInfo().split(":")[1];
			String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath()
					+ "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";

			log.info("Username: {} Password: {}", username, password);
			log.info("dbUrl: {}", dbUrl);

			return new SQLDatabaseEngine(DriverManager.getConnection(dbUrl, username, password));
		} catch (URISyntaxException e) {
			log.error("Database URI is malformed!");
			throw new RuntimeException(e);
		} catch (SQLException e){
			log.error("Cannot connect to database");
			throw new DatabaseException(e);
		}
	}

	public static SQLDatabaseEngine connectToTest(DataSource ds) {
		try {
			return new SQLDatabaseEngine(ds.getConnection());
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	public ArrayList<Booking> getBookings(String customerId) {
    	return getResultsForQuery(
			"SELECT * FROM Bookings WHERE customerId=?;",
			Booking::fromResultSet,
			params(customerId)
		);
	}

	public Optional<Booking> getCurrentBooking(String cid) {
		for(Booking booking : getBookings(cid)){
			if(booking.fee == null){
				return Optional.of(booking);
			}
		}
		return Optional.empty();
	}

	public BigDecimal getAmountOwed(String customerId) {
		ArrayList<Booking> bookings = getBookings(customerId);
		return bookings.stream().map(booking -> (booking.fee.subtract(booking.paid))).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public ArrayList<Plan> getPlans() {
	    return getResultsForQuery("SELECT * FROM Plans;", Plan::fromResultSet);
    }

	public Optional<FAQ> getFAQ(String questionId) {
	    return getResultForQuery(
            "SELECT question, answer FROM faq WHERE questionId=?;",
            FAQ::fromResultSet,
            params(questionId)
        );
    }

	public Optional<Tour> getTour(String pid, Date date) {
	    return getResultForQuery(
            "SELECT * FROM Tours WHERE planID=? AND tourDate=?;",
            Tour::fromResultSet,
            params(pid, date)
        );
    }

	public Optional<Plan> getPlan(String pid) {
	    return getResultForQuery(
            "SELECT * FROM Plans WHERE id=?;",
            Plan::fromResultSet,
            params(pid)
        );
	}

	public void insertBooking(String cid, String pid){
		Date defaultDate = new Date(0);
		executeStatement(
			"INSERT INTO bookings(customerId, planId, tourDate) VALUES(?,?,?)",
			params(cid, pid, defaultDate)
		);
	}

	public void updateBookingDate(String cid, String pid, Date date){
		Date defaultDate = new Date(0); //TODO: Why do we need this?
		executeStatement(
			"UPDATE Bookings SET tourDate=? WHERE customerId=? AND planId=? AND tourDate=?",
			params(date, cid, pid, defaultDate)
		);
	}

	public void updateBooking(String cid, String pid, Date date, String field, String value) {
		executeStatement(
			String.format("UPDATE Bookings SET %s = ? WHERE customerId = ? AND planId = ? AND tourDate = ?", field),
			params(date, pid, cid, value)
		);
	}

	public void updateBooking(String cid, String pid, Date date, String field, int value){
		executeStatement(
			String.format("UPDATE Bookings SET %s = ? WHERE customerId = ? AND planId = ? AND tourDate = ?", field),
			params(date, pid, cid, value)
		);
	}

	public void updateBooking(String cid, String pid, Date date, String field, BigDecimal value){
		executeStatement(
			String.format("UPDATE Bookings SET %s = ? WHERE customerId = ? AND planId = ? AND tourDate = ?", field),
			params(date, pid, cid, value)
		);
	}

	public void insertTag(Tag tag) {
		String query = String.format("INSERT INTO Tags(name, customerID) VALUES('%s','%s')",tag.customerId,tag.name);
		executeStatement(query);
	}

	public ArrayList<Tag> getTags(String cid) {
		return getResultsForQuery(
            "SELECT name FROM Tags where customerId = ?;",
            Tag::fromResultSet,
            params(cid)
        );
	}

	public void insertDialogue(Dialogue dlg) {
		Timestamp ts = Timestamp.from(dlg.sendTime.toInstant());
		executeStatement(
			"INSERT INTO Dialogues(customerId, sendTime, content) VALUES(?,?,?)",
			params(dlg.content, ts, dlg.customerId)
		);
	}

	public ArrayList<Dialogue> getDialogues(String cid) {
		return getResultsForQuery(
            "SELECT sendTime, content FROM Tags where customerId = ?;",
            Dialogue::fromResultSet,
            params(cid)
        );
	}

	public Optional<Customer> getCustomer(String cid) {
		return getResultForQuery(
            "SELECT * FROM Customers where id = ?",
            Customer::fromResultSet,
            params(cid)
		);
	}

	public void insertCustomer(String cid) {
		executeStatement(
			"INSERT INTO Customers(id,state) VALUES(?, 'new');",
			params(cid)
		);
	}

	public void updateCustomerState(String cid, String state) {
		executeStatement(
			"UPDATE Customers SET state = ? WHERE id = ?",
			params(state, cid)
		);
	}

	public void updateCustomer(String cid, String field, String value) {
		executeStatement(
			String.format("UPDATE Customers SET %s = ? WHERE id = ?", field),
			params(value, cid)
		);
	}

	public void updateCustomer(String cid, String field, int value) {
		executeStatement(
			String.format("UPDATE Customers SET %s = ? WHERE id = ?", field),
			params(value, cid)
		);
	}

	public void updateCustomer(String cid, String field, BigDecimal value){
		executeStatement(
			String.format("UPDATE Customers SET %s = ? WHERE id = ?", field),
			params(value, cid)
		);
	}

	public boolean isTourFull(String pid, Date date) {
		// TODO(What should this be)
		return false;
	}

    /**
     * Executes a query in the database, transforms each row of the result into a model type
     * and returns an array of models.
     * @param query The SQL query to execute
     * @param modelReader The function used to transform a database row into a model
     * @param <T> The model type to retrieve
     * @return An ArrayList of models
     */
	private <T> ArrayList<T> getResultsForQuery (String query, SQLModelReader<T> modelReader) {
		return getResultsForQuery(query, modelReader, new Object[0]);
	}

    /**
     * Executes a query in the database and transforms the first row of the result if it
     * exists. A DatabaseException is thrown if the query does not evalaute to any rows.
     * @param query The SQL query to execute
     * @param modelReader The function used to transform a database row into a model
     * @param params The parameters to be passed into the SQL query
     * @param <T> The model type to retrieve
     * @return The first row of the result of the query, transformed into a model.
     */
	private <T> T tryGetResultForQuery (String query, SQLModelReader<T> modelReader, Object[] params) {
		return getResultForQuery(query, modelReader, params).orElseThrow(
				() -> new DatabaseException("Query did not return any rows")
		);
	}

    /**
     * Executes a query in the database and transforms the first row of the result if it
     * exists.
     * @param query The SQL query to execute
     * @param modelReader The function used to transform a database row into a model
     * @param params The parameters to be passed into the SQL query
     * @param <T> The model type to retrieve
     * @return If the query evaluated to 1 or more rows, and Optional containing the first
     *         row transformed into a model, otherwise an empty Optional.
     */
	private <T> Optional<T> getResultForQuery (String query, SQLModelReader<T> modelReader, Object[] params) {
		return getResultsForQuery(query, modelReader, params).stream().findFirst();
	}

    /**
     * Executes a query in the database, transforms each row of the result into a model type
     * and returns an array of models.
     * @param query The SQL query to execute
     * @param modelReader The function used to transform a database row into a model
     * @param params The parameters to be passed into the SQL query
     * @param <T> The model type to retrieve
     * @return An ArrayList of models
     */
	private <T> ArrayList<T> getResultsForQuery (String query, SQLModelReader<T> modelReader, Object[] params) {
		log.info("New getResultsForQuery '{}'", query);
		ArrayList<T> results = new ArrayList<>();
		try (
			PreparedStatement stmt = connection.prepareStatement(query);
		) {
			setParameters(stmt, params);
			log.info("Prepared query '{}'", stmt.toString());
			try(ResultSet resultSet = stmt.executeQuery()) {
				while (resultSet.next()) {
					T result = modelReader.apply(resultSet);
					log.info("Got result for query: '{}'", result.toString());
					results.add(result);
				}
				return results;
			}
		} catch (SQLException e) {
			log.info("Query '{}' failed", query);
			throw new DatabaseException(e);
		}
	}

	private void setParameters(PreparedStatement stmt, Object[] params) throws SQLException {
		for (int i = 0; i < params.length; i++) {
			// TODO: This is brittle. We need a better way...
			if (params[i] instanceof String) {
				stmt.setString(i + 1, (String) params[i]);
			} else if (params[i] instanceof Integer) {
				stmt.setInt(i + 1, (Integer) params[i]);
			} else if (params[i] instanceof Date) {
				stmt.setDate(i + 1, (Date) params[i]);
			} else if (params[i] instanceof  Timestamp) {
				stmt.setTimestamp(i + 1, (Timestamp) params[i]);
			} else if (params[i] instanceof  BigDecimal) {
				stmt.setBigDecimal(i + 1, (BigDecimal) params[i]);
			} else {
				throw new RuntimeException(String.format("Can't handle type %s!", params[i].getClass().getTypeName()));
			}
		}
	}

	private void executeStatement(String query) {
		executeStatement(query, new Object[0]);
	}

	private void executeStatement(String query, Object[] parameters) {
		try (
			PreparedStatement stmt = connection.prepareStatement(query);
		) {
			log.info("Prepared query '{}'", stmt.toString());
			setParameters(stmt, parameters);
			stmt.execute();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
}
