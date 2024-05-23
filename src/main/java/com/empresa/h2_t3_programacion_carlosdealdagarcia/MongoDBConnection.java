package com.empresa.h2_t3_programacion_carlosdealdagarcia;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

public class MongoDBConnection {
    private static final String CONNECTION_STRING = "mongodb+srv://admin:admin@cluster0.gomt1im.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
    private MongoDatabase database;

    public MongoDBConnection() {
        MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
        database = mongoClient.getDatabase("hito2_mongo");
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }

    public boolean registerUser(Persona user) {
        MongoCollection<Document> collection = database.getCollection("usuarios");
        Document existingUser = collection.find(new Document("correo", user.getCorreo())).first();

        if (existingUser != null) {
            return false;
        }

        Document newUser = new Document("nombre", user.getNombre())
                .append("correo", user.getCorreo())
                .append("contrasena", Cipher.encrypt(user.getContrasena()));

        collection.insertOne(newUser);
        return true;
    }

    public boolean authenticateUser(String correo, String contrasena) {
        MongoCollection<Document> collection = database.getCollection("usuarios");
        Document query = new Document("correo", correo);
        Document user = collection.find(query).first();

        if (user != null) {
            String storedPassword = user.getString("contrasena");
            return Cipher.encrypt(contrasena).equals(storedPassword);
        }

        return false;
    }
}