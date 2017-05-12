import java.util.Vector;

public class Node {
	
	public enum MoDe {
		
		BOOTSTRAPPING, SYNCHING, SYNCHED
	}

	int birth;
//	Vector<Integer> queries;
	int maxqueries;
	int totalqueries;
	int bscounter; // bootstrapping counter
	
	int element; // id
    Node next;
    Node prev;
    MoDe mode;
    Vector<RegionnBlocks> rnb;
    int departdc; // counter indication that downloading has
    					// to be done due to other nodes' departures 
    int dlblckcount; // downloaded blocks counter
    int rearranging; // counter indication that rearranging has
    					// to be done due to other nodes' departures/insertions
    int tobenoticed;
    
	public Node(int element, int GC, String s) {
		this.element = element;
		this.birth = GC;
//		this.queries = new Vector<Integer>();
		this.maxqueries = 0;
		this.totalqueries = 0;
		this.tobenoticed = Chord.CYC_NTC_DPRTR;
		
		if (s.equals("bsalready")) {
			this.bscounter = Chord.N_O_BLOCKS;
			this.mode = MoDe.SYNCHED;
		}
		else {
			this.bscounter = 0;
			this.mode = MoDe.BOOTSTRAPPING;
		}
	}
	

}
