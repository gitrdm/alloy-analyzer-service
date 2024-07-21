package AlloyAnalyzerService.DotExporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Graph {

    // ================================ adjustable options
    // ========================================================================//

    /**
     * Minimum horizontal distance between adjacent nodes.
     */
    static final int xJump = 30;

    /**
     * Minimum vertical distance between adjacent layers.
     */
    static final int yJump = 60;

    /**
     * The horizontal distance between the first self-loop and the node itself.
     */
    static final int selfLoopA = 40;

    /**
     * The horizontal padding to put on the left side of a self-loop's edge label.
     */
    static final int selfLoopGL = 2;

    /**
     * The horizontal padding to put on the right side of a self-loop's edge label.
     */
    static final int selfLoopGR = 20;

    // =============================== fields
    // ======================================================================================//

    /**
     * The default magnification.
     */
    final double defaultScale;

    /**
     * The left edge.
     */
    private int left = 0;

    /**
     * The top edge.
     */
    private int top = 0;

    /**
     * The bottom edge.
     */
    private int bottom = 0;

    /**
     * The total width of the graph; this value is computed by layout().
     */
    private int totalWidth = 0;

    /**
     * The total height of the graph; this value is computed by layout().
     */
    private int totalHeight = 0;


    /**
     * The list of nodes; must stay in sync with GraphNode.graph and GraphNode.pos
     * (every node is in exactly one graph's nodelist, and appears exactly once in
     * that graph's nodelist)
     */
    final List<GraphNode> nodelist = new ArrayList<GraphNode>();

    /**
     * The list of edges; must stay in sync with GraphEdge.a.graph and
     * GraphEdge.b.graph (every edge is in exactly one graph's edgelist, and appears
     * exactly once in that graph's edgelist)
     */
    final List<GraphEdge> edgelist = new ArrayList<GraphEdge>();

    /**
     * An unmodifiable view of the list of nodes.
     */
    public final List<GraphNode> nodes = Collections.unmodifiableList(nodelist);

    /**
     * An unmodifiable view of the list of edges.
     */
    public final List<GraphEdge> edges = Collections.unmodifiableList(edgelist);

    /**
     * An unmodifiable empty list.
     */
    private final List<GraphNode> emptyListOfNodes = Collections.unmodifiableList(new ArrayList<GraphNode>(0));

    // ============================================================================================================================//

    /**
     * Constructs an empty Graph object.
     */
    public Graph(double defaultScale) {
        this.defaultScale = defaultScale;
    }

    // ============================================================================================================================//


    /**
     * Helper method that encodes a String for printing into a DOT file.
     */
    static String esc(String name) {
        if (name.indexOf('\"') < 0)
            return name;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '\"')
                out.append('\\');
            out.append(c);
        }
        return out.toString();
    }

    // ============================================================================================================================//

    /**
     * Returns a DOT representation of this graph.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph \"graph\" {\n" + "graph [fontsize=12]\n" + "node [fontsize=12]\n" + "edge [fontsize=12]\n" + "rankdir=TB;\n");
        for (GraphEdge e : edges)
            sb.append(e);
        for (GraphNode n : nodes)
            sb.append(n);
        sb.append("}\n");
        return sb.toString();
    }
}
