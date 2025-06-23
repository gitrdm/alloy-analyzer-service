package AlloyAnalyzerService;

import AlloyAnalyzerService.DotExporter.*;
import io.grpc.stub.StreamObserver;
import filestream.FileStreamGrpc;
import filestream.Filestream.AnalysisResult;
import filestream.Filestream.FileStreamRequest;

import java.io.IOException;

import edu.mit.csail.sdg.alloy4.*;
import edu.mit.csail.sdg.alloy4graph.DotDirection;
import edu.mit.csail.sdg.alloy4viz.*;
import edu.mit.csail.sdg.ast.Command;
import edu.mit.csail.sdg.parser.CompUtil;
import edu.mit.csail.sdg.translator.*;

import edu.mit.csail.sdg.parser.CompModule;

import java.io.*;
import java.util.List;
import java.util.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * AlloyAnalyzerService provides a gRPC-based service for analyzing Alloy models and exporting results as DOT graphs.
 * <p>
 * This class implements the FileStream gRPC service, allowing clients to upload Alloy model files and analysis commands,
 * and receive streamed analysis results (such as DOT format for graph visualization).
 * </p>
 *
 * <h2>Usage</h2>
 * <ul>
 *   <li>Start the service using the provided Main class.</li>
 *   <li>Send Alloy model files and commands using a compatible gRPC client.</li>
 *   <li>Receive streamed analysis results, including DOT output for visualization.</li>
 * </ul>
 *
 * <h2>Key Methods</h2>
 * <ul>
 *   <li>{@link #uploadAndAnalyze(StreamObserver)}: Handles the main gRPC call for model analysis.</li>
 * </ul>
 *
 * <h2>See Also</h2>
 * <ul>
 *   <li>filestream.proto: gRPC service definition</li>
 *   <li>README.md: Project overview and usage</li>
 * </ul>
 */
public class AlloyAnalyzerService extends FileStreamGrpc.FileStreamImplBase {
    public static class SilentGraphMaker {
        /**
         * The theme customization.
         */
        private final BackgroundState view;

        /**
         * The projected instance for the graph currently being generated.
         */
        private final AlloyInstance instance;

        /**
         * The projected model for the graph currently being generated.
         */
        private final AlloyModel model;

        /**
         * The map that contains all edges and what the AlloyTuple that each edge
         * corresponds to.
         */
        private final Map<GraphEdge, AlloyTuple> edges = new LinkedHashMap<GraphEdge, AlloyTuple>();

        /**
         * The map that contains all nodes and what the AlloyAtom that each node
         * corresponds to.
         */
        private final Map<GraphNode, AlloyAtom> nodes = new LinkedHashMap<GraphNode, AlloyAtom>();

        /**
         * This maps each atom to the node representing it; if an atom doesn't have a
         * node, it won't be in the map.
         */
        private final Map<AlloyAtom, GraphNode> atom2node = new LinkedHashMap<AlloyAtom, GraphNode>();

        /**
         * This stores a set of additional labels we want to add to an existing node.
         */
        private final Map<GraphNode, Set<String>> attribs = new LinkedHashMap<GraphNode, Set<String>>();

        /**
         * The resulting graph.
         */
        private final Graph graph;

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
            final Map<AlloyRelation, Integer> rels = new TreeMap<AlloyRelation, Integer>();
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
            for (Map.Entry<GraphNode, Set<String>> e : attribs.entrySet()) {
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
                    /* label=moreLabel.toString(); */
                } else {
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

        /**
         * Attach tuple values as attributes to existing nodes.
         */
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
            Map<GraphNode, String> map = new LinkedHashMap<GraphNode, String>();
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
            for (Map.Entry<GraphNode, String> e : map.entrySet()) {
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
         * @param atom     - the atom
         * @param showSets - whether the label should also show the sets that this atom
         *                 belongs to
         *                 <p>
         *                 eg. If atom A is the 3rd atom in type T, and T's label is
         *                 "Person", then the return value would be "Person3".
         *                 <p>
         *                 eg. If atom A is the only atom in type T, and T's label is
         *                 "Person", then the return value would be "Person".
         *                 <p>
         *                 eg. If atom A is the 3rd atom in type T, and T's label is
         *                 "Person", and T belongs to the sets Set1, Set2, and Set3. However,
         *                 only Set1 and Set2 have "show in relational attribute == on", then
         *                 the return value would be "Person (Set1, Set2)".
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

    /**
     * Handles the UploadAndAnalyze gRPC call.
     * <p>
     * Receives a stream of FileStreamRequest messages (containing AlloyProperties and file chunks),
     * parses and analyzes the Alloy model, and streams back AnalysisResult messages (DOT output or errors).
     * </p>
     *
     * @param responseObserver the stream observer for sending analysis results to the client
     * @return a stream observer for receiving file upload and command requests
     */
    @Override
    public StreamObserver<FileStreamRequest> uploadAndAnalyze(StreamObserver<AnalysisResult> responseObserver) {
        return new StreamObserver<FileStreamRequest>() {
            private StringBuilder fileContent = new StringBuilder();

            private String inputCommand;

            @Override
            public void onNext(FileStreamRequest request) {
                if (request.getRequestCase() == FileStreamRequest.RequestCase.PROPS) {
                    inputCommand = request.getProps().getCommand();
//                    System.out.println("Received command: " + inputCommand);
                } else if (request.getRequestCase() == FileStreamRequest.RequestCase.CHUNK) {
                    fileContent.append(new String(request.getChunk().getContent().toByteArray()));
//                    System.out.println("Received chunk: " + request.getChunk().getSequence());
                }
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                System.out.println("Processed file with command: " + inputCommand);
//                String result = "";

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

                A4Options options = new A4Options();
                options.solver = A4Options.SatSolver.SAT4J;
                try {
                    CompModule world = CompUtil.parseEverything_fromString(rep, String.valueOf(fileContent));
                    int ix = 0;

                    Command commandToRun = null;
                    List<Command> commands = world.getAllCommands();
                    System.out.println("Received command: " + inputCommand);
                    System.out.println("Available commands in model:");
                    for (Command command : commands) {
                        System.out.println("  " + command.toString());
                        if (command.toString().equalsIgnoreCase(inputCommand)) {
                            commandToRun = command;
                            break;
                        }
                    }

                    if (commandToRun != null) {
                        // Execute the command
                        System.out.println("============ Command " + commandToRun + ": ============");
                        A4Solution solution = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), commandToRun, options);
                        boolean found = false;
                        // Check the first solution
                        System.out.println("Solution satisfiable: " + solution.satisfiable());
                        if (solution.satisfiable()) {
                            found = true;
                            try {
                                StringWriter sw = new StringWriter();
                                PrintWriter pw = new PrintWriter(sw);
                                solution.writeXML(pw, null, null);
                                pw.flush();
                                sw.flush();
                                String txt = sw.toString();
                                AlloyInstance originalInstance = StaticInstanceReader.parseInstance(new StringReader(txt), 0);
                                BackgroundState vizState = new BackgroundState(originalInstance);
                                Map<AlloyType, AlloyAtom> map = new LinkedHashMap<AlloyType, AlloyAtom>();
                                AlloyProjection emptyProjection = new AlloyProjection(map);
                                Graph graph = new Graph(vizState.getFontSize() / 12.0D);
                                SilentGraphMaker.produceGraph(graph, originalInstance, vizState, emptyProjection);
                                String dot = graph.toString();
                                String json = graphToJson(graph);
                                FileWriter fw = new FileWriter("filePath." + ix + ".dot");
                                fw.write(dot);
                                fw.close();
                                AnalysisResult analysisResult = AnalysisResult.newBuilder().setDot(dot).setJson(json).build();
                                responseObserver.onNext(analysisResult);
                            } catch (IOException e) {
                                System.out.println("Error: unable to generate the graph");
                            }
                            ix++;
                        }
                        while (solution != solution.next()) {
                            solution = solution.next();
                            System.out.println("Solution satisfiable: " + solution.satisfiable());
                            if (solution.satisfiable()) {
                                found = true;
                                try {
                                    StringWriter sw = new StringWriter();
                                    PrintWriter pw = new PrintWriter(sw);
                                    solution.writeXML(pw, null, null);
                                    pw.flush();
                                    sw.flush();
                                    String txt = sw.toString();
                                    AlloyInstance originalInstance = StaticInstanceReader.parseInstance(new StringReader(txt), 0);
                                    BackgroundState vizState = new BackgroundState(originalInstance);
                                    Map<AlloyType, AlloyAtom> map = new LinkedHashMap<AlloyType, AlloyAtom>();
                                    AlloyProjection emptyProjection = new AlloyProjection(map);
                                    Graph graph = new Graph(vizState.getFontSize() / 12.0D);
                                    SilentGraphMaker.produceGraph(graph, originalInstance, vizState, emptyProjection);
                                    String dot = graph.toString();
                                    String json = graphToJson(graph);
                                    FileWriter fw = new FileWriter("filePath." + ix + ".dot");
                                    fw.write(dot);
                                    fw.close();
                                    AnalysisResult analysisResult = AnalysisResult.newBuilder().setDot(dot).setJson(json).build();
                                    responseObserver.onNext(analysisResult);
                                } catch (IOException e) {
                                    System.out.println("Error: unable to generate the graph");
                                }
                                ix++;
                            }
                        }
                        if (!found) {
                            String result = "No solution found for command: " + inputCommand;
                            System.out.println(result);
                            AnalysisResult analysisResult = AnalysisResult.newBuilder().setDot("").setJson("").build();
                            responseObserver.onNext(analysisResult);
                        }
                    } else {
                        String result = "Command '" + inputCommand + "' not found.";
                        System.out.println(result);
                        AnalysisResult analysisResult = AnalysisResult.newBuilder().setDot(result).setJson("").build();
                        responseObserver.onNext(analysisResult);
                    }
//                    A4Solution solution = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), world.getAllCommands().get(0), options);
//                    result = solution.toString();
                } catch (Exception e) {
                    String result = "Error analyzing file: " + e.getMessage();
                    AnalysisResult analysisResult = AnalysisResult.newBuilder().setDot(result).setJson("").build();
                    responseObserver.onNext(analysisResult);
                }
                responseObserver.onCompleted();
            }
        };
    }

    /**
     * Utility to convert a Graph to a simple JSON representation (nodes/edges/attributes).
     */
    private static String graphToJson(Graph graph) {
        JsonObject root = new JsonObject();
        JsonArray nodes = new JsonArray();
        JsonArray edges = new JsonArray();
        for (GraphNode n : graph.nodes) {
            JsonObject node = new JsonObject();
            node.addProperty("id", n.uuid != null ? n.uuid.toString() : n.toString());
            node.addProperty("label", n.getLabel());
            node.addProperty("color", n.getColor());
            node.addProperty("shape", n.getShape());
            nodes.add(node);
        }
        for (GraphEdge e : graph.edges) {
            JsonObject edge = new JsonObject();
            edge.addProperty("from", e.getA().uuid != null ? e.getA().uuid.toString() : e.getA().toString());
            edge.addProperty("to", e.getB().uuid != null ? e.getB().uuid.toString() : e.getB().toString());
            edge.addProperty("label", e.getLabel());
            edge.addProperty("color", e.getColor());
            edges.add(edge);
        }
        root.add("nodes", nodes);
        root.add("edges", edges);
        return root.toString();
    }
}

