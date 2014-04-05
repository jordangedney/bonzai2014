import bonzai.api.*;

import java.util.*;

public class CompetitorAI implements AI {

	private WeightComparator pathWeight = new CompetitorWeightComparator();
	int path = 0;
	int last = 0;

	int attackTeam = 0;

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
		Actor closest;
		Wizard wizard = state.getMyWizard();

		if(state.getMyMana() > 200){
			//Move towards enemy blockers and cast if can
			closest = closestEnemyBlocker(state, wizard);
			moveAndCast(state, closest);
			if(closest != null) { return; }
		}

		//Move towards neutral blockers and cast if can
		closest = closestNeutralBlocker(state, wizard);
		moveAndCast(state, closest);
		if(closest != null) { return; }

		//Get neutral hats
		closest = closestEnemyHat(state, wizard);
		moveAndCast(state, closest);
		if(closest != null) { return; }
		
		//Get neutral hats
		closest = closestNeutralHat(state, wizard);
		moveAndCast(state, closest);
		if(closest != null) { return; }

		//Get neutral scout
		closest = closestNeutralScout(state, wizard);
		moveAndCast(state, closest);
		if(closest != null) { return; }

		//Get neutral scout
		closest = closestNeutralCleaner(state, wizard);
		moveAndCast(state, closest);
		if(closest != null) { return; }


		//Wizard cant see anything... Move in random direction for as long as possible
		int myTeam = state.getMyTeamNumber();
		int players = state.getNumberOfPlayers();
		ArrayList<Node> enemyBases = new ArrayList<Node>();
		for(int i = 1; i <= players; i++){
			if(i != myTeam){
				enemyBases.add(state.getBase(i));
			}
		}

		Node enemyBase = enemyBases.get(0);
		int maxHatCount = 0;
		int hatCount = 0;
		for(Node base : enemyBases){
			ArrayList<Actor> hats = base.getActors();
			for(Actor hat : hats){
				if(hat.getType() == Actor.HAT){
					hatCount++;
				}
			}
			if(hatCount > maxHatCount){
				maxHatCount = hatCount;
				enemyBase = base;
			}
			hatCount = 0;
		}

		path = wizard.getDirection(enemyBase, pathWeight);
		wizard.move(path);

