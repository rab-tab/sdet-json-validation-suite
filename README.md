SDET Test - JSON Validation

A lightweight Java project for validating JSON data using  JSONPath queries. This project demonstrates automated validation of order data, including format checks, aggregation, and custom rules.

Tech Stack
-----------------------------

* JUnit 5 – Unit testing framework
* JSONPath – Querying and validating JSON fields with expressions
* Jackson – JSON parsing and serialization
* Everit JSON Schema – JSON schema validation
* Surefire HTML - Reporting  

Usage
----------------------------
1. Run all tests
   mvn clean test

 This runs:
* Schema validation tests
* Format and field validation tests
* Aggregation tests using OrderUtil

2.Generating HTML reports
  After running tests, generate the Surefire HTML report:
  mvn surefire-report:report

  Open the report:
  target/site/surefire-report.html

 Handling Expected Failures
 ----------------------------
* Certain tests are tagged with @Tag("expected-failure") to indicate known failing cases.Maven is configured with <testFailureIgnore>true</testFailureIgnore> so CI passes while still running failing tests.
* JSON schema validation runs once before all tests to prevent redundant checks.  


How to Extend
-----------------------------
1. Add new JSON test files Place your file in src/test/resources/ (e.g., new-orders.json). 
2. Add or update JSON schema Add a corresponding schema in the same folder (new-orders-schema.json). 
3. Add new test data providers Extend OrderDataProvider or CachedOrderProvider to include new JSON files. 
4. Write new tests
    * Create new test classes under src/test/java.
    * Extend BaseTest for automatic schema validation if needed.
    * Use @Tag("expected-failure") for tests that are known to fail but shouldn’t break CI.
