package com.example.bot.spring.model;

public class Tag {
	public final String name;
	public final String customerId;
	
	public Tag(String name, String cid) {
		this.name = name;
		this.customerId = cid;
	}

	@Override
	public String toString() {
		return "Tag{" +
				"name='" + name + '\'' +
				", customerId='" + customerId + '\'' +
				'}';
	}
}
