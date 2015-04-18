/**
 * Warlight AI Game Bot
 *
 * Last update: January 29, 2015
 *
 * @author Jim van Eeden
 * @version 1.1
 * @License MIT License (http://opensource.org/Licenses/MIT)
 */

package bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import map.Map;
import map.Region;
import map.SuperRegion;
import map.Territory;
import move.AttackTransferMove;
import move.PlaceArmiesMove;
import move.Move;

public class BotState {

	private String myName = "";
	private String opponentName = "";

	private static String staticMyName = "";
	private static String staticOpponentName = "";

	// This map is known from the start, contains all the regions and how they
	// are connected, doesn't change after initialization
	private final Map fullMap = new Map();

	// This map represents everything the player can see, updated at the end of
	// each round.
	private Map visibleMap;

	// list of regions the player can choose the start from
	private ArrayList<Region> pickableStartingRegions;

	// wastelands, i.e. neutral regions with a larger amount of armies on them.
	// Given before the picking of starting regions
	private ArrayList<Region> wastelands;

	// list of all the opponent's moves, reset at the end of each round
	private ArrayList<Move> opponentMoves;

	private int startingArmies; // number of armies the player can place on map
	private int maxRounds;
	private int roundNumber;

	private long totalTimebank; // total time that can be in the timebank

	// the amount of time that is added to the timebank per requested move
	private long timePerMove;

	// the queue of superRegions to be captured
	private LinkedList<SuperRegion> superRegToConquer;

	// the list of territories near enemies or neutral territories
	private LinkedList<Region> myEdgeTerritories;

	// the list of territories surrounded by my territories
	private LinkedList<Region> myInnerTerritories;

	// the list of attack/transfer moves
	private ArrayList<AttackTransferMove> attackTransferMoves;

	//the list of neutral target regions to conquer 
	private LinkedList<Region> neutralTargetRegions;
	/**
	 * Constructor.
	 */
	public BotState() {
		opponentMoves = new ArrayList<Move>();
		roundNumber = 0;
		superRegToConquer = new LinkedList<SuperRegion>();
		myInnerTerritories = new LinkedList<Region>();
		attackTransferMoves = new ArrayList<AttackTransferMove>();
		neutralTargetRegions = new LinkedList<Region>();
	}

	/**
	 * Updates the starting regions and the game information not related to the
	 * map.
	 * 
	 * @param key
	 * @param parts
	 */
	public void updateSettings(String key, String[] parts) {
		String value;

		if (key.equals("starting_regions") && parts.length > 3) {
			setPickableStartingRegions(parts);
			return;
		}
		value = parts[2];

		if (key.equals("your_bot")) { // bot's own name
			myName = parts[2];
			staticMyName = myName;
		} else if (key.equals("opponent_bot")) { // opponent's name
			opponentName = value;
			staticOpponentName = opponentName;
		} else if (key.equals("max_rounds"))
			maxRounds = Integer.parseInt(value);
		else if (key.equals("timebank"))
			totalTimebank = Long.parseLong(value);
		else if (key.equals("time_per_move"))
			timePerMove = Long.parseLong(value);
		else if (key.equals("starting_armies")) {
			startingArmies = Integer.parseInt(value);
			roundNumber++; // next round
		}
	}

