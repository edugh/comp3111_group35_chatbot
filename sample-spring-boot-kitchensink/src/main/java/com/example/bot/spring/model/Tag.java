package com.example.bot.spring.model;

import com.example.bot.spring.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Tag {
	public final String name;
	public final String customerId;
	
	public Tag(String name, String cid) {
		this.name = name;
		this.customerId = cid;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Tag tag = (Tag) o;

		if (name != null ? !name.equals(tag.name) : tag.name != null) return false;
		return customerId != null ? customerId.equals(tag.customerId) : tag.customerId == null;
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (customerId != null ? customerId.hashCode() : 0);
		return result;
	}

	public String toString() {
		return "Tag{" +
				"name='" + name + '\'' +
				", customerId='" + customerId + '\'' +
				'}';
	}

	public static Tag fromResultSet(ResultSet resultSet) {
		try {
			return new Tag(resultSet.getString(1),
					resultSet.getString(2));
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
}
