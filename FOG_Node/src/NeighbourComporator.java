
import java.util.Comparator;
public class NeighbourComporator implements Comparator<Neighbour> {

	

	@Override
	public int compare(Neighbour n1, Neighbour n2) {
		if(n1.getQ_DELAY()>n2.getQ_DELAY()){
			return 1;
		}else if(n1.getQ_DELAY()<n2.getQ_DELAY()){
			return -1;
		}else{
		
		return 0;
		}
		}

}
