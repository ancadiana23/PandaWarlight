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

public class BotStarter implements Bot {
	@Override
	/**
	 * A method that returns which region the bot would like to start on, the pickable regions are stored in the BotState.
	 * The bots are asked in turn (ABBAABBAAB) where they would like to start and return a single region each time they are asked.
	 * This method returns one random region from the given pickable regions.
	 */
	public Region getStartingRegion(BotState state, Long timeOut) {
		ArrayList<Region> pickableStartingRegions = state
				.getPickableStartingRegions();

		// Pick the region from the superRegion with the highest priority
		Region max = pickableStartingRegions.get(0);
		max.getSuperRegion().computePriority();

		for (int i = 1; i < pickableStartingRegions.size(); i++) {
			Region region = pickableStartingRegions.get(i);
			SuperRegion superRegion = region.getSuperRegion();
			superRegion.computePriority();
			Float maxPriority = max.getSuperRegion().getPriority();
			Float priority = superRegion.getPriority();

			if (priority > maxPriority)
				max = region;
			else if (priority.equals(maxPriority)) {
				int maxNoRegions = max.getSuperRegion().getSubRegions().size();
				int noRegions = superRegion.getSubRegions().size();

				if (maxNoRegions > noRegions)
					max = region;
			}
		}

		int regionId = max.getId();
		Region startingRegion = state.getFullMap().getRegion(regionId);
		SuperRegion superRegion = startingRegion.getSuperRegion();
		LinkedList<SuperRegion> superRegToConquer = state
				.getSuperRegToConquer();

		// Add the SuperRegion to the list of SuperRegions we want
		// to conquer
		if (!superRegToConquer.contains(superRegion)) {
			superRegion.computePriority();
			superRegToConquer.add(superRegion);
		}
		return startingRegion;
	}

	/**
	 * Deploys armies to the regions which are neighboring enemy territories
	 * 
	 * @param regions
	 * @param armiesLeft
	 * @param placeArmiesMoves
	 * @param name
	 * @return
	 */
	public int defend(LinkedList<Region> regions, int armiesLeft,
			ArrayList<PlaceArmiesMove> placeArmiesMoves, String name) {

		// if we have armies left after protecting what we can surely protect,
		// put them in this region to fortify it
		Region inDanger = null;

		for (Region region : regions) {
			int neededArmies = region.armiesNeededToDefend();

			if (neededArmies > 0)
				// we can defend
				if (neededArmies <= armiesLeft) {
					placeArmiesMoves.add(new PlaceArmiesMove(name, region,
							neededArmies));
					region.setArmies(neededArmies + region.getArmies());
					region.setArmiesForDefense(neededArmies);
					armiesLeft -= neededArmies;
				} else {
					// fortify this region later
					if (inDanger == null)
						inDanger = region;
					else if (inDanger.getPriority() < region.getPriority())
						inDanger = region;
				}

			if (armiesLeft <= 0)
				break;
			else if (inDanger != null) {
				placeArmiesMoves.add(new PlaceArmiesMove(name, inDanger,
						armiesLeft));
				inDanger.setArmies(armiesLeft + inDanger.getArmies());
				armiesLeft = 0;
			}
		}

		return armiesLeft;
	}

