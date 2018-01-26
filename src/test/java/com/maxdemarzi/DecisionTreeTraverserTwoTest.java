package com.maxdemarzi;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static junit.framework.TestCase.assertEquals;

public class DecisionTreeTraverserTwoTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Rule
    public final Neo4jRule neo4j = new Neo4jRule()
            .withFixture(MODEL_STATEMENT)
            .withProcedure(DecisionTreeTraverser.class);

    @Test
    public void testTraversal() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY1);
        int count = response.get("results").get(0).get("data").size();
        assertEquals(1, count);
        ArrayList<Map> path1 = mapper.convertValue(response.get("results").get(0).get("data").get(0).get("row").get(0), ArrayList.class);
        assertEquals("incorrect", path1.get(path1.size() - 1).get("id"));
    }

    private static final Map QUERY1 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL com.maxdemarzi.traverse.decision_tree_two('funeral', {answer_1:'yeah', answer_2:'yeah', answer_3:'yeah'}) yield path return path")));

    @Test
    public void testTraversalTwo() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY2);
        int count = response.get("results").get(0).get("data").size();
        assertEquals(1, count);
        ArrayList<Map> path1 = mapper.convertValue(response.get("results").get(0).get("data").get(0).get("row").get(0), ArrayList.class);
        assertEquals("unknown", path1.get(path1.size() - 1).get("id"));
    }

    private static final Map QUERY2 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL com.maxdemarzi.traverse.decision_tree_two('funeral', {answer_1:'what', answer_2:'', answer_3:''}) yield path return path")));

    @Test
    public void testTraversalThree() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY3);
        int count = response.get("results").get(0).get("data").size();
        assertEquals(1, count);
        ArrayList<Map> path1 = mapper.convertValue(response.get("results").get(0).get("data").get(0).get("row").get(0), ArrayList.class);
        assertEquals("correct", path1.get(path1.size() - 1).get("id"));
    }

    private static final Map QUERY3 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL com.maxdemarzi.traverse.decision_tree_two('funeral', {answer_1:'what', answer_2:'yeah', answer_3:'okay'}) yield path return path")));


    private static final String MODEL_STATEMENT =
            "CREATE (tree:Tree { id: 'funeral' })" +
                    "CREATE (good_man_rule:Rule { name: 'Was Lil Jon a good man?', parameter_names: 'answer_1', parameter_types:'String', script:'switch (answer_1) { case \"yeah\": return \"OPTION_1\"; case \"what\": return \"OPTION_2\"; case \"okay\": return \"OPTION_3\"; default: return \"UNKNOWN\"; }' })" +
                    "CREATE (good_man_two_rule:Rule { name: 'I said, was he a good man?', parameter_names: 'answer_2', parameter_types:'String', script:'switch (answer_2) { case \"yeah\": return \"OPTION_1\"; case \"what\": return \"OPTION_2\"; case \"okay\": return \"OPTION_3\"; default: return \"UNKNOWN\"; }' })" +
                    "CREATE (rest_in_peace_rule:Rule { name: 'May he rest in peace', parameter_names: 'answer_3', parameter_types:'String', script:'switch (answer_3) { case \"yeah\": return \"OPTION_1\"; case \"what\": return \"OPTION_2\"; case \"okay\": return \"OPTION_3\"; default: return \"UNKNOWN\"; } ' })" +
                    "CREATE (answer_correct:Answer { id: 'correct'})" +
                    "CREATE (answer_incorrect:Answer { id: 'incorrect'})" +
                    "CREATE (answer_unknown:Answer { id: 'unknown'})" +
                    "CREATE (tree)-[:HAS]->(good_man_rule)" +
                    "CREATE (good_man_rule)-[:OPTION_1]->(answer_incorrect)" +
                    "CREATE (good_man_rule)-[:OPTION_2]->(good_man_two_rule)" +
                    "CREATE (good_man_rule)-[:OPTION_3]->(answer_incorrect)" +
                    "CREATE (good_man_rule)-[:UNKNOWN]->(answer_unknown)" +

                    "CREATE (good_man_two_rule)-[:OPTION_1]->(rest_in_peace_rule)" +
                    "CREATE (good_man_two_rule)-[:OPTION_2]->(answer_incorrect)" +
                    "CREATE (good_man_two_rule)-[:OPTION_3]->(answer_incorrect)" +
                    "CREATE (good_man_two_rule)-[:UNKNOWN]->(answer_unknown)" +

                    "CREATE (rest_in_peace_rule)-[:OPTION_1]->(answer_incorrect)" +
                    "CREATE (rest_in_peace_rule)-[:OPTION_2]->(answer_incorrect)" +
                    "CREATE (rest_in_peace_rule)-[:OPTION_3]->(answer_correct)" +
                    "CREATE (rest_in_peace_rule)-[:UNKNOWN]->(answer_unknown)";
}
