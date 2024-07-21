package AlloyAnalyzerService.DotExporter;

import edu.mit.csail.sdg.alloy4graph.DotPalette;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable; this defines the set of possible colors.
 * <p>
 * <b>Thread Safety:</b> Can be called only by the AWT event thread.
 */

public enum DotColor {

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

    /**
     * The text to display.
     */
    private final String displayText;

    /**
     * The list of colors to use, corresponding to the current palette; if there are
     * more palette choices than colors.size(), then the extra palettes would all
     * use the first color.
     */
    private final List<String> colors = new ArrayList<String>();

    /**
     * Construct a new DotColor.
     */
    private DotColor(String text, String... colors) {
        displayText = text;
        for (int i = 0; i < colors.length; i++) {
            this.colors.add(colors[i]);
        }
    }

    /**
     * This maps each dot color name into the corresponding Java Color object.
     */
    private static final Map<String, Color> name2color = new HashMap<String, Color>();

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

    /**
     * This value is used in writing XML.
     */
    @Override
    public String toString() {
        return displayText;
    }
}
