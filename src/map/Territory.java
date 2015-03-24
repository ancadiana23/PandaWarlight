package map;

/**
 * @author PandaCoders Abstract class used to make Region and SuperRegion
 *         Comparable with respect to their priority (to be conquered or to
 *         defend)
 */
public abstract class Territory implements Comparable<Territory> {

	protected Float priority;
	protected int id;

	public Float getPriority() {
		return priority;
	}

	public void setPriority(float priority) {
		this.priority = priority;
	}

	@Override
	public abstract int compareTo(Territory territory);

	/*
	 * two territories are equal when they have the same id
	 */
	@Override
	public boolean equals(Object o) {
		if (id == ((Territory) o).id)
			return true;
		return false;
	}

	public abstract void computePriority();

}
