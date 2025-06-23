package AlloyAnalyzerService.DotExporter;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GraphNode {

    // =============================== adjustable options
    // ==================================================

    /**
     * This determines the minimum width of a dummy node.
     */
    private static final int dummyWidth = 30;

    /**
     * This determines the minimum height of a dummy node.
     */
    private static final int dummyHeight = 10;

    /**
     * This determines the minimum amount of padding added above, left, right, and
     * below the text label.
     */
    private static final int labelPadding = 5;

    /**
     * Color to use to show a highlighted node.
     */
    private static final Color COLOR_CHOSENNODE = Color.LIGHT_GRAY;

    // =============================== cached for performance
    // ===================================

    /**
     * Caches the value of sqrt(3.0). The extra digits in the definition will be
     * truncated by the Java compiler.
     */
    private static final double sqrt3 = 1.7320508075688772935274463415058723669428052538103806280558D;

    /**
     * Caches the value of sin(36 degree). The extra digits in the definition will
     * be truncated by the Java compiler.
     */
    private static final double sin36 = 0.5877852522924731291687059546390727685976524376431459910723D;

    /**
     * Caches the value of cos(36 degree). The extra digits in the definition will
     * be truncated by the Java compiler.
     */
    private static final double cos36 = 0.8090169943749474241022934171828190588601545899028814310677D;

    /**
     * Caches the value of cos(18 degree). The extra digits in the definition will
     * be truncated by the Java compiler.
     */
    private static final double cos18 = 0.9510565162951535721164393333793821434056986341257502224473D;

    /**
     * Caches the value of tan(18 degree). The extra digits in the definition will
     * be truncated by the Java compiler.
     */
    private static final double tan18 = 0.3249196962329063261558714122151344649549034715214751003078D;

    // =============================== these fields do not affect the computed
    // bounds ===============================================

    /**
     * a user-provided annotation that will be associated with this node (can be
     * null) (need not be unique)
     */
    public final Object uuid;

    /**
     * The graph that this node belongs to; must stay in sync with Graph.nodelist
     * and Graph.layerlist
     */
    final Graph graph;

    /**
     * The layer that this node is in; must stay in sync with Graph.layerlist
     */
    private int layer = 0;

    /**
     * The current position of this node in the graph's node list; must stay in sync
     * with Graph.nodelist
     */
    int pos;

    /**
     * The "in" edges not including "self" edges; must stay in sync with GraphEdge.a
     * and GraphEdge.b
     */
    final LinkedList<GraphEdge> ins = new LinkedList<GraphEdge>();

    /**
     * The "out" edges not including "self" edges; must stay in sync with
     * GraphEdge.a and GraphEdge.b
     */
    final LinkedList<GraphEdge> outs = new LinkedList<GraphEdge>();

    // =============================== these fields affect the computed bounds
    // ===================================================

    /**
     * The "self" edges; must stay in sync with GraphEdge.a and GraphEdge.b
     * <p>
     * When this value changes, we should invalidate the previously computed bounds
     * information.
     */
    final LinkedList<GraphEdge> selfs = new LinkedList<GraphEdge>();

    /**
     * The font boldness.
     * <p>
     * When this value changes, we should invalidate the previously computed bounds
     * information.
     */
    private boolean fontBold = false;

    /**
     * The node labels; if null or empty, then the node has no labels.
     * <p>
     * When this value changes, we should invalidate the previously computed bounds
     * information.
     */
    private java.util.List<String> labels = null;

    /**
     * The node color; never null.
     * <p>
     * When this value changes, we should invalidate the previously computed bounds
     * information.
     */
    private Color color = Color.WHITE;

    /**
     * The line style; never null.
     * <p>
     * When this value changes, we should invalidate the previously computed bounds
     * information.
     */
    private DotStyle style = DotStyle.SOLID;

    /**
     * The node shape; if null, then the node is a dummy node.
     * <p>
     * When this value changes, we should invalidate the previously computed bounds
     * information.
     */
    private DotShape shape = DotShape.BOX;

    // ===================================================================================================

    /**
     * Create a new node with the given list of labels, then add it to the given
     * graph.
     */
    public GraphNode(Graph graph, Object uuid, String... labels) {
        this.uuid = uuid;
        this.graph = graph;
        this.pos = graph.nodelist.size();
        graph.nodelist.add(this);
        if (labels != null && labels.length > 0) {
            this.labels = new ArrayList<String>(labels.length);
            for (int i = 0; i < labels.length; i++)
                this.labels.add(labels[i]);
        }
    }

    /**
     * Returns an unmodifiable view of the list of "in" edges.
     */
    public java.util.List<GraphEdge> inEdges() {
        return Collections.unmodifiableList(ins);
    }

    /**
     * Returns an unmodifiable view of the list of "out" edges.
     */
    public java.util.List<GraphEdge> outEdges() {
        return Collections.unmodifiableList(outs);
    }

    /**
     * Returns an unmodifiable view of the list of "self" edges.
     */
    public List<GraphEdge> selfEdges() {
        return Collections.unmodifiableList(selfs);
    }

    /**
     * Returns the node's current position in the node list, which is always between
     * 0 and node.size()-1
     */
    int pos() {
        return pos;
    }

    /**
     * Returns the node shape (or null if the node is a dummy node).
     */
    DotShape shape() {
        return shape;
    }

    /**
     * Changes the node shape (where null means change the node into a dummy node),
     * then invalidate the computed bounds.
     */
    public GraphNode set(DotShape shape) {
        if (this.shape != shape) {
            this.shape = shape;
        }
        return this;
    }

    /**
     * Changes the node color, then invalidate the computed bounds.
     */
    public GraphNode set(Color color) {
        if (this.color != color && color != null) {
            this.color = color;
        }
        return this;
    }

    /**
     * Changes the line style, then invalidate the computed bounds.
     */
    public GraphNode set(DotStyle style) {
        if (this.style != style && style != null) {
            this.style = style;
        }
        return this;
    }

    /**
     * Changes the font boldness, then invalidate the computed bounds.
     */
    public GraphNode setFontBoldness(boolean bold) {
        if (this.fontBold != bold) {
            this.fontBold = bold;
        }
        return this;
    }

    /**
     * Add the given label after the existing labels, then invalidate the computed
     * bounds.
     */
    public GraphNode addLabel(String label) {
        if (label == null || label.length() == 0)
            return this;
        if (labels == null)
            labels = new ArrayList<String>();
        labels.add(label);
        return this;
    }

    /**
     * Returns a DOT representation of this node (or "" if this is a dummy node)
     */
    @Override
    public String toString() {
        if (shape == null)
            return ""; // This means it's a virtual node
        int rgb = color.getRGB() & 0xFFFFFF;
        String text = (rgb == 0xFF0000 || rgb == 0x0000FF || rgb == 0) ? "FFFFFF" : "000000";
        String main = Integer.toHexString(rgb);
        while (main.length() < 6) {
            main = "0" + main;
        }
        StringBuilder out = new StringBuilder();
        out.append("\"N" + pos + "\"");
        out.append(" [");
        out.append("uuid=\"");
        if (uuid != null)
            out.append(Graph.esc(uuid.toString()));
        out.append("\", label=\"");
        boolean first = true;
        if (labels != null)
            for (String label : labels)
                if (label.length() > 0) {
                    out.append((first ? "" : "\\n") + Graph.esc(label));
                    first = false;
                }
        out.append("\", color=\"#" + main + "\"");
        out.append(", fontcolor = \"#" + text + "\"");
        out.append(", shape = \"" + shape.getDotText() + "\"");
        out.append(", style = \"filled, " + style.getDotText() + "\"");
        out.append("]\n");
        return out.toString();
    }

    /**
     * Returns the node label as a single string (concatenates all labels with \n).
     */
    public String getLabel() {
        if (labels == null || labels.isEmpty()) return "";
        return String.join("\n", labels);
    }

    /**
     * Returns the node color as a hex string (e.g., #RRGGBB).
     */
    public String getColor() {
        if (color == null) return "#000000";
        int rgb = color.getRGB() & 0xFFFFFF;
        return String.format("#%06x", rgb);
    }

    /**
     * Returns the node shape as a string.
     */
    public String getShape() {
        return shape == null ? "ellipse" : shape.toString().toLowerCase();
    }
}
