/**
Tcp server
 * 
 */

/**
 * @author krishna
 *
 */
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TCP_Listen implements Runnable{

    protected int          serverPort   = 8080;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;
    protected Hashtable<String, Neighbour> nb_table = null;
    fogNode f = null;

    public TCP_Listen(fogNode fn){
    	this.f = fn;
    	this.nb_table=f.getneighbour_Table();
        this.serverPort = f.getMY_TCP();
    }

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        openServerSocket(); //start server
        while(!isStopped()){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
                ListenFromClient sendS = new ListenFromClient(clientSocket,this.f);
				new Thread(sendS).start();
          
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
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port"+this.serverPort, e);
        }
    }

}