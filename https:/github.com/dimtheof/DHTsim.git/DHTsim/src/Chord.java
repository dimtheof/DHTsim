import java.util.Iterator;
import java.util.Random;
import java.util.Vector;



/*
 * Simple Simulation for big scale
 * and highly dynamic chord DHT
 */
public class Chord {
	
	public static int N_O_BLOCKS = 1150; // 115.000 MB / 100
	public static int CYC_NTC_DPRTR = 30;  // 30 seconds
	public static int CYC_SNC_DPRTR = 5;   // 5 seconds
	public static int CYC_SNC_INSRT = 5;   // 5 seconds
	public static int I_D_CHANCE = 500;	   // 1/P(insrt/dprt per sec)
	public static int SECOND_LOOPS = 20000;// Simulation loops (seconds)
	
	// chord list: a list of the active nodes' id s
	public static DoubleLinkedList chordl = new DoubleLinkedList();
	// chord table: a hash table of the active nodes' id s
	// public static Hashtable<Integer,Node> chordt = new Hashtable<Integer,Node>(); 
	// a list of bootstrapping nodes
	public static Vector<Node> bsl = new Vector<Node>();
	// a list of downloading nodes
	public static Vector<Node> dll = new Vector<Node>();
	// a list of rearranging nodes
	public static Vector<Node> rrl = new Vector<Node>();
	// a list of departed nodes (unnoticed yet)
	public static Vector<Node> dnl = new Vector<Node>();
	
	static Integer GC;
	
	static Integer K;
	
	public static void InsertRandomNode(String s) {
		
		Random rn = new Random();
		int element = Math.abs(rn.nextInt());
		Node newnode = new Node(element, GC, s);
		chordl.addSorted(newnode);
//		chordt.put(element, newnode);
		if (!s.equals("bsalready"))
			bsl.addElement(newnode);
		InsertionInform(newnode);
		System.out.println(GC + ": New node inserted, bsl size: "+bsl.size()+
				", chordl size: "+chordl.size());
	}
	
	
	public static void DepartRandomNode() {
		
		if (chordl.size() < 2) {
			System.out.println("NOT removing the last node..");
			return;
		}
		Random rn = new Random();
		int i = Math.abs(rn.nextInt()) % chordl.size();
		Node remnode = chordl.nodeAt(i);
		dnl.add(remnode);
		System.out.println(GC + ": Ungraceful departure of #"+remnode.element+".. "
				+ "it'll take some time to be noticed");
		
	}
	
	public static void FinalDeparture(Node remnode) {
//		chordt.remove(remnode.element);
		chordl.remove(remnode);
		bsl.remove(remnode);
		dll.remove(remnode);
		rrl.remove(remnode);
		
		System.out.println(GC + ": Node #"+remnode.element+" departed, mode: "+remnode.mode+", bsl size: "+bsl.size()+
				", chordl size: "+chordl.size());
	}
	
	/*
	 * We need to inform the K predecessors
	 * about the departure.
	 * THE PREDECESSORS WILL DOWNLOAD
	 * the replica of one of the departed
	 * node's successors.
	 * They will also rearrange their
	 * replicas data.
	 * 
	 * We will emulate this based on the
	 * consideration that the number of nodes
	 * is big and the data well distributed.
	 */
	public static void DepartureInform(Node n) {
		
		Node tmpnode;
		Vector<Node> v = chordl.getKPrev(n, K);
		Iterator<Node> itr = v.iterator();
		if (chordl.size() <= K) {
			while (itr.hasNext()) {
				tmpnode = itr.next();
				tmpnode.rearranging += CYC_SNC_DPRTR;
				tmpnode.mode = Node.MoDe.SYNCHING;
				if (!rrl.contains(tmpnode))
					rrl.add(tmpnode);
			}
		}
		
		else {
			while (itr.hasNext()) {
				tmpnode = itr.next();
				tmpnode.departdc++;
				if (tmpnode.dlblckcount == 0)
					tmpnode.dlblckcount = N_O_BLOCKS / chordl.size();
				tmpnode.mode = Node.MoDe.SYNCHING;
//				tmpnode.rearranging++; // we assume that rearranging
									// is being done concurrently with
									// the downloading
				if (!dll.contains(tmpnode))
					dll.add(tmpnode);
//				if (!rrl.contains(tmpnode))
//					rrl.add(tmpnode);
			}
		}
		FinalDeparture(n);
		return;
	}