		ArrayList<Actor> enemyHat = enemyBase.getActors();
		for(Actor hat : enemyHat){
			if(hat.getType() == Actor.HAT && hat.getTeam() != myTeam){
				if(wizard.isAdjacent(hat) || wizard.getLocation() == enemyBase){
					wizard.castMagic(hat);
				}
				else{
					path = wizard.getDirection(hat.getLocation(), pathWeight);
					wizard.move(path);
				}
			}
		}
	}



	/**
	 * Move, block, or unBlock with your blockers.
	 * 
	 * @param state
	 */
	private void moveBlockers(AIGameState state) {
		for (Blocker blocker : state.getMyBlockers()) {
			// Target, and attack enemy wizards.
			// This needs to be changed to the closest wizard.
			// This code will break for more than one wizard on screen.
			if (state.getEnemyWizards().size() > 0) {
				Actor wizard = closestWizard(state, blocker);
					Node wizardLocation = wizard.getLocation();
					int moveDirection = blocker.getDirection(wizardLocation,
							pathWeight);

					// If a blocker is on top of a wizard, block, else, move
					// towards it.
					if (blocker.getLocation().equals(wizard.getLocation())) {
						if (!blocker.getLocation().equals(state.getMyWizard())) {
							blocker.block();
						}
					} else {
						blocker.move(moveDirection);
					}
				}
			
			else{
				
				ArrayList<Node> enemyBases = new ArrayList<Node>();
				for(int i = 1; i <= state.getNumberOfPlayers(); i++){
					if(i != state.getMyTeamNumber()){
						enemyBases.add(state.getBase(i));
					}
				}
				//path = blocker.getDirection(enemyBases.get( ((int)Math.random()*enemyBases.size())), pathWeight);
				path = blocker.getDirection(enemyBases.get(1) , pathWeight);
				System.out.println("path = " +path);
				if(blocker.isBlocking()){
					blocker.unBlock();
				}
				else{
					blocker.move(path);
				}
				//blocker.shout("dssdf");
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
				Actor hat = closestNeutralHat(state, scout);
				path = scout.getDirection(hat.getLocation(), pathWeight);
				scout.move(path);
				path = scout.getDirection(hat.getLocation(), pathWeight);
				scout.move(path);
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


	// Given an actor (self),  an arraylist of actors, and an int target,
	// it will return the closest actor to self, from actors of type target.
	private Actor closestEnemyHat(AIGameState state, Actor self) {		
		int pathLength = 1000;
		Actor closestActor = null;
		ArrayList<Node> shortestPath = null;
		for(Actor actor: state.getEnemyHats()){
			ArrayList<Node> newPath = state.getPath(self, actor.getLocation(), pathWeight);
			if(newPath.size() < pathLength){
				shortestPath = newPath;
				pathLength = newPath.size();
				closestActor = actor;
			}

		}
		return closestActor;
	}

	// Given an actor (self),  an arraylist of actors, and an int target,
	// it will return the closest actor to self, from actors of type target.
	private Actor closestNeutralHat(AIGameState state, Actor self) {		
		int pathLength = 1000;
		Actor closestActor = null;
		ArrayList<Node> shortestPath = null;
		for(Actor actor: state.getNeutralHats()){
			ArrayList<Node> newPath = state.getPath(self, actor.getLocation(), pathWeight);
			if(newPath.size() < pathLength){
				shortestPath = newPath;
				pathLength = newPath.size();
				closestActor = actor;
			}

		}
		return closestActor;
	}


	private Actor closestEnemyBlocker(AIGameState state, Actor self) {		
		int pathLength = 1000;
		Actor closestActor = null;
		ArrayList<Node> shortestPath = null;
		for(Actor actor: state.getEnemyBlockers()){
			ArrayList<Node> newPath = state.getPath(self, actor.getLocation(), pathWeight);
			if(newPath.size() < pathLength){
				shortestPath = newPath;
				pathLength = newPath.size();
				closestActor = actor;
			}

		}
		return closestActor;
	}

	private Actor closestNeutralBlocker(AIGameState state, Actor self) {		
		int pathLength = 1000;
		Actor closestActor = null;
		ArrayList<Node> shortestPath = null;
		for(Actor actor: state.getNeutralBlockers()){
			ArrayList<Node> newPath = state.getPath(self, actor.getLocation(), pathWeight);
			if(newPath.size() < pathLength){
				shortestPath = newPath;
				pathLength = newPath.size();
				closestActor = actor;
			}

		}
		return closestActor;
	}


	private Actor closestNeutralScout(AIGameState state, Actor self) {		
		int pathLength = 1000;
		Actor closestActor = null;
		ArrayList<Node> shortestPath = null;
		for(Actor actor: state.getNeutralScouts()){
			ArrayList<Node> newPath = state.getPath(self, actor.getLocation(), pathWeight);
			if(newPath.size() < pathLength){
				shortestPath = newPath;
				pathLength = newPath.size();
				closestActor = actor;
			}

		}
		return closestActor;
	}


	private Actor closestNeutralCleaner(AIGameState state, Actor self) {		
		int pathLength = 1000;
		Actor closestActor = null;
		ArrayList<Node> shortestPath = null;
		for(Actor actor: state.getNeutralCleaners()){
			ArrayList<Node> newPath = state.getPath(self, actor.getLocation(), pathWeight);
			if(newPath.size() < pathLength){
				shortestPath = newPath;
				pathLength = newPath.size();
				closestActor = actor;
			}

		}
		return closestActor;
	}
	
	
	private Actor closestWizard(AIGameState state, Actor self) {		
		int pathLength = 1000;
		Actor closestActor = null;
		ArrayList<Node> shortestPath = null;
		for(Actor actor: state.getEnemyWizards()){
			ArrayList<Node> newPath = state.getPath(self, actor.getLocation(), pathWeight);
			if(newPath.size() < pathLength){
				shortestPath = newPath;
				pathLength = newPath.size();
				closestActor = actor;
			}

		}
		return closestActor;
	}
	

	private void moveAndCast(AIGameState state, Actor closest){
		Wizard wizard = state.getMyWizard();
		if(closest != null){
			if(wizard.canCast(closest)){
				wizard.castMagic(closest);
			}
			else{
				path = wizard.getDirection(closest.getLocation(), pathWeight);
				wizard.move(path);
			}
			return;
		}
	}
	
	
	
}
