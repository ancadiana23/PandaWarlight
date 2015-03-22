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

import java.util.ArrayList;

import bot.BotState;

public class Region extends Territory {

	private int id;
	// private LinkedList<Region> neighbors;
	private ArrayList<Integer> neighbors;
	private SuperRegion superRegion;
	private int armies;
	private String playerName;

	public Region(int id, SuperRegion superRegion) {
		this.id = id;
		this.superRegion = superRegion;
		this.neighbors = new ArrayList<Integer>();
		this.playerName = "unknown";
		this.armies = 0;

		superRegion.addSubRegion(this);
	}

	public Region(int id, SuperRegion superRegion, String playerName, int armies) {
		this.id = id;
		this.superRegion = superRegion;
		this.neighbors = new ArrayList<Integer>();
		this.playerName = playerName;
		this.armies = armies;

		superRegion.addSubRegion(this);
	}

	public void addNeighbor(Region neighbor) {
		if (!neighbors.contains(neighbor.id)) {
			neighbors.add(neighbor.id);
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
	public ArrayList<Integer> getNeighbors() {
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

	public int armiesNeededToCapture() {
		return (int) Math.round((1.7 * armies));
	}

	public int armiesItCanKill() {
		return (int) Math.round((0.6 * armies));
	}
	public int armiesNeededToDefend(){
		int enemyArmies = (int) Math.abs(getPriority() + getArmies());
		return (int) (Math.round((enemyArmies * 0.6)) - getArmies() + 1);
	}

	public int getUnknownNeighbors() {
		int counter = 0;
		for (int neighbor : neighbors) {
			if (BotState.getInstance().getVisibleMap().getRegion(neighbor)
					.getPlayerName().equals("unknown"))
				counter++;
		}
		return counter;
	}

	public int getEnemyNeighbors(String enemy) {
		int counter = 0;
		// System.err.println("Search visible neighbors for region:"+this.id);
		// System.err.println("Neighbors:" + this.neighbors.size());
		for (int neighbor : neighbors) {
			// System.err.println(neighbor.playerName + "with id "+neighbor.id);
			// System.err.println(BotState.getInstance().getVisibleMap().getRegion(neighbor.id).playerName);
			if (BotState.getInstance().getVisibleMap().getRegion(neighbor)
					.ownedByPlayer(enemy))
				counter++;
		}
		// System.err.println("No enemies found "+counter);
		return counter;
	}

	public int getEmptyNeighbors(String enemy, String me) {
		int counter = 0;
		Region neighbor;
		for (int neighborId : neighbors) {
			neighbor = BotState.getInstance().getVisibleMap().getRegion(neighborId);
			if (!neighbor.ownedByPlayer(enemy) && !neighbor.ownedByPlayer(me)
					&& neighbor.getArmies() != 0)
				counter++;
		}
		return counter;
	}

	@Override
	public void computePriority(String opponentName) {
		int enemyArmies = 0;
		Region neighbor;
		for (int neighborId : getNeighbors())
		{
			neighbor=BotState.getInstance().getVisibleMap().getRegion(neighborId);
			if (neighbor.ownedByPlayer(opponentName))
				enemyArmies += neighbor.getArmies() - 1;
		System.err.println("det: enemy: " + enemyArmies + " mine: " + getArmies());
		}
		this.setPriority(enemyArmies-getArmies());
	}

}
