import java.security.KeyStore.Entry;


public class fwdRequestStringBuilder {

	
	public StringBuilder fwdRequestString = null;
	
	public fwdRequestStringBuilder(Request r) {
		fwdRequestString = new StringBuilder();
		String s =r.getMessage().trim()+" ";
		int end = s.indexOf('O');
		// Format string 
		if(end!=-1){
			s = s.substring(0,end);
		}else{
			//System.out.println("index of big O not found");
		}
		
		fwdRequestString.append(s);
		fwdRequestString.append("OFF:");
		fwdRequestString.append(r.getCurrentOffset()+1);
		//r.setCurrentOffset(r.getCurrentOffset()+1);
		//fwdRequestString.
		
		//fwdRequestString = fwdRequestString.append
		
		fwdRequestString = fwdRequestString.append(" ");
		for(String visitedNode:r.getVisitedNodes()){
			s = visitedNode+" ";
			fwdRequestString = fwdRequestString.append(new String(s+"Forward "));
		}
		
	}

	public String getFwdRequestString() {
		return fwdRequestString.toString();
	}

	public void setFwdRequestString(StringBuilder fwdRequestString) {
		this.fwdRequestString = fwdRequestString;
	}
	
	public String appendSt(String s){
		fwdRequestString.append(s);
		return fwdRequestString.toString();
	}
	
}
