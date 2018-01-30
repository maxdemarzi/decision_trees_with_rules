# Decision Trees With Rules
POC Decision Tree traverser with rules

This project requires Neo4j 3.3.x or higher

Instructions
------------ 

This project uses maven, to build a jar-file with the procedure in this
project, simply package the project with maven:

    mvn clean package

This will produce a jar-file, `target/decision_trees_with_rules-1.0-SNAPSHOT.jar`,
that can be copied to the `plugin` directory of your Neo4j instance.

    cp target/decision_trees_with_rules-1.0-SNAPSHOT.jar neo4j-enterprise-3.3.1/plugins/.
    

Download and Copy two additional files to your Neo4j plugins directory:

    http://central.maven.org/maven2/org/codehaus/janino/commons-compiler/3.0.8/commons-compiler-3.0.8.jar
    http://central.maven.org/maven2/org/codehaus/janino/janino/3.0.8/janino-3.0.8.jar


Edit your Neo4j/conf/neo4j.conf file by adding this line:

    dbms.security.procedures.unrestricted=com.maxdemarzi.*    

Restart your Neo4j Server.

Create the Schema by running this stored procedure:

    CALL com.maxdemarzi.schema.generate
    
Create some test data:

    CREATE (tree:Tree { id: 'bar entrance' })
    CREATE (over21_rule:Rule { name: 'Over 21?', parameter_names: 'age', parameter_types:'int', expression:'age >= 21' })
    CREATE (gender_rule:Rule { name: 'Over 18 and female', parameter_names: 'age,gender', parameter_types:'int,String', expression:'(age >= 18) && gender.equals(\"female\")' })
    CREATE (answer_yes:Answer { id: 'yes'})
    CREATE (answer_no:Answer { id: 'no'})
    CREATE (tree)-[:HAS]->(over21_rule)
    CREATE (over21_rule)-[:IS_TRUE]->(answer_yes)
    CREATE (over21_rule)-[:IS_FALSE]->(gender_rule)
    CREATE (gender_rule)-[:IS_TRUE]->(answer_yes)
    CREATE (gender_rule)-[:IS_FALSE]->(answer_no)
    
Try it:

    CALL com.maxdemarzi.traverse.decision_tree('bar entrance', {gender:'male', age:'20'}) yield path return path;
    CALL com.maxdemarzi.traverse.decision_tree('bar entrance', {gender:'female', age:'19'}) yield path return path;
    CALL com.maxdemarzi.traverse.decision_tree('bar entrance', {gender:'male', age:'23'}) yield path return path;     
    
    
Evaluating Scripts instead of expressions.

Create some test data:

    CREATE (tree:Tree { id: 'funeral' })
    CREATE (good_man_rule:Rule { name: 'Was Lil Jon a good man?', parameter_names: 'answer_1', parameter_types:'String', expression:'switch (answer_1) { case \"yeah\": return \"OPTION_1\"; case \"what\": return \"OPTION_2\"; case \"okay\": return \"OPTION_3\"; default: return \"UNKNOWN\"; }' })
    CREATE (good_man_two_rule:Rule { name: 'I said, was he a good man?', parameter_names: 'answer_2', parameter_types:'String', expression:'switch (answer_2) { case \"yeah\": return \"OPTION_1\"; case \"what\": return \"OPTION_2\"; case \"okay\": return \"OPTION_3\"; default: return \"UNKNOWN\"; }' })
    CREATE (rest_in_peace_rule:Rule { name: 'May he rest in peace', parameter_names: 'answer_3', parameter_types:'String', expression:'switch (answer_3) { case \"yeah\": return \"OPTION_1\"; case \"what\": return \"OPTION_2\"; case \"okay\": return \"OPTION_3\"; default: return \"UNKNOWN\"; } ' })
    CREATE (answer_correct:Answer { id: 'correct'})
    CREATE (answer_incorrect:Answer { id: 'incorrect'})
    CREATE (answer_unknown:Answer { id: 'unknown'})
    CREATE (tree)-[:HAS]->(good_man_rule)
    CREATE (good_man_rule)-[:OPTION_1]->(answer_incorrect)
    CREATE (good_man_rule)-[:OPTION_2]->(good_man_two_rule)
    CREATE (good_man_rule)-[:OPTION_3]->(answer_incorrect)
    CREATE (good_man_rule)-[:UNKNOWN]->(answer_unknown)
    
    CREATE (good_man_two_rule)-[:OPTION_1]->(rest_in_peace_rule)
    CREATE (good_man_two_rule)-[:OPTION_2]->(answer_incorrect)
    CREATE (good_man_two_rule)-[:OPTION_3]->(answer_incorrect)
    CREATE (good_man_two_rule)-[:UNKNOWN]->(answer_unknown)
    
    CREATE (rest_in_peace_rule)-[:OPTION_1]->(answer_incorrect)
    CREATE (rest_in_peace_rule)-[:OPTION_2]->(answer_incorrect)
    CREATE (rest_in_peace_rule)-[:OPTION_3]->(answer_correct)
    CREATE (rest_in_peace_rule)-[:UNKNOWN]->(answer_unknown);    

    
Try it:


    CALL com.maxdemarzi.traverse.decision_tree_two('funeral', {answer_1:'yeah', answer_2:'yeah', answer_3:'yeah'}) yield path return path    
    CALL com.maxdemarzi.traverse.decision_tree_two('funeral', {answer_1:'what', answer_2:'', answer_3:''}) yield path return path    
    CALL com.maxdemarzi.traverse.decision_tree_two('funeral', {answer_1:'what', answer_2:'yeah', answer_3:'okay'}) yield path return path    