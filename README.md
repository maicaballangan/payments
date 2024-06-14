INTRODUCTION
------------
This is an API Gateway integrated with Stripe

REQUIREMENTS
------------
This has been tested on Windows, Mac OS X, and Linux operating systems.
The minimum requirements are as follows:

* Git >= 1.9
* JDK >= 1.8 
* Play Framework = 1.5.x https://www.playframework.com/download#older-versions
* RAM >= 512MB (1GB preferred) for Play
* RAM >= 4GB (8GB preferred) for Docker

REPO CONTENTS
-------------
You should now have the following non-empty files and folders:

        app/                source files
        bin/                executable files
        conf/               configuration files
        data/               flat files
          attachments/      attachments
          cassandra/        Cassandra schemas
          elasticsearch/    ElasticSearch schemas
          geodb/            Geographical fixtures
          jmeter/           JMeter file and fixtures
          keys/             keystore file
          neo4j             Neo4j schemas
          postman/          Postman fixtures
          redis/            Redis fixtures
          riak/	            Riak fixtures 
        docs/               document files
          data model/       data model
          uml/              UML diagrams
        public/             static files       
        test/               test files
        CHANGELOG.md        Changelog
        LICENSE.md          license file
        README.md           this file                      

GETTING STARTED
---------------
`git clone --recursive -j11 https://github.com/maicaballangan/payments.git payments` 

http://localhost:9009/v1 

If you are asked for a Basic HTTP Authentication, enter:

username: payments  
password: payments@@1

Make sure you have enough RAM!!

ENJOY :-)

CHECK PERSISTENCE
-----------------
Now you can go to the links listed in the docker folder README file to view your fixtures

IDE INSTALLATION
----------------
Project files can be created for NetBeans, Eclipse, and IntelliJ.

* NetBeans:

    'Open Project' and point to the project directory. Make sure JDK 8 has been added. Configure the source level to 1.8
    under project properties.


* Eclipse:

    Choose 'Import..." and point to the project directory. Make sure JDK 8 has been added. Select JDK 8 as the project
    JDK.


* IntelliJ:

    Choose 'Open Project' and point to the project directory. Make sure JDK 8 has been added. Select JDK 8 as the project
    JDK.


CODE COVERAGE
--------------
You can run code coverage with `./bin/coverage.sh`

DEPENDENCY CHECK 
----------------
You can run a dependency check with `./bin/dependency-check.sh`

PERFORMANCE TESTING
--------------------
You can run performance testing with `./bin/performance-local.sh`
