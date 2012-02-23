import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;


public class Solve_USPS {

	Vertex[] startGraph;
	Vertex[] bestGraph;
	int edges;
	int bestClosed;
	long start;
	
	/*
	 * Solve_USPS constructor
	 */
	public Solve_USPS(int size) {
		startGraph = new Vertex[size];
		bestGraph = new Vertex[size];
		bestClosed = 0;
		start = System.currentTimeMillis();}//end constructor
		
	/*
	 * Main method begins the program
	 * Read the input file
	 * Create the graph
	 * Call the solve method
	 */
	public static void main(String[] args) throws Exception {
		
		String file = args[0];
//		String file = "rand-10-10";
		
		//read in file
		Scanner scanner = new Scanner(new File(file));

		//create graph with appropriate number of verts
		int i, j;
		i = scanner.nextInt(); //num of verts
		j = scanner.nextInt(); //num of edges

		
		Solve_USPS s = new Solve_USPS(i);
		s.edges = j;
		for(int count = 0; count < i; count++) {
			s.startGraph[count] = new Vertex(count);
			s.bestGraph[count] = new Vertex(count);}
		
		while((scanner.hasNext())) {
			i = scanner.nextInt();
			j = scanner.nextInt();
			s.startGraph[i].addEdge(j);
			s.startGraph[j].addEdge(i);}//end while

		scanner.close();
		
		//check if the graph is a ring, and solve if it is
		if(s.ring(s.startGraph)) {
			Vertex start = s.getVertex(s.startGraph, (Integer) 0);
			start.state = 1;
			Vertex prev = start;
			Vertex curr = s.getVertex(s.startGraph, start.edges.get(0));
			start.edges.remove((Integer) curr.value);
			curr.edges.remove((Integer) prev.value);
			int counter = 0;
			while(curr != start) {
				if(counter < 2) {
					curr.state = -1;
					prev = curr;
					curr = s.getVertex(s.startGraph, prev.edges.get(0));
					curr.edges.remove((Integer) prev.value);
					counter++;}
				else {
					counter = 0;
					curr.state = 1;
					prev = curr;
					curr = s.getVertex(s.startGraph, prev.edges.get(0));
					curr.edges.remove((Integer) prev.value);}}
			s.bestGraph = s.startGraph;
			s.bestClosed = s.getVertexStates(s.startGraph)[0];}//end else, while, if

		//the graph is not a ring
		else {
			s.startGraph = s.polynomialTime(s.startGraph);
			s.solve(s.startGraph);}//end else
		
		System.out.print(s.bestClosed + ": ");
		for(int k = 0; k < s.bestGraph.length; k++) {
			if(s.bestGraph[k].state == -1) {
				System.out.print(s.bestGraph[k].value + " ");}}
		System.out.println();}//if, for, method
	
	/*
	 * Solve the given graph using recursive branching
	 * Include optimizations from other functions
	 */
	public void solve(Vertex[] testGraph) {

		//if we have reached the end of allowable time, return best solution thus far
		long time = System.currentTimeMillis();
		if(time - start > 59500) {
			return;}
		
		//check for solution bounding
		if(boundingOptimizations(testGraph)) {
			return;}//we cannot get a better answer than what we already have

		//does this branch already contain a solution?
		if(coversAllVerts(testGraph)) {
			endBranch(testGraph);
			return;}//end if
		
		//find the next vertex to branch on
		int index = -1;
		Vertex testVertex = getHighestDegreeUndecided(testGraph);
		if(testVertex != null) {
			index = testVertex.value;}//end if

		//if index is still -1 at this point:
		//all vertices have been evaluated
		//we have reached the end of a branch
		//end recursion
		//return
		if(index == -1) {
			int[] states = getVertexStates(testGraph);
			int closedVertices = states[0];
			if(closedVertices > bestClosed && isValid(testGraph)) {
				for(int i = 0; i < testGraph.length; i++) {
					bestGraph[i] = testGraph[i];
					bestClosed = closedVertices;}//end if, for
				return;}//end for, if
			return;}//end if
		
		//branch by opening the next node
		Vertex[] graphOpen = copyGraph(testGraph);
		open(graphOpen, graphOpen[index]);
		solve(graphOpen);

		//branch by closing the next node
		Vertex[] graphClose = copyGraph(testGraph);
		if(backTrackingOptimizations(testGraph, index)==false) {
			close(graphClose, getVertex(graphClose, index));
			solve(graphClose);}//end if
		
		//end recursion
		return;}//end method

	
	
	
	
	
	
	/*
	 * Check if the graph is a ring
	 */
	public boolean ring(Vertex[] testGraph) {
		for(int c = 0; c < testGraph.length; c++) {
			if(testGraph[c].degree != 2) {
				return false;}}
		return true;}
	
	/*
	 * Polynomial time optimizations for nodes of degrees 0 or 1
	 */
	public Vertex[] polynomialTime(Vertex[] graph) {	
		for(int j = 0; j < graph.length; j++) {
			Vertex v = graph[j];
			
			//if v has node 0
			if(v.degree == 0 && v.state == 0) {
				graph[j].state = 1;
				v.access = true;}//end if
			
			//else v is a leaf
			else if(v.degree == 1 && v.state == 0) {
				v.state = -1;
				v.access = true;
				Vertex neighbor = graph[v.edges.get(0)];
				neighbor.state = 1;
				v.access = true;
				bestClosed++;}}//end if, for
		return graph;
	}//end method
	
