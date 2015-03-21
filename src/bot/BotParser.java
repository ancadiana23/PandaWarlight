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
import java.util.Scanner;

import map.Region;
import move.PlaceArmiesMove;
import move.AttackTransferMove;

public class BotParser {
	
	final Scanner scan;
	
	final Bot bot;
<<<<<<< HEAD

=======
	
	BotState currentState;
	
>>>>>>> origin/progress
	public BotParser(Bot bot)
	{
		this.scan = new Scanner(System.in);
		this.bot = bot;
	}
	
	public void run()
	{
		while(scan.hasNextLine())
		{
			String line = scan.nextLine().trim();
			if(line.length() == 0) { continue; }
			String[] parts = line.split(" ");
			if(parts[0].equals("pick_starting_region")) //pick which regions you want to start with
			{
				BotState.getInstance().setPickableStartingRegions(parts);
				Region startingRegion = bot.getStartingRegion(BotState.getInstance(), Long.valueOf(parts[1]));
				
				System.out.println(startingRegion.getId());
			}
			else if(parts.length == 3 && parts[0].equals("go")) 
			{
				//we need to do a move
				String output = "";
				if(parts[1].equals("place_armies")) 
				{
					//place armies
					ArrayList<PlaceArmiesMove> placeArmiesMoves = bot.getPlaceArmiesMoves(BotState.getInstance(),Long.valueOf(parts[2]));
					for(PlaceArmiesMove move : placeArmiesMoves)
						output = output.concat(move.getString() + ",");
				} 
				else if(parts[1].equals("attack/transfer")) 
				{
					//attack/transfer
					ArrayList<AttackTransferMove> attackTransferMoves = bot.getAttackTransferMoves(BotState.getInstance(),Long.valueOf(parts[2]));
					for(AttackTransferMove move : attackTransferMoves)
						output = output.concat(move.getString() + ",");
				}
				if(output.length() > 0)
					System.out.println(output);
				else
					System.out.println("No moves");
			} else if(parts[0].equals("settings")) {
				//update settings
				BotState.getInstance().updateSettings(parts[1], parts);
			} else if(parts[0].equals("setup_map")) {
				//initial full map is given
				BotState.getInstance().setupMap(parts);
			} else if(parts[0].equals("update_map")) {
				//all visible regions are given
				BotState.getInstance().updateMap(parts);
			} else if(parts[0].equals("opponent_moves")) {
				//all visible opponent moves are given
				BotState.getInstance().readOpponentMoves(parts);
			} else {
				System.err.printf("Unable to parse line \"%s\"\n", line);
			}
		}
	}

}
