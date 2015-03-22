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

/**
 * This is a simple bot that does random (but correct) moves.
 * This class implements the Bot interface and overrides its Move methods.
 * You can implement these methods yourself very easily now,
 * since you can retrieve all information about the match from variable “state”.
 * When the bot decided on the move to make, it returns an ArrayList of Moves. 
 * The bot is started by creating a Parser to which you add
 * a new instance of your bot, and then the parser is started.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import map.Pair;
import map.Region;
import map.SuperRegion;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

public class BotStarter implements Bot {
	@Override
	/**
	 * A method that returns which region the bot would like to start on, the pickable regions are stored in the BotState.
	 * The bots are asked in turn (ABBAABBAAB) where they would like to start and return a single region each time they are asked.
	 * This method returns one random region from the given pickable regions.
	 */
	public Region getStartingRegion(BotState state, Long timeOut) {
		double rand = Math.random();
		int r = (int) (rand * state.getPickableStartingRegions().size());
		int regionId = state.getPickableStartingRegions().get(r).getId();
		Region startingRegion = state.getFullMap().getRegion(regionId);

		LinkedList<Pair<SuperRegion>> superRegionsToCapture = state
				.getSuperRegionsToCapture();
		if (superRegionsToCapture == null)
			superRegionsToCapture = new LinkedList<Pair<SuperRegion>>();
		SuperRegion superRegion = startingRegion.getSuperRegion();
		Pair<SuperRegion> pair = new Pair<SuperRegion>(superRegion,
				superRegion.getValue());

		if (!superRegionsToCapture.contains(pair)) {
			System.err.println("superReg: " + superRegion.getId());
			superRegionsToCapture.add(pair);
		}
		return startingRegion;
	}

	@Override
	/**
	 * This method is called for at first part of each round. This example puts two armies on random regions
	 * until he has no more armies left to place.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state,
			Long timeOut) {

		ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		String myName = state.getMyPlayerName();
		int armies = 2;
		int armiesLeft = state.getStartingArmies();
		LinkedList<Region> visibleRegions = state.getVisibleMap().getRegions();
		// Start1
		state.detMyEdgeRegions();
		LinkedList<Region> myEdgeRegions = state.getMyEdgeRegions();
		boolean visibleEnemies = false;

		for (Region reg : myEdgeRegions)
			if (reg.getEnemyNeighbors(state.getOpponentPlayerName()) != 0) {
				visibleEnemies = true;
				break;
			}

		LinkedList<Pair<Region>> priorityRegions = state.getRegionsPriority();
		Collections.sort(priorityRegions);
		LinkedList<Pair<Region>>  regionsForRemainingArmies = new LinkedList<Pair<Region>>();
		if (visibleEnemies) {
			System.err.println("Visible enemies!");
			// apply strategy for vis enemies
			for (Pair<Region> pair : priorityRegions) {
				Region region = pair.getRegion();
				int enemyArmies = (int) Math.abs(pair.getPriority()
						+ region.getArmies());
				int neededArmies = (int) (Math.round((enemyArmies * 0.6))
						- region.getArmies() + 1);
				System.err.println("Enemy armies: " + enemyArmies);
				System.err.println("before if needed " + neededArmies
						+ " armiesL:" + armiesLeft);
				if (neededArmies > 0 && neededArmies <= armiesLeft) {
					System.err.println("Needed armies: " + neededArmies);
					placeArmiesMoves.add(new PlaceArmiesMove(myName, region,
							neededArmies));
					armiesLeft -= neededArmies;
					System.err.println("ArmiesLeftaftermove" + armiesLeft);
				} else if (neededArmies > armiesLeft)
					regionsForRemainingArmies.add(pair);
				if (armiesLeft <= 0)
					return placeArmiesMoves;
			}
		}

		// //extend
		// //Get the SuperRegions we want to capture first
		LinkedList<Pair<SuperRegion>> superRegionsToCapture = state
				.getSuperRegionsToCapture();
		//
		// //See if some of the targets are captured and remove them
		for (int i = 0; i < superRegionsToCapture.size(); i++) {

			if (superRegionsToCapture.get(i).getRegion().ownedByPlayer()
					.equals(myName)) {
				superRegionsToCapture.remove(i);
				i--;
			}
		}

		if (superRegionsToCapture.isEmpty())
			System.err.println(" sr EMPTY!!!");

		// If we're done capturing all the planned SuperRegions, add new targets
		if (superRegionsToCapture.isEmpty()) {
			for (Region reg : myEdgeRegions) {
				Region neighbor;
				for (int neighborId : reg.getNeighbors()) {
					neighbor = BotState.getInstance().getVisibleMap()
							.getRegion(neighborId);
					SuperRegion superRegion = neighbor.getSuperRegion();
					if (!superRegion.ownedByPlayer().equals(myName)) {
						Pair<SuperRegion> pair = new Pair<SuperRegion>(
								superRegion, superRegion.getValue());

						if (!superRegionsToCapture.contains(pair))
							superRegionsToCapture.add(pair);
					}
				}
			}
		}

		// Sort the targets by priority (biggest priority first)
		// and by size if priorities are equal (smallest SuperRegions first)
		Collections.sort(superRegionsToCapture);

		// //This is...SPARTAAAAAAAAAAAAAAAAAAAA
		//
		// //conquer
		// //slash
		// //kill
		for (Pair<SuperRegion> pair : superRegionsToCapture)
			System.err.println("sup: " + pair.getRegion().getId() + "  prior: "
					+ pair.getPriority());
		for (Pair<SuperRegion> pair : superRegionsToCapture) {
			SuperRegion superRegion = pair.getRegion();
			System.err.println("superreg to deploy: " + superRegion.getId());
			
			for (Region reg : superRegion.getSubRegions())
				System.err.println("subreg: " + reg.getId());
			
			for (Region region : myEdgeRegions) {
				System.err.println("region : " + region.getId());
				Region neighbor;
				for (Integer neighborId : region.getNeighbors()) {
					neighbor = BotState.getInstance().getVisibleMap()
							.getRegion(neighborId);
					System.err.println("neigh: " + neighbor.getId() + "own: "
							+ neighbor.getPlayerName());
					System.err.println("contains? "
							+ superRegion.getSubRegions().contains(neighbor));// just...why?

					System.err.println("contains2222? "
							+ (neighbor.getSuperRegion().getId() == superRegion
									.getId()));
					System.err.println("owned?: "
							+ neighbor.ownedByPlayer("neutral"));
					
					if (neighbor.getSuperRegion().getId() == superRegion
							.getId() && !neighbor.ownedByPlayer(myName)) {
						
						int neededArmies = neighbor.armiesNeededToCapture();
						System.err.println("extend Armies left: " + armiesLeft
								+ " arm Need: " + neededArmies);
						
						if (neededArmies > 0) {
							if (armiesLeft >= neededArmies) {
								placeArmiesMoves.add(new PlaceArmiesMove(
										myName, region, neededArmies));
								armiesLeft -= neededArmies;
							} 
							/*else {
								//****************************************************************************
								//in loc sa punem random punem cat mai avem unde ne trebuie msi mult
								placeArmiesMoves.add(new PlaceArmiesMove(
										myName, region, armiesLeft));
								armiesLeft = 0;
								break;
								//*****************************************************************************
							}*/
						}
					}
				}
			}
		}
		
		if (armiesLeft > 0) {
			
		}
		//
		//
		// End1
		// System.err.println("Random placement:..armies: " + armiesLeft);
		// while(armiesLeft > 0)
		// {
		// double rand = Math.random();
		// int r = (int) (rand * visibleRegions.size());
		// Region region = visibleRegions.get(r);
		//
		// if (region.ownedByPlayer(myName)) {
		// placeArmiesMoves
		// .add(new PlaceArmiesMove(myName, region, armies));
		// armiesLeft -= armies;
		// }
		// }

		return placeArmiesMoves;
	}

	@Override
	/**
	 * This method is called for at the second part of each round. This example attacks if a region has
	 * more than 6 armies on it, and transfers if it has less than 6 and a neighboring owned region.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state,
			Long timeOut) {
		ArrayList<AttackTransferMove> attackTransferMoves = new ArrayList<AttackTransferMove>();
		String myName = state.getMyPlayerName();
		int armies = 5;
		int maxTransfers = 10;
		int transfers = 0;

		for (Region fromRegion : state.getVisibleMap().getRegions()) {
			if (fromRegion.ownedByPlayer(myName)) // do an attack
			{
				Region neighbor;
				ArrayList<Region> possibleToRegions = new ArrayList<Region>();
				for (int neighborId : fromRegion.getNeighbors()) {
					neighbor = BotState.getInstance().getVisibleMap()
							.getRegion(neighborId);
					//**********************************************************************
					//pierdeam destule runde mutandu-ne pur si simplu intre noi
					//daca ne mutam armatele trebuie sa o facem eficient altfel mananca  timp
					if (!neighbor.ownedByPlayer(myName))
						possibleToRegions.add(neighbor);
				}
				while (!possibleToRegions.isEmpty()) {
					double rand = Math.random();
					int r = (int) (rand * possibleToRegions.size());
					Region toRegion = possibleToRegions.get(r);

					if (!toRegion.getPlayerName().equals(myName)
							&& fromRegion.getArmies() > 6) // do an attack
					{
						attackTransferMoves.add(new AttackTransferMove(myName,
								fromRegion, toRegion, armies));
						break;
					} else if (toRegion.getPlayerName().equals(myName)
							&& fromRegion.getArmies() > 1
							&& transfers < maxTransfers) // do a transfer
					{
						attackTransferMoves.add(new AttackTransferMove(myName,
								fromRegion, toRegion, armies));
						transfers++;
						break;
					} else
						possibleToRegions.remove(toRegion);
				}
			}
		}

		return attackTransferMoves;
	}

	public static void main(String[] args) {
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
