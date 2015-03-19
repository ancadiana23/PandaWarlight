package map;

public class Pair<V> implements Comparable<Pair<V>>{
	private V region;
	private Float priority;
	
	public Pair(V region, float priority) {
		this.region = region;
		this.priority = priority;
	}
	
	public V getRegion() {
		return region;
	}
	
	public float getPriority() {
		return priority;
	}
	
	public void setPriority(Float priority) {
		this.priority = priority;
	}

	@Override
	public int compareTo(Pair<V> pair) {
		int compareResult = priority.compareTo(pair.priority);
		if (compareResult == 0)
			if (region instanceof SuperRegion) {
				Integer thisReg = (Integer)((SuperRegion)region).getSubRegions().size();
				Integer otherReg = (Integer)((SuperRegion)pair.region).getSubRegions().size();
				
				return thisReg.compareTo(otherReg);
			}	
		
		return -compareResult;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object pair) {
		return region == ((Pair<V>)pair).region;
	}
}
