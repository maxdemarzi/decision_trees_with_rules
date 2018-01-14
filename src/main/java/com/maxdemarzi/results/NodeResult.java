package com.maxdemarzi.results;

import org.neo4j.graphdb.Node;

public class NodeResult {
    public final Node node;

    public NodeResult(Node node) {
        this.node = node;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o != null && getClass() == o.getClass() && node.equals(((NodeResult) o).node);
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }
}