import java.net.*;
public class Neighbour {
	String key;
	String MY_IP;
	int TCP_PORT;
	Socket TCP_SOCKET;
	boolean is_client ;
	Long IP_Number ;
	Long Q_DELAY;
	
	
	public Neighbour(String mY_IP, int tCP_PORT) {
		super();
		MY_IP = mY_IP;
		key = mY_IP+":"+tCP_PORT+"";
		TCP_PORT = tCP_PORT;
		is_client = false;
		Q_DELAY = Long.MAX_VALUE;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getMY_IP() {
		return MY_IP;
	}
	public void setMY_IP(String mY_IP) {
		MY_IP = mY_IP;
	}
	public synchronized Long getQ_DELAY() {
		return Q_DELAY;
	}
	public synchronized void setQ_DELAY(Long q_DELAY) {
		this.Q_DELAY = q_DELAY;
	}
	public boolean Is_client() {
		return is_client;
	}
	public void setIs_client(boolean is_client) {
		this.is_client = is_client;
	}
	public int getTCP_PORT() {
		return TCP_PORT;
	}
	public Long getIP_Number() {
		return IP_Number;
	}
	public void setIP_Number(Long iP_Number) {
		IP_Number = iP_Number;
	}
	public void setTCP_PORT(int tCP_PORT) {
		TCP_PORT = tCP_PORT;
	}
	public synchronized Socket getTCP_SOCKET() {
		return TCP_SOCKET;
	}
	public synchronized void setTCP_SOCKET(Socket tCP_SOCKET) {
		TCP_SOCKET = tCP_SOCKET;
	}
	
	
	
}
