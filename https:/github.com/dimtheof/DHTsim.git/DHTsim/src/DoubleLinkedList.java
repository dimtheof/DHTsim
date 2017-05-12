import java.util.NoSuchElementException;
import java.util.Vector;

public class DoubleLinkedList {
 
    public Node head;
    public Node tail;
    private int size;
     
    public DoubleLinkedList() {
        size = 0;
    }

    /**
     * returns the size of the linked list
     * @return
     */
    public int size() { return size; }
     
    /**
     * return whether the list is empty or not
     * @return
     */
    public boolean isEmpty() { return size == 0; }
     
    /**
     * adds element at the starting of the linked list
     * @param element
     */
    public void addFirst(Node node) {

        if (head != null ) {
        	head.prev = node;
        	node.next = head;
        	node.prev = tail;
        	tail.next = node;
        	head = node;
        }
        else {
        	head = node;
        	node.prev = node;
        	node.next = node;
        	tail = node;
        }
        size++;
    }
    
    public void addSorted(Node node) {
    	int element = node.element;
    	if (size() == 0) 
    		addFirst(node);
    	else if (head.element > element)  
    		addFirst(node);
    	else if (tail.element < element)
    		addLast(node);
    	else {
    		
    		size++;
    		Node iterator = head;
    		while(element > iterator.element){
    			iterator = iterator.next;
    		}    		
    		node.prev = iterator.prev;
        	node.next = iterator;
        	iterator.prev.next = node;
        	iterator.prev = node;
        	
    	}
    	
       
 //       System.out.println("adding: "+element);
    }
     
    /**
     * adds element at the end of the linked list
     * @param element
     */
    public void addLast(Node node) {
    	
        if (tail != null) {
        	tail.next = node;
        	node.prev = tail;
        	head.prev = node;
        	node.next = head;
        	tail = node;
        	
        }
        else {
        	tail = node;
        	node.next = head;
        	node.prev = head.prev; // the head itself?
        	head = node;
        }
       
        size++;
    }
     
    /**
     * this method walks forward through the linked list
     */
    public void iterateForward(){
         
        System.out.println("iterating forward..");
        Node tmp = head;
        while(tmp != null){
            System.out.println(tmp.element);
            tmp = tmp.next;
            if (tmp.element == head.element)
            	break;
        }
    }
     
    /**
     * this method walks backward through the linked list
     */
    public void iterateBackward(){
         
        System.out.println("iterating backword..");
        Node tmp = tail;
        while(tmp != null){
            System.out.println(tmp.element);
            tmp = tmp.prev;
            if (tmp.element == tail.element)
            	break;
        }
    }
     
    /**
     * this method removes element from the start of the linked list
     * @return
     */
    public int removeFirst() {
        if (size == 0) throw new NoSuchElementException();
        Node tmp = head;
        head = head.next;
        head.prev = tail;
        size--;
 //       System.out.println("deleted: "+tmp.element);
        return tmp.element;
    }
     
    /**
     * this method removes element from the end of the linked list
     * @return
     */
    public int removeLast() {
        if (size == 0) throw new NoSuchElementException();
        Node tmp = tail;
        tail = tail.prev;
        tail.next = head;
        size--;
 //       System.out.println("deleted: "+tmp.element);
        return tmp.element;
    }
    
    public void remove(Node node) {
        if (size == 0) throw new NoSuchElementException();
        if (node.equals(head))
        	removeFirst();
        else if (node.equals(tail))
        	removeLast();
        else {
            node.prev.next = node.next;
            node.next.prev = node.prev;
            size--;
        }

        return;
    }
    public void removeElementAt(int i) {
    	
    	
    	if (i == 0) {
    		removeFirst();
    		return;
    	}
    	else if (i == size()-1) {
    		removeLast();
    		return;
    	}
    	size--;
    	Node iterator = head;
    	while(i-- > 0)
    		iterator = iterator.next;
    	iterator.prev.next = iterator.next;
    	iterator.next.prev = iterator.prev;
    }
    public Node getPrev(Node n) {
    	if (n.equals(head))
    		return null;
    	
    	return n.prev;	
    }
    
    public Vector<Node> getKPrev(Node n, int k) {
    	
    	Vector<Node> v = new Vector<Node>();
    	Node tmp = n;
    	int i = Math.min(size() - 1, k);
    	for (; i > 0; i--) {
    		tmp = tmp.prev;
    		v.add(tmp);
    	}
    	return v;
    }
    
    public Vector<Node> getKNext(Node n, int k) {
    	
    	if (n == null)
    		System.out.println("NULLLLLLLLLLLLLLLLLL");
    	Vector<Node> v = new Vector<Node>();
    	Node tmp = n;
    	int i = Math.min(size() - 1, k);
    	for (; i > 0; i--) {
    		tmp = tmp.next;
    		v.add(tmp);
    	}
    	return v;
    }
    
    public int elementAt(int i) {
    	Node iterator = head;
    	while(i-- > 0)
    		iterator = iterator.next;
    	
    	return iterator.element;
    }
    public Node nodeAt(int i) {
    	Node iterator = head;
    	while(i-- > 0)
    		iterator = iterator.next;
    	
    	return iterator;
    }
    
}