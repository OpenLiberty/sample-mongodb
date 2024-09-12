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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
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
        //{"name":"Test","rank":"Captain","crewID":"12345"}
        restClient.target(baseURL + "it").request().post(Entity.json("{\"name\":\"Test\",\"rank\":\"Captain\",\"crewID\":\"12345\"}"));

        Response response = restClient.target(baseURL).request().get();
        JsonReader reader = Json.createReader(new StringReader(response.readEntity(String.class)));
        JsonArray array = reader.readArray();
        System.out.println(array);
    }
}