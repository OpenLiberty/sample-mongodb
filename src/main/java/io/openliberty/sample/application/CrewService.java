/*******************************************************************************
* Copyright (c) 2018 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package io.openliberty.sample.application;

import java.util.Set;

import java.io.StringWriter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.Json;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import javax.validation.Validator;
import javax.validation.ConstraintViolation;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Path("/crew")
@ApplicationScoped
public class CrewService {

	@Inject
	MongoDatabase db;

	@Inject
    Validator validator;

	@POST
	@Path("/add") 
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON) 
	public String add(CrewMember crewMember) {
		
		Set<ConstraintViolation<CrewMember>> violations = validator.validate(crewMember);
		if(violations.size() > 0) {
			JsonArrayBuilder messages = Json.createArrayBuilder();
			for (ConstraintViolation<CrewMember> v : violations) { 			
				messages.add(v.getMessage());
			}
			return messages.build().toString();
        }

		MongoCollection<Document> crew = db.getCollection("Crew");
		Document newCrewMember = new Document();
		newCrewMember.put("Name",crewMember.getName());
		newCrewMember.put("Rank",crewMember.getRank());
		newCrewMember.put("CrewID",crewMember.getCrewID());
		crew.insertOne(newCrewMember);
		return "";
	}

	@GET
	@Path("/remove/{id}")
	public String remove(@PathParam("id") String id) {

		MongoCollection<Document> crew = db.getCollection("Crew");
		crew.deleteOne(new Document("_id", new ObjectId(id))); 
		return "";
	}



	@GET
	@Path("/retrieve")
    public String retrieve() {
		StringWriter sb = new StringWriter();

        try {
			MongoCollection<Document> crew = db.getCollection("Crew");
			sb.append("[");
			boolean first = true;
			for (Document d : crew.find()) {
				if (!first) sb.append(",");
				else first = false;
				sb.append(d.toJson());
			}
			sb.append("]");
        } catch (Exception e) {
            e.printStackTrace(System.out);

        }
        return sb.toString();
    }

}