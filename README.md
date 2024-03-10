# Trade enrichment solution instruction
### Preconditions
- To run this solution, in addition to these technologies:
    - Spring boot 3.2.1
    - Java 17
    - Maven
- Install additionally:
    - hazelcast 5.3.6 (https://docs.hazelcast.com/hazelcast/5.3/getting-started/install-hazelcast)
Hazelcast was chosen to store unique information about products, for further interaction with them by Traders.

Hazelcast needs to be started on default port, 5701. 
In the future, when configuring Hazelcast in cloud providers,
it will be necessary to make changes to the solution code to interact with it over the network.

### How to run the solution
The solution can be launched as from the command line:
- mvn spring-boot:run

or from the IDE, by setting up the project configuration and then launching it

### How to use the solution
To use the solution, you need to query the running application,
being in the folder with the project code and with the forwarded csv file to be processed:
```curl
curl --data @src/test/resources/trade.csv --header 'Content-Type: text/csv' http://localhost:8080/api/v1/enrich
```
The file with processed trade data will be returned as a result.

### Discussion, ideas and further improvements
The following improvements can be realised:
  - Adding a Worker pool of threads to implement faster reading of a csv file, but it will be necessary to take into account the order of records that will form after the file is read, as the threads will be executed independently of each other
  - Add more test coverage: for file cleanup service, interaction with haselcast
  - Change the method of clearing created files from the Scheduled background job to a method that would be executed upon arrival of the corresponding event, which would contain information about the required file
  - Additional configuration of haselcast product map, adding life time for map objects and adding logic to check the presence of an object in the map and pre-add the object to the map, in case of its absence, but here it is necessary to take into account the time of searching such an object from csv file with products

