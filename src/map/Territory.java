package map;

public abstract class Territory implements Comparable<Territory>{

	private Float priority;
	protected int id;
	
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
	
	@Override
	public boolean equals(Object o) {
		if (id == ((Territory)o).id)
			return true;
		return false;
	}
	public abstract void computePriority();
	
}
