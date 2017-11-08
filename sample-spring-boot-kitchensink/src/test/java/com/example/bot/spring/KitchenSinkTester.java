package com.example.bot.spring;


import static org.assertj.core.api.Assertions.assertThat;

import org.jooq.DSLContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { KitchenSinkTester.class, SQLDatabaseEngine.class })
public class KitchenSinkTester {
	@Autowired
	private MockDatabaseEngine databaseEngine;

	@Autowired
	DSLContext create;
	
	@Test
	public void testDummy() throws Exception {
		assertThat(true);
	}

}
