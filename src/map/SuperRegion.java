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

public class SuperRegion extends Territory {

	private int armiesReward;
	private LinkedList<Region> subRegions;

	public SuperRegion(int id, int armiesReward) {
		this.id = id;
		this.armiesReward = armiesReward;
		subRegions = new LinkedList<Region>();
	}

	public void addSubRegion(Region subRegion) {
		if (!subRegions.contains(subRegion))
			subRegions.add(subRegion);
	}

	/**
	 * @return A string with the name of the player that fully owns this
	 *         SuperRegion
	 */
	public String ownedByPlayer() {
		String playerName = subRegions.getFirst().getPlayerName();
		for (Region region : subRegions) {
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
	 * @return The number of armies a Player is rewarded when he fully owns this
	 *         SuperRegion
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

	/**
	 * 
	 * @return The average value of the SuperRegion
	 */
	public float getValue() {
		int armies = 0;
		for (Region region : subRegions)
			if (region.ownedByPlayer("unknown"))
				if (BotState.getWasteLands().contains(region))
					armies += 6;
				else
					armies += 2;
			else
				if (!region.ownedByPlayer(BotState.getMyPlayerNameStatic()))
					armies += region.getArmies();
		return (float) armiesReward / armies;
	}

	/**
	 * SuperRegions are compared with respect to their priority; if the
	 * priorities are equal then they are compared with respect to the number of
	 * foreign Regions they contain
	 */
	@Override
	public int compareTo(Territory territory) {
		if (priority == territory.priority) {

			Integer thisReg = regionsNotConquered();
			Integer otherReg = (Integer) ((SuperRegion) territory).regionsNotConquered();
			return thisReg.compareTo(otherReg);
		}
		return -this.priority.compareTo(territory.priority);
	}

	/**
	 * Sets the priority as the average value of the SuperRegion
	 */
	@Override
	public void computePriority() {
		setPriority(getValue());
	}

	/**
	 * 
	 * @return The number of foreign regions contained by the SuperRegion
	 */
	public int regionsNotConquered() {
		int count = 0;
		// for every region in this SuperRegion
		// if the Region is not owned by me increment a counter
		for (Region region : subRegions) {
			if (!region.ownedByPlayer(BotState.getMyPlayerNameStatic()))
				count++;
		}
		return count;
	}
}
