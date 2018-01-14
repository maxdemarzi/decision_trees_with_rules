package com.maxdemarzi;

import com.maxdemarzi.schema.Labels;
import com.maxdemarzi.schema.RelationshipTypes;
import org.codehaus.janino.ExpressionEvaluator;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;

import java.util.Collections;
import java.util.Map;

public class DecisionTreeExpander implements PathExpander {
    private Map<String, Object> facts;
    ExpressionEvaluator ee = new ExpressionEvaluator();

    public DecisionTreeExpander(Map<String, Object> facts) {
        this.facts = facts;
        ee.setExpressionType(boolean.class);
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
                if (isTrue(path.endNode())) {
                    return path.endNode().getRelationships(Direction.OUTGOING, RelationshipTypes.IS_TRUE);
                } else {
                    return path.endNode().getRelationships(Direction.OUTGOING, RelationshipTypes.IS_FALSE);
                }
            } catch (Exception e) {
                // Could not continue this way!
                return Collections.emptyList();
            }
        }

        // Otherwise, not sure what to do really.
        return Collections.emptyList();
    }

    private boolean isTrue(Node rule) throws Exception {

            Map<String, Object> ruleProperties = rule.getAllProperties();
            String[] parameterNames = Magic.explode((String) ruleProperties.get("parameter_names"));
            Class<?>[] parameterTypes = Magic.stringToTypes((String) ruleProperties.get("parameter_types"));
            Object[] arguments = new Object[parameterNames.length];
            for (int j = 0; j < parameterNames.length; ++j) {
                arguments[j] = Magic.createObject(parameterTypes[j], (String) facts.get(parameterNames[j]));
            }

            ee.setParameters(parameterNames, parameterTypes);
            ee.cook((String)ruleProperties.get("expression"));

            return (boolean) ee.evaluate(arguments);
    }

    @Override
    public PathExpander reverse() {
        return null;
    }
}
