package map;

public abstract class Territory implements Comparable<Territory>{
	private Float priority;

	public Float getPriority() {
		return priority;
	}

	public void setPriority(float priority) {
		this.priority = priority;
	}
	@Override
	public int compareTo(Territory territory) {
		return territory.getPriority().compareTo(this.getPriority());
	}
	public abstract void computePriority(String opponentName);
}
