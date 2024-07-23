package dev.zafrias.reports.base.impl;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import dev.zafrias.reports.base.Criteria;
import dev.zafrias.reports.base.Report;
import dev.zafrias.reports.base.ReportDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class ReportDatabaseImpl implements ReportDatabase {

    private final static String COLLECTION = "reports";
    private final FileConfiguration config;
    private MongoClient client;
    private MongoDatabase database;

    public ReportDatabaseImpl(FileConfiguration config) {
        this.config = config;
    }

    private Object option(String name) {
        return config.get("mongo-database." + name);
    }

    private boolean collectionExists() {
        ListCollectionNamesIterable collectionList = database.listCollectionNames();

        try (MongoCursor<String> cursor = collectionList.iterator()) {
            while (cursor.hasNext()) {
                if (cursor.next().equals(COLLECTION)) {
                    return true;
                }
            }

        }

        return false;
    }

    private static Bson singleCriteriaToFilter(Criteria criteria) {
        return Filters.eq(criteria.getField(), criteria.getField());
    }

    private static Bson multipleCriteriaToFilter(Criteria... criteria) {
        Bson[] filters = new Bson[criteria.length];
        for (int i = 0; i < criteria.length; i++) {
            filters[i] = singleCriteriaToFilter(criteria[i]);
        }
        return Filters.and(filters);
    }

    @Override
    public void connect() {
        boolean useAuth = (boolean) option("use-authentication");

        ConnectionString connectionString = new
                ConnectionString("mongodb://" + (useAuth ? option("user") +
                ":" + option("password") + "@" : "")
                + option("host")
                + ":" + option("port") + "/?authSource=" + option("database-name"));

        client = MongoClients.create(MongoClientSettings.builder()
                .applyConnectionString(connectionString).build());
        database = client.getDatabase((String) option("database-name"));

        if (!collectionExists()) {
            database.createCollection(COLLECTION);
        }
    }


    @Override
    public CompletableFuture<Void> saveReport(Report report) {
        return CompletableFuture.runAsync(() ->
                database.getCollection(COLLECTION)
                        .insertOne(report.toDocument()));
    }

    @Override
    public CompletableFuture<Report> loadReport(Criteria... criteria) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = database.getCollection(COLLECTION)
                    .find(multipleCriteriaToFilter(criteria))
                    .first();
            return Report.fromDocument(document);
        });
    }

    @Override
    public CompletableFuture<Collection<Report>> loadAllReports(Criteria... criteria) {
        return CompletableFuture.supplyAsync(() -> {

            Set<Report> reports = new HashSet<>();
            try (MongoCursor<Document> cursor = database.getCollection(COLLECTION)
                    .find(multipleCriteriaToFilter(criteria))
                    .iterator()) {
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    reports.add(Report.fromDocument(doc));
                }
            }
            return reports;
        });
    }

    @Override
    public CompletableFuture<Void> updateReports(Criteria criteria, Bson updates) {
        return CompletableFuture.runAsync(() -> {
            Bson filter = singleCriteriaToFilter(criteria);
            database.getCollection(COLLECTION)
                    .updateOne(filter, updates);
        });
    }

    @Override
    public CompletableFuture<Void> deleteReport(Criteria criteria) {
        return CompletableFuture.runAsync(() -> database.getCollection(COLLECTION)
                .deleteOne(singleCriteriaToFilter(criteria)));
    }

    @Override
    public void disconnect() {
        this.client = null;
        this.database = null;
    }
}
