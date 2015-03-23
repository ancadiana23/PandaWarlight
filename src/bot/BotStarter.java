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

import map.Region;
import map.SuperRegion;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

public class BotStarter implements Bot 
{
	@Override
	/**
	 * A method that returns which region the bot would like to start on, the pickable regions are stored in the BotState.
	 * The bots are asked in turn (ABBAABBAAB) where they would like to start and return a single region each time they are asked.
	 * This method returns one random region from the given pickable regions.
	 */
	public Region getStartingRegion(BotState state, Long timeOut)
	{
		double rand = Math.random();
		int r = (int) (rand*state.getPickableStartingRegions().size());
		int regionId = state.getPickableStartingRegions().get(r).getId();
		Region startingRegion = state.getFullMap().getRegion(regionId);
		SuperRegion superRegion = startingRegion.getSuperRegion();
		LinkedList<SuperRegion> superRegToConquer = state.getSuperRegToConquer();
		
		if (!superRegToConquer.contains(superRegion)) {
			superRegion.computePriority();
			superRegToConquer.add(superRegion);
		}
		return startingRegion;
	}

	public int defend(LinkedList<Region> regions, int armiesLeft, 
			ArrayList<PlaceArmiesMove> placeArmiesMoves, String name ) {
		Region inDanger = null;
		for (Region region : regions) {
			int neededArmies = region.armiesNeededToDefend();
			
			if (neededArmies > 0)
				//we can defend
				if (neededArmies <= armiesLeft) {
					placeArmiesMoves.add(new PlaceArmiesMove(name, region, neededArmies));
					armiesLeft -= neededArmies;
				} else {
					//fortify this region later
					if (inDanger == null)
						inDanger = region;
					else if (inDanger.getPriority() < region.getPriority())
						inDanger = region;
				}
			
			if (armiesLeft <= 0)
				break;
			else if (inDanger != null) {
				placeArmiesMoves.add(new PlaceArmiesMove(name, inDanger, armiesLeft));
				armiesLeft = 0;
			}
		}
		
		return armiesLeft;
	}
	
