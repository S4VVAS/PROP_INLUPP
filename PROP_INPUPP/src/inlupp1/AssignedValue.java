package inlupp1;

public class AssignedValue {
	private String id;
	private Double value;

	public AssignedValue(Lexeme id, Lexeme value) {
		if (id.value() instanceof String && value.value() instanceof Double)
			this.id = (String) id.value();
		this.value = (Double) value.value();
	}

	public String getId() {
		return id;
	}

	public double getValue() {
		return value;
	}
}