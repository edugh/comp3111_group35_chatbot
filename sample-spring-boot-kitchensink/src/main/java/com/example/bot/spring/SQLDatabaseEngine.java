package com.example.bot.spring;

import lombok.extern.slf4j.Slf4j;
import java.net.URISyntaxException;
import java.net.URI;
import java.sql.*;

@Slf4j
public class SQLDatabaseEngine extends DatabaseEngine {

	public Connection getConnection() throws URISyntaxException, SQLException {
		Connection connection;
		URI dbUri = new URI("postgres://bguxamugzixwea:0373a326383fc0f179a4dcdc1c3379be05161a0573064f72cc130e0db28f186a@ec2-54-243-124-240.compute-1.amazonaws.com:5432/dd0r30nupaffkk");

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
