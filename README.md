![](https://github.com/OpenLiberty/open-liberty/blob/master/logos/logo_horizontal_light_navy.png)

# MongoDB Sample
This sample shows how to store data with MongoDB using CDI and MicroProfile Config.

## Environment Set Up
To run this sample, first [download](https://github.com/OpenLiberty/sample-mongodb/archive/master.zip) or clone this repo - to clone:
```
git clone git@github.com:OpenLiberty/sample-mongodb.git
```

### Setup MongoDB
You will also need a MongoDB instance to use this sample. If you have Docker installed, you can use the following:

```
docker pull mongo
docker run --name mongo-sample -p 127.0.0.1:27017:27017 -d mongo
```

If you don't have Docker, you can install MongoDB manually from [mongodb.com](https://docs.mongodb.com/manual/administration/install-community/)

Next, you will need to create a user for authentication. Issue the following commands from the command line: 

If you're using docker, the command line is accessible with: `docker exec -it mongo-sample bash`

```
mongo
use testdb
db.createUser({user: 'sampleUser', pwd:'openliberty', roles: [{ role: 'readWrite', db:'testdb'}]})
```

You should see the following:
```
Successfully added user: {
        "user" : "sampleUser",
        "roles" : [
                {
                        "role" : "readWrite",
                        "db" : "testdb"
                }
        ]
}
```
Now you are ready to run the sample. Type `exit` to get out of the mongo shell, and if using docker type `exit` again to exit the docker shell.

## Running the Sample
From inside the sample-mongodb directory, build and start the application in Open Liberty with the following command:
```
mvn clean package liberty:run-server
```
The server will listen on port 9080 by default. You can change the port (for example, to port 9081) by adding `mvn clean package liberty:run-server -DtestServerHttpPort=9081` to the end of the Maven command.

Once the server has started, the application is availible at http://localhost:9080/mongo/

### Try it out
Give the sample a try by registering a crew member. Enter a name (a String), an ID Number (an Integer), and select a Rank from the menu, then click 'Register Crew Member'.

Two more boxes will appear, one with your crew members (which you can click to remove) and one showing how your data looks in MongoDB.

### How it works
This application uses a CDI producer ([MongoProducer.java](https://github.com/OpenLiberty/sample-mongodb/tree/master/src/main/java/io/openliberty/sample/mongo/MongoProducer.java)) to inject a MongoDatabase. For more info on using a CDI producer with MongoDB, check out this [blog post](openliberty.io/blog/). It provides access to the database from three REST endpoints in [CrewService.java](https://github.com/OpenLiberty/sample-mongodb/tree/master/src/main/java/io/openliberty/sample/application/CrewService.java): `/add`, `/remove/{id}`, and `/retrieve`.

The `/add` endpoint uses [Bean Validation](https://openliberty.io/guides/bean-validation.html) to validate the data we recieve from the front end. [CrewMember.java](https://github.com/OpenLiberty/sample-mongodb/tree/master/src/main/java/io/openliberty/sample/application/CrewMember.java) shows the constraints as well as the messages we return to the user if those constraints aren't met.
```java
@NotEmpty(message = "All crew members must have a name!")
private String name;

@Pattern(regexp = "(Captain|Officer|Engineer)",  message = "Crew member must be one of the listed ranks!")
private String rank;

@Pattern(regexp = "^\\d+$", message = "ID Number must be a non-negative integer!")
private String crewID; 
```
After validation, we use the injected MongoDatabase to insert a new document with the crew member's information:
```java
MongoCollection<Document> crew = db.getCollection("Crew");
Document newCrewMember = new Document();
newCrewMember.put("Name",crewMember.getName());
newCrewMember.put("Rank",crewMember.getRank());
newCrewMember.put("CrewID",crewMember.getCrewID());
crew.insertOne(newCrewMember);
```

The `/remove/{id}` endpoint deletes a document corrosponding to the path parameter {id}
```java
crew.deleteOne(new Document("_id", new ObjectId(id))); 
```
The `/retrive` endpoint retrives the data and does some formatting for the front end.
```java
MongoCollection<Document> crew = db.getCollection("Crew");
for (Document d : crew.find()) {
	sb.append(d.toJson());
}
```


