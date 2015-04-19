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
import java.util.List;

import bot.BotState;

public class Region extends Territory {

	private LinkedList<Region> neighbors;
	private SuperRegion superRegion;
	private int armies;
	// armies that are kept especially to protect from neighboring enemies
	private int armiesForDefense;
	private String playerName;

	public Region(int id, SuperRegion superRegion) {
		this.id = id;
		this.superRegion = superRegion;
		this.neighbors = new LinkedList<Region>();
		this.playerName = "unknown";
		this.armies = 0;
		this.armiesForDefense = 0;

		superRegion.addSubRegion(this);
	}

	public Region(int id, SuperRegion superRegion, String playerName, int armies) {
		this.id = id;
		this.superRegion = superRegion;
		this.neighbors = new LinkedList<Region>();
		this.playerName = playerName;
		this.armies = armies;

		superRegion.addSubRegion(this);
	}

	public void addNeighbor(Region neighbor) {
		if (!neighbors.contains(neighbor)) {
			neighbors.add(neighbor);
			neighbor.addNeighbor(this);
		}
	}

	/**
	 * @param region
	 *            a Region object
	 * @return True if this Region is a neighbor of given Region, false
	 *         otherwise
	 */
	public boolean isNeighbor(Region region) {
		if (neighbors.contains(region))
			return true;
		return false;
	}

	/**
	 * @param playerName
	 *            A string with a player's name
	 * @return True if this region is owned by given playerName, false otherwise
	 */
	public boolean ownedByPlayer(String playerName) {
		if (playerName.equals(this.playerName))
			return true;
		return false;
	}

	/**
	 * @param armies
	 *            Sets the number of armies that are on this Region
	 */
	public void setArmies(int armies) {
		this.armies = armies;
	}

	/**
	 * @param playerName
	 *            Sets the Name of the player that this Region belongs to
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	/**
	 * @return The id of this Region
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return A list of this Region's neighboring Regions
	 */
	public LinkedList<Region> getNeighbors() {
		return neighbors;
	}

	/**
	 * @return The SuperRegion this Region is part of
	 */
	public SuperRegion getSuperRegion() {
		return superRegion;
	}

	/**
	 * @return The number of armies on this region
	 */
	public int getArmies() {
		return armies;
	}

	/**
	 * @return A string with the name of the player that owns this region
	 */
	public String getPlayerName() {
		return playerName;
	}

	public int getArmiesForDefense() {
		return armiesForDefense;
	}

	public void setArmiesForDefense(int armiesForDefense) {
		this.armiesForDefense = armiesForDefense;
	}

	/**
	 * Sets the priority for the current Region as the difference between the
	 * number of armies that can attack it and its armies
	 */
	@Override
	public void computePriority() {
		int enemyArmies = 0;
		String enemy = BotState.getOpponentPlayerNameStatic();
		for (Region neighbor : neighbors)
			if (neighbor.ownedByPlayer(enemy))
				enemyArmies += neighbor.getArmies() - 1;
		setPriority(enemyArmies - armies);
	}

	/**
	 * 
	 * @return The armies that are needed in order to capture this region
	 */
	public int armiesNeededToCapture() {
		return (int) Math.round(1.7 * armies);
	}

	/**
	 * 
	 * @return The number of armies that this Region can kill at the moment
	 */
	public int armiesItCanKill() {
		return (int) Math.round(0.6 * armies);
	}

	/**
	 * 
	 * @return The number of armies the Bot should add to this Region to defend
	 *         it against all its neighboring enemies
	 */
	public int armiesNeededToDefend() {
		// the priority is the number of armies that can attack this Region
		// minus its own armies
		// getPriority() + armies = number of armies that can attack this Region
		int enemyArmies = (int) Math.abs(priority + armies);
		int neededArmies = (int) Math.round((enemyArmies * 0.6)) - armies + 1;

		setArmiesForDefense(neededArmies + armies);
		return neededArmies;
	}

	/**
	 * 
	 * @param List
	 *            of Regions to defend itself against
	 * @return The number of armies this Region needs in order to defend itself
	 *         against the enemies contained in the list
	 */
	public int armiesNeededToDefend(List<Region> neighbors) {
		int enemyArmies = 0;
		String enemy = BotState.getOpponentPlayerNameStatic();
		for (Region neighbor : neighbors)
			if (neighbor.ownedByPlayer(enemy))
				enemyArmies += neighbor.getArmies() - 1;

		int neededArmies = (int) (Math.round(enemyArmies * 0.6)) + 1;

		setArmiesForDefense(neededArmies);
		return neededArmies;
	}

	public int getUnknownNeighbors() {
		int counter = 0;
		for (Region neighbor : neighbors) {
			if (neighbor.ownedByPlayer("unknown") && neighbor.armies == 0)
				counter++;
		}
		return counter;
	}

	/**
	 * 
	 * @param Name
	 *            of the enemy
	 * @return Number of neighbors owned by the enemy
	 */
	public int getEnemyNeighbors(String enemy) {
		int counter = 0;

		for (Region neighbor : neighbors) {
			if (neighbor.ownedByPlayer(enemy))
				counter++;
		}
		return counter;
	}

	public int getNeutralNeighbors(String enemy, String me) {
		int counter = 0;
		for (Region neighbor : neighbors) {
			if (!neighbor.ownedByPlayer(enemy) && !neighbor.ownedByPlayer(me)
					&& neighbor.armies != 0)
				counter++;
		}
		return counter;
	}

	/**
	 * Regions are to be sorted in descending order of the armies the Bot needs
	 * to add to them
	 */
	@Override
	public int compareTo(Territory territory) {
		return -this.priority.compareTo(territory.priority);
	}

}