	/**
	 * Deploys armies in order to expand. It deploys armies based on which
	 * regions are in the SuperRegions' priorities.
	 * 
	 * @param superRegions
	 * @param edgeRegions
	 * @param armiesLeft
	 * @param placeArmiesMoves
	 * @param state
	 * @return
	 */
	public int deployToExpand(LinkedList<SuperRegion> superRegions,
			LinkedList<Region> edgeRegions, int armiesLeft,
			ArrayList<PlaceArmiesMove> placeArmiesMoves, BotState state) {
		Region notEnough = null;
		String myName = state.getMyPlayerName();

		// Clear the list of attacks/tranfers (they were the attacks from the
		// last round)
		state.clearAttackTransferMove();

		// Deploy the armies in the regions which belong to the SuperRegions
		// with
		// the biggest priorities
		for (SuperRegion superRegion : superRegions) {
			for (Region region : edgeRegions) {
				LinkedList<Region> neighbors = region.getNeighbors();

				// Search through each of our edge regions for the neighbors we
				// can conquer
				// and deploy to the current region the necessary amount of
				// armies needed to conquer
				// those neighbors.
				for (Region neighbor : neighbors)
					if (!neighbor.ownedByPlayer(myName)
							&& neighbor.getSuperRegion().getId() == superRegion
									.getId()) {
						int neededArmies = neighbor.armiesNeededToCapture();
						int toDeploy = neededArmies - region.getArmies() + 1;

						if (region.getArmiesForDefense() > 0)
							toDeploy += region.getArmiesForDefense();

						if (toDeploy > 0)
							if (armiesLeft >= neededArmies) {
								placeArmiesMoves.add(new PlaceArmiesMove(
										myName, region, toDeploy));
								region.setArmies(toDeploy + region.getArmies());
								state.addAttackTransferMove(new AttackTransferMove(
										myName, region, neighbor, neededArmies));
								armiesLeft -= toDeploy;
							}
							// If we will have armies left, this will be the
							// region in which we will
							// add the rest of the armies in order to conquer
							// their neighbor later
							else {
								if (notEnough == null)
									notEnough = region;
								else if (notEnough.getPriority() < region
										.getPriority())
									notEnough = region;
							}

						if (armiesLeft <= 0)
							break;
						else if (notEnough != null) {
							placeArmiesMoves.add(new PlaceArmiesMove(myName,
									notEnough, armiesLeft));
							notEnough.setArmies(armiesLeft
									+ notEnough.getArmies());
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
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state,
			Long timeOut) {
		ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		String myName = state.getMyPlayerName();
		int armies = 2;
		int armiesLeft = state.getStartingArmies();

		// Determine the edges
		state.detMyEdgeTerritories();
		LinkedList<Region> edgeTerritories = state.getMyEdgeTerritories();
		boolean visibleEnemies = false;

		// See if there are visible enemies
		for (Region region : edgeTerritories)
			if (region.getEnemyNeighbors(state.getOpponentPlayerName()) != 0) {
				visibleEnemies = true;
				break;
			}

		// Sort our edge territories by their priorities
		state.sortTerritories(edgeTerritories);

		// If there are enemies neighboring our territories,
		// defend our territories
		if (visibleEnemies) {
			armiesLeft = defend(edgeTerritories, armiesLeft, placeArmiesMoves,
					myName);

			if (armiesLeft <= 0)
				return placeArmiesMoves;
		}

		// Get the list of the SuperRegions we want to conquer
		LinkedList<SuperRegion> superRegionsToConquer = state
				.getSuperRegToConquer();

		// See if some of the targets are captured and remove them
		for (int i = 0; i < superRegionsToConquer.size(); i++) {

			SuperRegion superRegion = superRegionsToConquer.get(i);
			SuperRegion superReg = state.getFullMap().getSuperRegion(
					superRegion.getId());

			if (superReg.ownedByPlayer().equals(myName)) {
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

		// Sort the SuperRegions by priority (the first we want to conquer
		// is first)
		state.sortTerritories(superRegionsToConquer);

		// Deploy armies to the regions from which we want to expand
		armiesLeft = deployToExpand(superRegionsToConquer, edgeTerritories,
				armiesLeft, placeArmiesMoves, state);
		if (armiesLeft <= 0)
			return placeArmiesMoves;

		// Random placement, but chooses from our edges
		while (armiesLeft > 0) {
			double rand = Math.random();
			int r = (int) (rand * edgeTerritories.size());
			Region region = edgeTerritories.get(r);

			if (region.ownedByPlayer(myName)) {
				placeArmiesMoves
						.add(new PlaceArmiesMove(myName, region, armies));
				region.setArmies(armies + region.getArmies());
				armiesLeft -= armies;
			}
		}

		return placeArmiesMoves;
	}

	 /**
	  * The regions that have no neighboring enemies or neutral lands have no use for their armies.
	  * This method uses a breatdh-first search to determine where these idle armies should be
	  * transfered to.
	  * @param state
	  * @return
	  */
	 private ArrayList<AttackTransferMove> getIdleArmiesTransferMoves(
	   BotState state) {
	  
	  ArrayList<AttackTransferMove> res = new ArrayList<AttackTransferMove>();
	  LinkedList<Region> innerRegions = state.getMyInnerTerritories();
	  LinkedList<Region> edgeTerritories = state.getMyEdgeTerritories();

	  //We process every region that has idle armies
	  for (Region innerRegion : innerRegions)
	   if (innerRegion.getArmies() > 1) {

	    LinkedList<Region> edgeNeighbors = new LinkedList<Region>();
	    LinkedList<Region> neighbors = innerRegion.getNeighbors();
	    
	    //We look for edges we could move into right now
	    for (Region neighbor : neighbors)
	     if (edgeTerritories.contains(neighbor))
	      edgeNeighbors.add(neighbor);

	    //If we find any, we move into the highest priority one
	    if (!edgeNeighbors.isEmpty()) {
	     state.sortTerritories(edgeNeighbors);
	     res.add(new AttackTransferMove(state.getMyPlayerName(),
	       innerRegion, edgeNeighbors.get(0), innerRegion
	         .getArmies() - 1));
	    } else {
	     //We use an auxiliary list: when we reach an edge, this list determines what
	     //neighbor we found it from
	     LinkedList<Region> from = new LinkedList<Region>();
	     from.addAll(neighbors);
	     
	     //We reuse edgeNeighbors as a queue
	     ArrayList<Region> temp = new ArrayList<Region>();
	     edgeNeighbors = neighbors;
	     
	     //We start the BFS
	     int curr = 0;
	     while (curr < edgeNeighbors.size()) {
	      LinkedList<Region> tempNeighbors = edgeNeighbors.get(
	        curr).getNeighbors();
	      for (Region next : tempNeighbors)
	       //Check if we found an edge
	       if (edgeTerritories.contains(next)) {
	        temp.add(next);
	       }
	       else {
	        if (!edgeNeighbors.contains(next)) {
	         from.add(from.get(curr));
	         edgeNeighbors.add(next);
	        }
	       }
	      ++curr;
	     }

	     //We sort the edges we found to get the highest priority one,
	     //then determine how we got to that edge and transfer everything to that particular region
	     state.sortTerritories(temp);
	     for (int i = 0; i < edgeNeighbors.size(); ++i)
	      if (edgeNeighbors.get(i) == temp.get(0)) {
	       res.add(new AttackTransferMove(state
	         .getMyPlayerName(), innerRegion, from
	         .get(i), innerRegion.getArmies() - 1));
	       break;
	      }
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
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state,
			Long timeOut) {
		ArrayList<AttackTransferMove> attackTransferMoves = getIdleArmiesTransferMoves(state);
		attackTransferMoves.addAll(state.getAttackTransferMoves());

		String myName = state.getMyPlayerName();
		LinkedList<Region> edgeRegions = state.getMyEdgeTerritories();

		state.sortTerritories(edgeRegions);
		Collections.reverse(edgeRegions);
		LinkedList<SuperRegion> superRegionsToConquer = state
				.getSuperRegToConquer();

		for (Region fromRegion : edgeRegions) {
			ArrayList<Region> possibleToRegions = new ArrayList<Region>();
			possibleToRegions.addAll(fromRegion.getNeighbors());
			ArrayList<Region> enemiesInOurSuperRegion = new ArrayList<Region>();
			ArrayList<Region> enemiesNotInOurSuperRegion = new ArrayList<Region>();

			for (int i = 0; i < possibleToRegions.size(); i++) {
				Region toRegion = possibleToRegions.get(i);

				if (!toRegion.getPlayerName().equals(myName)
						&& fromRegion.getArmies() > toRegion
								.armiesNeededToCapture()) {

					int priority = toRegion.getArmies();

					toRegion.setPriority(priority);

					if (superRegionsToConquer.contains(toRegion
							.getSuperRegion()))
						enemiesInOurSuperRegion.add(toRegion);
					else
						enemiesNotInOurSuperRegion.add(toRegion);
				}

			}

			Collections.sort(enemiesInOurSuperRegion);
			Collections.reverse(enemiesInOurSuperRegion);
			Collections.sort(enemiesNotInOurSuperRegion);
			Collections.reverse(enemiesNotInOurSuperRegion);
			ArrayList<Region> enemyRegions = new ArrayList<Region>();

			enemyRegions.addAll(enemiesInOurSuperRegion);
			enemyRegions.addAll(enemiesNotInOurSuperRegion);

			if (enemyRegions.size() == 1) {
				attackTransferMoves.add(new AttackTransferMove(myName,
						fromRegion, enemyRegions.get(0),
						fromRegion.getArmies() - 1));
				continue;
			}

			for (Region enemyRegion : enemyRegions) {
				int myArmies = fromRegion.getArmies()
						- fromRegion.armiesNeededToDefend(possibleToRegions)
						- 1;

				if (myArmies <= 0)
					break;
				int armiesNeededToAttack = enemyRegion.armiesNeededToCapture();

				if (myArmies >= armiesNeededToAttack) {
					attackTransferMoves.add(new AttackTransferMove(myName,
							fromRegion, enemyRegion, armiesNeededToAttack));
					possibleToRegions.remove(enemyRegion);
					fromRegion.setArmies(fromRegion.getArmies()
							- armiesNeededToAttack);
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