	// initial map is given to the bot with all the information except for
	// player and armies info
	public void setupMap(String[] mapInput) {
		int i, regionId, superRegionId, wastelandId, reward;

		if (mapInput[1].equals("super_regions")) {
			for (i = 2; i < mapInput.length; i++) {
				try {
					superRegionId = Integer.parseInt(mapInput[i]);
					i++;
					reward = Integer.parseInt(mapInput[i]);
					fullMap.add(new SuperRegion(superRegionId, reward));
				} catch (Exception e) {
					System.err.println("Unable to parse SuperRegions");
				}
			}
		} else if (mapInput[1].equals("regions")) {
			for (i = 2; i < mapInput.length; i++) {
				try {
					regionId = Integer.parseInt(mapInput[i]);
					i++;
					superRegionId = Integer.parseInt(mapInput[i]);
					SuperRegion superRegion = fullMap
							.getSuperRegion(superRegionId);
					fullMap.add(new Region(regionId, superRegion));
				} catch (Exception e) {
					System.err.println("Unable to parse Regions "
							+ e.getMessage());
				}
			}
		} else if (mapInput[1].equals("neighbors")) {
			for (i = 2; i < mapInput.length; i++) {
				try {
					Region region = fullMap.getRegion(Integer
							.parseInt(mapInput[i]));
					i++;
					String[] neighborIds = mapInput[i].split(",");
					for (int j = 0; j < neighborIds.length; j++) {
						Region neighbor = fullMap.getRegion(Integer
								.parseInt(neighborIds[j]));
						region.addNeighbor(neighbor);
					}
				} catch (Exception e) {
					System.err.println("Unable to parse Neighbors "
							+ e.getMessage());
				}
			}
		} else if (mapInput[1].equals("wastelands")) {
			wastelands = new ArrayList<Region>();
			for (i = 2; i < mapInput.length; i++) {
				try {
					wastelandId = Integer.parseInt(mapInput[i]);
					wastelands.add(fullMap.getRegion(wastelandId));
				} catch (Exception e) {
					System.err.println("Unable to parse wastelands "
							+ e.getMessage());
				}
			}
		}
	}

	// regions from wich a player is able to pick his preferred starting region
	public void setPickableStartingRegions(String[] input) {
		pickableStartingRegions = new ArrayList<Region>();
		for (int i = 2; i < input.length; i++) {
			int regionId;
			try {
				regionId = Integer.parseInt(input[i]);
				Region pickableRegion = fullMap.getRegion(regionId);
				pickableStartingRegions.add(pickableRegion);
			} catch (Exception e) {
				System.err.println("Unable to parse pickable regions "
						+ e.getMessage());
			}
		}
	}

	// visible regions are given to the bot with player and armies info
	public void updateMap(String[] mapInput) {
		visibleMap = fullMap.getMapCopy();
		for (int i = 1; i < mapInput.length; i++) {
			try {
				Region region = visibleMap.getRegion(Integer
						.parseInt(mapInput[i]));
				String playerName = mapInput[i + 1];
				int armies = Integer.parseInt(mapInput[i + 2]);

				region.setPlayerName(playerName);
				region.setArmies(armies);
				i += 2;
			} catch (Exception e) {
				System.err.println("Unable to parse Map Update "
						+ e.getMessage());
			}
		}
		ArrayList<Region> unknownRegions = new ArrayList<Region>();

		// remove regions which are unknown.
		for (Region region : visibleMap.regions)
			if (region.getPlayerName().equals("unknown"))
				unknownRegions.add(region);
		for (Region unknownRegion : unknownRegions)
			visibleMap.getRegions().remove(unknownRegion);
	}

	// Parses a list of the opponent's moves every round.
	// Clears it at the start, so only the moves of this round are stored.
	public void readOpponentMoves(String[] moveInput) {
		opponentMoves.clear();
		for (int i = 1; i < moveInput.length; i++) {
			try {
				Move move;
				if (moveInput[i + 1].equals("place_armies")) {
					Region region = visibleMap.getRegion(Integer
							.parseInt(moveInput[i + 2]));
					String playerName = moveInput[i];
					int armies = Integer.parseInt(moveInput[i + 3]);
					move = new PlaceArmiesMove(playerName, region, armies);
					i += 3;
				} else if (moveInput[i + 1].equals("attack/transfer")) {
					Region fromRegion = visibleMap.getRegion(Integer
							.parseInt(moveInput[i + 2]));
					if (fromRegion == null) // might happen if the region isn't
											// visible
						fromRegion = fullMap.getRegion(Integer
								.parseInt(moveInput[i + 2]));

					Region toRegion = visibleMap.getRegion(Integer
							.parseInt(moveInput[i + 3]));
					if (toRegion == null) // might happen if the region isn't
											// visible
						toRegion = fullMap.getRegion(Integer
								.parseInt(moveInput[i + 3]));

					String playerName = moveInput[i];
					int armies = Integer.parseInt(moveInput[i + 4]);
					move = new AttackTransferMove(playerName, fromRegion,
							toRegion, armies);
					i += 4;
				} else { // never happens
					continue;
				}
				opponentMoves.add(move);
			} catch (Exception e) {
				System.err.println("Unable to parse Opponent moves "
						+ e.getMessage());
			}
		}
	}

