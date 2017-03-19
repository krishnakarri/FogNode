import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;

import org.omg.CORBA.portable.InputStream;


public class ListenFromClient implements Runnable {

	Socket clientSocket = null;
	fogNode f = null;
	Hashtable<String, Neighbour> nb_table = null;
	protected boolean      isStopped    = false;
	BestNeighbourQueue bn_queue_ch = null;
	Q_Delay q_delay_t =null;
	BlockingQueue<Request> block_queue_t = null;
	public ListenFromClient(Socket s,fogNode fn) {
		clientSocket = s;
		this.f = fn;
		nb_table=this.f.getneighbour_Table();
		q_delay_t = f.q_delay;
		bn_queue_ch = f.getBn_queue();
		block_queue_t = f.getRequest_queue().getblock_queue();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(!isStopped()){

			try {
				String clientip = clientSocket.getInetAddress().toString();
				clientip =clientip.trim().substring(1);
				//int clientport = clientSocket.getPort();
				//  System.out.println("cp is"+clientport); 
				java.io.InputStream is = clientSocket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String message ;
				while((message =br.readLine())!=null){
			//	System.out.println("Mesg ---------------------from client"+message);
				String message_break[] = message.split(" ");
				String neigbhour_port_number ="";
				Long response_time = null;
				f.log_write.write(message+"\n");
				//f.log_write.write("clien"+bn_queue_ch.getBest_Neighbour_Queue().peek().TCP_PORT+"\n");
				f.log_write.flush();
				if(message_break.length<3){
					response_time= Long.parseLong(message_break[0]);
					neigbhour_port_number = message_break[1];

					String s = clientip+":"+neigbhour_port_number;
					// System.out.println("s is "+s);
					/* 
					 * Store client socket taken from periodic update
					 */
					if(nb_table.containsKey(s)){
						//	System.out.println("entered");
						Neighbour retrieved = null; 
						retrieved = nb_table.get(s);
						if(nb_table.get(s).getTCP_SOCKET()==null){

							retrieved.setTCP_SOCKET(clientSocket);

						}

						retrieved.setQ_DELAY(response_time);
						bn_queue_ch.removeNeighbour(retrieved);
						bn_queue_ch.addNeighbour(retrieved);

					}
				}
				else {
					// forwarded message case

					//case of forwarded request from neighbour
					Request fwded_request = new Request(message);
					
					fwded_request.setVisitedNodesFromMsg();
					if(fwded_request.getForwardLimit()<=fwded_request.getCurrentOffset()){
						String cloud_send = message+" "+f.getMyKey()+" "+"Forward";
						fwded_request.setMessage(cloud_send);
						f.cl.cloudQueue.offer(fwded_request);
						System.out.println("hand over to cloud"+fwded_request.getMessage());
					} else{
						// check if request can be processed

						Long current_q_delay = q_delay_t.getQ_delay();
						current_q_delay = current_q_delay+fwded_request.getProcessing_time();
						if(f.Max_Response_Time>=current_q_delay){

							try {
								fwded_request.setMessage(fwded_request.getMessage()+" "+f.getMyKey()+" Processed!");
								block_queue_t.put(fwded_request);
								f.q_delay.setQ_delay(current_q_delay);
								System.out.println("processed-->"+message);
							} catch (InterruptedException e) {

								e.printStackTrace();
							}	

						} else{
							// forward to best neighbour

							
							//get best neighbour and fwd request to it
							Neighbour best_neighbour_fwd = bn_queue_ch.retreiveBestNeighbour(f.q_delay.getQ_delay(),fwded_request);

							//in case if no best neghbour forward to cloud
							if(best_neighbour_fwd==null){
								String cloud_send = message+" "+f.getMyKey()+" "+"Forward";
								fwded_request.setMessage(cloud_send);
								f.cl.cloudQueue.offer(fwded_request);
								System.out.println("no best neighbour fwd to cloud "+fwded_request.getMessage());
							}else {
								//forward to best neighbour
								
								fwdRequestStringBuilder send = new fwdRequestStringBuilder(fwded_request);
								String fwdrequest = send.appendSt(f.getMyKey()+" "+"Forward"+"\n");
								
								//System.out.println("more than queue delay! Tcp_forward "+best_neighbour_fwd.TCP_PORT+"->"+fwdrequest);
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
				//for(int i=0;i<bn_queue_ch.getBest_Neighbour_Queue().size();i++){
			//	f.log_write.write("Message received from client "+neigbhour_port_number+" value "+response_time+"\n");
				
				//}


				}
			}
			catch (IOException e) {


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
