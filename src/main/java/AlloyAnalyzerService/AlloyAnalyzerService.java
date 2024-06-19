package AlloyAnalyzerService;

import static edu.mit.csail.sdg.alloy4graph.Artist.getBounds;

import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ConstSet;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.OurPDFWriter;
import edu.mit.csail.sdg.alloy4.Pair;
import edu.mit.csail.sdg.alloy4.Util;
import edu.mit.csail.sdg.alloy4.XMLNode;
// import edu.mit.csail.sdg.alloy4graph.DotColor; <-- modified
// import edu.mit.csail.sdg.alloy4graph.Artist; <-- modified
import edu.mit.csail.sdg.alloy4graph.DotDirection;
import edu.mit.csail.sdg.alloy4graph.DotPalette;
// import edu.mit.csail.sdg.alloy4graph.DotShape; <-- modified
// import edu.mit.csail.sdg.alloy4graph.DotStyle; <-- modified
// import edu.mit.csail.sdg.alloy4graph.Graph; <-- modified
// import edu.mit.csail.sdg.alloy4graph.GraphEdge; <-- modified
// import edu.mit.csail.sdg.alloy4graph.GraphNode; <-- modified
import edu.mit.csail.sdg.alloy4viz.AlloyAtom;
import edu.mit.csail.sdg.alloy4viz.AlloyElement;
import edu.mit.csail.sdg.alloy4viz.AlloyInstance;
import edu.mit.csail.sdg.alloy4viz.AlloyModel;
import edu.mit.csail.sdg.alloy4viz.AlloyProjection;
import edu.mit.csail.sdg.alloy4viz.AlloyRelation;
import edu.mit.csail.sdg.alloy4viz.AlloySet;
import edu.mit.csail.sdg.alloy4viz.AlloyTuple;
import edu.mit.csail.sdg.alloy4viz.AlloyType;
// import edu.mit.csail.sdg.alloy4viz.VizState; <-- modified
import edu.mit.csail.sdg.alloy4viz.StaticInstanceReader;
import edu.mit.csail.sdg.alloy4viz.StaticProjector;
// import edu.mit.csail.sdg.alloy4viz.StaticGraphMaker; <-- modified
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Module;
import edu.mit.csail.sdg.parser.CompUtil;
import edu.mit.csail.sdg.translator.A4Options;
import edu.mit.csail.sdg.translator.A4Solution;
import edu.mit.csail.sdg.translator.TranslateAlloyToKodkod;

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import alloy.AlloyAnalyzerGrpc;
import alloy.AlloyAnalyzerOuterClass;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4viz.AlloyInstance;
import edu.mit.csail.sdg.alloy4viz.StaticInstanceReader;
import edu.mit.csail.sdg.alloy4viz.VizGUI;
import edu.mit.csail.sdg.ast.Sig;
import edu.mit.csail.sdg.translator.*;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4viz.AlloyAtom;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.ast.Module;
import edu.mit.csail.sdg.parser.CompUtil;

class Artist {

    /** The font name. */
    private static final String fontName = "Lucida Grande";

    /** The font size. */
    private static final int    fontSize = 12;

    /** The corresponding OurPDFWriater. */
    private OurPDFWriter        pdf;

    /**
     * Construct an empty artist.
     */
    public Artist() {
        this.pdf = null;
    }

    /**
     * Construct an artist that acts as a wrapper around the given OurPDFWriter
     * object.
     */
    public Artist(OurPDFWriter pdfWriter) {
        this.pdf = pdfWriter;
    }

    /** Shifts the coordinate space by the given amount. */
    public void translate(int x, int y) {
        pdf.shiftCoordinateSpace(x, y);
    }

    /** Draws a circle of the given radius, centered at (0,0) */
    public void drawCircle(int radius) {
        pdf.drawCircle(radius, false);
    }

    /** Fills a circle of the given radius, centered at (0,0) */
    public void fillCircle(int radius) {
        pdf.drawCircle(radius, true);
    }

    /** Draws a line from (x1,y1) to (x2,y2) */
    public void drawLine(int x1, int y1, int x2, int y2) {
        pdf.drawLine(x1, y1, x2, y2);
    }

    /** Changes the current color. */
    public void setColor(Color color) {
        pdf.setColor(color);
    }

    /** Returns true if left<=x<=right or right<=x<=left. */
    private static boolean in(double left, double x, double right) {
        return (left <= x && x <= right) || (right <= x && x <= left);
    }

    /**
     * Modifies the given Graphics2D object to use the line style representing by
     * this object.
     * <p>
     * NOTE: as a special guarantee, if gr2d==null, then this method returns
     * immediately without doing anything.
     * <p>
     * NOTE: just like the typical AWT and Swing methods, this method can be called
     * only by the AWT event dispatching thread.
     */
    public void set(DotStyle style, double scale) {
        switch (style) {
            case BOLD :
                pdf.setBoldLine();
                return;
            case DOTTED :
                pdf.setDottedLine();
                return;
            case DASHED :
                pdf.setDashedLine();
                return;
            default :
                pdf.setNormalLine();
                return;
        }
    }

    /** Saves the current font boldness. */
    private boolean fontBoldness = false;

    /** Changes the current font. */
    public void setFont(boolean fontBoldness) {
        this.fontBoldness = fontBoldness;
    }
}

class GraphNode {

    // =============================== adjustable options
    // ==================================================

    /** This determines the minimum width of a dummy node. */
    private static final int   dummyWidth       = 30;

    /** This determines the minimum height of a dummy node. */
    private static final int   dummyHeight      = 10;

    /**
     * This determines the minimum amount of padding added above, left, right, and
     * below the text label.
     */
    private static final int   labelPadding     = 5;

    /** Color to use to show a highlighted node. */
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
    public final Object         uuid;

    /**
     * The graph that this node belongs to; must stay in sync with Graph.nodelist
     * and Graph.layerlist
     */
    final Graph                 graph;

    /**
     * The layer that this node is in; must stay in sync with Graph.layerlist
     */
    private int                 layer   = 0;

    /**
     * The current position of this node in the graph's node list; must stay in sync
     * with Graph.nodelist
     */
    int                         pos;

    /**
     * The "in" edges not including "self" edges; must stay in sync with GraphEdge.a
     * and GraphEdge.b
     */
    final LinkedList<GraphEdge> ins     = new LinkedList<GraphEdge>();

    /**
     * The "out" edges not including "self" edges; must stay in sync with
     * GraphEdge.a and GraphEdge.b
     */
    final LinkedList<GraphEdge> outs    = new LinkedList<GraphEdge>();

    // =============================== these fields affect the computed bounds
    // ===================================================

    /**
     * The "self" edges; must stay in sync with GraphEdge.a and GraphEdge.b
     * <p>
     * When this value changes, we should invalidate the previously computed bounds
     * information.
     */
    final LinkedList<GraphEdge> selfs    = new LinkedList<GraphEdge>();

    /**
     * The font boldness.
     * <p>
     * When this value changes, we should invalidate the previously computed bounds
     * information.
     */
    private boolean             fontBold = false;

