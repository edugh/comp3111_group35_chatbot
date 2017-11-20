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

/**
 * Handles connection logic to real or test databases, serializing results into our models, and
 * storing our queries as functions
 */
@Slf4j
public class DatabaseEngine {

    private Connection connection;

    public DatabaseEngine(Connection c) {
        this.connection = c;
    }

    /**
     * Factory method for the production database.
     * @return a DatabaseEngine connection to "production"
     */
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

    /**
     * Factory method for test database
     * @param ds a datasource to connect in place of production database
     * @return A DatabaseEngine connection to "testing"
     */
    public static DatabaseEngine connectToTest(DataSource ds) {
        try {
            return new DatabaseEngine(ds.getConnection());
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Get a set containing the id of every customer
     * @return a set containing the id of every customer
     */
    public Set<String> getCustomerIdSet() {
        Set<String> set = new HashSet<>();
        ArrayList<String> cidList = getResultsForQuery(
                "SELECT id FROM Customers",
                resultSet -> resultSet.getString(1)
        );
        set.addAll(cidList);
        return set;
    }

    /**
     * Get a list of all the bookings for a particular customer
     * @param customerId a particular customer
     * @return a list of all the bookings for a particular customer
     */
    public ArrayList<Booking> getBookings(String customerId) {
        return getResultsForQuery(
                "SELECT * FROM Bookings WHERE customerId=?;",
                Booking::fromResultSet,
                params(customerId)
        );
    }

    /**
     * Get the current booking for the given customer. The current booking
     * is the incomplete booking stored while the user is in booking flow.
     * @param cid a particular customer
     * @return the current booking for the customer
     */
    public Booking getCurrentBooking(String cid) {
        for (Booking booking : getBookings(cid)) {
            if (booking.fee == null) {
                return booking;
            }
        }
        throw new IllegalStateException("Can't give confirmation because there is no current booking");
    }

    /**
     * Gets the total amount owed by a particular customer
     * @param customerId the customer that may owe money
     * @return the total amount the aforementioned customer owes
     */
    public BigDecimal getAmountOwed(String customerId) {
        ArrayList<Booking> bookings = getBookings(customerId);
        return bookings.stream().map(booking -> (booking.fee.subtract(booking.paid))).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get the fee for a particular a customer has booked
     * @param customerId the customer who has made the booking
     * @param tour the tour of the booking
     * @return the fee for the booking
     */
    public BigDecimal getFee(String customerId, Tour tour) {
        return tryGetResultForQuery(
                "SELECT fee FROM bookings where planid = ? and tourdate = ? and customerid = ?;",
                rs -> rs.getBigDecimal(1),
                params(tour.planId, tour.tourDate, customerId)
        );
    }

    /**
     * Get the amount owed by a customer for a particular tour
     * @param customerId the customer that may owe money
     * @param tour the tour of the booking
     * @return the amount owed for the booking
     */
    public BigDecimal getAmountOwed(String customerId, Tour tour) {
        return tryGetResultForQuery(
            "SELECT (fee - paid) FROM bookings where planid = ? and tourdate = ? and customerid = ?;",
            rs -> rs.getBigDecimal(1),
            params(tour.planId, tour.tourDate, customerId)
        );
    }

    /**
     * Get all plans a customer has booked at least once
     * @param customerId the customer
     * @return a list containing all plans a customer has booked at least once
     */
    public ArrayList<Plan> getPastPlansForUser(String customerId) {
        return getResultsForQuery(
                "SELECT Plans.* FROM Plans INNER JOIN Bookings ON (Plans.id = Bookings.planId) WHERE Bookings.customerId=?;",
                Plan::fromResultSet,
                params(customerId)
        );
    }

    /**
     * Get all plans
     * @return a list of all plans
     */
    public ArrayList<Plan> getPlans() {
        return getResultsForQuery("SELECT * FROM Plans;", Plan::fromResultSet);
    }

    /**
     * Get an FAQ answer to a question
     * @param questionId the question
     * @return the answer
     */
    public Optional<FAQ> getFAQ(String questionId) {
        return getResultForQuery(
                "SELECT question, answer FROM faq WHERE questionId=?;",
                FAQ::fromResultSet,
                params(questionId)
        );
    }

    /**
     * Get a tour on a particular date from a particular plan
     * @param pid the id of the plan this tour corresponds to
     * @param date the date the tour is on
     * @return the tour on the supplied date from the supplied plan
     */
    public Optional<Tour> getTour(String pid, Date date) {
        return getResultForQuery(
                "SELECT * FROM Tours WHERE planID=? AND tourDate=?;",
                Tour::fromResultSet,
                params(pid, date)
        );
    }

    /**
     * Get all tours on a particular date from a particular plan
     * @param pid the id of the plan these tours corresponds to
     * @param date the date the tours are on
     * @return all tours on the supplied date from the supplied plan
     */
    public ArrayList<Tour> getTours(String pid, Date date) {
        return getResultsForQuery(
                "SELECT * FROM Tours WHERE planID=? AND tourDate=?;",
                Tour::fromResultSet,
                params(pid, date)
        );
    }

    /**
     * Get all tours on a particular date
     * @param date the date the tours are on
     * @return the tours on the given date
     */
    public ArrayList<Tour> getTours(Date date) {
        return getResultsForQuery(
            "SELECT * FROM Tours WHERE tourDate=?;",
            Tour::fromResultSet,
            params(date)
        );
    }

    /**
     * Get a plan from a given plan id
     * @param pid the plan id
     * @return the plan with this id
     */
    public Optional<Plan> getPlan(String pid) {
        return getResultForQuery(
                "SELECT * FROM Plans WHERE id=?;",
                Plan::fromResultSet,
                params(pid)
        );
    }

    /**
     * Insert an incomplete booking by a customer for a plan
     * @param cid the id of the customer
     * @param pid the id of the plan
     */
    public void insertBooking(String cid, String pid) {
        Date defaultDate = new Date(0);
        executeStatement(
                "INSERT INTO bookings(customerId, planId, tourDate) VALUES(?,?,?)",
                params(cid, pid, defaultDate)
        );
    }

    /**
     * Insert a new booking into the database
     * @param cid the id of the customer
     * @param pid the id of the plan
     * @param date the date the tour is on
     * @param adults number of adults
     * @param children number of children
     * @param toddlers numbder of toddlers
     * @param fee fee of the booking
     * @param paid amount of the booking paid already
     */
    public void insertBooking(String cid, String pid, Date date, Integer adults, Integer children, Integer toddlers, BigDecimal fee, BigDecimal paid) {
        executeStatement(
                "INSERT INTO bookings(customerId, planId, tourDate, adults, children, toddlers, fee, paid) VALUES(?,?,?,?,?,?,?,?)",
                params(cid, pid, date, adults, children, toddlers, fee, paid)
        );
    }

    /**
     * Update the date of a booking
     * @param cid the id of the customer who has made the booking
     * @param pid the id of the plan the booking pertains to
     * @param date the date of the booked tour
     */
    public void updateBookingDate(String cid, String pid, Date date) {
        Date defaultDate = new Date(0); //TODO: Why do we need this?
        executeStatement(
                "UPDATE Bookings SET tourDate=? WHERE customerId=? AND planId=? AND tourDate=?",
                params(date, cid, pid, defaultDate)
        );
    }

    /**
     * Update an integer type field of a particular bookings
     * @param cid the id of the customer who has made the booking
     * @param pid the id of the plan the booking pertains to
     * @param date the date of the booked tour
     * @param field the name of the integer field to update
     * @param value the new value for the field
     */
    public void updateBooking(String cid, String pid, Date date, String field, int value) {
        executeStatement(
                String.format("UPDATE Bookings SET %s = ? WHERE customerId = ? AND planId = ? AND tourDate = ?", field),
                params(value, cid, pid, date)
        );
    }

    /**
     * Update a BigDecimal type field of a particular bookings
     * @param cid the id of the customer who has made the booking
     * @param pid the id of the plan the booking pertains to
     * @param date the date of the booked tour
     * @param field the name of the BigDecimal field to update
     * @param value the new value for the field
     */
    public void updateBooking(String cid, String pid, Date date, String field, BigDecimal value) {
        executeStatement(
                String.format("UPDATE Bookings SET %s = ? WHERE customerId = ? AND planId = ? AND tourDate = ?", field),
                params(value, cid, pid, date)
        );
    }

    /**
     * Delete a booking
     * @param cid the id of the customer who made the booking
     * @param pid the id of the plan of the booking
     * @param date the date the tour will commence on
     */
    public void dropBooking(String cid, String pid, Date date) {
        executeStatement(
                "DELETE FROM Bookings WHERE customerId = ? AND planId = ? AND tourDate = ?",
                params(cid, pid, date)
        );
    }

    /**
     * Insert a new dialogue
     * @param dlg the dialogue to insert
     */
    public void insertDialogue(Dialogue dlg) {
        Timestamp ts = Timestamp.from(dlg.sendTime.toInstant());
        executeStatement(
                "INSERT INTO Dialogues(customerId, sendTime, content) VALUES(?,?,?)",
                params(dlg.customerId, ts, dlg.content)
        );
    }

    /**
     * Get a list of all dialogue of a customer
     * @param cid the customer to get dialogues from
     * @return Get a list of all dialogue of a customer
     */
    public ArrayList<Dialogue> getDialogues(String cid) {
        return getResultsForQuery(
                "SELECT * FROM Dialogues where customerId = ?;",
                Dialogue::fromResultSet,
                params(cid)
        );
    }

    /**
     * Get a list of all dialogues
     * @return a list of all dialogues
     */
    public ArrayList<Dialogue> getAllDialogues() {
        return getResultsForQuery(
                "SELECT * FROM Dialogues;",
                Dialogue::fromResultSet
        );
    }

    /**
     * Get a customer for the given customer id
     * @param cid the customers id
     * @return a customer with the id provided
     */
    public Optional<Customer> getCustomer(String cid) {
        return getResultForQuery(
                "SELECT * FROM Customers where id = ?",
                Customer::fromResultSet,
                params(cid)
        );
    }

    /**
     * Insert a new customer into the database, left in the 'new' state.
     * @param cid the id of the new customer
     */
    public void insertCustomer(String cid) {
        executeStatement(
                "INSERT INTO Customers(id,state) VALUES(?, 'new');",
                params(cid)
        );
    }

    /**
     * Insert a new customer into the database
     * @param cid the customers id
     * @param name the customers name
     * @param age the age of the customer
     * @param gender the gender of the customer
     * @param phoneNumber the phone number of the customer
     */
    public void insertCustomer(String cid, String name, int age, String gender, String phoneNumber) {
        executeStatement(
                "INSERT INTO Customers(id,name,age,gender,phoneNumber,state) VALUES(?,?,?,?,?, 'new');",
                params(cid, name, age, gender, phoneNumber)
        );
    }

    /**
     * Transition the state of an existing customer.
     * @param cid the id of the customer whose state to update
     * @param state the new state to put the customer in
     */
    public void updateCustomerState(String cid, String state) {
        executeStatement(
                "UPDATE Customers SET state = ? WHERE id = ?",
                params(state, cid)
        );
    }

    /**
     * Update a string field of a particular customer
     * @param cid the id of the customer to update
     * @param field the name of the string field
     * @param value the new value of said field
     */
    public void updateCustomer(String cid, String field, String value) {
        executeStatement(
                String.format("UPDATE Customers SET %s = ? WHERE id = ?", field),
                params(value, cid)
        );
    }

    /**
     * Update an int field of a particular customer
     * @param cid the id of the customer to update
     * @param field the name of the int field
     * @param value the new value of the aforementioned field
     */
    public void updateCustomer(String cid, String field, int value) {
        executeStatement(
                String.format("UPDATE Customers SET %s = ? WHERE id = ?", field),
                params(value, cid)
        );
    }

    /**
     * Determines whether a tour is at its capacity
     * @param pid the id of the plan of the tour
     * @param date the date the tour will occur on
     * @return whether the tour is full or not
     */
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

    /**
     * Get a list of discounts for a plan on a particular date
     * @param pid the id of the plan of the tour
     * @param date the date of the tour
     * @return a list of discounts offered for this tour
     */
    public ArrayList<Discount> getDiscounts(String pid, Date date) {
        return getResultsForQuery(
                "SELECT * FROM DiscountBookings WHERE planId = ? and tourDate = ?;",
                Discount::fromResultSet,
                params(pid, date)
        );
    }


    /**
     * Find the number of discounted seats a customer can book
     * @param cid the id of the customer
     * @param pid the plan the customer wants to book
     * @param date the date on which they want to book it
     * @return the number of discounted seats available
     */
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

    /**
     * Determines whether a tour has discounted seats left
     * @param pid the plan of the tour to be checked
     * @param date the date of the tour
     * @return whether any discounts are available
     */
    private boolean isDiscountFull(String pid, Date date) {
        List<Discount> discountList = this.getDiscounts(pid, date);
        return discountList.size() >= 4;
    }

    /**
     * Insert a new discount into the database
     * @param cid the customer receiving the discount
     * @param pid the id of the plan of the tour
     * @param date the date of the tour
     * @param numberSeats the number of discounted seats available
     * @return whether or not the insert was a success
     */
    public boolean insertDiscount(String cid, String pid, Date date, Integer numberSeats) {
        if (isDiscountFull(pid, date) || checkDiscount(cid, pid, date) > 0) {
            return false;
        } else {
            executeStatement(
                    "INSERT INTO discountBookings(customerId, planId, tourDate, seats) VALUES(?,?,?,?);",
                    params(cid, pid, date, numberSeats)
            );
            return true;
        }
    }

    /**
     * Gets discounts from the given time
     * @param timestamp the time at which discounts may apply
     * @return a list of discounts for this time
     */
    public ArrayList<DiscountSchedule> getDiscountSchedules(Timestamp timestamp) {
        return getResultsForQuery(
                "SELECT * FROM DiscountTours WHERE sendTime = ?",
                DiscountSchedule::fromResultSet,
                params(timestamp)
        );
    }

    /**
     * Get customers on a particular tour
     * @param t the tour to check
     * @return the customers on said tour
     */
    public ArrayList<Customer> getCustomers(Tour t) {
        return getResultsForQuery(
            "SELECT customers.* FROM bookings JOIN customers ON bookings.customerid = customers.id " +
                "WHERE planid = ? and tourdate = ?;",
            Customer::fromResultSet,
            params(t.planId, t.tourDate)
        );
    }

    /**
     * Gets the booking status (tour, plan, customers booked) of a date
     * @param date the date to check
     * @return a list of bookings with tour & plan on this date
     */
    public ArrayList<BookingStatus> getBookingStatus(Date date) {
        ArrayList<BookingStatus> statuses = new ArrayList<>();
        for(Plan p : getPlans()) {
            for(Tour t : getTours(p.id, date)) {
                statuses.add(new BookingStatus(t, p, getCustomers(t)));
            }
        }
        return statuses;
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

    /**
     * Set the parameters of a prepared statement depending on their type
     * @param stmt a prepared statement to fill with parameters
     * @param params a list of parameters to fill with
     * @throws SQLException
     */
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

    /**
     * Executes an update or insert statement
     * @param query the string representation of the query
     * @param parameters a list of objects to insert in place of '?'
     */
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
