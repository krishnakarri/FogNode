
public class Q_Delay {

	Long q_delay =0L;

	public synchronized Long getQ_delay() {
		return q_delay;
	}
	
	public synchronized void setQ_delay(Long d){
		this.q_delay = d;
	}
  
	public synchronized void addQ_delay(Long delay) {
		this.q_delay = this.q_delay+delay;
	}
	public synchronized void subQ_delay(Long subd){
		System.out.print("subtracted from"+this.q_delay+"-->");
		this.q_delay = this.q_delay-subd;
		System.out.println("to "+this.q_delay);
		
		if(q_delay<0)
		System.out.println("q_delay set to negative");
	}
	
}
