package com.example.bot.spring;

import lombok.extern.slf4j.Slf4j;
import java.net.URISyntaxException;
import java.net.URI;
import java.sql.*;

@Slf4j
public class SQLDatabaseEngine extends DatabaseEngine {

	public Connection getConnection() {
		try {
			URI dbUri = new URI(System.getenv("DATABASE_URL"));
			String username = dbUri.getUserInfo().split(":")[0];
			String password = dbUri.getUserInfo().split(":")[1];
			String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath()
					+ "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";

			log.info("Username: {} Password: {}", username, password);
			log.info("dbUrl: {}", dbUrl);

			return DriverManager.getConnection(dbUrl, username, password);
		} catch (URISyntaxException e) {
			log.error("Database URI is malformed!");
			throw new RuntimeException(e);
		} catch (SQLException e){
			log.error("Cannot connect to database");
			throw new DatabaseException(e);
		}
	}
}