    /**
     * The node labels; if null or empty, then the node has no labels.
     * <p>
     * When this value changes, we should invalidate the previously computed bounds
     * information.
     */
    private List<String>        labels   = null;

    /**
     * The node color; never null.
     * <p>
     * When this value changes, we should invalidate the previously computed bounds
     * information.
     */
    private Color               color    = Color.WHITE;

    /**
     * The line style; never null.
     * <p>
     * When this value changes, we should invalidate the previously computed bounds
     * information.
     */
    private DotStyle            style    = DotStyle.SOLID;

    /**
     * The node shape; if null, then the node is a dummy node.
     * <p>
     * When this value changes, we should invalidate the previously computed bounds
     * information.
     */
    private DotShape            shape    = DotShape.BOX;

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
    public List<GraphEdge> inEdges() {
        return Collections.unmodifiableList(ins);
    }

    /**
     * Returns an unmodifiable view of the list of "out" edges.
     */
    public List<GraphEdge> outEdges() {
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

    // ===================================================================================================

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
}

class GraphEdge {

    // =============================== fields
    // =======================================================================================

    /**
     * a user-provided annotation that will be associated with this edge (can be
     * null) (need not be unique)
     */
    public final Object              uuid;

    /**
     * a user-provided annotation that will be associated with this edge (all edges
     * with same group will be highlighted together)
     */
    public final Object              group;

    /**
     * The "from" node; must stay in sync with GraphNode.ins and GraphNode.outs and
     * GraphNode.selfs
     */
    private GraphNode                a;

    /**
     * The "to" node; must stay in sync with GraphNode.ins and GraphNode.outs and
     * GraphNode.selfs
     */
    private GraphNode                b;

    /**
     * The label (can be ""); NOTE: label will be drawn only if the start node is
     * not a dummy node.
     */
    private final String             label;

    /**
     * Whether to draw an arrow head on the "from" node; default is false.
     */
    private boolean                  ahead  = false;

    /**
     * Whether to draw an arrow head on the "to" node; default is true.
     */
    private boolean                  bhead  = true;

    /** The color of the edge; default is BLACK; never null. */
    private Color                    color  = Color.BLACK;

    /**
     * The line-style of the edge; default is SOLID; never null.
     */
    private DotStyle                 style  = DotStyle.SOLID;

    /**
     * The edge weight; default is 1; always between 1 and 10000 inclusively.
     */
    private int                      weight = 1;

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

    /** Returns the "from" node. */
    public GraphNode a() {
        return a;
    }

    /** Returns the "to" node. */
    public GraphNode b() {
        return b;
    }

    /** Swaps the "from" node and "to" node. */
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

    /** Changes the "to" node to the given node. */
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

    /** Returns the line style; never null. */
    public DotStyle style() {
        return style;
    }

    /** Returns the line color; never null. */
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

    /** Returns the label on this edge. */
    public String label() {
        return label;
    }

    /** Sets the edge weight between 1 and 10000. */
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

    /** Sets the line style. */
    public GraphEdge set(DotStyle style) {
        if (style != null)
            this.style = style;
        return this;
    }

    /** Sets the line color. */
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



class Graph {

    // ================================ adjustable options
    // ========================================================================//

    /** Minimum horizontal distance between adjacent nodes. */
    static final int  xJump      = 30;

    /** Minimum vertical distance between adjacent layers. */
    static final int  yJump      = 60;

    /**
     * The horizontal distance between the first self-loop and the node itself.
     */
    static final int  selfLoopA  = 40;

    /**
     * The horizontal padding to put on the left side of a self-loop's edge label.
     */
    static final int  selfLoopGL = 2;

    /**
     * The horizontal padding to put on the right side of a self-loop's edge label.
     */
    static final int  selfLoopGR = 20;

    // =============================== fields
    // ======================================================================================//

    /** The default magnification. */
    final double                  defaultScale;

    /** The left edge. */
    private int                   left             = 0;

    /** The top edge. */
    private int                   top              = 0;

    /** The bottom edge. */
    private int                   bottom           = 0;

    /**
     * The total width of the graph; this value is computed by layout().
     */
    private int                   totalWidth       = 0;

    /**
     * The total height of the graph; this value is computed by layout().
     */
    private int                   totalHeight      = 0;


    /**
     * The list of nodes; must stay in sync with GraphNode.graph and GraphNode.pos
     * (every node is in exactly one graph's nodelist, and appears exactly once in
     * that graph's nodelist)
     */
    final List<GraphNode>         nodelist         = new ArrayList<GraphNode>();

    /**
     * The list of edges; must stay in sync with GraphEdge.a.graph and
     * GraphEdge.b.graph (every edge is in exactly one graph's edgelist, and appears
     * exactly once in that graph's edgelist)
     */
    final List<GraphEdge>         edgelist         = new ArrayList<GraphEdge>();

    /** An unmodifiable view of the list of nodes. */
    public final List<GraphNode>  nodes            = Collections.unmodifiableList(nodelist);

    /** An unmodifiable view of the list of edges. */
    public final List<GraphEdge>  edges            = Collections.unmodifiableList(edgelist);

    /** An unmodifiable empty list. */
    private final List<GraphNode> emptyListOfNodes = Collections.unmodifiableList(new ArrayList<GraphNode>(0));

    // ============================================================================================================================//

    /** Constructs an empty Graph object. */
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

    /** Returns a DOT representation of this graph. */
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


/**
 * Immutable; this defines the set of possible colors.
 * <p>
 * <b>Thread Safety:</b> Can be called only by the AWT event thread.
 */

enum DotColor {

    MAGIC("Magic", "magic"),
    YELLOW("Yellow", "gold", "yellow", "lightgoldenrod", "yellow"),
    GREEN(
            "Green",
            "limegreen",
            "green2",
            "darkolivegreen2",
            "chartreuse2"),
    BLUE(
            "Blue",
            "cornflowerblue",
            "blue",
            "cadetblue",
            "cyan"),
    RED(
            "Red",
            "palevioletred",
            "red",
            "salmon",
            "magenta"),
    GRAY("Gray", "lightgray"),
    WHITE("White", "white"),
    BLACK("Black", "black");

    /** The text to display. */
    private final String       displayText;

    /**
     * The list of colors to use, corresponding to the current palette; if there are
     * more palette choices than colors.size(), then the extra palettes would all
     * use the first color.
     */
    private final List<String> colors = new ArrayList<String>();

    /** Construct a new DotColor. */
    private DotColor(String text, String... colors) {
        displayText = text;
        for (int i = 0; i < colors.length; i++) {
            this.colors.add(colors[i]);
        }
    }

    /**
     * This maps each dot color name into the corresponding Java Color object.
     */
    private static final Map<String,Color> name2color = new HashMap<String,Color>();

    /**
     * Returns the list of values that the user is allowed to select from.
     */
    public static Object[] valuesWithout(DotColor exclude) {
        Object[] ans = new Object[values().length - 1];
        int i = 0;
        for (DotColor d : values())
            if (d != exclude)
                ans[i++] = d;
        return ans;
    }