	/*
	 * Backtracking optimizations
	 * Backtrack when we reach an invalid solution
	 * Specifically, if a vertex and all of its neighbors are closed, we have an invalid solution
	 * Return, and do not continue this branch
	 */
	public boolean backTrackingOptimizations(Vertex[] graph, int index) {
		boolean backtrack = false;
		
		//the vertex you are closing has all decided and closed neighbors
		ArrayList<Integer> neighbors = graph[index].getNeighbors();
		int count = 0;
		for(Integer n : neighbors) {
			Vertex v = getVertex(graph, n);
			if(v.state == -1) {
				count+=1;}}//end if, for
		if(count == neighbors.size()) {
			backtrack = true;}//end if
		return backtrack;}//end method
	
	/*
	 * Check if a better solution can be found than the current best on a given branch
	 * Return true if we should stop traveling
	 */
	public boolean boundingOptimizations(Vertex[] graph) {
		int[] states = getVertexStates(graph);
		if(states[0] + states[1] < bestClosed) {
			return true;}//end if
		return false;}//end method

	
	/*
	 * Check if the evaluated graph is valid
	 * Return true if it is valid, false otherwise
	 */
	public boolean isValid(Vertex[] testGraph) {
		boolean valid = true;
		for(Vertex v : testGraph) {
			boolean openNeighbor = false;
			if(v.state != -1) {
				openNeighbor = true;}//end if
			if(v.state == -1) {
				ArrayList<Integer> n = v.getNeighbors();
				for(Integer e : n) {
					Vertex w = getVertex(testGraph, e);
					if(w.state == 1) {
						openNeighbor = true;
						break;}}}//end if, for, if
			if(openNeighbor == false) {
				valid = false;
				break;}}//end if, for
		return valid;}//end method
	
	/*
	 * Check to see if the open vertices cover the entire graph
	 * Return true if we have a fully covered graph, false otherwise
	 */
	public boolean coversAllVerts(Vertex[] testGraph) {
		int[] check = new int[testGraph.length];

		//find how many vertices are covered currently
		//do so by checking all verts of state 1 and their neighbors
		for(int i = 0; i < testGraph.length; i++) {
			if(testGraph[i].state == 1) {
				check[i] = 1; 
				ArrayList<Integer> n = testGraph[i].getNeighbors();
				for(int j = 0; j < n.size(); j++) {
					check[(getVertex(testGraph, n.get(j)).value)] = 1;}}}//end for, if, for
		
		//how many vertices have we covered with the testgraph?
		//find the sum
		int sum = 0;
		for(int k = 0; k < check.length; k++) {
			sum += check[k];}//end for
		if(sum == testGraph.length) {
			return true;}//end if
		return false;}//end method

	

	/*
	 * We have found a solution
	 * End the branch even though all of its vertices are not evaluated
	 */
	public void endBranch(Vertex[] testGraph) {
		for(int i = 0; i < testGraph.length; i++) {
			if(testGraph[i].state == 0) {
				testGraph[i].state = -1;}}//end if, for
		bestGraph = testGraph;
		bestClosed = getVertexStates(testGraph)[0];}//end method

	
	
	
	/*
	 * Copy a graph for use in recursion
	 */
	public Vertex[] copyGraph(Vertex[] toCopy) {
		Vertex[] copy = new Vertex[startGraph.length];
		for(int i = 0; i < toCopy.length; i++) {
			Vertex v = toCopy[i];
			Vertex w = v.copy();
			copy[i] = w;}
		return copy;}
	
	/*
	 * Close a vertex in a given graph
	 */
	public void close(Vertex[] graph, Vertex v) {
		//close vertex
		v.state = -1;
		
		//decrement vertex's neighbors' degree
		for(Integer i: v.edges) {
			Vertex w = getVertex(graph, i);
			w.degree--;}}//end for, method
	
	/*
	 * Open a vertex in a given graph
	 */
	public void open(Vertex[] graph, Vertex v) {
		//open vertex
		v.state = 1;
		
		//set vertex's neighbors' access to true
		for(Integer i: v.edges) {
			Vertex w = getVertex(graph, i);
			w.access = true;}}//end for, method
	
	/*
	 * Get a vertex from an input graph
	 */
	public Vertex getVertex(Vertex[] testGraph, Integer i) {
		for(Vertex v : testGraph) {
			if(v.value == i.intValue()) {
				return v;}}//end if, for
		return null;}//end method
	
	/*
	 * Returns the number of vertices in each state for a given graph
	 * states[0] = closed vertices
	 * states[1] = undecided vertices
	 * states[2] = open vertices
	 */
	public int[] getVertexStates(Vertex[] graph) {
		int[] states = new int[3];
		for(Vertex w : graph) {
			if(w.state == -1) {
				states[0]++;}//end if
			else if(w.state == 1) {
				states[2]++;}//end elif
			else{
				states[1]++;}}//end else, for
		return states;}//end method

	/*
	 * Get the vertex of highest degree in an input graph
	 */
	public Vertex getHighestDegreeUndecided(Vertex[] graph) {
		Vertex highest = null;
		for(int j = 0; j < graph.length; j++) {
			if(graph[j].state == 0) {
				highest = graph[j];
				break;}}//end if, for
		if(highest == null) {
			return highest;}
		for(int i = 0; i < graph.length; i++) {
			if(graph[i].degree > highest.degree && graph[i].state == 0) {
				highest = graph[i];}}//end if, for
		return highest;}//end method

	
}
