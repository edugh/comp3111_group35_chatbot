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
public class Tag {
	public final String name;
	public final String customerId;

	public static Tag fromResultSet(ResultSet resultSet) throws SQLException {
		return new Tag(resultSet.getString(1), resultSet.getString(2));
	}
}
