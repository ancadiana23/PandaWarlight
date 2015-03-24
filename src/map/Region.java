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


public class Region extends Territory{
	
	private LinkedList<Region> neighbors;
	private SuperRegion superRegion;
	private int armies;
	private int armiesForDefense;
	private String playerName;
	
	public Region(int id, SuperRegion superRegion)
	{
		this.id = id;
		this.superRegion = superRegion;
		this.neighbors = new LinkedList<Region>();
		this.playerName = "unknown";
		this.armies = 0;
		this.armiesForDefense = 0;
		
		superRegion.addSubRegion(this);
	}
	
	public Region(int id, SuperRegion superRegion, String playerName, int armies)
	{
		this.id = id;
		this.superRegion = superRegion;
		this.neighbors = new LinkedList<Region>();
		this.playerName = playerName;
		this.armies = armies;
		
		superRegion.addSubRegion(this);
	}
	
	public void addNeighbor(Region neighbor)
	{
		if(!neighbors.contains(neighbor))
		{
			neighbors.add(neighbor);
			neighbor.addNeighbor(this);
		}
	}
	
	/**
	 * @param region a Region object
	 * @return True if this Region is a neighbor of given Region, false otherwise
	 */
	public boolean isNeighbor(Region region)
	{
		if(neighbors.contains(region))
			return true;
		return false;
	}

	/**
	 * @param playerName A string with a player's name
	 * @return True if this region is owned by given playerName, false otherwise
	 */
	public boolean ownedByPlayer(String playerName)
	{
		if(playerName.equals(this.playerName))
			return true;
		return false;
	}
	
	/**
	 * @param armies Sets the number of armies that are on this Region
	 */
	public void setArmies(int armies) {
		this.armies = armies;
	}
	
	/**
	 * @param playerName Sets the Name of the player that this Region belongs to
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
	
	@Override
	public void computePriority() {
		int enemyArmies = 0;
		String enemy = BotState.getOpponentPlayerNameStatic();
		for (Region neighbor : neighbors)
			if (neighbor.ownedByPlayer(enemy))
				enemyArmies += neighbor.getArmies() - 1;
		setPriority(enemyArmies - armies);
	}
	
	public int armiesNeededToCapture() {
		return (int) Math.round(1.7 * armies);
	}

	public int armiesItCanKill() {
		return (int) Math.round(0.6 * armies);
	}
	public int armiesNeededToDefend(){
		int enemyArmies = (int) Math.abs(getPriority() + armies);
		return (int) (Math.round((enemyArmies * 0.6)) - armies + 1);
	}
	public int armiesNeededToDefend(List<Region> neighbors){
		int enemyArmies = 0;
		String enemy = BotState.getOpponentPlayerNameStatic();
		for (Region neighbor : neighbors )
			if (neighbor.ownedByPlayer(enemy))
				enemyArmies += neighbor.getArmies() - 1;
	
		return (int) (Math.round(enemyArmies * 0.6));
	}

	public int getUnknownNeighbors() {
		int counter = 0;
		for (Region neighbor : neighbors) {
			if (neighbor.ownedByPlayer("unknown")  && neighbor.armies == 0)
				counter++;
		}
		return counter;
	}

	public int getEnemyNeighbors(String enemy) {
		int counter = 0;
		
		for (Region neighbor : neighbors) {
			if (neighbor.ownedByPlayer(enemy))
				counter++;
		}
		// System.err.println("No enemies found "+counter);
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

	@Override
	public int compareTo(Territory territory) {
		return -this.getPriority().compareTo(territory.getPriority());
	}
	
}
