import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;


public class ListenFromServer implements Runnable{

	public Socket socket;
	protected boolean      isStopped    = false;
	fogNode f = null;
	Hashtable<String, Neighbour> nb_table = null;
	BestNeighbourQueue bn_queue_ch = null;
	Q_Delay q_delay_t =null;
	BlockingQueue<Request> block_queue_t = null;

	public  ListenFromServer(Socket st,fogNode fn) {
		// TODO Auto-generated constructor stub
		this.socket = st;
		this.f = fn;
		nb_table = f.getneighbour_Table();
		q_delay_t = f.q_delay;
		bn_queue_ch = f.getBn_queue();
		block_queue_t = f.getRequest_queue().getblock_queue();
	}  
	@Override
	public void run() {
		
			try {
				InputStream is = socket.getInputStream();
				String serverip = socket.getInetAddress().toString();
				serverip =serverip.trim().substring(1);
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String message;
				while((message=br.readLine())!=null){
				String message_break[] = message.split(" ");
				
				if(message_break.length<3){

					//case of periodic updates
					Long response_time = Long.parseLong(message_break[0]);
					String neigbhour_port_number = message_break[1];
					String s = serverip+":"+neigbhour_port_number;

					if(nb_table.containsKey(s)){
						//	System.out.println("entered");
						Neighbour retrieved = null; 
						retrieved = nb_table.get(s);

						retrieved.setQ_DELAY(response_time);
						bn_queue_ch.removeNeighbour(retrieved);
						bn_queue_ch.addNeighbour(retrieved);

						//for(int i=0;i<bn_queue_ch.getBest_Neighbour_Queue().size();i++){
						f.log_write.write(message+"\n");
						f.log_write.flush();
						//}


					}
				} else {
					f.log_write.write("Message received from the server : " +message+"\n");
					
					f.log_write.flush();
					//case of forwarded request from neighbour
					Request fwded_request = new Request(message);
					fwded_request.setVisitedNodesFromMsg();
					if(fwded_request.getForwardLimit()<=fwded_request.getCurrentOffset()){
						//Request forward limit reched so send to clouds
						String cloud_send = message+" "+f.getMyKey()+" "+"Forward";
						fwded_request.setMessage(cloud_send);
						f.cl.cloudQueue.offer(fwded_request);
						System.out.println("hand over to cloud" + fwded_request.getMessage());
					} else{
						// check if request can be processed

						Long current_q_delay = q_delay_t.getQ_delay();
						current_q_delay = current_q_delay+fwded_request.getProcessing_time();
						if(f.Max_Response_Time>=current_q_delay){

							try {
								fwded_request.setMessage(fwded_request.getMessage()+" "+f.getMyKey()+" Processed!");
								block_queue_t.put(fwded_request);
								f.q_delay.setQ_delay(current_q_delay);
								System.out.println("Processed-->"+fwded_request.getMessage());
							} catch (InterruptedException e) {

								e.printStackTrace();
							}	

						} else{
							// forward to best neighbour

							
							//get best neighbour and fwd request to it
							Neighbour best_neighbour_fwd = bn_queue_ch.retreiveBestNeighbour(f.q_delay.getQ_delay(),fwded_request);

							//in case if no best neghbour forward to cloud
							if(best_neighbour_fwd==null){
								// No best neighbour found so send to cloud
								String cloud_send = message+" "+f.getMyKey()+" "+"Forward";
								fwded_request.setMessage(cloud_send);
								f.cl.cloudQueue.offer(fwded_request);
								System.out.println(" best neigh null fwd to cloud "+fwded_request.getMessage());
							}else {
								//forward to best neighbour
								System.out.println("more than queue delay! forwarding to best neigh "+best_neighbour_fwd.TCP_PORT+"->"+ message);
								fwdRequestStringBuilder send = new fwdRequestStringBuilder(fwded_request);
								String fwdrequest = send.appendSt(f.getMyKey()+" "+"Forward"+"\n");
								/* socket to send fwdrequest string */
								try {
									Socket s = best_neighbour_fwd.getTCP_SOCKET();
									OutputStream os = s.getOutputStream();
									OutputStreamWriter osw = new OutputStreamWriter(os);
									BufferedWriter bw = new BufferedWriter(osw);
									bw.write(fwdrequest);
									bw.flush();
								} catch (Exception e) {
									e.printStackTrace();
								}

							}	

						}
					}

				}
			

				// System.out.println("Message received from the server : " +message);
			} 
			}catch (IOException e) {
				// TODO Auto-generated catch block
				//	e.printStackTrace();
			}

		} 

	

	private synchronized boolean isStopped() {
		return this.isStopped;
	}

	public synchronized void stop(){
		this.isStopped = true;
		try {

			f.log_write.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}


}
