import java.util.ArrayList;

public class Vertex implements Comparable<Vertex> {
	//value/location of index in start graph
	int value;
	//ArrayList of vertices connected to this vertex
	ArrayList<Integer> edges;
	//degree of this vertex
	int degree;
	//current state of the post office: 1 is open, 0 is undecided, -1 is closed
	int state;
	//does the vertex have a neighbor that is open?
	boolean access;
	
	public Vertex(int i) {
		edges = new ArrayList<Integer>();
		degree = 0;
		state = 0;
		value = i;
		access = false;}//end method	
	
	public void addEdge(int e) {
		edges.add(e);
		degree++;}//end method

	public void removeEdge(int vertToRemove) {
		degree--;
		edges.remove(Integer.valueOf(vertToRemove));}//end method
	
	public int compareTo(Vertex w) {
		if(this.value > w.value) {
			return 1;}//end if
		else if(this.value < w.value) {
			return -1;}//end else if
		else {
			return 0;}}//end else, method
	
	public Vertex copy() {
		Vertex clone = new Vertex(this.value);
		clone.state = this.state;
		clone.value = this.value;
		clone.degree = this.degree;
		clone.access = this.access;
		for(Integer e: this.edges) {
			int x = e.intValue();
			Integer i = x;
			clone.edges.add(i);}//end for
		return clone;}//end method	
	
	public int getDegree() {
		return degree;};
	
	public int getValue() {
		return value;}//end method	
	
	public int getState() {
		return state;}//end method
	
	public boolean getAccess() {
		return access;}//end method
	
	public ArrayList<Integer> getNeighbors() {
		return edges;}//end method
}//end class