    /**
     * Convert this color into its corresponding Java Color object.
     */
    public Color getColor(DotPalette pal) {
        String name = getDotText(pal);
        Color ans = name2color.get(name);
        if (ans != null)
            return ans;
        else if (name.equals("palevioletred"))
            ans = new Color(222, 113, 148);
        else if (name.equals("red"))
            ans = new Color(255, 0, 0);
        else if (name.equals("salmon"))
            ans = new Color(255, 130, 115);
        else if (name.equals("magenta"))
            ans = new Color(255, 0, 255);
        else if (name.equals("limegreen"))
            ans = new Color(49, 207, 49);
        else if (name.equals("green2"))
            ans = new Color(0, 239, 0);
        else if (name.equals("darkolivegreen2"))
            ans = new Color(189, 239, 107);
        else if (name.equals("chartreuse2"))
            ans = new Color(115, 239, 0);
        else if (name.equals("gold"))
            ans = new Color(255, 215, 0);
        else if (name.equals("yellow"))
            ans = new Color(255, 255, 0);
        else if (name.equals("lightgoldenrod"))
            ans = new Color(239, 223, 132);
        else if (name.equals("cornflowerblue"))
            ans = new Color(99, 150, 239);
        else if (name.equals("blue"))
            ans = new Color(0, 0, 255);
        else if (name.equals("cadetblue"))
            ans = new Color(90, 158, 165);
        else if (name.equals("cyan"))
            ans = new Color(0, 255, 255);
        else if (name.equals("lightgray"))
            ans = new Color(214, 214, 214);
        else if (name.equals("white"))
            ans = Color.WHITE;
        else
            ans = Color.BLACK;
        name2color.put(name, ans);
        return ans;
    }

    /**
     * Returns the String that should be written into the dot file for this value,
     * when used with the given palette.
     */
    public String getDotText(DotPalette pal) {
        int i = 0;
        for (Object choice : DotPalette.values()) {
            if (i >= colors.size())
                break;
            if (pal == choice)
                return colors.get(i);
            i++;
        }
        return colors.get(0);
    }

    /**
     * Returns the String that will be displayed in the GUI to represent this value.
     */
    public String getDisplayedText() {
        return displayText;
    }

    /**
     * This method is used in parsing the XML value into a valid color; returns null
     * if there is no match.
     */
    public static DotColor parse(String x) {
        if (x != null)
            for (DotColor d : values())
                if (d.toString().equals(x))
                    return d;
        return null;
    }

    /** This value is used in writing XML. */
    @Override
    public String toString() {
        return displayText;
    }
}

enum DotShape {

    /** Ellipse */
    ELLIPSE("Ellipse", "ellipse"),
    /** Box */
    BOX("Box", "box"),
    /** Circle */
    CIRCLE("Circle", "circle"),
    /** Egg */
    EGG("Egg", "egg"),
    /** Triangle */
    TRIANGLE("Triangle", "triangle"),
    /** Diamond */
    DIAMOND("Diamond", "diamond"),
    /** Trapezoid */
    TRAPEZOID("Trapezoid", "trapezium"),
    /** Parallelogram */
    PARALLELOGRAM("Parallelogram", "parallelogram"),
    /** House */
    HOUSE("House", "house"),
    /** Hexagon */
    HEXAGON("Hexagon", "hexagon"),
    /** Octagon */
    OCTAGON("Octagon", "octagon"),
    /** Double Circle */
    DOUBLE_CIRCLE("Dbl Circle", "doublecircle"),
    /** Double Octagon */
    DOUBLE_OCTAGON("Dbl Octagon", "doubleoctagon"),
    /** Triple Octagon */
    TRIPLE_OCTAGON("Tpl Octagon", "tripleoctagon"),
    /** Inverted Triangle */
    INV_TRIANGLE("Inv Triangle", "invtriangle"),
    /** Inverted House */
    INV_HOUSE("Inv House", "invhouse"),
    /** Inverted Trapezoid */
    INV_TRAPEZOID("Inv Trapezoid", "invtrapezium"),
    /** Lined Diamond */
    M_DIAMOND("Lined Diamond", "Mdiamond"),
    /** Lined Square */
    M_SQUARE("Lined Square", "Msquare"),
    /** Lined Circle */
    M_CIRCLE("Lined Circle", "Mcircle");

    /** The description of this line style. */
    private final String name;

    /** The corresponding DOT attribute. */
    private final String dotName;

    /** Constructs a DotShape object. */
    private DotShape(String name, String dotName) {
        this.name = name;
        this.dotName = dotName;
    }

    /**
     * Returns the String that will be displayed in the GUI to represent this value.
     */
    public String getDisplayedText() {
        return name;
    }

    /**
     * Returns the String that should be written into the dot file for this value,
     * when used with the given palette.
     */
    public String getDotText() {
        return dotName;
    }

    /**
     * This method is used in parsing the XML value into a valid Shape; returns null
     * if there is no match.
     */
    public static DotShape parse(String x) {
        if (x != null)
            for (DotShape d : values())
                if (d.name.equals(x))
                    return d;
        return null;
    }

    /** This value is used in writing XML. */
    @Override
    public String toString() {
        return name;
    }
}


enum DotStyle {

    /** Solid line. */
    SOLID("Solid", "solid"),

    /** Dashed line. */
    DASHED("Dashed", "dashed"),

    /** Dotted line. */
    DOTTED("Dotted", "dotted"),

    /** Bold line. */
    BOLD("Bold", "bold");

    /** The description of this line style. */
    private final String name;

    /** The corresponding DOT attribute. */
    private final String dotName;

    /** Constructs a DotStyle object. */
    private DotStyle(String name, String dotName) {
        this.name = name;
        this.dotName = dotName;
    }

    /**
     * Returns the String that will be displayed in the GUI to represent this value.
     */
    public String getDisplayedText() {
        return name;
    }

    /**
     * Returns the String that should be written into the dot file for this value,
     * when used with the given palette.
     */
    public String getDotText() {
        return dotName;
    }

    /**
     * This method is used in parsing the XML value into a valid style; returns null
     * if there is no match.
     */
    public static DotStyle parse(String x) {
        if (x != null)
            for (DotStyle d : values())
                if (d.name.equals(x))
                    return d;
        return null;
    }

    /** This value is used in writing XML. */
    @Override
    public String toString() {
        return name;
    }
}


class BackgroundState {
    /**
     * Construct a new BackgroundState (with default theme settings) for the given
     * instance.
     */
    public BackgroundState(AlloyInstance originalInstance) {
        this.originalInstance = originalInstance;
        this.currentModel = originalInstance.model;
        resetTheme();
        loadInstance(originalInstance);
    }