	public String getMyPlayerName() {
		return myName;
	}

	public String getOpponentPlayerName() {
		return opponentName;
	}

	public static String getMyPlayerNameStatic() {
		return staticMyName;
	}

	public static String getOpponentPlayerNameStatic() {
		return staticOpponentName;
	}

	public int getStartingArmies() {
		return startingArmies;
	}

	public int getRoundNumber() {
		return roundNumber;
	}

	public Map getVisibleMap() {
		return visibleMap;
	}

	public Map getFullMap() {
		return fullMap;
	}

	public ArrayList<Move> getOpponentMoves() {
		return opponentMoves;
	}

	public ArrayList<Region> getPickableStartingRegions() {
		return pickableStartingRegions;
	}

	public ArrayList<Region> getWasteLands() {
		return wastelands;
	}

	public LinkedList<SuperRegion> getSuperRegToConquer() {
		return superRegToConquer;
	}

	public LinkedList<Region> getMyEdgeTerritories() {
		return myEdgeTerritories;
	}

	public LinkedList<Region> getMyInnerTerritories() {
		return myInnerTerritories;
	}

	public ArrayList<AttackTransferMove> getAttackTransferMoves() {
		return attackTransferMoves;
	}
	
	public LinkedList<Region> getNeutralTargetRegions() {
		return neutralTargetRegions;
	}

	/**
	 * Adds an AttackTransferMove to the list of attackTransferMoves.
	 * 
	 * @param move
	 */
	public void addAttackTransferMove(AttackTransferMove move) {
		attackTransferMoves.add(move);
		Region fromRegion = move.getFromRegion();
		
		//update the number of armies in the fromRegion after an attack
		fromRegion.setArmies(fromRegion.getArmies() - move.getArmies());
	}

	/**
	 * Resets the attackTransferMoves.
	 */
	public void clearAttackTransferMove() {
		attackTransferMoves.clear();
	}
	
	/**
	 * Resets the neutralTargetRegions.
	 */
	public void clearNeutralTargetRegions() {
		neutralTargetRegions.clear();
	}

	/**
	 * Verifies if a Region is in the center of my territories Returns true if
	 * it is in myInnerTerritories, false otherwise.
	 * 
	 * @param region
	 * @return
	 */
	public boolean areAllNeighborsAllies(Region region) {
		LinkedList<Region> neighbors = region.getNeighbors();
		for (Region neighbor : neighbors)
			if (!neighbor.ownedByPlayer(myName))
				return false;
		return true;
	}

	/**
	 * Separates my territories into 2 lists : myInnerTerritories and
	 * myEdgeTerritories.
	 */
	public void detMyEdgeTerritories() {
		myEdgeTerritories = new LinkedList<Region>();
		myInnerTerritories = new LinkedList<Region>();
		LinkedList<Region> regions = visibleMap.getRegions();

		for (Region region : regions)
			if (region.ownedByPlayer(myName))
				if (!areAllNeighborsAllies(region))
					myEdgeTerritories.add(region);
				else
					myInnerTerritories.add(region);
	}

	/**
	 * Calculates the priority of each territory, then sorts by it.
	 * 
	 * @param territories
	 */
	public void sortTerritories(List<? extends Territory> territories) {
		for (Territory territory : territories)
			territory.computePriority();
		Collections.sort(territories);
	}

}
