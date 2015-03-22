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
		return -this.getPriority().compareTo(territory.getPriority());
	}
	public abstract void computePriority(String opponentName);
}
