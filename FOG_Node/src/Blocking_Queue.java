import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class Blocking_Queue {

	
	public BlockingQueue<Request> block_queue = new ArrayBlockingQueue<Request>(4096);

	public synchronized BlockingQueue<Request> getblock_queue() {
		return block_queue;
	}

	/*public synchronized void setRequest_queue(BlockingQueue<Request> request_queue) {
		this.request_queue = request_queue;
	}*/
	
	
	
	
}
