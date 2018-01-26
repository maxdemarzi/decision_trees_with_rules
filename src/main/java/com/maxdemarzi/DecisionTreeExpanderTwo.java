package com.maxdemarzi;

import com.maxdemarzi.schema.Labels;
import com.maxdemarzi.schema.RelationshipTypes;
import org.codehaus.janino.ScriptEvaluator;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;

import java.util.Collections;
import java.util.Map;

public class DecisionTreeExpanderTwo implements PathExpander {
    private Map<String, String> facts;
    ScriptEvaluator se = new ScriptEvaluator();

    public DecisionTreeExpanderTwo(Map<String, String> facts) {
        this.facts = facts;
        se.setReturnType(String.class);
    }

    @Override
    public Iterable<Relationship> expand(Path path, BranchState branchState) {
        // If we get to an Answer stop traversing, we found a valid path.
        if (path.endNode().hasLabel(Labels.Answer)) {
            return Collections.emptyList();
        }

        // If we have Rules to evaluate, go do that.
        if (path.endNode().hasRelationship(Direction.OUTGOING, RelationshipTypes.HAS)) {
            return path.endNode().getRelationships(Direction.OUTGOING, RelationshipTypes.HAS);
        }

        if (path.endNode().hasLabel(Labels.Rule)) {
            try {
                return path.endNode().getRelationships(Direction.OUTGOING, choosePath(path.endNode()));
            } catch (Exception e) {
                // Could not continue this way!
                return Collections.emptyList();
            }
        }

        // Otherwise, not sure what to do really.
        return Collections.emptyList();
    }

    private RelationshipType choosePath(Node rule) throws Exception {
        // Get the properties of the rule stored in the node
        Map<String, Object> ruleProperties = rule.getAllProperties();
        String[] parameterNames = Magic.explode((String) ruleProperties.get("parameter_names"));
        Class<?>[] parameterTypes = Magic.stringToTypes((String) ruleProperties.get("parameter_types"));

        // Fill the arguments array with their corresponding values
        Object[] arguments = new Object[parameterNames.length];
        for (int j = 0; j < parameterNames.length; ++j) {
            arguments[j] = Magic.createObject(parameterTypes[j], facts.get(parameterNames[j]));
        }

        // Set our parameters with their matching types
        se.setParameters(parameterNames, parameterTypes);

        // And now we "cook" (scan, parse, compile and load) the expression.
        se.cook((String)ruleProperties.get("expression"));

        return RelationshipType.withName((String) se.evaluate(arguments));
    }

    @Override
    public PathExpander reverse() {
        return null;
    }
}
