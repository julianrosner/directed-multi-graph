/**
 * Abstract state:
 * Graph represents a directed multi-graph containing nodes with a specified type for data and
 * edges labeled by the second specified type
 */

import java.util.*;

public class Graph <N extends Comparable<N>, E extends Comparable<E>> {

    private final List<Node<N,E>> entries;

    // Abstraction Function:
    // Graph, g, represents the directed multigraph
    // with each node represented by a Node object in entries.
    // The edges of these nodes are represented by the List of
    // Edge objects held by each Node. When a Node's Edge object has a
    // Node as its 'childNode' that is equivalent to a node with an edge
    // pointing to said child node
    //
    // Representation Invariant for every Graph g:
    // entries != null
    // Every Node in entries must be non-null and marked by
    // a unique, piece of data of the given type that no other Node has.
    // Every edge between Two Nodes is non-null and is marked by a piece of data
    // of its specified type
    // No Nodes can have two edges with the same label going to the same Node
    // For every edge in the graph, its childNode is also in the graph
    // The Nodes are in sorted order lexicographically
    // Each Node's Edges are in sorted order lexicographically

    //Note to Self: Test implementation where entries is a TreeSet<Node>
    //Note to Self: Consider implementation where Edge has a Type N object instead of a second Node

    /**
     * @spec.effects creates new Graph containing no Nodes or Edges
     */
    public Graph(){
        entries = new ArrayList<Node<N,E>>();
        this.checkRep();
    }

