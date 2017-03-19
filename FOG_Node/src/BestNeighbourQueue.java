import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;


public class BestNeighbourQueue  {

	Comparator<Neighbour> comparator = new NeighbourComporator();
	PriorityQueue<Neighbour> best_Neighbour = new PriorityQueue<Neighbour>(
			10,comparator);


	public  PriorityQueue<Neighbour> getBest_Neighbour_Queue() {
		return best_Neighbour;
	}

	public synchronized boolean addNeighbour(Neighbour n){
		return best_Neighbour.offer(n);

	}
	public synchronized Neighbour peek_min_Neigbour(){
		return best_Neighbour.peek();
	}

	public synchronized boolean removeNeighbour(Neighbour n){

		return best_Neighbour.remove(n);
	}
	public synchronized Neighbour retreiveBestNeighbour(Long fnodedelay,Request r){
		ArrayList<Neighbour> removedNodes = new ArrayList<Neighbour>();
		ArrayList<String> visited_map = r.getVisitedNodes();
		Neighbour bestNeighbour = null;
		Neighbour  removed = null;
		for(int i =0;i<=best_Neighbour.size();i++) {
			String lookup_key = best_Neighbour.peek().getKey();
			if(!visited_map.contains(lookup_key)){
				
				bestNeighbour =  best_Neighbour.peek();
				if(bestNeighbour.getTCP_SOCKET()!=null){
				break;
				}else{	
					removed = best_Neighbour.poll();
					removedNodes.add(removed);
					bestNeighbour = null;
				}
				/*if(fnodedelay >= bestNeighbour.getQ_DELAY()){
					break;
				}else{
					bestNeighbour = null;
				}*/
			}else{
				removed = best_Neighbour.poll();
				removedNodes.add(removed);
			}
		}
		if(!removedNodes.isEmpty())
			best_Neighbour.addAll(removedNodes);

		if(bestNeighbour!=null)
			return bestNeighbour;
		else
			return null;
	}


}
