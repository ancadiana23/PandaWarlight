/**
 * Warlight AI Game Bot
 *
 * Last update: January 29, 2015
 *
 * @author Jim van Eeden
 * @version 1.1
 * @License MIT License (http://opensource.org/Licenses/MIT)
 */

package map;
import java.util.LinkedList;

import bot.BotState;

public class SuperRegion extends Territory{
	
	private int armiesReward;
	private LinkedList<Region> subRegions;
	
	public SuperRegion(int id, int armiesReward)
	{
		this.id = id;
		this.armiesReward = armiesReward;
		subRegions = new LinkedList<Region>();
	}
	
	public void addSubRegion(Region subRegion)
	{
		if(!subRegions.contains(subRegion))
			subRegions.add(subRegion);
	}
	
	/**
	 * @return A string with the name of the player that fully owns this SuperRegion
	 */
	public String ownedByPlayer()
	{
		String playerName = subRegions.getFirst().getPlayerName();
		for(Region region : subRegions)
		{
			if (!playerName.equals(region.getPlayerName()))
				return null;
		}
		return playerName;
	}
	
	/**
	 * @return The id of this SuperRegion
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return The number of armies a Player is rewarded when he fully owns this SuperRegion
	 */
	public int getArmiesReward() {
		return armiesReward;
	}
	
	/**
	 * @return A list with the Regions that are part of this SuperRegion
	 */
	public LinkedList<Region> getSubRegions() {
		return subRegions;
	}
	
	public float getValue() {
		return (float) armiesReward / subRegions.size();
	}

	@Override
	public int compareTo(Territory territory) {
		if (priority == territory.priority) {
			Integer thisReg = regionsNotConquered();
			Integer otherReg = (Integer) ((SuperRegion) territory).regionsNotConquered();
			return thisReg.compareTo(otherReg);
		}
		return -this.priority.compareTo(territory.priority);
	}
	
	@Override
	public void computePriority() {
		setPriority(getValue());
	}
	
	public int regionsNotConquered() {
		int count = 0;
		for (Region region : subRegions) {
			if (!region.ownedByPlayer(BotState.getMyPlayerNameStatic()))
				count++;
		}
		return count;
	}
}
