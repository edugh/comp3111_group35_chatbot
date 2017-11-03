package com.example.bot.spring.model;

public class Customer {
	public final String id;
	public final String name;
	public final String gender;
	public final int age;
	public final String phoneNumber;
	public final String state;
	
	public Customer(
			String id,
			String name,
			String gender,
			int age,
			String phoneNumber,
			String state
			) {
		this.id = id;
		this.name = name;
		this.gender = gender;
		this.age = age;
		this.phoneNumber = phoneNumber;
		this.state = state;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Customer customer = (Customer) o;

		if (age != customer.age) return false;
		if (id != null ? !id.equals(customer.id) : customer.id != null) return false;
		if (name != null ? !name.equals(customer.name) : customer.name != null) return false;
		if (gender != null ? !gender.equals(customer.gender) : customer.gender != null) return false;
		if (phoneNumber != null ? !phoneNumber.equals(customer.phoneNumber) : customer.phoneNumber != null)
			return false;
		return state != null ? state.equals(customer.state) : customer.state == null;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (gender != null ? gender.hashCode() : 0);
		result = 31 * result + age;
		result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0);
		result = 31 * result + (state != null ? state.hashCode() : 0);
		return result;
	}
}
