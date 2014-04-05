import java.util.*;

import bonzai.api.*;

public class CompetitorWeightComparator implements WeightComparator
{
	public CompetitorWeightComparator() {
		super();
	}
	
	@Override
	public double compare(Node arg0) {
		List<Actor> actors = arg0.getActors();
		for(Actor a : actors){
			if(a instanceof Blocker)
				return 99;
		}
		return 0;
	}
}