    /** Clears the current theme. */
    public void resetTheme() {
        currentModel = originalInstance.model;
        projectedTypes.clear();
        useOriginalNames = false;
        hidePrivate = true;
        hideMeta = true;
        fontSize = 12;
        nodePalette = DotPalette.CLASSIC;
        edgePalette = DotPalette.CLASSIC;
        nodeColor.clear();
        nodeColor.put(null, DotColor.WHITE);
        nodeStyle.clear();
        nodeStyle.put(null, DotStyle.SOLID);
        nodeVisible.clear();
        nodeVisible.put(null, true);
        label.clear();
        label.put(null, "");
        number.clear();
        number.put(null, true);
        hideUnconnected.clear();
        hideUnconnected.put(null, false);
        showAsAttr.clear();
        showAsAttr.put(null, false);
        showAsLabel.clear();
        showAsLabel.put(null, true);
        shape.clear();
        shape.put(null, DotShape.ELLIPSE);
        weight.clear();
        weight.put(null, 0);
        attribute.clear();
        attribute.put(null, false);
        mergeArrows.clear();
        mergeArrows.put(null, true);
        constraint.clear();
        constraint.put(null, true);
        layoutBack.clear();
        layoutBack.put(null, false);
        edgeColor.clear();
        edgeColor.put(null, DotColor.MAGIC);
        edgeStyle.clear();
        edgeStyle.put(null, DotStyle.SOLID);
        edgeVisible.clear();
        edgeVisible.put(null, true);
        // Provide some nice defaults for "Int" and "seq/Int" type
        AlloyType sigint = AlloyType.INT;
        label.put(sigint, "");
        number.put(sigint, true);
        hideUnconnected.put(sigint, true);
        AlloyType seqidx = AlloyType.SEQINT;
        label.put(seqidx, "");
        number.put(seqidx, true);
        hideUnconnected.put(seqidx, true);
        // Provide some nice defaults for meta model stuff
        AlloyType set = AlloyType.SET;
        AlloyRelation ext = AlloyRelation.EXTENDS, in = AlloyRelation.IN;
        shape.put(null, DotShape.BOX);
        nodeColor.put(null, DotColor.YELLOW);
        nodeStyle.put(null, DotStyle.SOLID);
        shape.put(set, DotShape.ELLIPSE);
        nodeColor.put(set, DotColor.BLUE);
        label.put(set, "");
        edgeColor.put(ext, DotColor.BLACK);
        weight.put(ext, 100);
        layoutBack.put(ext, true);
        edgeColor.put(in, DotColor.BLACK);
        weight.put(in, 100);
        layoutBack.put(in, true);
        applyDefaultVar(); // [electrum] dashed style for variable elements
        // Done
        changedSinceLastSave = false;
    }

    /**
     * Paints variable items as dashed if no other style has been set by the user.
     * Must be run every time since new elements may have been introduced.
     */
    void applyDefaultVar() {
        // if parent also var or has style, inherit, otherwise paint dashed
        for (AlloyType r : currentModel.getTypes())
            if (nodeStyle.get(r) == null && r.isVar && !(currentModel.getSuperType(r).isVar || nodeStyle.get(currentModel.getSuperType(r)) != null))
                nodeStyle.put(r, DotStyle.DASHED);
        for (AlloyRelation r : currentModel.getRelations())
            if (edgeStyle.get(r) == null && r.isVar)
                edgeStyle.put(r, DotStyle.DASHED);
        for (AlloySet r : currentModel.getSets())
            if (nodeStyle.get(r) == null && r.isVar && !(r.getType().isVar || nodeStyle.get(r.getType()) != null))
                nodeStyle.put(r, DotStyle.DASHED);
    }

    /**
     * Load a new instance into this BackgroundState object (the input argument is treated
     * as a new unprojected instance); if world!=null, it is the root of the AST
     */
    public void loadInstance(AlloyInstance unprojectedInstance) {
        this.originalInstance = unprojectedInstance;
        for (AlloyType t : getProjectedTypes())
            if (!unprojectedInstance.model.hasType(t))
                projectedTypes.remove(t);
        currentModel = StaticProjector.project(unprojectedInstance.model, projectedTypes);
    }

    /** True if the theme has been modified since last save. */
    private boolean changedSinceLastSave = false;

    /** True if the theme has been modified since last save. */
    public boolean changedSinceLastSave() {
        return changedSinceLastSave;
    }

    /**
     * Sets the "changed since last save" flag.
     */
    private void change() {
        changedSinceLastSave = true;
    }

    /**
     * If oldValue is different from newValue, then sets the "changed since last
     * save" flag.
     */
    private void changeIf(Object oldValue, Object newValue) {
        if (oldValue == null) {
            if (newValue == null)
                return;
        } else {
            if (oldValue.equals(newValue))
                return;
        }
        change();
    }

    /*
     * ========================================================= ================
     * ===================
     */

    /**
     * If x is an AlloyType, x is not univ, then return its parent (which could be
     * univ); If x is an AlloySet, then return x's type; All else, return null.
     */
    private AlloyType parent(AlloyElement x, AlloyModel model) {
        if (x instanceof AlloySet)
            return ((AlloySet) x).getType();
        if (x instanceof AlloyType)
            return model.getSuperType((AlloyType) x);
        return null;
    }

    /*
     * ========================================================= ================
     * ===================
     */

    /** The original unprojected instance. */
    private AlloyInstance originalInstance;

    /** Returns the original unprojected model. */
    public AlloyInstance getOriginalInstance() {
        return originalInstance;
    }

    /** Returns the original unprojected model. */
    public AlloyModel getOriginalModel() {
        return originalInstance.model;
    }

    /*
     * ========================================================= ================
     * ===================
     */

    /** The current (possibly projected) model. */
    private AlloyModel currentModel;

    /** Returns the current (possibly projected) model. */
    public AlloyModel getCurrentModel() {
        return currentModel;
    }

    /*
     * ========================================================= ================
     * ===================
     */

    /** The set of types we are currently projecting over. */
    private Set<AlloyType> projectedTypes = new TreeSet<AlloyType>();

    /**
     * Gets an unmodifiable copy of the set of types we are currently projecting
     * over.
     */
    public ConstSet<AlloyType> getProjectedTypes() {
        return ConstSet.make(projectedTypes);
    }

    /**
     * Returns true iff the type is not univ, and it is a toplevel type.
     */
    public boolean canProject(final AlloyType type) {
        return isTopLevel(type) && !type.isVar; // [electrum] can't project over mutable variable
    }

    /**
     * Returns true iff the type is not univ, and it is a toplevel type.
     */
    public boolean isTopLevel(final AlloyType type) {
        return AlloyType.UNIV.equals(originalInstance.model.getSuperType(type));
    }

    /**
     * Adds type to the list of projected types if it's a toplevel type.
     */
    public void project(AlloyType type) {
        if (canProject(type))
            if (projectedTypes.add(type)) {
                currentModel = StaticProjector.project(originalInstance.model, projectedTypes);
                change();
            }
    }

    /**
     * Removes type from the list of projected types if it is currently projected.
     */
    public void deproject(AlloyType type) {
        if (projectedTypes.remove(type)) {
            currentModel = StaticProjector.project(originalInstance.model, projectedTypes);
            change();
        }
    }

