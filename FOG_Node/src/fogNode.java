
import java.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class fogNode {
	
	int Max_Response_Time;
	boolean server_started = false;
	long t;
	String myKey;   // To append to response
	String MY_IP;
	String received_IP;
	int MY_UDP;
	int MY_TCP;
	Long MY_IP_Number;
	Q_Delay q_delay = new Q_Delay();
	Hashtable<String, Neighbour> neighbour_Table = new Hashtable<String, Neighbour>();
	Blocking_Queue request_queue = new Blocking_Queue();
	File log_file = null;
	BufferedWriter log_write = null;
	BestNeighbourQueue bn_queue = new BestNeighbourQueue();
	cloud cl=null;
		
	private void createSockets() {
		
		/* establish client server relation and store sockets */
		
		for(Entry<String, Neighbour> entry:neighbour_Table.entrySet()){
			
			String neighbour_ip = entry.getKey();
			Neighbour n = entry.getValue();
			Long neighbourIPNumber = n.getIP_Number();
			bn_queue.addNeighbour(n);
			//comparison 
			if(comapareIP(this.getMY_IP_Number(), neighbourIPNumber,
					this.MY_TCP, n.getTCP_PORT())){
				if (!this.is_server_started()){
			//	TCP_Listen server = new TCP_Listen(getMY_TCP(),getneighbour_Table());
			//	new Thread(server).start();
				this.setServer_started(true);
				}
				n.setIs_client(true);
				System.out.println("Started server");
				System.out.println("IPS are "+this.getMyKey()+"neigh--->"+neighbour_ip);
				
			}else{
				System.out.println("opened socket with"+entry.getKey());
				String host = n.getMY_IP();
	            int port = n.getTCP_PORT();
	           
	            try {
	            	InetAddress address = InetAddress.getByName(host);
					Socket socket = new Socket(address, port);
					n.setTCP_SOCKET(socket);
					n.setIs_client(false);
					ListenFromServer listenS = new ListenFromServer(socket,this);
					new Thread(listenS).start();
					/*
					InputStream is = socket.getInputStream();
		            InputStreamReader isr = new InputStreamReader(is);
		            BufferedReader br = new BufferedReader(isr);
		            String message = br.readLine();
		            System.out.println("Message received from the server : " +message);
		            */
				} catch (IOException e) {
					
				 //e.printStackTrace();
				}
				
			}
			
		}
		
		
	}
	public boolean comapareIP(Long IP1,Long IP2,int port1,int port2){
		/* Method returns true if ip1 < ip2
		 * or ip1==ip2 and port1<port2
		 * true = start server
		 */
		if(IP1>IP2){
			return false;
		}else if(IP1<IP2){
			return true;
		}else{
			if(port1>port2)
				return false;
		}
		
		return true;
	}
	public static Long convertIPToNumber(String IP){
		String ip_split[] = IP.split("\\.");
		long nip =(Long.parseLong(ip_split[0]) << 24 ) + ( Long.parseLong(ip_split[1] )<< 16 ) +
		(Long.parseLong(ip_split[2]) << 8 ) + ( Long.parseLong(ip_split[3]) << 0 );
		return nip;
	}
	


	public static void main(String args[]){
		
		if(args.length==0){
			System.out.println("No Arguments passed! Exiting!");
			return ;
		}else {
					
			fogNode f = new fogNode();
			
			f.setMax_Response_Time(Integer.parseInt(args[0]));
			f.setT(Integer.parseInt(args[1]));
			String convert = args[2];
			try {
				
				InetAddress address = InetAddress.getByName(args[2]);
				convert = address.getHostAddress();
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			f.setMY_IP(convert);
			f.setMY_IP_Number(convertIPToNumber(f.getMY_IP()));
			f.setMY_UDP(Integer.parseInt(args[3]));
			f.setMY_TCP(Integer.parseInt(args[4]));
			f.setMyKey(f.getMY_IP()+":"+f.getMY_TCP());
			try {
			StringBuffer log_file_location = new StringBuffer(
					"C:\\Users\\krishna\\Desktop\\mccallum\\acn-kamilsarac\\ACN_Project\\FOG_Node\\");
			log_file_location.append(args[4]);
			log_file_location.append(".txt");
			f.log_file = new File(log_file_location.toString());
			 if (f.log_file.exists()) {
				 f.log_file.delete();
				
             }
			 f.log_file.createNewFile();
		
				f.log_write = new BufferedWriter(new FileWriter(f.log_file));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for(int i =5;i<args.length;i=i+2){
				InetAddress convert_add = null;
				try {
					convert_add = InetAddress.getByName(args[i]);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Neighbour n = new Neighbour(convert_add.getHostAddress(),Integer.parseInt(args[i+1]));
				
			
				n.setIP_Number(convertIPToNumber(n.getMY_IP()));
				f.neighbour_Table.put(n.getMY_IP()+":"+n.getTCP_PORT(), n);
			}
			
			/* establishing client server relation ship
			 * 
			 */
			f.createSockets();
			
			/* udp listen thread to listen to incoming requests */
			UDP_Listen udp_server = new UDP_Listen(f);
			new Thread(udp_server).start();
			
			TCP_Listen tcp_server = new TCP_Listen(f);
			new Thread(tcp_server).start();
			
			/* periodic updates thread */
			periodic_update pu = new periodic_update(f);
			new Thread(pu).start();
			
			/*Processing request thread*/
			ProcessRequest pr = new ProcessRequest(f);
			new Thread(pr).start();
			
			/*Cloud thread*/
			f.cl = new cloud();
			new Thread(f.cl).start();
			
						
		}
	
		
	}

	public String getMyKey() {
		return myKey;
	}
	public void setMyKey(String myKey) {
		this.myKey = myKey;
	}
	public BestNeighbourQueue getBn_queue() {
		return bn_queue;
	}
	public void setBn_queue(BestNeighbourQueue bn_queue) {
		this.bn_queue = bn_queue;
	}
	public Blocking_Queue getRequest_queue() {
		return request_queue;
	}
	
	public boolean is_server_started() {
		return server_started;
	}
	public void setServer_started(boolean server_started) {
		this.server_started = server_started;
	}
	public int getMax_Response_Time() {
		return Max_Response_Time;
	}



	public void setMax_Response_Time(int max_Response_Time) {
		Max_Response_Time = max_Response_Time;
	}



	public long getT() {
		return t;
	}



	public void setT(int t) {
		this.t = t;
	}



	public String getMY_IP() {
		return MY_IP;
	}



	public void setMY_IP(String mY_IP) {
		MY_IP = mY_IP;
	}



	public int getMY_UDP() {
		return MY_UDP;
	}



	public void setMY_UDP(int mY_UDP) {
		MY_UDP = mY_UDP;
	}



	public int getMY_TCP() {
		return MY_TCP;
	}



	public void setMY_TCP(int mY_TCP) {
		MY_TCP = mY_TCP;
	}



	public Hashtable<String, Neighbour> getneighbour_Table() {
		return neighbour_Table;
	}



	public void setneighbour_Table(Hashtable<String, Neighbour> neighbour_Table) {
		this.neighbour_Table = neighbour_Table;
	}

	public Long getMY_IP_Number() {
		return MY_IP_Number;
	}

	public void setMY_IP_Number(Long mY_IP_Number) {
		MY_IP_Number = mY_IP_Number;
	}


	

}
