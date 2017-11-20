package com.example.bot.spring;

import com.example.bot.spring.model.*;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URI;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import static com.example.bot.spring.Utils.params;

@Slf4j
public class DatabaseEngine {

    private Connection connection;

    public DatabaseEngine(Connection c) {
        this.connection = c;
    }

    public static DatabaseEngine connectToProduction() {
        try {
            URI dbUri = new URI(System.getenv("DATABASE_URL"));
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath()
                    + "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";

            log.info("Username: {} Password: {}", username, password);
            log.info("dbUrl: {}", dbUrl);

            return new DatabaseEngine(DriverManager.getConnection(dbUrl, username, password));
        } catch (URISyntaxException e) {
            log.error("Database URI is malformed!");
            throw new RuntimeException(e);
        } catch (SQLException e) {
            log.error("Cannot connect to database");
            throw new DatabaseException(e);
        }
    }

    public static DatabaseEngine connectToTest(DataSource ds) {
        try {
            return new DatabaseEngine(ds.getConnection());
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Set<String> getCustomerIdSet() {
        Set<String> set = new HashSet<>();
        ArrayList<String> cidList = getResultsForQuery(
                "SELECT id FROM Customers",
                resultSet -> resultSet.getString(1)
        );
        set.addAll(cidList);
        return set;
    }

    public ArrayList<Booking> getBookings(String customerId) {
        return getResultsForQuery(
                "SELECT * FROM Bookings WHERE customerId=?;",
                Booking::fromResultSet,
                params(customerId)
        );
    }

    public Booking getCurrentBooking(String cid) {
        for (Booking booking : getBookings(cid)) {
            if (booking.fee == null) {
                return booking;
            }
        }
        throw new IllegalStateException("Can't give confirmation because there is no current booking");
    }

    public BigDecimal getAmountOwed(String customerId) {
        ArrayList<Booking> bookings = getBookings(customerId);
        return bookings.stream().map(booking -> (booking.fee.subtract(booking.paid))).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public ArrayList<Plan> getPastPlansForUser(String customerId) {
        return getResultsForQuery(
                "SELECT Plans.* FROM Plans INNER JOIN Bookings ON (Plans.id = Bookings.planId) WHERE Bookings.customerId=?;",
                Plan::fromResultSet,
                params(customerId)
        );
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

    public void insertBooking(String cid, String pid) {
        Date defaultDate = new Date(0);
        executeStatement(
                "INSERT INTO bookings(customerId, planId, tourDate) VALUES(?,?,?)",
                params(cid, pid, defaultDate)
        );
    }

    public void insertBooking(String cid, String pid, Date date, Integer adults, Integer children, Integer toddlers, BigDecimal fee, BigDecimal paid) {
        executeStatement(
                "INSERT INTO bookings(customerId, planId, tourDate, adults, children, toddlers, fee, paid) VALUES(?,?,?,?,?,?,?,?)",
                params(cid, pid, date, adults, children, toddlers, fee, paid)
        );
    }

    public void updateBookingDate(String cid, String pid, Date date) {
        Date defaultDate = new Date(0); //TODO: Why do we need this?
        executeStatement(
                "UPDATE Bookings SET tourDate=? WHERE customerId=? AND planId=? AND tourDate=?",
                params(date, cid, pid, defaultDate)
        );
    }

    public void updateBooking(String cid, String pid, Date date, String field, String value) {
        executeStatement(
                String.format("UPDATE Bookings SET %s = ? WHERE customerId = ? AND planId = ? AND tourDate = ?", field),
                params(value, cid, pid, date)
        );
    }

    public void updateBooking(String cid, String pid, Date date, String field, int value) {
        executeStatement(
                String.format("UPDATE Bookings SET %s = ? WHERE customerId = ? AND planId = ? AND tourDate = ?", field),
                params(value, cid, pid, date)
        );
    }

    public void updateBooking(String cid, String pid, Date date, String field, BigDecimal value) {
        executeStatement(
                String.format("UPDATE Bookings SET %s = ? WHERE customerId = ? AND planId = ? AND tourDate = ?", field),
                params(value, cid, pid, date)
        );
    }

    public void dropBooking(String cid, String pid, Date date) {
        executeStatement(
                "DELETE FROM Bookings WHERE customerId = ? AND planId = ? AND tourDate = ?",
                params(cid, pid, date)
        );
    }

    public void insertTag(Tag tag) {
        executeStatement(
                "INSERT INTO Tags(name, customerID) VALUES(?,?)",
                params(tag.name, tag.customerId)
        );
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
                params(dlg.customerId, ts, dlg.content)
        );
    }

    public ArrayList<Dialogue> getDialogues(String cid) {
        return getResultsForQuery(
                "SELECT * FROM Dialogues where customerId = ?;",
                Dialogue::fromResultSet,
                params(cid)
        );
    }

    public ArrayList<Dialogue> getAllDialogues() {
        return getResultsForQuery(
                "SELECT * FROM Dialogues;",
                Dialogue::fromResultSet
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

    public void insertCustomer(String cid, String name, int age, String gender, String phoneNumber) {
        executeStatement(
                "INSERT INTO Customers(id,name,age,gender,phoneNumber,state) VALUES(?,?,?,?,?, 'new');",
                params(cid, name, age, gender, phoneNumber)
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

    public void updateCustomer(String cid, String field, BigDecimal value) {
        executeStatement(
                String.format("UPDATE Customers SET %s = ? WHERE id = ?", field),
                params(value, cid)
        );
    }

    public boolean isTourFull(String pid, Date date) {
        Tour tour = getTour(pid, date).get();
        return tryGetResultForQuery(
            "SELECT SUM(Bookings.adults + Bookings.children + Bookings.toddlers) FROM bookings " +
                "JOIN Customers ON customerId = id " +
                "WHERE planId = ? and tourDate = ?;",
            (rs) -> rs.getInt(1),
            params(tour.planId, date)
        ) >= tour.capacity;
    }

    public ArrayList<Discount> getDiscounts(String pid, Date date) {
        return getResultsForQuery(
                "SELECT * FROM DiscountBookings WHERE planId = ? and tourDate = ?;",
                Discount::fromResultSet,
                params(pid, date)
        );
    }


    public int checkDiscount(String cid, String pid, Date date) {
        List<Discount> discountList = getResultsForQuery(
                "SELECT seats FROM DiscountBookings WHERE customerId = ? and planId = ? and tourDate = ?;",
                Discount::fromResultSet,
                params(cid, pid, date)
        );
        if (discountList.size() > 0) {
            return discountList.get(0).seats;
        } else {
            return 0;
        }
    }

    private boolean isDiscountFull(String pid, Date date) {
        List<Discount> discountList = this.getDiscounts(pid, date);
        return discountList.size() >= 4;
    }

    public boolean insertDiscount(String cid, String pid, Date date) {
        if (isDiscountFull(pid, date) || checkDiscount(cid, pid, date) > 0) {
            return false;
        } else {
            executeStatement(
                    "INSERT INTO discountBooking(customerId, planId, tourDate) VALUES(?,?,?);",
                    params(cid, pid, date)
            );
            return true;
        }
    }

    public ArrayList<DiscountSchedule> getDiscountSchedules(Timestamp timestamp) {
        return getResultsForQuery(
                "SELECT * FROM DiscountTours",
                DiscountSchedule::fromResultSet,
                params(timestamp)
        );
    }

    public ArrayList<Tour> getTours(String pid) {
        return getResultsForQuery(
            "SELECT * FROM tours WHERE planid = ?;",
            Tour::fromResultSet,
            params(pid)
        );
    }

    /**
     * Executes a query in the database, transforms each row of the result into a model type
     * and returns an array of models.
     *
     * @param query       The SQL query to execute
     * @param modelReader The function used to transform a database row into a model
     * @param <T>         The model type to retrieve
     * @return An ArrayList of models
     */
    private <T> ArrayList<T> getResultsForQuery(String query, SQLModelReader<T> modelReader) {
        return getResultsForQuery(query, modelReader, new Object[0]);
    }

    /**
     * Executes a query in the database and transforms the first row of the result if it
     * exists. A DatabaseException is thrown if the query does not evalaute to any rows.
     *
     * @param query       The SQL query to execute
     * @param modelReader The function used to transform a database row into a model
     * @param params      The parameters to be passed into the SQL query
     * @param <T>         The model type to retrieve
     * @return The first row of the result of the query, transformed into a model.
     */
    private <T> T tryGetResultForQuery(String query, SQLModelReader<T> modelReader, Object[] params) {
        return getResultForQuery(query, modelReader, params).orElseThrow(
                () -> new DatabaseException("Query did not return any rows")
        );
    }

    /**
     * Executes a query in the database and transforms the first row of the result if it
     * exists.
     *
     * @param query       The SQL query to execute
     * @param modelReader The function used to transform a database row into a model
     * @param params      The parameters to be passed into the SQL query
     * @param <T>         The model type to retrieve
     * @return If the query evaluated to 1 or more rows, and Optional containing the first
     * row transformed into a model, otherwise an empty Optional.
     */
    private <T> Optional<T> getResultForQuery(String query, SQLModelReader<T> modelReader, Object[] params) {
        return getResultsForQuery(query, modelReader, params).stream().findFirst();
    }

    /**
     * Executes a query in the database, transforms each row of the result into a model type
     * and returns an array of models.
     *
     * @param query       The SQL query to execute
     * @param modelReader The function used to transform a database row into a model
     * @param params      The parameters to be passed into the SQL query
     * @param <T>         The model type to retrieve
     * @return An ArrayList of models
     */
    private <T> ArrayList<T> getResultsForQuery(String query, SQLModelReader<T> modelReader, Object[] params) {
        log.info("New getResultsForQuery '{}'", query);
        ArrayList<T> results = new ArrayList<>();
        try (
                PreparedStatement stmt = connection.prepareStatement(query);
        ) {
            setParameters(stmt, params);
            log.info("Prepared query '{}'", stmt.toString());
            try (ResultSet resultSet = stmt.executeQuery()) {
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
            } else if (params[i] instanceof Timestamp) {
                stmt.setTimestamp(i + 1, (Timestamp) params[i]);
            } else if (params[i] instanceof BigDecimal) {
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