	/*
	 * We need to inform the K predecessors
	 * about the insertion.
	 * THE PREDECESSORS MAY STOP DOWNLOADING
	 * replicas due to previous departure.
	 * In any case, they will rearrange their
	 * replicas data.
	 */
	public static void InsertionInform(Node n) {
		
		Node tmpnode;
		Vector<Node> v = chordl.getKPrev(n, K);
		Iterator<Node> itr = v.iterator();
		while (itr.hasNext()) {
			tmpnode = itr.next();
			tmpnode.rearranging += CYC_SNC_INSRT;
			
			if (tmpnode.departdc > 0) // inform to stop a downloading, if any
				if (tmpnode.departdc-- == 1)
					tmpnode.dlblckcount = 0;
			
		}		
		return;
	}
	
	public static void ServiceSynching() {
			
		// BOOTSRAPPERS
		int start;
		int position;
		Random rn = new Random();
		Node itrnode;
		Iterator<Node> itrb = bsl.iterator();
		Vector<Node> tbr = new Vector<Node>();
		
		while(itrb.hasNext()) {
			itrnode = itrb.next();
			start = chordl.size() * itrnode.bscounter / N_O_BLOCKS - 1;
			position = start - Math.abs(rn.nextInt()) % K;
			
			if (position < 0)
				position += chordl.size();
			
			chordl.nodeAt(position).totalqueries++;
//			chordl.nodeAt(position).queries.addElement(GC);
			if (++itrnode.bscounter == N_O_BLOCKS) {
				tbr.add(itrnode);
				itrnode.mode = Node.MoDe.SYNCHED;
			}
		}
		RemoveFromVector(tbr, bsl);
		
		// DOWNLOADERS
		tbr.clear();
		Iterator<Node> itrd = dll.iterator();
		Vector<Node> v;
		Node randnode;
		int randpos;
		while (itrd.hasNext()) {
			itrnode = itrd.next();
			v = chordl.getKNext(itrnode, K);
			v = chordl.getKNext(v.lastElement(), K);
			randpos =  Math.abs(rn.nextInt()) % v.size();
			randnode = v.elementAt(randpos);
//			System.out.println("Node "+itrnode.element+" with Kprev v size "+v.size()
//			+" picked randpos "+randpos+": #"+randnode.element);
			randnode.totalqueries++;
			if (--itrnode.dlblckcount == 0) 
				if (--itrnode.departdc > 0) 
					itrnode.dlblckcount = N_O_BLOCKS / chordl.size();
				else
					tbr.add(itrnode);
		}
		RemoveFromVector(tbr, dll);
		
		// REARRANGERS
		tbr.clear();
		Iterator<Node> itrr = rrl.iterator();
		while (itrr.hasNext()) {
			itrnode = itrd.next();
			if (--itrnode.rearranging == 0)
				tbr.add(itrnode);
		}
		RemoveFromVector(tbr, rrl);
		
		// DEPARTED
		tbr.clear();
		Iterator<Node> itrde = dnl.iterator();
		while (itrde.hasNext()) {
			itrnode = itrde.next();
			if (itrnode.tobenoticed == 0 | --itrnode.tobenoticed == 0) {
				if (!dnl.contains(itrnode.prev) || !dnl.contains(itrnode.next)) {
					tbr.add(itrnode);
				}
			}	
		}
		RemoveFromVector(tbr, dnl);
	}
	