    /** Removes every entry from the list of projected types. */
    public void deprojectAll() {
        if (projectedTypes.size() > 0) {
            projectedTypes.clear();
            currentModel = StaticProjector.project(originalInstance.model, projectedTypes);
            change();
        }
    }

    /*
     * ========================================================= ================
     * ===================
     */

    /** Whether to use the original atom names. */
    private boolean useOriginalNames = false;

    /** Returns whether we will use original atom names. */
    public boolean useOriginalName() {
        return useOriginalNames;
    }

    /** Sets whether we will use original atom names or not. */
    public void useOriginalName(Boolean newValue) {
        if (newValue != null && useOriginalNames != newValue) {
            change();
            useOriginalNames = newValue;
        }
    }

    /*
     * ========================================================= ================
     * ===================
     */

    /** Whether to hide private sigs/fields/relations. */
    private boolean hidePrivate = false;

    /**
     * Returns whether we will hide private sigs/fields/relations.
     */
    public boolean hidePrivate() {
        return hidePrivate;
    }

    /**
     * Sets whether we will hide private sigs/fields/relations.
     */
    public void hidePrivate(Boolean newValue) {
        if (newValue != null && hidePrivate != newValue) {
            change();
            hidePrivate = newValue;
        }
    }

    /*
     * ========================================================= ================
     * ===================
     */

    /** Whether to hide meta sigs/fields/relations. */
    private boolean hideMeta = true;

    /**
     * Returns whether we will hide meta sigs/fields/relations.
     */
    public boolean hideMeta() {
        return hideMeta;
    }

    /** Sets whether we will hide meta sigs/fields/relations. */
    public void hideMeta(Boolean newValue) {
        if (newValue != null && hideMeta != newValue) {
            change();
            hideMeta = newValue;
        }
    }

    /*
     * ========================================================= ================
     * ===================
     */

    /** The graph's font size. */
    private int fontSize = 12;

    /** Returns the font size. */
    public int getFontSize() {
        return fontSize;
    }

    /** Sets the font size. */
    public void setFontSize(int n) {
        if (fontSize != n && fontSize > 0) {
            change();
            fontSize = n;
        }
    }

    /*
     * ========================================================= ================
     * ===================
     */

    /** The default node palette. */
    private DotPalette nodePalette;

    /** Gets the default node palette. */
    public DotPalette getNodePalette() {
        return nodePalette;
    }

    /** Sets the default node palette. */
    public void setNodePalette(DotPalette x) {
        if (nodePalette != x && x != null) {
            change();
            nodePalette = x;
        }
    }

    /*
     * ========================================================= ================
     * ===================
     */

    /** The default edge palette. */
    private DotPalette edgePalette;

    /** Gets the default edge palette. */
    public DotPalette getEdgePalette() {
        return edgePalette;
    }

    /** Sets the default edge palette. */
    public void setEdgePalette(DotPalette x) {
        if (edgePalette != x && x != null) {
            change();
            edgePalette = x;
        }
    }

    /*
     * ========================================================= ================
     * ===================
     */

    // An important invariant to maintain: every map here must map null to a
    // nonnull value.
    public final MInt           weight          = new MInt();
    public final MString        label           = new MString();
    public final MMap<DotColor> nodeColor       = new MMap<DotColor>();
    public final MMap<DotColor> edgeColor       = new MMap<DotColor>();
    public final MMap<DotStyle> nodeStyle       = new MMap<DotStyle>();
    public final MMap<DotStyle> edgeStyle       = new MMap<DotStyle>();
    public final MMap<DotShape> shape           = new MMap<DotShape>();
    public final MMap<Boolean>  attribute       = new MMap<Boolean>(true, false);
    public final MMap<Boolean>  mergeArrows     = new MMap<Boolean>(true, false);
    public final MMap<Boolean>  constraint      = new MMap<Boolean>(true, false);
    public final MMap<Boolean>  layoutBack      = new MMap<Boolean>(true, false);
    public final MMap<Boolean>  edgeVisible     = new MMap<Boolean>(true, false);
    public final MMap<Boolean>  nodeVisible     = new MMap<Boolean>(true, false);
    public final MMap<Boolean>  number          = new MMap<Boolean>(true, false);
    public final MMap<Boolean>  hideUnconnected = new MMap<Boolean>(true, false);
    public final MMap<Boolean>  showAsAttr      = new MMap<Boolean>(true, false);
    public final MMap<Boolean>  showAsLabel     = new MMap<Boolean>(true, false);

    public final class MInt {

        private final LinkedHashMap<AlloyElement,Integer> map = new LinkedHashMap<AlloyElement,Integer>();

        private MInt() {}

        private void clear() {
            map.clear();
            change();
        }

        private void putAll(MInt x) {
            map.putAll(x.map);
            change();
        }

        public int get(AlloyElement x) {
            Integer ans = map.get(x);
            if (ans == null)
                return 0;
            else
                return ans;
        }

        public void put(AlloyElement x, Integer v) {
            if (v == null || v < 0)
                v = 0;
            changeIf(map.put(x, v), v);
        }
    }

    public final class MString {

        private final LinkedHashMap<AlloyElement,String> map = new LinkedHashMap<AlloyElement,String>();

        private MString() {}

        private void clear() {
            map.clear();
            change();
        }

        private void putAll(MString x) {
            map.putAll(x.map);
            change();
        }

        public String get(AlloyElement x) {
            String ans = map.get(x);
            if (ans == null)
                ans = x.getName().trim();
            return ans;
        }

        public void put(AlloyElement x, String v) {
            if (x == null && v == null)
                v = "";
            if (x != null && x.getName().equals(v))
                v = null;
            changeIf(map.put(x, v), v);
        }
    }

    public final class MMap<T> {

        private final LinkedHashMap<AlloyElement,T> map = new LinkedHashMap<AlloyElement,T>();
        private final T                             onValue;
        private final T                             offValue;

        private MMap() {
            onValue = null;
            offValue = null;
        }

        private MMap(T on, T off) {
            this.onValue = on;
            this.offValue = off;
        }

        private void clear() {
            map.clear();
            change();
        }

        private void putAll(MMap<T> x) {
            map.putAll(x.map);
            change();
        }

        public T get(AlloyElement obj) {
            return map.get(obj);
        }

        public T resolve(AlloyElement obj) {
            AlloyModel m = currentModel;
            for (AlloyElement x = obj;; x = parent(x, m)) {
                T v = map.get(x);
                if (v != null)
                    return v;
            }
        }

        /**
         * Set the value for the given object; can be "null" to mean "inherit"
         */
        public void put(AlloyElement obj, T value) {
            if (obj == null && value == null)
                return;
            Object old = map.put(obj, value);
            if ((old == null && value != null) || (old != null && !old.equals(value)))
                change();
        }

    }

    // Reads the value for that type/set/relation.
    // If x==null, then we guarantee the return value is nonnull
    // If x!=null, then it may return null (which means "inherited")
    // (Note: "label" and "weight" will never return null)

