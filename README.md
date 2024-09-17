![](https://github.com/OpenLiberty/open-liberty/blob/master/logos/logo_horizontal_light_navy.png)

# MongoDB Sample
This sample shows how to store data with MongoDB using CDI and MicroProfile Config, as well as data validation with Jakarta Bean Validation.

## Environment Set Up
To run this sample, first [download](https://github.com/OpenLiberty/sample-mongodb/archive/main.zip) or clone this repo - to clone:
```
git clone git@github.com:OpenLiberty/sample-mongodb.git
```

### Setup MongoDB
You will also need a MongoDB instance to use this sample. If you have Docker installed, you can use the following:

```
docker run -d --name liberty_mongo -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=sampleUser -e MONGO_INITDB_ROOT_PASSWORD=openliberty mongo:7.0.7
```

If you don't have Docker, you can install MongoDB manually from [mongodb.com](https://docs.mongodb.com/manual/administration/install-community/)

Next, you will need to create a user for authentication. Issue the following commands from the command line: 

If you're using docker, you can skip this step.

```
mongosh
use testdb
db.createUser({user: 'sampleUser', pwd:'openliberty', roles: [{ role: 'readWrite', db:'admin'}]})
```

You should see the following:
```
{ ok: 1 }
```
Now you are ready to run the sample. Type `exit` to get out of the mongo shell.

## Running the Sample
From inside the sample-mongodb directory, build and start the application in Open Liberty with the following command:
```
./mvnw liberty:dev
```

Once the server has started, the application is available at http://localhost:9080

### Try it out
Give the sample a try by registering a crew member. Enter a name (a String), an ID Number (an Integer), and select a Rank from the menu, then click 'Register Crew Member'.

Two more boxes will appear, one with your crew members (which you can click to remove) and one showing how your data looks in MongoDB.

### Stop MongoDB
If you started MongoDB using docker, you can stop the container with:
```
docker stop liberty_mongo
```

### How it works
This application uses a CDI producer ([MongoProducer.java](https://github.com/OpenLiberty/sample-mongodb/tree/master/src/main/java/io/openliberty/sample/mongo/MongoProducer.java)) to inject a MongoDatabase. For more info on using a CDI producer with MongoDB, check out this [blog post](https://openliberty.io/blog/2019/02/19/mongodb-with-open-liberty.html). It provides access to the database in a RESTful manner in [CrewService.java](https://github.com/OpenLiberty/sample-mongodb/tree/master/src/main/java/io/openliberty/sample/application/CrewService.java) using the `/db/crew` endpoint.

Calling `POST /{id}` on the endpoint uses [Bean Validation](https://openliberty.io/guides/bean-validation.html) to validate the data we receive from the front end. [CrewMember.java](https://github.com/OpenLiberty/sample-mongodb/tree/master/src/main/java/io/openliberty/sample/application/CrewMember.java) shows the constraints as well as the messages we return to the user if those constraints aren't met.
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

Calling `DELETE /{id}` on the endpoint deletes a document corresponding to the path parameter {id}
```java
crew.deleteOne(new Document("_id", new ObjectId(id))); 
```
Calling `GET` on the endpoint retrieves the data and does some formatting for the front end.
```java
MongoCollection<Document> crew = db.getCollection("Crew");
for (Document d : crew.find()) {
	sb.append(d.toJson());
}
```


