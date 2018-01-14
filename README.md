# Decision Trees With Rules
POC Decision Tree traverser with rules

This project requires Neo4j 3.3.x or higher

Instructions
------------ 

This project uses maven, to build a jar-file with the procedure in this
project, simply package the project with maven:

    mvn clean package

This will produce a jar-file, `target/matcher-1.0-SNAPSHOT.jar`,
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
    CREATE (over21_rule:Rule { parameter_names: 'age', parameter_types:'int', expression:'age >= 21' })
    CREATE (gender_rule:Rule { parameter_names: 'age,gender', parameter_types:'int,String', expression:'(age >= 18) && gender.equals(\"female\")' })
    CREATE (answer_yes:Answer { id: 'yes'})
    CREATE (answer_no:Answer { id: 'no'})
    CREATE (tree)-[:HAS]->(over21_rule)
    CREATE (over21_rule)-[:IS_TRUE]->(answer_yes)
    CREATE (over21_rule)-[:IS_FALSE]->(gender_rule)
    CREATE (gender_rule)-[:IS_TRUE]->(answer_yes)
    CREATE (gender_rule)-[:IS_FALSE]->(answer_no)
    
Try it:

    CALL com.maxdemarzi.traverse.decision_tree('bar entrance', {gender:'male', age:'20'}) yield path return path;
    CALL com.maxdemarzi.traverse.decision_tree('bar entrance', {gender:'female', age:'19'}) yield path return path
    CALL com.maxdemarzi.traverse.decision_tree('bar entrance', {gender:'male', age:'23'}) yield path return path        