	public int deployToExpand(LinkedList<SuperRegion> superRegions, LinkedList<Region> edgeRegions, 
			int armiesLeft, ArrayList<PlaceArmiesMove> placeArmiesMoves, String name) {
		Region notEnough = null;
		
		for (SuperRegion superRegion : superRegions) {
			for (Region region : edgeRegions) {
				LinkedList<Region> neighbors = region.getNeighbors();
				
				for (Region neighbor : neighbors)
					if (!neighbor.ownedByPlayer(name) && 
							neighbor.getSuperRegion().getId() == superRegion.getId()) {
						int neededArmies = neighbor.armiesNeededToCapture();
						
						if (neededArmies > 0)
							if (armiesLeft >= neededArmies) {
								placeArmiesMoves.add(new PlaceArmiesMove(name, region, neededArmies));
								armiesLeft -= neededArmies;
							} else {
								if(notEnough == null)
									notEnough = region;
								else if (notEnough.getPriority() < region.getPriority())
									notEnough = region;
							}
						
						if (armiesLeft <= 0)
							break;
						else if (notEnough != null) {
							placeArmiesMoves.add(new PlaceArmiesMove(name, notEnough, armiesLeft));
							armiesLeft = 0;
						}
					}
			}
		}
		
		return armiesLeft;
	}
	@Override
	/**
	 * This method is called for at first part of each round. This example puts two armies on random regions
	 * until he has no more armies left to place.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) 
	{
		
		ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		String myName = state.getMyPlayerName();
		int armies = 2;
		int armiesLeft = state.getStartingArmies();
		LinkedList<Region> visibleRegions = state.getVisibleMap().getRegions();
		
		//Determine the edges
		state.detMyEdgeTerritories();
		LinkedList<Region> edgeTerritories = state.getMyEdgeTerritories();
		boolean visibleEnemies = false;
		
		//See if there are visible enemies
		for (Region region : edgeTerritories) 
			if (region.getEnemyNeighbors(state.getOpponentPlayerName()) != 0) {
				visibleEnemies = true;
				break;
			}
		
		//Debug start
		System.err.println("Before sort:");
		for (Region region : edgeTerritories)
			System.err.print(region.getPriority() + " ");
		System.err.println();
		
		state.sortTerritories(edgeTerritories);
		
		System.err.println("After sort:");
		for (Region region : edgeTerritories)
			System.err.print(region.getPriority() + " ");
		System.err.println();
		
		//Debug end
		
		if (visibleEnemies) {
			System.err.println("Enemies!");
			armiesLeft = defend(edgeTerritories, armiesLeft, placeArmiesMoves, myName);
			
			if (armiesLeft <= 0)
				return placeArmiesMoves;
		}
		else {
			LinkedList<SuperRegion> superRegionsToConquer = state.getSuperRegToConquer();
			
			//See if some of the targets are captured and remove them
			for (int i = 0; i < superRegionsToConquer.size(); i++) {

				if (superRegionsToConquer.get(i).ownedByPlayer().equals(myName)) {
					superRegionsToConquer.remove(i);
					i--;
				}
			}
			
			// If we're done capturing all the planned SuperRegions, add new targets
			if (superRegionsToConquer.isEmpty()) {
				for (Region region : edgeTerritories) {
					LinkedList<Region> neighbors = region.getNeighbors();
					
					for (Region neighbor : neighbors) {
						SuperRegion superRegion = neighbor.getSuperRegion();
						
						if (!superRegion.ownedByPlayer().equals(myName)) {
							if (!superRegionsToConquer.contains(superRegion)) {
								superRegion.computePriority();
								superRegionsToConquer.add(superRegion);
							}
						}
					}
				}
			}
			//Debug 2
			System.err.println("Super before sort:");
			for (SuperRegion superRegion : superRegionsToConquer)
				System.err.print("prior: " + superRegion.getPriority() + "notC: " 
			+ superRegion.regionsNotConquered());
			System.err.println();
			
			state.sortTerritories(superRegionsToConquer);
			
			//Debug 2
			System.err.println("Super after sort:");
			for (SuperRegion superRegion : superRegionsToConquer)
				System.err.print("prior: " + superRegion.getPriority() + "notC: " 
			+ superRegion.regionsNotConquered());
			System.err.println();
			
			//End debug2
			armiesLeft = deployToExpand(superRegionsToConquer, edgeTerritories, armiesLeft, placeArmiesMoves, myName);
			if (armiesLeft <= 0)
				return placeArmiesMoves;
			
		}
		
		//Their random 
		System.err.println("Raaandom");
		while(armiesLeft > 0)
		{
			double rand = Math.random();
			int r = (int) (rand*visibleRegions.size());
			Region region = visibleRegions.get(r);
			
			if(region.ownedByPlayer(myName))
			{
				placeArmiesMoves.add(new PlaceArmiesMove(myName, region, armies));
				armiesLeft -= armies;
			}
		}
		
		return placeArmiesMoves;
	}

	private ArrayList<AttackTransferMove> getIdleArmiesTransferMoves(BotState state, long TimeOut)
	 {
	  ArrayList<AttackTransferMove> res = new ArrayList<AttackTransferMove>();
	  LinkedList<Region> regions = state.getMyInnerTerritories();
	  
	  for(Region reg: regions)
	  {
	   ArrayList<Region> edgeNeighbors = new ArrayList<Region>();
	   LinkedList<Region> neighbors = reg.getNeighbors();
	   
	   for(Region neighbor: neighbors)
	    if(state.getMyEdgeTerritories().contains(neighbor))
	     edgeNeighbors.add(neighbor);
	   
	   if(!edgeNeighbors.isEmpty())
	   {
	    Collections.sort(edgeNeighbors);
	    res.add(new AttackTransferMove(state.getMyPlayerName(), reg, edgeNeighbors.get(0), reg.getArmies()-1));
	   }
	   else
	   {
	    ArrayList<Region> temp = new ArrayList<Region>();
	    while(!edgeNeighbors.isEmpty())
	    {
	     LinkedList<Region> tempNeighbors  = edgeNeighbors.get(0).getNeighbors();
	     for(Region next: tempNeighbors)
	      if(state.getMyEdgeTerritories().contains(next))
	       temp.add(next);
	      else
	       edgeNeighbors.add(next); 
	    }
	    
	    Collections.sort(edgeNeighbors);
	    res.add(new AttackTransferMove(state.getMyPlayerName(), reg, edgeNeighbors.get(0), reg.getArmies()-1));
	   }
	  }
	  
	  return res;
	 }
	@Override
	/**
	 * This method is called for at the second part of each round. This example attacks if a region has
	 * more than 6 armies on it, and transfers if it has less than 6 and a neighboring owned region.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) 
	{
		ArrayList<AttackTransferMove> attackTransferMoves = new ArrayList<AttackTransferMove>();
		String myName = state.getMyPlayerName();
		int armies = 5;
		int maxTransfers = 10;
		int transfers = 0;
		//attackTransferMoves = getIdleArmiesTransferMoves(state, timeOut);
		
		
		for(Region fromRegion : state.getVisibleMap().getRegions())
		{
			if(fromRegion.ownedByPlayer(myName)) //do an attack
			{
				ArrayList<Region> possibleToRegions = new ArrayList<Region>();
				possibleToRegions.addAll(fromRegion.getNeighbors());
				
				while(!possibleToRegions.isEmpty())
				{
					double rand = Math.random();
					int r = (int) (rand*possibleToRegions.size());
					Region toRegion = possibleToRegions.get(r);
					
					if(!toRegion.getPlayerName().equals(myName) && fromRegion.getArmies() > 6) //do an attack
					{
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armies));
						break;
					}
					else if(toRegion.getPlayerName().equals(myName) && fromRegion.getArmies() > 1
								&& transfers < maxTransfers) //do a transfer
					{
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, armies));
						transfers++;
						break;
					}
					else
						possibleToRegions.remove(toRegion);
				}
			}
		}
		
		return attackTransferMoves;
	}

	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}

}
