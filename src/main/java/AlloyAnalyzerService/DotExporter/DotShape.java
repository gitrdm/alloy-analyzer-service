package AlloyAnalyzerService.DotExporter;

public enum DotShape {

    /**
     * Ellipse
     */
    ELLIPSE("Ellipse", "ellipse"),
    /**
     * Box
     */
    BOX("Box", "box"),
    /**
     * Circle
     */
    CIRCLE("Circle", "circle"),
    /**
     * Egg
     */
    EGG("Egg", "egg"),
    /**
     * Triangle
     */
    TRIANGLE("Triangle", "triangle"),
    /**
     * Diamond
     */
    DIAMOND("Diamond", "diamond"),
    /**
     * Trapezoid
     */
    TRAPEZOID("Trapezoid", "trapezium"),
    /**
     * Parallelogram
     */
    PARALLELOGRAM("Parallelogram", "parallelogram"),
    /**
     * House
     */
    HOUSE("House", "house"),
    /**
     * Hexagon
     */
    HEXAGON("Hexagon", "hexagon"),
    /**
     * Octagon
     */
    OCTAGON("Octagon", "octagon"),
    /**
     * Double Circle
     */
    DOUBLE_CIRCLE("Dbl Circle", "doublecircle"),
    /**
     * Double Octagon
     */
    DOUBLE_OCTAGON("Dbl Octagon", "doubleoctagon"),
    /**
     * Triple Octagon
     */
    TRIPLE_OCTAGON("Tpl Octagon", "tripleoctagon"),
    /**
     * Inverted Triangle
     */
    INV_TRIANGLE("Inv Triangle", "invtriangle"),
    /**
     * Inverted House
     */
    INV_HOUSE("Inv House", "invhouse"),
    /**
     * Inverted Trapezoid
     */
    INV_TRAPEZOID("Inv Trapezoid", "invtrapezium"),
    /**
     * Lined Diamond
     */
    M_DIAMOND("Lined Diamond", "Mdiamond"),
    /**
     * Lined Square
     */
    M_SQUARE("Lined Square", "Msquare"),
    /**
     * Lined Circle
     */
    M_CIRCLE("Lined Circle", "Mcircle");

    /**
     * The description of this line style.
     */
    private final String name;

    /**
     * The corresponding DOT attribute.
     */
    private final String dotName;

    /**
     * Constructs a DotShape object.
     */
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

    /**
     * This value is used in writing XML.
     */
    @Override
    public String toString() {
        return name;
    }
}