    // Reads the value for that atom based on an existing AlloyInstance; return
    // value is never null.
    public DotColor nodeColor(AlloyAtom a, AlloyInstance i) {
        for (AlloySet s : i.atom2sets(a)) {
            DotColor v = nodeColor.get(s);
            if (v != null)
                return v;
        }
        return nodeColor.resolve(a.getType());
    }

    public DotStyle nodeStyle(AlloyAtom a, AlloyInstance i) {
        for (AlloySet s : i.atom2sets(a)) {
            DotStyle v = nodeStyle.get(s);
            if (v != null)
                return v;
        }
        return nodeStyle.resolve(a.getType());
    }

    public DotShape shape(AlloyAtom a, AlloyInstance i) {
        for (AlloySet s : i.atom2sets(a)) {
            DotShape v = shape.get(s);
            if (v != null)
                return v;
        }
        return shape.resolve(a.getType());
    }

    public boolean nodeVisible(AlloyAtom a, AlloyInstance i) {
        // If it's in 1 or more set, then TRUE if at least one of them is TRUE.
        // If it's in 0 set, then travel up the chain of AlloyType and return
        // the first non-null value.
        if (i.atom2sets(a).size() > 0) {
            for (AlloySet s : i.atom2sets(a))
                if (nodeVisible.resolve(s))
                    return true;
            return false;
        }
        return nodeVisible.resolve(a.getType());
    }

}


/**
 * This class demonstrates how to access Alloy4 via the compiler methods.
 */


public class AlloyAnalyzerService extends AlloyAnalyzerGrpc.AlloyAnalyzerImplBase {    public static class SilentGraphMaker {
        /** The theme customization. */
        private final BackgroundState            view;

        /**
         * The projected instance for the graph currently being generated.
         */
        private final AlloyInstance              instance;

        /**
         * The projected model for the graph currently being generated.
         */
        private final AlloyModel                 model;

        /**
         * The map that contains all edges and what the AlloyTuple that each edge
         * corresponds to.
         */
        private final Map<GraphEdge,AlloyTuple>  edges     = new LinkedHashMap<GraphEdge,AlloyTuple>();

        /**
         * The map that contains all nodes and what the AlloyAtom that each node
         * corresponds to.
         */
        private final Map<GraphNode,AlloyAtom>   nodes     = new LinkedHashMap<GraphNode,AlloyAtom>();

        /**
         * This maps each atom to the node representing it; if an atom doesn't have a
         * node, it won't be in the map.
         */
        private final Map<AlloyAtom,GraphNode>   atom2node = new LinkedHashMap<AlloyAtom,GraphNode>();

        /**
         * This stores a set of additional labels we want to add to an existing node.
         */
        private final Map<GraphNode,Set<String>> attribs   = new LinkedHashMap<GraphNode,Set<String>>();

        /** The resulting graph. */
        private final Graph                      graph;

        public static void produceGraph(Graph graph, AlloyInstance originalInstance, BackgroundState view, AlloyProjection proj) {
            new SilentGraphMaker(graph, originalInstance, view, proj);
        }

        /**
         * The constructor takes an Instance and a View, then insert the generate
         * graph(s) into a blank cartoon.
         */
        public SilentGraphMaker(Graph graph, AlloyInstance originalInstance, BackgroundState view, AlloyProjection proj) {
            final boolean hidePrivate = view.hidePrivate();
            final boolean hideMeta = view.hideMeta();
            final Map<AlloyRelation,Integer> rels = new TreeMap<AlloyRelation,Integer>();
            this.graph = graph;
            this.view = view;
            instance = StaticProjector.project(originalInstance, proj);
            model = instance.model;
            for (AlloyRelation rel : model.getRelations()) {
                rels.put(rel, null);
            }
            int ci = 0;
            for (AlloyRelation rel : model.getRelations()) {
                DotColor c = view.edgeColor.resolve(rel);
                int count = ((hidePrivate && rel.isPrivate) || !view.edgeVisible.resolve(rel)) ? 0 : edgesAsArcs(hidePrivate, hideMeta, rel);
                rels.put(rel, count);
                if (count > 0)
                    ci = (ci + 1) % (6 /* Colors size */);
            }
            for (AlloyAtom atom : instance.getAllAtoms()) {
                List<AlloySet> sets = instance.atom2sets(atom);
                if (sets.size() > 0) {
                    for (AlloySet s : sets)
                        if (view.nodeVisible.resolve(s) && !view.hideUnconnected.resolve(s)) {
                            createNode(hidePrivate, hideMeta, atom);
                            break;
                        }
                } else if (view.nodeVisible.resolve(atom.getType()) && !view.hideUnconnected.resolve(atom.getType())) {
                    createNode(hidePrivate, hideMeta, atom);
                }
            }
            for (AlloyRelation rel : model.getRelations())
                if (!(hidePrivate && rel.isPrivate))
                    if (view.attribute.resolve(rel))
                        edgesAsAttribute(rel);
            for (Map.Entry<GraphNode,Set<String>> e : attribs.entrySet()) {
                Set<String> set = e.getValue();
                if (set != null)
                    for (String s : set)
                        if (s.length() > 0)
                            e.getKey().addLabel(s);
            }
        }

        /**
         * Return the node for a specific AlloyAtom (create it if it doesn't exist yet).
         *
         * @return null if the atom is explicitly marked as "Don't Show".
         */
        private GraphNode createNode(final boolean hidePrivate, final boolean hideMeta, final AlloyAtom atom) {
            GraphNode node = atom2node.get(atom);
            if (node != null)
                return node;
            if ((hidePrivate && atom.getType().isPrivate) || (hideMeta && atom.getType().isMeta) || !view.nodeVisible(atom, instance))
                return null;
            // Make the node
            DotColor color = view.nodeColor(atom, instance);
            DotStyle style = view.nodeStyle(atom, instance);
            DotShape shape = view.shape(atom, instance);
            String label = atomname(atom, false);
            node = new GraphNode(graph, atom, label)/*.set(shape)*/.set(color.getColor(view.getNodePalette()))/*.set(style)*/;
            // Get the label based on the sets and relations
            String setsLabel = "";
            boolean showLabelByDefault = view.showAsLabel.get(null);
            for (AlloySet set : instance.atom2sets(atom)) {
                String x = view.label.get(set);
                if (x.length() == 0)
                    continue;
                Boolean showLabel = view.showAsLabel.get(set);
                if ((showLabel == null && showLabelByDefault) || (showLabel != null && showLabel.booleanValue()))
                    setsLabel += ((setsLabel.length() > 0 ? ", " : "") + x);
            }
            if (setsLabel.length() > 0) {
                Set<String> list = attribs.get(node);
                if (list == null)
                    attribs.put(node, list = new TreeSet<String>());
                list.add("(" + setsLabel + ")");
            }
            nodes.put(node, atom);
            atom2node.put(atom, node);
            return node;
        }

