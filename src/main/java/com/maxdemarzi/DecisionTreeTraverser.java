package com.maxdemarzi;

import com.maxdemarzi.results.PathResult;
import com.maxdemarzi.schema.Labels;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

public class DecisionTreeTraverser {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;
    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log;

    private static final DecisionTreeEvaluator decisionTreeEvaluator = new DecisionTreeEvaluator();

    @Procedure(name = "com.maxdemarzi.traverse.decision_tree", mode = Mode.READ)
    @Description("CALL com.maxdemarzi.traverse.decision_tree(tree, facts) - traverse decision tree")
    public Stream<PathResult> traverseDecisionTree(@Name("tree") String id, @Name("facts") Map<String, Object> facts) throws IOException {
        // Which Decision Tree are we interested in?
        Node tree = db.findNode(Labels.Tree, "id", id);
        if ( tree != null) {
            // Find the paths by traversing this graph and the facts given
            return decisionPath(tree, facts);
        }
        return null;
    }

    private Stream<PathResult> decisionPath(Node tree, Map<String, Object> facts) {
        TraversalDescription myTraversal = db.traversalDescription()
                .depthFirst()
                .expand(new DecisionTreeExpander(facts))
                .evaluator(decisionTreeEvaluator);

        return myTraversal.traverse(tree).stream().map(PathResult::new);
    }
}
