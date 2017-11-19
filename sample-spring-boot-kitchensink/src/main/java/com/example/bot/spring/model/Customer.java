package com.example.bot.spring.model;

import com.example.bot.spring.DatabaseException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.sql.ResultSet;
import java.sql.SQLException;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Customer {
	public final String id;
	public final String name;
	public final String gender;
	public final int age;
	public final String phoneNumber;
	public final String state;

	public static Customer fromResultSet(ResultSet resultSet) throws SQLException {
		return new Customer(resultSet.getString(1),
			resultSet.getString(2),
			resultSet.getString(3),
			resultSet.getInt(4),
			resultSet.getString(5),
			resultSet.getString(6));
	}
}
