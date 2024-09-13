/*******************************************************************************
* Copyright (c) 2024 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package io.openliberty.sample.it;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringReader;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;

public class MongoIT {
        
    private static Client restClient;
    private static String baseURL;
    
    @BeforeAll
    public static void setup() throws Exception {             
        String port = System.getProperty("http.port");
        baseURL = "http://localhost:" + port + "/db/crew/";
        
        restClient = ClientBuilder.newClient();
    }

    @AfterAll
    public static void teardown() throws Exception {
        restClient.close();
    }

    /**
     * Calls the schedule endpoint, and confirms the first two messages over the
     * websocket are available and increment.
     */
    @Test
    public void CreateRetrieveDeleteTest() throws InterruptedException {
        restClient.target(baseURL + "it").request().post(Entity.json("{\"name\":\"Test\",\"rank\":\"Captain\",\"crewID\":\"12345\"}"));

        Response response = restClient.target(baseURL).request().get();
        JsonReader reader = Json.createReader(new StringReader(response.readEntity(String.class)));
        JsonArray array = reader.readArray();
        System.out.println(array);
        String id = null;
        for (JsonValue value : array) {
            JsonObject obj = value.asJsonObject();
            if (obj.getString("Name").equals("Test") &&
                obj.getString("Rank").equals("Captain") &&
                obj.getString("CrewID").equals("12345"))
                id = obj.getJsonObject("_id").getString("$oid");
        }
        assertNotNull(id, "CrewMember not found in returned value: " + array);

        restClient.target(baseURL + id).request().delete();

        response = restClient.target(baseURL).request().get();
        reader = Json.createReader(new StringReader(response.readEntity(String.class)));
        array = reader.readArray();

        for (JsonValue value : array) {
            System.out.println(value.asJsonObject().getJsonObject("_id").getString("$oid"));
            if (id == value.asJsonObject().getJsonObject("_id").getString("$oid"))
                fail("CrewMember should have been deleted, but id was found: " + id);
        }
    }
}