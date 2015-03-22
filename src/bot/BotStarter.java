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

import map.Map;
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

		LinkedList<SuperRegion> superRegionsToCapture = state.getSuperRegionsToCapture();
		if (superRegionsToCapture == null)
			superRegionsToCapture = new LinkedList<SuperRegion>();
		SuperRegion startingSuperRegion = startingRegion.getSuperRegion();

		if (!superRegionsToCapture.contains(startingSuperRegion)) {
			System.err.println("superReg de inceput: " + startingSuperRegion.getId());
			superRegionsToCapture.add(startingSuperRegion);
			startingSuperRegion.computePriority(state.getOpponentPlayerName());
		}
		return startingRegion;
	}
	
	 private ArrayList<AttackTransferMove> getIdleArmiesTransferMoves(BotState state, long TimeOut)
	 {
	  ArrayList<AttackTransferMove> res = new ArrayList<AttackTransferMove>();
	  LinkedList<Region> regions = state.getMyInnerTerritory();
	  
	  for(Region reg: regions)
	  {
	   ArrayList<Region> edgeNeighbors = new ArrayList<Region>();
	   ArrayList<Region> neighbors = reg.getNeighbors();
	   
	   for(Region neighbor: neighbors)
	    if(state.getMyEdgeRegions().contains(neighbor))
	     edgeNeighbors.add(neighbor);
	   
	   if(!edgeNeighbors.isEmpty())
	   {
	    Collections.sort(edgeNeighbors);
	    res.add(new AttackTransferMove(state.getMyPlayerName(), reg, edgeNeighbors.get(o), reg.getArmies()-1));
	   }
	   else
	   {
	    ArrayList<Region> temp = new ArrayList<Region>();
	    while(!edgeNeighbors.isEmpty())
	    {
	     ArrayList<Region> tempNeighbors  = edgeNeighbors.get(0).getNeighbors();
	     for(Region next: tempNeighbors)
	      if(state.getMyEdgeRegions().contains(next))
	       temp.add(next);
	      else
	       edgeNeighbors.add(next); 
	    }
	    
	    Collections.sort(edgeNeighbors);
	    res.add(new AttackTransferMove(state.getMyPlayerName(), reg, edgeNeighbors.get(o), reg.getArmies()-1));
	   }
	  }
	  
	  return res;
	 }

	@Override
	/**
	 * This method is called for at first part of each round. This example puts two armies on random regions
	 * until he has no more armies left to place.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) {

		ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		String myName = state.getMyPlayerName();
		int armiesLeft = state.getStartingArmies();
		//LinkedList<Region> visibleRegions = state.getVisibleMap().getRegions();
		 Map visibleMap = state.getVisibleMap();
		// Start1
		state.detMyEdgeRegions();
		LinkedList<Region> myEdgeRegions = state.getMyEdgeRegions();
		boolean visibleEnemies = false;

		for (Region reg : myEdgeRegions)
			if (reg.getEnemyNeighbors(state.getOpponentPlayerName()) != 0) {
				visibleEnemies = true;
				break;
			}

		//Collections.sort(myEdgeRegions);
		state.sortRegions(myEdgeRegions);
		
		if (visibleEnemies) {
			System.err.println("Visible enemies!");
			Region inDanger = null;
			// apply strategy for vis enemies
			for (Region region : myEdgeRegions) {
				int neededArmies = region.armiesNeededToDefend();
				if (neededArmies > 0 && neededArmies <= armiesLeft) {
					// System.err.println("Needed armies: " + neededArmies);
					placeArmiesMoves.add(new PlaceArmiesMove(myName, region, neededArmies));
					armiesLeft -= neededArmies;
					System.err.println("ArmiesLeftaftermove" + armiesLeft);
				} else if (neededArmies > armiesLeft)
					if (inDanger == null)
						inDanger = region;
				if (armiesLeft <= 0)
					return placeArmiesMoves;
			}
			// regiunea cu prioritatea cea mai mare pentru care nu avem destule
			// armate
			// punem pe ea tot ce avem ca in 2-3 runde sa fie in siguranta
			if (inDanger != null) {
				placeArmiesMoves.add(new PlaceArmiesMove(myName, inDanger, armiesLeft));
				return placeArmiesMoves;
			}
		}

		// //extend
		// //Get the SuperRegions we want to capture first
		LinkedList<SuperRegion> superRegionsToCapture = state.getSuperRegionsToCapture();
		//
		// //See if some of the targets are captured and remove them
		for (int i = 0; i < superRegionsToCapture.size(); i++) {

			if (superRegionsToCapture.get(i).ownedByPlayer().equals(myName)) {
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
					neighbor = visibleMap.getRegion(neighborId);
					SuperRegion superRegion = neighbor.getSuperRegion();
					if (!superRegion.ownedByPlayer().equals(myName)) {
						if (!superRegionsToCapture.contains(superRegion)) {
							superRegion.computePriority(state.getOpponentPlayerName());
							superRegionsToCapture.add(superRegion);
						}
						
					}
				}
			}
		}
		System.err.println("before sort");
		for (SuperRegion superRegion : superRegionsToCapture)
			System.err.println("superReg : "  + superRegion.getId() +
					" prio :" + superRegion.getPriority() + "uncap: " + superRegion.regionsNotConquered());
		// Sort the targets by priority (biggest priority first)
		// and by size if priorities are equal (smallest SuperRegions first)
		Collections.sort(superRegionsToCapture);
		
		// //This is...SPARTAAAAAAAAAAAAAAAAAAAA
		//
		// //conquer
		// //slash
		// //kill
		
//		for (Pair<SuperRegion> pair : superRegionsToCapture)
//			System.err.println("sup: " + pair.getRegion().getId() + "  prior: "
//					+ pair.getPriority());
		System.err.println("After sort:");
		for (SuperRegion superRegion : superRegionsToCapture)
			System.err.println("superReg : "  + superRegion.getId() +
					" prio :" + superRegion.getPriority() + "uncap: " + superRegion.regionsNotConquered());
		
		
		Region notEnough=null;
		for (SuperRegion superRegion : superRegionsToCapture) {
			System.err.println("superreg to deploy: " + superRegion.getId());
			
			for (Region reg : superRegion.getSubRegions())
				System.err.println("subreg: " + reg.getId());
			
			for (Region region : myEdgeRegions) {
				System.err.println("region : " + region.getId());
				Region neighbor;
				for (Integer neighborId : region.getNeighbors()) {
					neighbor = visibleMap.getRegion(neighborId);
					//System.err.println("neigh: " + neighbor.getId() + "own: "+ neighbor.getPlayerName());
					//System.err.println("contains? "	+ superRegion.getSubRegions().contains(neighbor));// just...why?

					//System.err.println("contains2222? "+ (neighbor.getSuperRegion().getId() == superRegion.getId()));
					//System.err.println("owned?: "+ neighbor.ownedByPlayer("neutral"));
					
					if (neighbor.getSuperRegion().getId() == superRegion.getId() && !neighbor.ownedByPlayer(myName)) {
						
						int neededArmies = neighbor.armiesNeededToCapture();
						System.err.println("extend Armies left: " + armiesLeft+ " arm Need: " + neededArmies);
						
						if (neededArmies > 0) {
							if (armiesLeft >= neededArmies) {
								placeArmiesMoves.add(new PlaceArmiesMove(myName, region,neededArmies));
								armiesLeft -= neededArmies;
							} else if(notEnough==null){
								notEnough=region;
							} 
						}
					}
				}
			}
		}
		if(notEnough!=null)
		{
			placeArmiesMoves.add(new PlaceArmiesMove(myName, notEnough,armiesLeft));
			return placeArmiesMoves;
		}

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
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) {
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
					neighbor = state.getVisibleMap().getRegion(neighborId);
					// **********************************************************************
					// pierdeam destule runde mutandu-ne pur si simplu intre noi
					// daca ne mutam armatele trebuie sa o facem eficient altfel
					// mananca timp
					if (!neighbor.ownedByPlayer(myName))
						possibleToRegions.add(neighbor);
				}
				while (!possibleToRegions.isEmpty()) {
					double rand = Math.random();
					int r = (int) (rand * possibleToRegions.size());
					Region toRegion = possibleToRegions.get(r);

					if (!toRegion.getPlayerName().equals(myName) && fromRegion.getArmies() > 6) // do
																								// an
																								// attack
					{
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion,
								toRegion, armies));
						break;
					} else if (toRegion.getPlayerName().equals(myName)
							&& fromRegion.getArmies() > 1 && transfers < maxTransfers) // do
																						// a
																						// transfer
					{
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion,
								toRegion, armies));
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
