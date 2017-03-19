import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import org.omg.CORBA.portable.InputStream;


public class periodic_update implements Runnable{
	
	fogNode f;
	Long q_delay;
	Long sleep_time;
	Hashtable<String, Neighbour> n_table = null;
	int portnumber;
	protected boolean      isStopped    = false;
	BestNeighbourQueue bn_queue_ch = null;
	public periodic_update(fogNode fn){
		f =fn;
		q_delay=f.q_delay.getQ_delay();
		n_table= f.getneighbour_Table();
		sleep_time=f.getT();
		portnumber = f.getMY_TCP();
		bn_queue_ch = f.getBn_queue();
	}
	@Override
	public void run() {
		System.out.println("Started periodic updates");
		int number = portnumber;
		while(!isStopped()){
		
			for(Entry<String, Neighbour> entry:n_table.entrySet()){
				Neighbour n = entry.getValue();
				Socket s = n.getTCP_SOCKET();
				
					if(s!=null){
						try{
						OutputStream os = s.getOutputStream();
			            OutputStreamWriter osw = new OutputStreamWriter(os);
			            BufferedWriter bw = new BufferedWriter(osw);
			            String response_time = f.q_delay.getQ_delay()+"";
			            		//f.q_delay.getQ_delay()+""; 			            
			            String sendMessage = response_time+" "+number + "\n";
			            bw.write(sendMessage);
			            bw.flush();
			            
						}
						catch (Exception exception)
				        {
				         //   exception.printStackTrace();
				        }
					}else{
						if(!n.Is_client()){
						try
						{
							String host = n.getMY_IP();
							int port = n.getTCP_PORT();
							InetAddress address = InetAddress.getByName(host);
							s = new Socket(address, port);
							n.setTCP_SOCKET(s);
							n.setIs_client(false);
							ListenFromServer listenS = new ListenFromServer(s,this.f);
							new Thread(listenS).start();
						}
						
						catch (java.net.ConnectException exception)
						{
							System.out.println("server down"+n.getMY_IP()+" port-"+n.getTCP_PORT());
							//   exception.printStackTrace();
						}
						catch (Exception e){
							
						}

					}
				
			}
			}
			/* sent periodic updates to all neighbours
			 * now putting thread in sleep for t seconds
			 */

			try {
				Thread.sleep(sleep_time);
				
			} catch (InterruptedException e) {
				 System.out.println("Thread cannot sleep!");
				e.printStackTrace();
			}

			
		}

	}
	
	private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        
    }


}
