package AlloyAnalyzerService.DotExporter;

import edu.mit.csail.sdg.alloy4.OurPDFWriter;

import java.awt.*;

public class Artist {

    /**
     * The font name.
     */
    private static final String fontName = "Lucida Grande";

    /**
     * The font size.
     */
    private static final int fontSize = 12;

    /**
     * The corresponding OurPDFWriater.
     */
    private OurPDFWriter pdf;

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

    /**
     * Shifts the coordinate space by the given amount.
     */
    public void translate(int x, int y) {
        pdf.shiftCoordinateSpace(x, y);
    }

    /**
     * Draws a circle of the given radius, centered at (0,0)
     */
    public void drawCircle(int radius) {
        pdf.drawCircle(radius, false);
    }

    /**
     * Fills a circle of the given radius, centered at (0,0)
     */
    public void fillCircle(int radius) {
        pdf.drawCircle(radius, true);
    }

    /**
     * Draws a line from (x1,y1) to (x2,y2)
     */
    public void drawLine(int x1, int y1, int x2, int y2) {
        pdf.drawLine(x1, y1, x2, y2);
    }

    /**
     * Changes the current color.
     */
    public void setColor(Color color) {
        pdf.setColor(color);
    }

    /**
     * Returns true if left<=x<=right or right<=x<=left.
     */
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
            case BOLD:
                pdf.setBoldLine();
                return;
            case DOTTED:
                pdf.setDottedLine();
                return;
            case DASHED:
                pdf.setDashedLine();
                return;
            default:
                pdf.setNormalLine();
                return;
        }
    }

    /**
     * Saves the current font boldness.
     */
    private boolean fontBoldness = false;

    /**
     * Changes the current font.
     */
    public void setFont(boolean fontBoldness) {
        this.fontBoldness = fontBoldness;
    }
}
