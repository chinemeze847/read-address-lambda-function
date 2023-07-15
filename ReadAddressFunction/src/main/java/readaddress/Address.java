package readaddress;

/**
 * This class is used to build an address object that can be passed
 * to the WriteAddress Lambda function
 */
public class Address {
	private int id;

	private String number;
	private String street;

	private String state;
	private String country;

	// Constructors, getters, and setters

	public Address(int id, String number, String street, String state, String country) {
		this.id = id;
		this.number = number;
		this.street = street;
		this.state = state;
		this.country = country;
	}

	public Address() {

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@Override
	public String toString() {
		return "Address{" +
				"id=" + id +
				", street='" + street + '\'' +
				", city='" + number + '\'' +
				", state='" + state + '\'' +
				", country='" + country + '\'' +
				'}';
	}
}
