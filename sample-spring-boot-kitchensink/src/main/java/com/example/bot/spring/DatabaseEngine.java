package com.example.bot.spring;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.sql.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.bot.spring.model.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class DatabaseEngine {
    ArrayList<Booking> getBookings(String customerId) {
        String query = "SELECT * FROM Bookings WHERE customerId=?;";
        String[] params = { customerId };
        return getResultsForQuery(query, SQLDatabaseEngine::bookingFromResultSet, params);
    }

    public Booking getCurrentBooking(String cid){
        List<Booking> bkList= getBookings(cid);
        for(Booking booking : bkList){
            if(booking.fee == null){
                return booking;
            }
        }
        return null;
    }

    BigDecimal getAmmountOwed(String customerId) {
        ArrayList<Booking> bookings = getBookings(customerId);
        return bookings.stream().map(booking -> (booking.fee.subtract(booking.paid))).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    ArrayList<Plan> getPlans() {
        String query = "SELECT * FROM Plans;";
        return getResultsForQuery(query, SQLDatabaseEngine::planFromResultSet);
    }

    FAQ getFAQ(String questionId) {
        String query = "SELECT question, answer FROM faq WHERE questionId=?;";
        Object[] params = { questionId };
        ArrayList<FAQ> faqs = getResultsForQuery(query, SQLDatabaseEngine::faqFromResultSet, params);
        return faqs.size() == 0? null : faqs.get(0);
    }

    public Tour getTour(String pid, Date date) {
        String query = "SELECT * FROM Tours WHERE planId=? AND tourDate=?;";
        Object[] params = { pid, date };
        ArrayList<Tour> tours = getResultsForQuery(query, SQLDatabaseEngine::tourFromResultSet, params);
        return tours.size() == 0? null : tours.get(0);
    }

    public Plan getPlan(String pid) {
        String query = "SELECT * FROM Plans WHERE id=?;";
        String[] params = { pid };
        ArrayList<Plan> plans = getResultsForQuery(query, SQLDatabaseEngine::planFromResultSet, params);
        return plans.size() == 0? null : plans.get(0);
    }


    public static FAQ faqFromResultSet(ResultSet resultSet) throws SQLException {
        return new FAQ(resultSet.getString(1),
                resultSet.getString(2));
    }

    public static Booking bookingFromResultSet(ResultSet resultSet) throws SQLException {
        return new Booking(resultSet.getString(1),
                resultSet.getString(2),
                resultSet.getDate(3),
                resultSet.getInt(4),
                resultSet.getInt(5),
                resultSet.getInt(6),
                resultSet.getBigDecimal(7),
                resultSet.getBigDecimal(8),
                resultSet.getString(9));
    }

    public void insertBooking(String cid, String pid){
        Date defaultDate = new Date(0);
        String query = String.format("INSERT INTO bookings(customerId, planId, tourDate) VALUES('%s','%s','%s')", cid, pid, defaultDate);
        insertForQuery(query);
    }

    public void updateBookingDate(String cid, String pid, Date date){
        Date defaultDate = new Date(0);
        try {
            Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE Bookings SET tourDate=? WHERE customerId=? AND planId=? AND tourDate=?");
            stmt.setDate(1, date);
            stmt.setString(2, cid);
            stmt.setString(3, pid);
            stmt.setDate(4, defaultDate);
            stmt.executeUpdate();
            stmt.close();
            connection.close();
        } catch (SQLException | URISyntaxException e) {
            throw new DatabaseException(e);
        }
    }

    public void updateBooking(String cid, String pid, Date date, String field, String value){
        String query = String.format("UPDATE Bookings SET %s = ? " +
                "WHERE customerId = ? AND planId = ? AND tourDate = ?" ,field);
        try {
            Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setDate(4, date);
            stmt.setString(3, pid);
            stmt.setString(2, cid);
            stmt.setString(1, value);
            stmt.executeUpdate();
            stmt.close();
            connection.close();
        } catch (SQLException | URISyntaxException e) {
            throw new DatabaseException(e);
        }
    }

    public void updateBooking(String cid, String pid, Date date, String field, int value){
        String query = String.format("UPDATE Bookings SET %s = ? " +
                "WHERE customerId = ? AND planId = ? AND tourDate = ?" ,field);
        try {
            Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setDate(4, date);
            stmt.setString(3, pid);
            stmt.setString(2, cid);
            stmt.setInt(1, value);
            stmt.executeUpdate();
            stmt.close();
            connection.close();
        } catch (SQLException | URISyntaxException e) {
            throw new DatabaseException(e);
        }
    }

    public void updateBooking(String cid, String pid, Date date, String field, BigDecimal value){
        String query = String.format("UPDATE Bookings SET %s = ? " +
                "WHERE customerId = ? AND planId = ? AND tourDate = ?" ,field);
        try {
            Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setDate(4, date);
            stmt.setString(3, pid);
            stmt.setString(2, cid);
            stmt.setBigDecimal(1, value);
            stmt.executeUpdate();
            stmt.close();
            connection.close();
        } catch (SQLException | URISyntaxException e) {
            throw new DatabaseException(e);
        }
    }

    public static Tag tagFromResultSet(ResultSet resultSet)  throws SQLException{
        return new Tag(resultSet.getString(1),
                resultSet.getString(2));
    }

    void insertTag(Tag tag) {
        String query = String.format("INSERT INTO Tags(name, customerID) VALUES('%s','%s')",tag.customerId,tag.name);
        insertForQuery(query);
    }

    ArrayList<Tag> getTags(String cid) {
        String query = String.format("SELECT name FROM Tags where customerId = %s;", cid);
        return getResultsForQuery(query, SQLDatabaseEngine::tagFromResultSet);
    }

    public static Dialogue dialogueFromResultSet(ResultSet resultSet) {
        try {
            Timestamp ts = resultSet.getTimestamp(2);
            ZonedDateTime zonedDateTime =
                    ZonedDateTime.ofInstant(ts.toInstant(), ZoneOffset.UTC);
            return new Dialogue(resultSet.getString(1),
                    zonedDateTime,
                    resultSet.getString(3));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void insertDialogue(Dialogue dlg) {
        Timestamp ts = Timestamp.from(dlg.sendTime.toInstant());
        try {
            Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO Dialogues(customerId, sendTime, content) VALUES(?,?,?)");
            stmt.setString(3, dlg.content);
            stmt.setTimestamp(2, ts);
            stmt.setString(1, dlg.customerId);
            stmt.executeQuery();
            stmt.close();
            connection.close();
        } catch (SQLException | URISyntaxException e) {
            throw new DatabaseException(e);
        }
    }

    ArrayList<Dialogue> getDialogues(String cid) {
        String query = String.format("SELECT sendTime, content FROM Tags where customerId = %s;", cid);
        return getResultsForQuery(query, SQLDatabaseEngine::dialogueFromResultSet);
    }

    public static Customer customerFromResultSet(ResultSet resultSet) {
        try {
            return new Customer(resultSet.getString(1),
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getInt(4),
                    resultSet.getString(5),
                    resultSet.getString(6));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Customer getCustomer(String cid) {
        Customer customer = null;
        try {
            String query = String.format("SELECT * FROM Customers where id = '%s';", cid);
            Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                customer = customerFromResultSet(resultSet);
            }
            resultSet.close();
            stmt.close();
            connection.close();
            return customer;
        } catch (SQLException | URISyntaxException e) {
            throw new DatabaseException(e);
        }
    }

    public void insertCustomer(String cid){
        String query = String.format("INSERT INTO Customers(id,state) VALUES('%s', 'new');", cid);
        insertForQuery(query);
    }

    public void updateCustomerState(String cid, String state){
        String query = String.format("UPDATE Customers SET state = '%s' WHERE id = '%s'", state, cid);
        insertForQuery(query);
    }

    public void updateCustomer(String cid, String field, String value){
        String query = String.format("UPDATE Customers SET %s = '%s' WHERE id = '%s'",field, value, cid);
        insertForQuery(query);
    }

    public void updateCustomer(String cid, String field, int value){
        String query = String.format("UPDATE Customers SET %s = %d WHERE id = '%s'",field, value, cid);
        insertForQuery(query);
    }

    public void updateCustomer(String cid, String field, BigDecimal value){
        String query = String.format("UPDATE Customers SET %s = %d WHERE id = '%s'",field, value, cid);
        insertForQuery(query);
    }

    public static Plan planFromResultSet(ResultSet resultSet) {
        try {
            return new Plan(resultSet.getString(1),
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getInt(4),
                    resultSet.getString(5),
                    resultSet.getBigDecimal(6),
                    resultSet.getBigDecimal(7));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static Tour tourFromResultSet(ResultSet resultSet) {
        try {
            return new Tour(
                    resultSet.getString(1),
                    resultSet.getDate(2),
                    resultSet.getString(3),
                    resultSet.getString(4),
                    resultSet.getString(5),
                    resultSet.getInt(6),
                    resultSet.getInt(7)

            );
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public boolean isTourFull(String pid, Date date) {
        // TODO(What should this be)
        return false;
    }

    @FunctionalInterface
    private interface SQLModelReader<T> {
        T apply(ResultSet t) throws SQLException;
    }

    private <T> ArrayList<T> getResultsForQuery (String query, SQLModelReader<T> modelReader) {
        return getResultsForQuery(query, modelReader, null);
    }

    private <T> ArrayList<T> getResultsForQuery (String query, SQLModelReader<T> modelReader, Object[] params) {
        log.info("New getResultsForQuery '{}'", query);
        ArrayList<T> results = new ArrayList<>();
        try {
            Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(query);
            for (int i = 0; i < (params == null? 0 : params.length); i++) {
                if (params[i] instanceof String) {
                    stmt.setString(i + 1, (String) params[i]);
                } else if (params[i] instanceof Date) {
                    stmt.setDate(i + 1, (Date) params[i]);
                }
            }
            log.info("Prepared query '{}'", stmt.toString());
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                T result = modelReader.apply(resultSet);
                log.info("Got result for query: '{}'", result.toString());
                results.add(result);
            }
            resultSet.close();
            stmt.close();
            connection.close();
            return results;
        } catch (SQLException | URISyntaxException e) {
            log.info("Query '{}' failed", query);
            throw new DatabaseException(e);
        }
    }

    private void insertForQuery (String query) {
        log.info("New insertForQuery '{}'", query);
        try {
            Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.execute();
            stmt.close();
            connection.close();
        } catch (SQLException | URISyntaxException e) {
            throw new DatabaseException(e);
        }
    }

    abstract public Connection getConnection() throws URISyntaxException, SQLException;
}
