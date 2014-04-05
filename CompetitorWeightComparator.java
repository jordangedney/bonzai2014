import java.util.*;

import bonzai.api.*;

public class CompetitorWeightComparator implements WeightComparator
{
	public CompetitorWeightComparator() {
		super();
	}
	
	@Override
	public double compare(Node arg0) {
		
		if(arg0.isPassable() == false){
			return 99;
		}
		
		return 0;
	}
}
