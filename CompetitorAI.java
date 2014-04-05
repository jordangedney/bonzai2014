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
		
		if(state.getNeutralHats().size() != 0){ //Move towards neutral hats and cast if can
			List<Hat> neutralHats = state.getNeutralHats();
			
			if(wizard.isAdjacent(neutralHats.get(0))){
				wizard.castMagic(neutralHats.get(0));
			}
			else{
				path = wizard.getDirection(neutralHats.get(0).getLocation(), pathWeight);
				wizard.move(path);
			}
			return;
		}
		else if(state.getNeutralScouts().size() != 0){ //Move towards neutral scouts and cast if can
			List<Scout> scouts = state.getNeutralScouts();
			
			if(wizard.isAdjacent(scouts.get(0))){
				wizard.castMagic(scouts.get(0));
			}
			else{
				path = wizard.getDirection(scouts.get(0).getLocation(), pathWeight);
				wizard.move(path);
			}
			return;
		}
		else if(state.getNeutralBlockers().size() != 0){ //Move towards neutral blockers and cast if can
			List<Blocker> blockers = state.getNeutralBlockers();
			
			if(wizard.isAdjacent(blockers.get(0))){
				wizard.castMagic(blockers.get(0));
			}
			else{
				path = wizard.getDirection(blockers.get(0).getLocation(), pathWeight);
				wizard.move(path);
			}
			return;
		}
		else if(state.getNeutralCleaners().size() != 0){ //Move towards neutral cleaners and cast if can
			List<Cleaner> cleaners = state.getNeutralCleaners();
			
			if(wizard.isAdjacent(cleaners.get(0))){
				wizard.castMagic(cleaners.get(0));
			}
			else{
				path = wizard.getDirection(cleaners.get(0).getLocation(), pathWeight);
				wizard.move(path);
			}
			return;
		}
		else{ //Wizard cant see anything... Move in random direction for as long as possible
			int myTeam = state.getMyTeamNumber();
			int players = state.getNumberOfPlayers();
			int attackTeam = 0;
			for(int i = 1; i <= players; i++){
				if(i != myTeam){
					attackTeam = i;
					break;
				}
			}

			Node enemyBase = state.getBase(attackTeam);
			path = wizard.getDirection(enemyBase, pathWeight);
			wizard.move(path);
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
					if(!blocker.getLocation().equals(state.getMyWizard())){
						blocker.block();	
					}
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
			
			if(state.getNeutralHats().size() != 0){ //Check to see if scout can see a hat, if so move towards it and stay there
				ArrayList<Node> path = closestNeutralTarget(state, scout, Actor.HAT);
				scout.doubleMove(path);
			}
			
			else{ //Move randomly
				scout.doubleMove((int)(Math.random()*4), (int)(Math.random()*4));
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
	
	
	
	// Given an actor (self) and an arraylist of actors, it will return the
	// path of the closest actor to self, from actors.
	private ArrayList<Node> closest(AIGameState state, ArrayList<Actor> actorSet, Actor self) {		
		int pathLength = 1000;
		ArrayList<Node> shortestPath = null;
		for(Actor actor: actorSet){
			if(!actor.equals(self)){
				ArrayList<Node> newPath = state.getPath(self, actor.getLocation(), pathWeight);
				if(newPath.size() < pathLength){
					shortestPath = newPath;
					pathLength = newPath.size();
				}
			}
		}
		return shortestPath;
	}
	
	
	private ArrayList<Node> closestNeutralTarget(AIGameState state, Actor self, int target) {		
		int pathLength = 1000;
		ArrayList<Node> shortestPath = null;
		for(Actor actor: state.getNeutralActors()){
			if(!actor.equals(self)){
				if (actor.getID() == target){
					ArrayList<Node> newPath = state.getPath(self, actor.getLocation(), pathWeight);
					if(newPath.size() < pathLength){
						shortestPath = newPath;
						pathLength = newPath.size();
					}
				}
			}
		}
		return shortestPath;
	}
}
