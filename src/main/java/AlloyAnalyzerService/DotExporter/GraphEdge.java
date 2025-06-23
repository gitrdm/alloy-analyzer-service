package AlloyAnalyzerService.DotExporter;

import java.awt.*;

public class GraphEdge {

    // =============================== fields
    // =======================================================================================

    /**
     * a user-provided annotation that will be associated with this edge (can be
     * null) (need not be unique)
     */
    public final Object uuid;

    /**
     * a user-provided annotation that will be associated with this edge (all edges
     * with same group will be highlighted together)
     */
    public final Object group;

    /**
     * The "from" node; must stay in sync with GraphNode.ins and GraphNode.outs and
     * GraphNode.selfs
     */
    private GraphNode a;

    /**
     * The "to" node; must stay in sync with GraphNode.ins and GraphNode.outs and
     * GraphNode.selfs
     */
    private GraphNode b;

    /**
     * The label (can be ""); NOTE: label will be drawn only if the start node is
     * not a dummy node.
     */
    private final String label;

    /**
     * Whether to draw an arrow head on the "from" node; default is false.
     */
    private boolean ahead = false;

    /**
     * Whether to draw an arrow head on the "to" node; default is true.
     */
    private boolean bhead = true;

    /**
     * The color of the edge; default is BLACK; never null.
     */
    private Color color = Color.BLACK;

    /**
     * The line-style of the edge; default is SOLID; never null.
     */
    private DotStyle style = DotStyle.SOLID;

    /**
     * The edge weight; default is 1; always between 1 and 10000 inclusively.
     */
    private int weight = 1;

    // =========================================================================s====================================================

    /**
     * Construct an edge from "from" to "to" with the given arrow head settings,
     * then add the edge to the graph.
     */
    GraphEdge(GraphNode from, GraphNode to, Object uuid, String label, boolean drawArrowHeadOnFrom, boolean drawArrowHeadOnTo, DotStyle style, Color color, Object group) {
        if (group instanceof GraphNode)
            throw new IllegalArgumentException("group cannot be a GraphNode");
        if (group instanceof GraphEdge)
            throw new IllegalArgumentException("group cannot be a GraphEdge");
        if (group == null) {
            group = new Object();
        }
        a = from;
        b = to;
        if (a.graph != b.graph)
            throw new IllegalArgumentException("You cannot draw an edge between two different graphs.");
        if (a == b) {
            a.selfs.add(this);
        } else {
            a.outs.add(this);
            b.ins.add(this);
        }
        a.graph.edgelist.add(this);
        this.uuid = uuid;
        this.group = group;
        this.label = (label == null) ? "" : label;
        this.ahead = drawArrowHeadOnFrom;
        this.bhead = drawArrowHeadOnTo;
        if (style != null)
            this.style = style;
        if (color != null)
            this.color = color;
    }

    /**
     * Construct an edge from "from" to "to", then add the edge to the graph.
     */
    public GraphEdge(GraphNode from, GraphNode to, Object uuid, String label, Object group) {
        this(from, to, uuid, label, false, true, null, null, group);
    }

    /**
     * Returns the "from" node.
     */
    public GraphNode a() {
        return a;
    }

    /**
     * Returns the "to" node.
     */
    public GraphNode b() {
        return b;
    }

    /**
     * Swaps the "from" node and "to" node.
     */
    void reverse() {
        if (a == b)
            return;
        a.outs.remove(this);
        b.ins.remove(this);
        a.ins.add(this);
        b.outs.add(this);
        GraphNode x = a;
        a = b;
        b = x;
    }

    /**
     * Changes the "to" node to the given node.
     */
    void change(GraphNode newTo) {
        if (b.graph != newTo.graph)
            throw new IllegalArgumentException("You cannot draw an edge between two different graphs.");
        if (a == b)
            a.selfs.remove(this);
        else {
            a.outs.remove(this);
            b.ins.remove(this);
        }
        b = newTo;
        if (a == b)
            a.selfs.add(this);
        else {
            a.outs.add(this);
            b.ins.add(this);
        }
    }

    /**
     * Returns the edge weight (which is always between 1 and 10000 inclusively).
     */
    public int weight() {
        return weight;
    }

    /**
     * Returns the line style; never null.
     */
    public DotStyle style() {
        return style;
    }

    /**
     * Returns the line color; never null.
     */
    public Color color() {
        return color;
    }

    /**
     * Returns true if we will draw an arrow head on the "from" node.
     */
    public boolean ahead() {
        return ahead;
    }

    /**
     * Returns true if we will draw an arrow head on the "to" node.
     */
    public boolean bhead() {
        return bhead;
    }

    /**
     * Returns the label on this edge.
     */
    public String label() {
        return label;
    }

    /**
     * Returns the label for this edge.
     */
    public String getLabel() {
        return label == null ? "" : label;
    }

    /**
     * Returns the color for this edge as a hex string (e.g., #RRGGBB).
     */
    public String getColor() {
        if (color == null) return "#000000";
        int rgb = color.getRGB() & 0xFFFFFF;
        return String.format("#%06x", rgb);
    }

    /**
     * Returns the source node (from).
     */
    public GraphNode getA() {
        return a;
    }

    /**
     * Returns the target node (to).
     */
    public GraphNode getB() {
        return b;
    }

    /**
     * Sets the edge weight between 1 and 10000.
     */
    public GraphEdge set(int weightBetween1And10000) {
        if (weightBetween1And10000 < 1)
            weightBetween1And10000 = 1;
        if (weightBetween1And10000 > 10000)
            weightBetween1And10000 = 10000;
        weight = weightBetween1And10000;
        return this;
    }

    /**
     * Sets whether we will draw an arrow head on the "from" node, and whether we
     * will draw an arrow head on the "to" node.
     */
    public GraphEdge set(boolean from, boolean to) {
        this.ahead = from;
        this.bhead = to;
        return this;
    }

    /**
     * Sets the line style.
     */
    public GraphEdge set(DotStyle style) {
        if (style != null)
            this.style = style;
        return this;
    }

    /**
     * Sets the line color.
     */
    public GraphEdge set(Color color) {
        if (color != null)
            this.color = color;
        return this;
    }

    /**
     * Returns a DOT representation of this edge (or "" if the start node is a dummy
     * node)
     */
    @Override
    public String toString() {
        GraphNode a = this.a, b = this.b;
        if (a.shape() == null)
            return ""; // This means this edge is virtual
        while (b.shape() == null) {
            b = b.outs.get(0).b;
        }
        String color = Integer.toHexString(this.color.getRGB() & 0xFFFFFF);
        while (color.length() < 6) {
            color = "0" + color;
        }
        StringBuilder out = new StringBuilder();
        out.append("\"N" + a.pos() + "\"");
        out.append(" -> ");
        out.append("\"N" + b.pos() + "\"");
        out.append(" [");
        out.append("uuid = \"" + (uuid == null ? "" : Graph.esc(uuid.toString())) + "\"");
        out.append(", color = \"#" + color + "\"");
        out.append(", fontcolor = \"#" + color + "\"");
        out.append(", style = \"" + style.getDotText() + "\"");
        out.append(", label = \"" + Graph.esc(label) + "\"");
        out.append(", dir = \"" + (ahead && bhead ? "both" : (bhead ? "forward" : "back")) + "\"");
        out.append(", weight = \"" + weight + "\"");
        out.append("]\n");
        return out.toString();
    }
}
