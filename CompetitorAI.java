import bonzai.api.*;
import java.util.*;

public class CompetitorAI implements AI {
	
	private WeightComparator pathWeight = new CompetitorWeightComparator();
	int path = 0;
	int last = 0;
	
	/**
	 * You must have this function, all of the other functions in 
	 * this class are optional.
	 */
	@Override
	public void takeTurn(AIGameState state) {
		
		this.moveWizard(state);
		this.moveBlockers(state);
		this.moveCleaners(state);
		this.moveScouts(state);
		this.moveHats(state);
		
	}
	
	
	/**
	 * Move or castMagic with your Wizard
	 * @param state
	 */
	private void moveWizard(AIGameState state) {
		Wizard wizard = state.getMyWizard();
		List<Hat> neutralHats = state.getNeutralHats();
		
		if(neutralHats.size() != 0){
			if(wizard.isAdjacent(neutralHats.get(0))){
				wizard.castMagic(neutralHats.get(0));
			}
			else{
				path = wizard.getDirection(neutralHats.get(0).getLocation(), pathWeight);
				wizard.move(path);
			}
		}
		else{
			if(last == 0){
				wizard.move(Node.RIGHT);
				//last = 1;
			}
			else if(last == 1){
				wizard.move(Node.UP);
				last = 0;
			}
		}
		
		//Iterate through all visible enemy actors
				for(Actor e : state.getEnemyActors()) {
					if(wizard.canCast( e)) {
						wizard.castMagic(e);
					}
				}
				
				for(Actor e : state.getNeutralActors()) {
					if(wizard.canCast(e)) {
						wizard.castMagic(e);
					}
				}
	}
	
	/**
	 * Move, block, or unBlock with your blockers.
	 * @param state
	 */
	private void moveBlockers(AIGameState state) {
		for(Blocker blocker : state.getMyBlockers()) {
			
			// Target, and attack enemy wizards.
			//This needs to be changed to the closest wizard.
			//This code will break for more than one wizard on screen.
			for(Wizard wizard: state.getEnemyWizards()){
				Node wizardLocation = wizard.getLocation();
				int moveDirection = blocker.getDirection(wizardLocation, pathWeight);
				
				// If a blocker is on top of a wizard, block, else, move towards it.
				if(blocker.getLocation().equals(wizard.getLocation())) {
					blocker.block();
				} else {
					blocker.move(moveDirection);
				}	
			}
		}
	}
	
	/**
	 * Move or sweep with your cleaners.
	 * @param state
	 */
	private void moveCleaners(AIGameState state) {
		Wizard wizard = state.getMyWizard();
		Node wizardLocation = wizard.getLocation();
		for(Cleaner cleaner : state.getMyCleaners()) {
			int moveDirection = cleaner.getDirection(wizardLocation, pathWeight);
			
			// Move the cleaner closer to the wizard
			if(!cleaner.move(moveDirection)) {
				cleaner.shout("I am unable to move in that direction!");
			} else {
				if(!cleaner.canMove(moveDirection)) {
					//There is a blocking blocker in the direction of 'moveDirection'
				}
			}
			
			//If the cleaner can, it uses it's ability on a blocker instead of moving.
			for(Blocker enemyBlocker : state.getEnemyBlockers()) {
				if(cleaner.isAdjacent(enemyBlocker)) {
					cleaner.sweep(enemyBlocker);
				}
			}
		}
	}
	
	/**
	 * Move with your scouts.
	 * @param state
	 */
	private void moveScouts(AIGameState state) {
		for(Scout scout : state.getMyScouts()) {
			if(Math.random() > .8) { //80% chance to move randomly
				scout.doubleMove((int)(Math.random()*4), (int)(Math.random()*4));
			} else { //20% chance to move closer to your base.
				scout.doubleMove(state.getPath(scout, state.getMyBase(), pathWeight));
			}
		}
	}
	
	/**
	 * Do something with your hats!!!
	 * @param state
	 */
	private void moveHats(AIGameState state) {
		for(Hat hat : state.getMyHats()) {
			path = hat.getDirection(state.getMyBase(), pathWeight);
			hat.move(path);
		}
	}
}
