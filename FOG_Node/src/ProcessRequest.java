import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;


public class ProcessRequest implements Runnable {

	fogNode f = null;
	Blocking_Queue request_queue = null;
	BlockingQueue<Request> block_queue = null;
	Q_Delay q_delay_t = null;
	public ProcessRequest(fogNode fn){
		
		this.f = fn;
		request_queue = f.getRequest_queue();
		block_queue = request_queue.getblock_queue();
		q_delay_t = f.q_delay;
	}
	public void run() {
		while(true){
		try {
			
			Request retrieved = block_queue.take();
			//System.out.println("y");
			System.out.println("Retreived in process reqyest"+retrieved.getMessage());
			Thread.sleep(retrieved.getProcessing_time()*1000); 
			
			q_delay_t.subQ_delay(new Long(retrieved.getProcessing_time()));
			send(InetAddress.getByName(retrieved.getOwnHostName()), retrieved.getListenerPortNumber(),
					retrieved.getMessage());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("failed at ProcessRequest thread");
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		}	
		
	}
	 private void send(InetAddress IPAddress, int port, String data) {

	        DatagramSocket clientSocket = null;
	        try {
	            clientSocket = new DatagramSocket(); // make a Datagram socket
	        } catch (SocketException ex) {
	            System.out.println("Cannot make Datagram Socket!");
	            ex.printStackTrace();
	        }

	        byte[] sendData = new byte[data.getBytes().length]; // make a Byte array of the data to be sent
	        sendData = data.getBytes(); // get the bytes of the message
	        // craft the message to be sent
	        try {
	        	 DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
	            clientSocket.send(sendPacket); // send the message
	        } catch (IOException ex) {
	            System.out.println("I/O exception happened!");
	            ex.printStackTrace();
	        }

	    }

}
