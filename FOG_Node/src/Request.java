import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;


public class Request {

	String message;
	String msg_split[];


	private long seqNumber = 0; // squence number of the request to be sent

	private long interval; // local version of the interval (in ms) in ConfigFileReader
	private int processing_time;
	private int forwardLimit; // local version of forward limit in ConfigFileReader
	private String ownHostName; // local version of ownHostName in ConfigFileReader
	private int listenerPortNumber; // local version of listenerPortNumber in ConfigFileReader
	private InetAddress[] FogIPAddress;  // local version of FogIPAddress[] in ConfigFileReader
	private int[] FogPortNumber; // local version of FogPortNumber[] in ConfigFileReader
	private ArrayList<String> visitedNodes = null; //To avoid loop scenario
	private int currentOffset;

	public Request(String msg) {
		this.message = msg.trim();
		msg_split = message.split(" ");
		this.seqNumber = Long.parseLong(msg_split[0].substring(2));
		this.processing_time = Integer.parseInt(msg_split[1].substring(2));
		this.forwardLimit = Integer.parseInt(msg_split[2].substring(3));
		this.ownHostName = msg_split[3].substring(3);
		this.listenerPortNumber = Integer.parseInt(msg_split[4].trim().substring(2));
		visitedNodes = new ArrayList<String>();
	}
	
	
	//To do request from forward case write a constructor
	//also set current offset in this constructor
	public void setVisitedNodesFromMsg(){
		
		
		this.currentOffset = Integer.parseInt(msg_split[5].trim().substring(4));
		
		for(int i=6;i<msg_split.length;i=i+2){
			visitedNodes.add(msg_split[i]);
		}
		
	}
	public int getCurrentOffset() {
		return currentOffset;
	}
	public void setCurrentOffset(int currentOffset) {
		this.currentOffset = currentOffset;
	}
	public static void main(String[] args) {

		Request r = new Request("#:0 T:1 FL:4 IP:127.0.0.1 P:9000");

	}


	public ArrayList<String> getVisitedNodes() {
		return visitedNodes;
	}
	public void setVisitedNodes(ArrayList<String> visitedNodes) {
		this.visitedNodes = visitedNodes;
	}
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String[] getMsg_split() {
		return msg_split;
	}

	public void setMsg_split(String[] msg_split) {
		this.msg_split = msg_split;
	}

	public long getSeqNumber() {
		return seqNumber;
	}

	public void setSeqNumber(long seqNumber) {
		this.seqNumber = seqNumber;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public int getProcessing_time() {
		return processing_time;
	}

	public void setProcessing_time(int processing_time) {
		this.processing_time = processing_time;
	}

	public int getForwardLimit() {
		return forwardLimit;
	}

	public void setForwardLimit(int forwardLimit) {
		this.forwardLimit = forwardLimit;
	}

	public String getOwnHostName() {
		return ownHostName;
	}

	public void setOwnHostName(String ownHostName) {
		this.ownHostName = ownHostName;
	}

	public int getListenerPortNumber() {
		return listenerPortNumber;
	}

	public void setListenerPortNumber(int listenerPortNumber) {
		this.listenerPortNumber = listenerPortNumber;
	}

	public InetAddress[] getFogIPAddress() {
		return FogIPAddress;
	}

	public void setFogIPAddress(InetAddress[] fogIPAddress) {
		FogIPAddress = fogIPAddress;
	}

	public int[] getFogPortNumber() {
		return FogPortNumber;
	}

	public void setFogPortNumber(int[] fogPortNumber) {
		FogPortNumber = fogPortNumber;
	}


}
