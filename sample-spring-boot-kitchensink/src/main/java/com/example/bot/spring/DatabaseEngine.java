package com.example.bot.spring;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import com.example.bot.spring.model.*;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;

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


    public static FAQ faqFromResultSet(ResultSet resultSet) {
        try {
            return new FAQ(resultSet.getString(1), resultSet.getString(2));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static Booking bookingFromResultSet(ResultSet resultSet) {
        try {
            return new Booking(resultSet.getString(1),
                    resultSet.getString(2),
                    resultSet.getDate(3),
                    resultSet.getInt(4),
                    resultSet.getInt(5),
                    resultSet.getInt(6),
                    resultSet.getBigDecimal(7),
                    resultSet.getBigDecimal(8),
                    resultSet.getString(9));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void insertBooking(String cid, String pid){
        Date defaultDate = new Date(0);
        String query = String.format("INSERT INTO bookings(customerId, planId, tourDate) VALUES('%s','%s','%s')", cid, pid, defaultDate);
        insertForQuery(query);
    }

    public void updateBookingDate(String cid, String pid, Date date){
        Date defaultDate = new Date(0);
        try (
            Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(
                "UPDATE Bookings SET tourDate=? WHERE customerId=? AND planId=? AND tourDate=?");
        ) {
            stmt.setDate(1, date);
            stmt.setString(2, cid);
            stmt.setString(3, pid);
            stmt.setDate(4, defaultDate);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void updateBooking(String cid, String pid, Date date, String field, String value){
        String query = String.format("UPDATE Bookings SET %s = ? " +
                "WHERE customerId = ? AND planId = ? AND tourDate = ?" ,field);
        try (
            Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(query);
        ) {
            stmt.setDate(4, date);
            stmt.setString(3, pid);
            stmt.setString(2, cid);
            stmt.setString(1, value);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void updateBooking(String cid, String pid, Date date, String field, int value){
        String query = String.format("UPDATE Bookings SET %s = ? " +
                "WHERE customerId = ? AND planId = ? AND tourDate = ?" ,field);
        try (
            Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(query);
        ) {
            stmt.setDate(4, date);
            stmt.setString(3, pid);
            stmt.setString(2, cid);
            stmt.setInt(1, value);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void updateBooking(String cid, String pid, Date date, String field, BigDecimal value){
        String query = String.format("UPDATE Bookings SET %s = ? " +
                "WHERE customerId = ? AND planId = ? AND tourDate = ?" ,field);
        try (
            Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(query);
        ) {
            stmt.setDate(4, date);
            stmt.setString(3, pid);
            stmt.setString(2, cid);
            stmt.setBigDecimal(1, value);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static Tag tagFromResultSet(ResultSet resultSet) {
        try {
            return new Tag(resultSet.getString(1),
                    resultSet.getString(2));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
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
            return new Dialogue(resultSet.getString(1),
                    resultSet.getTimestamp(2),
                    resultSet.getString(3));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void insertDialogue(Dialogue dlg) {
        Timestamp ts = Timestamp.from(dlg.sendTime.toInstant());
        try (
            Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO Dialogues(customerId, sendTime, content) VALUES(?,?,?)");
        ) {
            stmt.setString(3, dlg.content);
            stmt.setTimestamp(2, ts);
            stmt.setString(1, dlg.customerId);
            stmt.executeQuery();
        } catch (SQLException e) {
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

    public static String customerIdFromResultSet(ResultSet resultSet) {
        try {
            return resultSet.getString(1);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Optional<Customer> getCustomer(String cid) {
        return getResultForQuery(
            "SELECT * FROM Customers where id = ?",
            SQLDatabaseEngine::customerFromResultSet,
            new Object[]{cid}
        );
    }

    public Set<String> getCustomerIdSet(){
        Set<String> set = new HashSet<>();
        ArrayList<String> cidList =  getResultsForQuery(
          "SELECT id FROM Customers",
            SQLDatabaseEngine::customerIdFromResultSet
        );
        set.addAll(cidList);
        return set;
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

    //Discount related
    public static Discount discountFromResultSet(ResultSet resultSet) {
        try {
            return new Discount(
                    resultSet.getString(1),
                    resultSet.getString(2),
                    resultSet.getDate(3),
                    resultSet.getInt(4)
            );
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static DiscountSchedule discountScheduleFromResultSet(ResultSet resultSet) {
        try {
            return new DiscountSchedule(
                    resultSet.getString(1),
                    resultSet.getDate(2),
                    resultSet.getTimestamp(3)
            );
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public ArrayList<Discount> getDiscounts(String pid, Date date) {
        String query = "SELECT * FROM DiscountBookings WHERE planId = ? and tourDate = ?;";
        String[] params = {pid, date.toString()};
        return getResultsForQuery(query, SQLDatabaseEngine::discountFromResultSet, params);
    }


    public int checkDiscount(String cid, String pid, Date date) {
        String query = "SELECT seats FROM DiscountBookings WHERE customerId = ? and planId = ? and tourDate = ?;";
        String[] params = {cid, pid, date.toString()};
        List<Discount> discountList = getResultsForQuery(query, SQLDatabaseEngine::discountFromResultSet, params);
        if(discountList.size()>0){
            return discountList.get(0).seats;
        }
        else
            return 0;
    }

    public boolean isDiscountFull(String pid, Date date) {
        List<Discount> discountList = this.getDiscounts(pid, date);
        return discountList.size() >= 4;
    }

    public boolean insertDiscount(String cid, String pid, Date date) {
        if (isDiscountFull(pid, date) || checkDiscount(cid, pid, date)>0) {
            return false;
        } else {
            String query = String.format("INSERT INTO discountBooking(customerId, planId, tourDate) " +
                    "VALUES('%s', '%s', %s);", cid, pid, date.toString());
            insertForQuery(query);
            return true;
        }
    }

    public ArrayList<DiscountSchedule> getDiscountSchedules(Timestamp timestamp) {
        String query = "SELECT * FROM DiscountTours";
        String[] params = {timestamp.toString()};
        return getResultsForQuery(query, SQLDatabaseEngine::discountScheduleFromResultSet, params);
    }

    @FunctionalInterface
    private interface SQLModelReader<T> {
        T apply(ResultSet t);
    }

    private <T> ArrayList<T> getResultsForQuery (String query, SQLModelReader<T> modelReader) {
        return getResultsForQuery(query, modelReader, new Object[0]);
    }

    private <T> T tryGetResultForQuery (String query, SQLModelReader<T> modelReader, @NotNull Object[] params) {
        return getResultForQuery(query, modelReader, params).orElseThrow(
            () -> new DatabaseException("Query did not return any rows")
        );
    }

    private <T> Optional<T> getResultForQuery (String query, SQLModelReader<T> modelReader, @NotNull Object[] params) {
        return getResultsForQuery(query, modelReader, params).stream().findFirst();
    }

    private <T> ArrayList<T> getResultsForQuery (String query, SQLModelReader<T> modelReader, @NotNull Object[] params) {
        log.info("New getResultsForQuery '{}'", query);
        ArrayList<T> results = new ArrayList<>();
        try (
            Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(query);
        ) {
            for (int i = 0; i < params.length; i++) {
                // TODO: This is brittle. We need a better way...
                if (params[i] instanceof String) {
                    stmt.setString(i + 1, (String) params[i]);
                } else if (params[i] instanceof Date) {
                    stmt.setDate(i + 1, (Date) params[i]);
                }
            }
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

    private void insertForQuery (String query) {
        log.info("New insertForQuery '{}'", query);
        try (
            Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(query);
        ) {
            stmt.execute();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    abstract public Connection getConnection();
}
