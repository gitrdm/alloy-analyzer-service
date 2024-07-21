package AlloyAnalyzerService.DotExporter;

public enum DotStyle {

    /**
     * Solid line.
     */
    SOLID("Solid", "solid"),

    /**
     * Dashed line.
     */
    DASHED("Dashed", "dashed"),

    /**
     * Dotted line.
     */
    DOTTED("Dotted", "dotted"),

    /**
     * Bold line.
     */
    BOLD("Bold", "bold");

    /**
     * The description of this line style.
     */
    private final String name;

    /**
     * The corresponding DOT attribute.
     */
    private final String dotName;

    /**
     * Constructs a DotStyle object.
     */
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

    /**
     * This value is used in writing XML.
     */
    @Override
    public String toString() {
        return name;
    }
}