	public static void RemoveFromVector(Vector<Node> tbr, Vector<Node> v) {
		Iterator<Node> itr = tbr.iterator();
		Node tmp;
		while (itr.hasNext()) {
			tmp = itr.next();
			if (v.equals(dnl))
				DepartureInform(tmp);
			v.remove(tmp);
			if (!bsl.contains(tmp) && !dll.contains(tmp) && !rrl.contains(tmp))
			tmp.mode = Node.MoDe.SYNCHED;
		}
	}
	
	public static void PrintStats(int numInitNodes, int insertions, int departures, double totalbslsize) {
		
		Node iterator = chordl.head;
		int maxcounter = 0;
		int synched = 0;
		int synching = 0;
		int bootstrapping = 0;
		int sumofmodes;
		for (int i = chordl.size(); i > 0; i--) {
			if (((100 * iterator.totalqueries)/(GC-iterator.birth + 1)) > maxcounter)
				maxcounter = (100 * iterator.totalqueries)/(GC-iterator.birth + 1);
			iterator = iterator.next;
			if (iterator.mode.equals(Node.MoDe.SYNCHED))
				synched++;
			else if (iterator.mode.equals(Node.MoDe.SYNCHING))
				synching++;
			else if (iterator.mode.equals(Node.MoDe.BOOTSTRAPPING))
				bootstrapping++;
			
		}
		sumofmodes = synched + synching + bootstrapping;
		System.out.println("Number of blocks: " + N_O_BLOCKS);
		System.out.println("Initial (bootstrapped) nodes: " + numInitNodes);
		System.out.println("Replication factor K: " + K);
		System.out.println("Total insertions/departures: " + insertions + "/" + departures );
		System.out.println("Total cycles: " + GC);
		System.out.println("Total nodes (instance) chordsize/sumofmodes: " + chordl.size() + "/" + sumofmodes);
		System.out.println("Max mean queries per node (instance, x100): " + maxcounter);
		System.out.println("Ratio per thousand synched/synching/bootstrapping: " +
				(1000*synched)/sumofmodes + "/" + (1000*synching)/sumofmodes + "/"+ (1000*bootstrapping)/sumofmodes);
		System.out.println("Mean bsl size: " + totalbslsize / GC);
	}

	public static void tmpfnc() {
		Node itr = chordl.head;
		@SuppressWarnings("unused")
		Node tmp;
		for (int i = 0; i < chordl.size(); i++) {
			tmp = itr.prev;
			itr = itr.next;	
		}
	}
	
	public static void main(String[] args) {
		
		int numInitNodes = 0;
		try {
			numInitNodes = Integer.parseInt(args[0]);
			K = Integer.parseInt(args[1]);
			GC = 0;
		}
		catch (Exception e) {
			System.out.println("Usage: java Chord <numInitNodes> <K>.....\n");
			return;
		}
			
		for (int i = 0; i < numInitNodes; i++) {
			InsertRandomNode("bsalready");
			System.out.println("chordl size: " + chordl.size());
			
		}
		
	//	chordl.iterateForward();
	//	PrintTable();
	//	DepartRandomNode();
	//	chordl.iterateForward();
		int j = -1;
		int insertions = 0;
		int departures = 0;
		double totalbslsize = 0;
		while(++j < SECOND_LOOPS) {
			GC++;
			Random rn = new Random();
			int i = Math.abs(rn.nextInt()) % I_D_CHANCE;
			switch(i){
			case 0:
				InsertRandomNode("");
				insertions++;
//				System.out.println("# of nodes: " + chordl.size() + " (+++++)");
				break;
			case 1:
				DepartRandomNode();
				departures++;
//				System.out.println("# of nodes: " + chordl.size() + " (-----)");
				break;
			default:
				break;
			}
			
			ServiceSynching();
			tmpfnc();
			totalbslsize += bsl.size();
		}
		PrintStats(numInitNodes, insertions, departures, totalbslsize);
		
		
/*		while (!bsl.isEmpty()) {
			GC++;
			ServiceSynching();
		}
		PrintQueries();
*/	
		
	}
}
