package com.maxdemarzi.results;

import org.neo4j.graphdb.Path;

public class PathResult {
    public final Path path;

    public PathResult(Path path) {
        this.path = path;
    }
}