    /**
     * @param nodeData is the data inside of a Node that the user
     * wants to know whether or not appears in the Graph
     * @return true if a node with the given data is found in this Graph,
     * false otherwise
     */
    //Note to self: could be made faster using binary search
    public boolean containsNode(N nodeData) {
        for(int i = 0; i < entries.size(); i++) {
            if(entries.get(i).getData().equals(nodeData)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @spec.modifies if no Node containing the same data is found, a
     * new Node containing said data is added to the Graph, otherwise
     * no modification is made to the graph
     * @param data is the data of specified type of the Node being added to the graph
     * @return returns true if no Node in the Graph contains the same data as this
     * @spec.requires data is not null, entries is in alphabetical order
     */
    public boolean addNode(N data) {
        Node<N,E> newEntry = new Node<N,E>(data);
        int index = Collections.binarySearch(entries, newEntry);
        //if index >= 0 an identical Node is already in the graph
        if(index < 0) {
            entries.add(-1 * index - 1, newEntry );
            this.checkRep();
        }
        return (index < 0);
    }

    /**
     * @spec.modifies if a Node containing the same data is found, it will
     * be removed from the graph and all Edges leading to it from other
     * Nodes will be removed as well. If no such node is found, no
     * modification is performed
     * @param data is the data of the Node being removed from the graph
     * @return returns true if there is an existing Node in the graph
     * matching the given data and it is successfully removed, else returns false.
     * @spec.requires data is not null
     */
    public boolean removeNode(N data) {
        Node<N,E> unwanted = nodeWithData(data);
        if(unwanted == null) {
            return false;
        }
        for(int i = 0; i < entries.size(); i++) {
            entries.get(i).removeEdgesLeadingTo(unwanted);
        }
        entries.remove(unwanted);
        this.checkRep();
        return true;
    }

    /**
     * @spec.requires Edge label isn't null, Nodes with parentData and
     * childData exist in graph, the Edges of the parent node are in alphabetical order
     * @param parentData is the data of the Node from which the new Edge leads
     * @param childData is the data of the Node into which the new Edge leads
     * @param label is the label of the new Edge
     * @spec.modifies If the graph contains Nodes containing the given data,
     * the graph is modified to contain a new Edge going from
     * the Node with data 'parentData' to the node with data 'childData' all with
     * the label 'label', if either of these Nodes is absent the graph is not modified
     * @return returns true if the Nodes with the specified data were found and
     * the Edge was added, else returns false.
     */
    public boolean addEdge(N parentData, N childData, E label) {
        Node<N,E> parent = nodeWithData(parentData);
        Edge<N,E> newEdge = new Edge<N,E>(label, new Node<N,E>(childData));
        boolean addable = parent.addEdge(newEdge);
        this.checkRep();
        return addable;
    }

    /**
     * @spec.requires Edge label isn't empty
     * @param parentData is the data of the Node from which the Edge will be removed
     * @param childData is the data of the Node that is led to by the Edge being removed
     * @param label is the label of the edge being removed
     * @spec.modifies If the graph is found to contain Nodes with data corresponding to
     * the given data, the graph is modified to remove the Edge going from
     * the Node with data 'parentData' to the Node with data 'childData' and with
     * the label 'label', otherwise the graph is not modified
     * @return returns true if the Nodes with the specified data were found and the
     * Edge was added, else returns false
     */
    public boolean removeEdge(N parentData, N childData, E label) {
        Node<N,E> child = nodeWithData(childData);
        Node<N,E> parent = nodeWithData(parentData);
        if(parent == null || child == null) {
            return false;
        }
        Edge<N,E> newEdge = new Edge<N,E>(label, child);
        boolean removable = parent.removeEdge(newEdge);
        this.checkRep();
        return removable;
    }

    /**
     * @spec.requires Node with corresponding data is present in graph
     * @param nodeData is the data stored in the Node for which
     * the user wants to see the Edge labels and their corresponding Nodes
     * @return a Map where the keys are the data
     * which label the Edges leading away from the Node with the given data
     * and the internal data is a List of data containing the data
     * inside the Nodes that this data's Node has Edges to.
     */
    public Map<E, List<N>> edgesOf(N nodeData) {
        Node<N,E> current = nodeWithData(nodeData);
        return current.nodeEdges();
    }

    /**
     * @return returns the number of Nodes in this Graph
     */
    public int numberOfNodes() {
        return entries.size();
    }

    /**
     * @return the number of Edges found in this graph
     */
    public int numberOfEdges() {
        int sum = 0;
        for(int i = 0; i < entries.size(); i++) {
            sum+= entries.get(i).numberOfEdges();
        }
        return sum;
    }

    /**
     * @spec.requires Nodes exist with startingData and endingData as their respective data
     * @param startingData is the parent Node from which the sought edges originate
     * @param endingData is the child Node into which the sought edges are leading
     * @return a List of data containing the Labels of edges originating at
     * the Node with startingData and ending at endingData
     */
    public List<E> edgesBetween(N startingData, N endingData) {
        Node<N,E> current = nodeWithData(startingData);
        return current.edgesTo(endingData);
    }

    /**
     * @spec.requires The graph contains a Node with data 'starting data' which
     * has an edge labeled by 'label'
     * @param startingData is the data of the Node from which this method starts
     * @param label is the Edge label this method follows to find children Nodes along
     * @return returns a List listing all data in Nodes with Edges labeled by 'label'
     *  leading into them from the Node with startingNode as its data.
     */
    public List<N> nextNodesForLabel(N startingData, E label) {
        Node<N,E> current = nodeWithData(startingData);
        return current.getChildrenOfLabel(label);
    }

    /**
     * @spec.requires Node with startingData exists in Graph
     * @param startingData is the data in the Node this method
     * finds the children of
     * @return a List containing the data of all Nodes
     * which are the direct child of the Node with given data
     */
    public List<N> childNodes(N startingData){
        Node<N,E> current = nodeWithData(startingData);
        return current.childNodes();
    }


    /**
     * @spec.requires entries is sorted alphabetically
     * @param data is the data held within the Node being sought
     * @return returns the Node holding data equal to given data,
     * if no such Node is present, returns null
     */
    private Node<N,E> nodeWithData(N data) {
        int index = Collections.binarySearch(entries, new Node<N,E>(data));
        if(index < 0) {
            return null;
        }
        return entries.get(index);
    }

    /**
     * @spec.requires entries is sorted alphabetically
     * @return returns a List of all data stored in every
     * Node of the graph sorted lexicographically
     */
    public List<N> allNodes() {
        List<N> allNodes = new ArrayList<N>();
        for(int i = 0; i < entries.size(); i++) {
            allNodes.add(entries.get(i).getData());
        }
        return allNodes;
    }

    /**
     * @return graph as String with one Node listed on every line
     * followed by a series of pairs, the first element of which is
     * the label of one of the Node's Edge and second element is
     * the data stored in the Node this leads to
     */
    @Override
    public String toString() {
        String allGraph = "";
        for(int i = 0; i < entries.size(); i++) {
            allGraph = allGraph + "\n" + entries.get(i).getData() + "'s connections:  " + edgesOf(entries.get(i).getData());
        }
        if(allGraph.length() > 0) {
            allGraph = allGraph.substring(1);
        }
        return allGraph;
    }

    /**
     * Ensures representation invariant holds
     */
    //Currently commented out to improve performance
    private void checkRep() {
        /*
        assert(entries != null);
        //inv1: after i iterations, i many nodes have been found
        //not to be null and to have non-null edges with children
        //in the graph
        for(Node currentNode : entries) {
            assert(currentNode != null);
        //inv2: inv1 and after j iterations of this loop
        //j many edges have been found not to be null
        //and to have a childNode in the graph
            for(Edge currentEdge : currentNode.getEdges()) {
                assert(currentEdge != null);
                assert(entries.contains(currentEdge.getChild()));
            }
        }

        for(int i = 0; i < entries.size() - 1; i++) {
            assert(!entries.get(i).getData().equals(entries.get(i + 1).getData()));
        }

        //inv1: after i iterations, i many Nodes have been found to
        //have edges with no duplicate entries
        for(Node currentNode : entries) {
            int entriesSize = currentNode.getEdges().size();
            Set<Edge> edgeSet = new TreeSet<Edge>();
        //inv2: inv1 and after j iterations, j many edges
        //have been added to the Set egdeSet
            for(int i = 0; i < currentNode.getEdges().size(); i++) {
                edgeSet.add(currentNode.getEdges().get(i));
            }
            assert(edgeSet.size() == entriesSize);
        }
        */
    }


    /**
     * Abstract state:
     * Edge represents an immutable directed graph edge containing reference to exactly
     * one Node and a label with data
     */
    private class Edge<N extends Comparable<N>, E extends Comparable<E>> implements Comparable<Edge<N,E>>{

        //$$ Consider Storing child as just its data type
        private final Node<N,E> child; //$$ probably will have to change to child<Type>
        private final E label;

        // Representation Invariant for every Edge:
        // child != null && label != null
        //
        // Abstraction Function:
        // Edge represents an edge in a multigraph where the
        // Node by which this Edge is held is the parent node,
        // the Node held by this Edge is its child node, and
        // the label field is the label of said edge

        /**
         * @spec.requires labelName, offSpring are not null
         * @spec.effects Constructs a new Edge with reference to given Node and holding given
         * label
         */
        public Edge(E labelName, Node<N,E> offspring) {
            child = offspring;
            label = labelName;
            this.checkRep();
        }

        /**
         * @return the label on this edge
         */
        public E getLabel() {
            return label;
        }

        /**
         * @return the child node this edge points to
         */
        public Node<N,E> getChild() { //$$$ will likely need to add type for node
            return child;
        }


        /**
         * @spec.requires Edges being compared belong to the same node
         * @param other is the Edge being compared to this one for containing the same information
         * @return returns true if other Edge has the same Node and label
         * as this, false otherwise
         */
        public boolean sameEdgeAs(Edge<N,E> other) {
            return child.dataEquals(other.getChild()) && label.equals(other.getLabel());
        }

        /**
         * Ensures representation invariant holds
         */
        private void checkRep() {
            assert(child != null && label != null);
        }

        //$$hopefully still works
        public int compareTo(Edge<N,E> other) {
            if(this.getChild().getData().compareTo(other.getChild().getData()) != 0) {
                return this.getChild().getData().compareTo(other.getChild().getData());
            } else {
                return this.getLabel().compareTo(other.getLabel());
            }
        }
    }

    /**
     * Abstract state:
     * Node represents a directed graph node containing one unchanging piece of data of a type
     * specified by the user and any number of data-labeled edges leading to other nodes
     *
     */
    private class Node<N extends Comparable<N>, E extends Comparable<E>> implements Comparable<Node<N,E>>{

        private final N data;
        private final List<Edge<N,E>> edges;

        // Representation Invariant for every Node:
        // data != null && edges != null && no element in edges is null
        // every element in edges not have null label or child node.
        // No two of this nodes edges can have the same label and go
        // to the same Node.
        // Also the Edges of a Node must be stored in alphabetical order
        //
        // Abstraction Function:
        // Node represents a node in a multigraph. This Node
        // is the parent node of every edge represented by the Edges
        // in the edges field. The Nodes that edges then lead
        // to are the child nodes of this node.


        /**
         * @spec.effects Constructs a new Node containing given data
         * @param dataGiven is the Node's data
         */
        public Node(N dataGiven) {
            data = dataGiven;
            edges = new ArrayList<Edge<N,E>>();
        }

        /**
         * @return this Node's data
         */
        public N getData() {
            return data;
        }

        /**
         * @param endingData is the data found within the Node that this method
         * finds Edges to that originate in this Node
         * @return A List containing the labels of Edges originating
         * in this Node and leading to the Node containing endingData
         */
        public List<E> edgesTo(N endingData) {
            List<E> labels = new ArrayList<E>();
            for(int i = 0; i < edges.size(); i++) {
                if(edges.get(i).getChild().getData().equals(endingData)) {
                    labels.add(edges.get(i).getLabel());
                }
            }
            return labels;
        }

        /**
         * @return the number of edges found in this graph
         */
        public int numberOfEdges() {
            return edges.size();
        }

        /**
         * @return a List of the data found in Nodes
         * which are direct children of this Node
         */
        public List<N> childNodes(){
            List<N> children = new ArrayList<N>();
            for(int i = 0; i < edges.size(); i++) {
                children.add(edges.get(i).getChild().getData());
            }
            return children;

        }
        /**
         * @return a Map where the keys are the data
         * which label the Edges leading away from this Node
         * and the internal data is a List containing the data
         * inside the Nodes that this Node has Edges to.
         */
        public Map<E, List<N>> nodeEdges() {
            Map<E, List<N>> edgeToNodes = new TreeMap<E, List<N>>();
            //inv: after i iterations of this loop, i pairs of Edge label for keys
            //and Node data for entries have been made into the Map edgeToNodes
            for(Edge<N,E> current : edges) {
                if(!edgeToNodes.containsKey(current.getLabel())) {
                    List<N> nodeList = new ArrayList<N>();
                    nodeList.add(current.getChild().getData());
                    edgeToNodes.put(current.getLabel(), nodeList);
                } else {
                    edgeToNodes.get(current.getLabel()).add(current.getChild().getData());
                }
            }
            return edgeToNodes;
        }

        /**
         * @spec.requires given object is a Node
         * @param second is the Node to compare against this for equal data
         * @return returns true if the data stored in each Node is identical
         * otherwise returns false
         */
        public boolean dataEquals(Node<N,E> second) {
            return data.equals(second.getData());
        }

        /**
         * @param newEdge is an additional Edge to add to the this Node
         * @return returns true if the Edge was successfully added, returns false otherwise
         * @spec.modifies if this Node does not already have an identical edge,
         * this Node gains another edge to point to, otherwise nothing is modified
         * @spec.requires newEdge is not null
         */
        public boolean addEdge(Edge<N,E> newEdge) {
            int index = Collections.binarySearch(edges, newEdge);
            //if index >= 0 this Node already has an identical Edge
            if(index < 0) {
                edges.add(-1 * index - 1, newEdge );
                this.checkRep();
            }
            return index < 0;
        }

        /**
         * @spec.modifies if the given Edge is found on this Node it is
         * removed from the graph, otherwise nothing is modified
         * @spec.requires 'unwantedEdge' is not null
         * of this Node
         * @param unwantedEdge is the Edge to be removed from this Node
         * @return returns true if the specified Edge is found and removed
         * otherwise returns false
         */
        public boolean removeEdge(Edge<N,E> unwantedEdge) {
            //inv: for all n such that 0 <= n < i
            //!unwantedEdge.equals(edges.get(n))
            boolean found = false;
            for(int i = 0; i < edges.size(); i++) {
                if(edges.get(i).sameEdgeAs(unwantedEdge)) {
                    edges.remove(i);
                    this.checkRep();
                    found = true;
                }
            }
            return found;
        }

        /**
         * @spec.modifies all Edges leading from this Node to the specified
         * Node are removed from the graph
         * @spec.requires 'childNode' is not null
         * @param childNode is the Node for which all Edges leading to it
         * from this Node will be removed
         */
        public void removeEdgesLeadingTo(Node<N,E> childNode) {
            for(int i = edges.size() - 1; i >= 0 ; i--) {
                if(edges.get(i).getChild().dataEquals(childNode)) {
                    edges.remove(i);
                }
            }
            this.checkRep();
        }

        /**
         * @spec.requires 'label' is not null
         * @param label is a datum representing the label of the Edge or Edges (of type
         * specified by the user) along which to find other Nodes
         * @return returns a List of the data of every other Node
         * this Node can reach by traveling along Edges with a label matching the data
         * given
         */
        public List<N> getChildrenOfLabel(E label) {
            List<N> reachableNodes = new ArrayList<N>();
            for(int i = 0; i < edges.size(); i++) {
                if(edges.get(i).getLabel().equals(label)){
                    reachableNodes.add(edges.get(i).getChild().getData());
                }
            }
            return reachableNodes;
        }

        /**
         * @spec.requires neither Node parameter is null
         * @param firstNode is the first of the Nodes being compared
         * @param secondNode is the second of the Nodes being compared
         * @return returns a positive value if the data in firstNode is
         * after that in secondNode, a negative value if the data stored in
         * secondNoce is after that in firstNode and returns 0 if the
         * data stored in the two Nodes is identical
         */
        public int compareTo(Node<N,E> secondNode) {
            return(this.getData().compareTo(secondNode.getData()));
        }

        /**
         * This is only to be used to assist checkRep()
         * @return the Edges of this Node
         */
        private List<Edge<N,E>> getEdges(){
            return edges;
        }

        /**
         * Ensures representation invariant holds
         */
        //Currently commented out to improve performance
        private void checkRep() {
            assert(data != null && edges != null);
            //for(Edge current : edges) {
            //assert(current != null);
            //current.checkRep();
            //}
            //int entriesSize = this.getEdges().size();
            //Set<Edge> edgeSet = new HashSet<Edge>();
            //for(int i = 0; i < this.getEdges().size(); i++) {
            //edgeSet.add(this.getEdges().get(i));
            //}
            //assert(edgeSet.size() == entriesSize);
        }
    }
}