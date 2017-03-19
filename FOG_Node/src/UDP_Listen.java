/**
Tcp server
 * 
 */

/**
 * @author krishna
 *
 */
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class UDP_Listen implements Runnable{
	protected fogNode f = null;
    protected int          serverPort   = 8080;
    protected DatagramSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;
    protected byte[] receiveData = new byte[20000];
    protected byte[] sendData = new byte[20000];
    BlockingQueue<Request> block_queue_t = null;
    Q_Delay q_delay_t =null;
    BestNeighbourQueue bn_queue_child = null;
    public UDP_Listen(fogNode fn){
    	this.f = fn;
        this.serverPort = f.getMY_UDP();
        block_queue_t = f.getRequest_queue().getblock_queue();
        q_delay_t = f.q_delay;
        bn_queue_child = f.getBn_queue();
    }

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
            System.out.println("Started Thread_UDP");
        }
        openServerSocket();
        while(! isStopped()){
        	Arrays.fill(receiveData, (byte) 0);
          DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            
            try {
                serverSocket.receive(receivePacket);
                //System.out.println("accepted connection");
                String sentence = new String( receivePacket.getData());
                System.out.println("RECEIVED: " + sentence);
                f.log_write.write("RECEIVED: " + sentence+"\n");
                f.log_write.flush();

                Request incoming_request = new Request(sentence);	
                Long current_q_delay = q_delay_t.getQ_delay();
                current_q_delay = current_q_delay+incoming_request.getProcessing_time();
                if(f.Max_Response_Time>=current_q_delay){
                	
                try {
                	incoming_request.setMessage(incoming_request.getMessage()+" "+"OFF:0 "+f.getMyKey()+" Processed!");
					block_queue_t.put(incoming_request);
					f.q_delay.setQ_delay(current_q_delay);
					System.out.println("UDP_Processed "+incoming_request.getMessage());
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}	
                	
                }
                else {
                	
                	//get best neighbour and fwd request to it
                	Neighbour best_neighbour_fwd = bn_queue_child.retreiveBestNeighbour(f.q_delay.getQ_delay(),incoming_request);
                	
                	//in case if no best neghbour forward to cloud
                	if(best_neighbour_fwd==null){
                		String cloud_send = incoming_request.getMessage()+" "+"OFF:0 "+f.getMyKey()+" "+"Forward";
						incoming_request.setMessage(cloud_send);
						f.cl.cloudQueue.offer(incoming_request);						
                		System.out.println("no best neighbour, fwd to cloud"+incoming_request.getMessage());
                	}else {
                		//forward to best neighbour
                		fwdRequestStringBuilder send = new fwdRequestStringBuilder(incoming_request);
                		String fwdrequest = send.appendSt(f.getMyKey()+" "+"Forward"+"\n");
                		System.out.println("more than queue delay! UDP_forward "+best_neighbour_fwd.getTCP_PORT()+"->"+fwdrequest);
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
            
                                
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException(
                    "Error accepting client connection", e);
            }
        
            }
        System.out.println("Server Stopped.") ;
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        this.serverSocket.close();
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new DatagramSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port 8080", e);
        }
    }

}