        /**
         * Create an edge for a given tuple from a relation (if neither start nor end
         * node is explicitly invisible)
         */
        private boolean createEdge(final boolean hidePrivate, final boolean hideMeta, AlloyRelation rel, AlloyTuple tuple, boolean bidirectional) {
            // This edge represents a given tuple from a given relation.
            //
            // If the tuple's arity==2, then the label is simply the label of the
            // relation.
            //
            // If the tuple's arity>2, then we append the node labels for all the
            // intermediate nodes.
            // eg. Say a given tuple is (A,B,C,D) from the relation R.
            // An edge will be drawn from A to D, with the label "R [B, C]"
            if ((hidePrivate && tuple.getStart().getType().isPrivate) || (hideMeta && tuple.getStart().getType().isMeta) || !view.nodeVisible(tuple.getStart(), instance))
                return false;
            if ((hidePrivate && tuple.getEnd().getType().isPrivate) || (hideMeta && tuple.getEnd().getType().isMeta) || !view.nodeVisible(tuple.getEnd(), instance))
                return false;
            GraphNode start = createNode(hidePrivate, hideMeta, tuple.getStart());
            GraphNode end = createNode(hidePrivate, hideMeta, tuple.getEnd());
            if (start == null || end == null)
                return false;
            boolean layoutBack = view.layoutBack.resolve(rel);
            String label = view.label.get(rel);
            if (tuple.getArity() > 2) {
                StringBuilder moreLabel = new StringBuilder();
                List<AlloyAtom> atoms = tuple.getAtoms();
                for (int i = 1; i < atoms.size() - 1; i++) {
                    if (i > 1)
                        moreLabel.append(", ");
                    moreLabel.append(atomname(atoms.get(i), false));
                }
                if (label.length() == 0) {
                    /* label=moreLabel.toString(); */ } else {
                    label = label + (" [" + moreLabel + "]");
                }
            }
            DotDirection dir = bidirectional ? DotDirection.BOTH : (layoutBack ? DotDirection.BACK : DotDirection.FORWARD);
            DotStyle style = view.edgeStyle.resolve(rel);
            DotColor color = view.edgeColor.resolve(rel);
            int weight = view.weight.get(rel);
            GraphEdge e = new GraphEdge((layoutBack ? end : start), (layoutBack ? start : end), tuple, label, rel);
            //e.set(style);
            e.set(dir != DotDirection.FORWARD, dir != DotDirection.BACK);
            e.set(weight < 1 ? 1 : (weight > 100 ? 10000 : 100 * weight));
            edges.put(e, tuple);
            return true;
        }

        /**
         * Create edges for every visible tuple in the given relation.
         */
        private int edgesAsArcs(final boolean hidePrivate, final boolean hideMeta, AlloyRelation rel) {
            int count = 0;
            if (!view.mergeArrows.resolve(rel)) {
                // If we're not merging bidirectional arrows, simply create an edge
                // for each tuple.
                for (AlloyTuple tuple : instance.relation2tuples(rel))
                    if (createEdge(hidePrivate, hideMeta, rel, tuple, false))
                        count++;
                return count;
            }
            // Otherwise, find bidirectional arrows and only create one edge for
            // each pair.
            Set<AlloyTuple> tuples = instance.relation2tuples(rel);
            Set<AlloyTuple> ignore = new LinkedHashSet<AlloyTuple>();
            for (AlloyTuple tuple : tuples) {
                if (!ignore.contains(tuple)) {
                    AlloyTuple reverse = tuple.getArity() > 2 ? null : tuple.reverse();
                    // If the reverse tuple is in the same relation, and it is not a
                    // self-edge, then draw it as a <-> arrow.
                    if (reverse != null && tuples.contains(reverse) && !reverse.equals(tuple)) {
                        ignore.add(reverse);
                        if (createEdge(hidePrivate, hideMeta, rel, tuple, true))
                            count = count + 2;
                    } else {
                        if (createEdge(hidePrivate, hideMeta, rel, tuple, false))
                            count = count + 1;
                    }
                }
            }
            return count;
        }

        /** Attach tuple values as attributes to existing nodes. */
        private void edgesAsAttribute(AlloyRelation rel) {
            // If this relation wants to be shown as an attribute,
            // then generate the annotations and attach them to each tuple's
            // starting node.
            // Eg.
            // If (A,B) and (A,C) are both in the relation F,
            // then the A node would have a line that says "F: B, C"
            // Eg.
            // If (A,B,C) and (A,D,E) are both in the relation F,
            // then the A node would have a line that says "F: B->C, D->E"
            // Eg.
            // If (A,B,C) and (A,D,E) are both in the relation F, and B belongs to
            // sets SET1 and SET2,
            // and SET1's "show in relational attribute" is on,
            // and SET2's "show in relational attribute" is on,
            // then the A node would have a line that says "F: B (SET1, SET2)->C,
            // D->E"
            //
            Map<GraphNode,String> map = new LinkedHashMap<GraphNode,String>();
            for (AlloyTuple tuple : instance.relation2tuples(rel)) {
                GraphNode start = atom2node.get(tuple.getStart());
                if (start == null)
                    continue; // null means the node won't be shown, so we can't
                // show any attributes
                String attr = "";
                List<AlloyAtom> atoms = tuple.getAtoms();
                for (int i = 1; i < atoms.size(); i++) {
                    if (i > 1)
                        attr += "->";
                    attr += atomname(atoms.get(i), true);
                }
                if (attr.length() == 0)
                    continue;
                String oldattr = map.get(start);
                if (oldattr != null && oldattr.length() > 0)
                    attr = oldattr + ", " + attr;
                if (attr.length() > 0)
                    map.put(start, attr);
            }
            for (Map.Entry<GraphNode,String> e : map.entrySet()) {
                GraphNode node = e.getKey();
                Set<String> list = attribs.get(node);
                if (list == null)
                    attribs.put(node, list = new TreeSet<String>());
                String attr = e.getValue();
                if (view.label.get(rel).length() > 0)
                    attr = view.label.get(rel) + ": " + attr;
                list.add(attr);
            }
        }

        /**
         * Return the label for an atom.
         *
         * @param atom - the atom
         * @param showSets - whether the label should also show the sets that this atom
         *            belongs to
         *            <p>
         *            eg. If atom A is the 3rd atom in type T, and T's label is
         *            "Person", then the return value would be "Person3".
         *            <p>
         *            eg. If atom A is the only atom in type T, and T's label is
         *            "Person", then the return value would be "Person".
         *            <p>
         *            eg. If atom A is the 3rd atom in type T, and T's label is
         *            "Person", and T belongs to the sets Set1, Set2, and Set3. However,
         *            only Set1 and Set2 have "show in relational attribute == on", then
         *            the return value would be "Person (Set1, Set2)".
         */
        private String atomname(AlloyAtom atom, boolean showSets) {
            String label = atom.getVizName(null, view.number.resolve(atom.getType()));
            if (!showSets)
                return label;
            String attr = "";
            boolean showInAttrByDefault = view.showAsAttr.get(null);
            for (AlloySet set : instance.atom2sets(atom)) {
                String x = view.label.get(set);
                if (x.length() == 0)
                    continue;
                Boolean showAsAttr = view.showAsAttr.get(set);
                if ((showAsAttr == null && showInAttrByDefault) || (showAsAttr != null && showAsAttr))
                    attr += ((attr.length() > 0 ? ", " : "") + x);
            }
            if (label.length() == 0)
                return (attr.length() > 0) ? ("(" + attr + ")") : "";
            return (attr.length() > 0) ? (label + " (" + attr + ")") : label;
        }

