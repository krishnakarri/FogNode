import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;


public class cloud implements Runnable {
	
	Queue<Request> cloudQueue = new LinkedList<Request>();
	protected boolean      isStopped    = false;

	@Override
	public void run() {
		while(!isStopped()){
			// At least one request exists for cloud to execute
			if(cloudQueue.size()>0){
				try{
					Request retrieved = cloudQueue.poll();
					System.out.println("Cloud retrieved"+retrieved.getMessage());
					Long sleepTime = (long)retrieved.getProcessing_time()/100; //100 times faster
					Thread.sleep(sleepTime);
					
					send(InetAddress.getByName(retrieved.getOwnHostName()), retrieved.getListenerPortNumber(),
							retrieved.getMessage()+" !!!PROCESSED BY CLOUD!!!" );
				}catch(Exception e){
					
				}
			}
	}
	}
	private synchronized boolean isStopped() {
		return this.isStopped;
	}

	public synchronized void stop(){
		this.isStopped = true;
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
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port); // craft the message to be sent
        try {
            clientSocket.send(sendPacket); // send the message
        } catch (IOException ex) {
            System.out.println("I/O exception happened!");
            ex.printStackTrace();
        }

    }
	
}