        public String esc(String name) {
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

    }


//    public static String toDot(A4Solution solution) {
//        StringBuilder dot = new StringBuilder();
//        dot.append("digraph {\n");
//
//        for (Sig sig : solution.getAllReachableSigs()) {
//            // Retrieve all atoms (instances) of the current signature
//            for (Object atom : solution.eval(sig)) {
//                System.out.println("Signature: " + sig + ", Atom: " + atom.toString());
//            }}
//        // Iterate over each atom and relation in the solution and build the DOT graph.
//        for (Sig sig : solution.getAllReachableSigs()) {
//            for (Object atom : solution.eval(sig)) {
//                dot.append("  ").append(atom.toString()).append(";\n");
//            }
//        }

//        for (String relation : solution.getAllReachableRelations()) {
//            for (Object[] tuple : solution.eval(relation)) {
//                dot.append("  ")
//                        .append(tuple[0].toString())
//                        .append(" -> ")
//                        .append(tuple[1].toString())
//                        .append(" [label=\"")
//                        .append(relation)
//                        .append("\"];\n");
//            }
//        }

//        dot.append("}");
//        return dot.toString();
//    }
    public static void getInstanceFromSolution(A4Solution solution) {
        // Iterate over each signature in the solution
        for (Sig sig : solution.getAllReachableSigs()) {
            System.out.println("Signature: " + sig.label);

            // Evaluate the signature in the solution to get all instances (atoms)
            A4TupleSet ts = solution.eval(sig);
            for (A4Tuple tuple : ts) {
                System.out.println("Atom: " + tuple);
            }

            // Iterate over each field in the signature (relations)
            for (Sig.Field field : sig.getFields()) {
                System.out.println("Field: " + field.label);

                // Evaluate the field in the solution to get all tuples
                A4TupleSet tsField = solution.eval(field);
                for (A4Tuple tuple : tsField) {
                    System.out.println("Relation: " + tuple);
                }
            }
        }
    }

    @Override
    public void analyzeModel(AlloyAnalyzerOuterClass.ModelRequest request, StreamObserver<AlloyAnalyzerOuterClass.ModelResponse> responseObserver) {
        String filePath = request.getFilePath();
        String result;


        // Alloy4 sends diagnostic messages and progress reports to the
        // A4Reporter.
        // By default, the A4Reporter ignores all these events (but you can
        // extend the A4Reporter to display the event for the user)
        A4Reporter rep = new A4Reporter() {

            // For example, here we choose to display each "warning" by printing
            // it to System.out
            @Override
            public void warning(ErrorWarning msg) {
                System.out.print("Relevance Warning:\n" + (msg.toString().trim()) + "\n\n");
                System.out.flush();
            }
        };


            // Parse+typecheck the model
            System.out.println("=========== Parsing+Typechecking " + filePath + " =============");
            Module world = CompUtil.parseEverything_fromFile(rep, null, filePath);

            // Choose some default options for how you want to execute the
            // commands
            A4Options options = new A4Options();

            options.solver = A4Options.SatSolver.SAT4J;
            int ix = 0;

            for (Command command : world.getAllCommands()) {
                // Execute the command
                System.out.println("============ Command " + command + ": ============");
                A4Solution ans = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);
                // Print the outcome
                System.out.println(ans);
                // If satisfiable...
                if (ans.satisfiable()) {
                    // You can query "ans" to find out the values of each set or
                    // type.
                    // This can be useful for debugging.
                    //
                    // You can also write the outcome to an XML file
                    ans.writeXML(filePath + ".xml");
                    // get Alloy instance
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ans.writeXML(pw, null, null);
                    pw.flush();
                    sw.flush();
                    String txt = sw.toString();

                    // Create an instance from a solution
                    AlloyInstance originalInstance = StaticInstanceReader.parseInstance(new StringReader(txt), 0);
//                     System.out.println("\n\noriginal instance: " + originalInstance.toString());

                    // Create backgroundState for a projection
                    BackgroundState vizState = new BackgroundState(originalInstance);

                    // System.out.println("\n\nviz state: " + vizState.toString());

                    // Create empty projection for the instance
                    Map<AlloyType,AlloyAtom> map = new LinkedHashMap<AlloyType,AlloyAtom>();
                    AlloyProjection emptyProjection = new AlloyProjection(map);
                    // Graph
                    Graph graph = new Graph(vizState.getFontSize() / 12.0D);
                    SilentGraphMaker.produceGraph(graph, originalInstance, vizState, emptyProjection);

                    try {
                        FileWriter fw = new FileWriter(filePath + "." + ix + ".dot");
                        System.out.println(graph.toString());
                        fw.write(graph.toString());
                        fw.close();
                    } catch (IOException e) {
                        System.out.println("Error: unable to generate the graph");
                    }
                    ix += 1;
                }
            }




        try {
            System.out.print(filePath);
            String alloyModel = new String(Files.readAllBytes(Paths.get(filePath)));
            Module world2 = CompUtil.parseEverything_fromString(null, alloyModel);

            A4Options options2 = new A4Options();
            options2.solver = A4Options.SatSolver.SAT4J;
//            Bitwidth=4 MaxSeq=3 SkolemDepth=1 Symmetry=20
            options2.skolemDepth = 1;
            options2.symmetry = 20;


            // Execute commands in the model
            StringBuilder resultBuilder = new StringBuilder();
            for (Command command : world2.getAllCommands()) {
                System.out.println(command);
                A4Solution solution = TranslateAlloyToKodkod.execute_command(rep, world2.getAllReachableSigs(), command, options2);
//                solution = solution;
                while (solution != solution.next()) {
                solution = solution.next();
                if (solution.satisfiable()) {
                    // You can query "ans" to find out the values of each set or type.
                    // This can be useful for debugging.
                    //
                    // You can also write the outcome to an XML file
                    solution.writeXML("alloy_example_output.xml");
                    //
                    // You can then visualize the XML file by calling this:
//                    if (solution==null) {

                    var v = new VizGUI(false, "alloy_example_output.xml", null);
//                    } else {
//                        solution.loadXML("alloy_example_output.xml", true);
//                    }

                    resultBuilder.append(solution.toString()).append("\n");
                }
//                var d = toDot(solution);
//                System.out.println(d);
            }
            result = resultBuilder.toString();
        System.out.println(result);
            }
        } catch (IOException | Err e) {
            result = "Error: " + e.getMessage();
        }

//        AlloyAnalyzerOuterClass.ModelResponse response = AlloyAnalyzerOuterClass.ModelResponse.newBuilder()
//                .setResult(result)
//                .build();
//        System.out.println(result);
//        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

