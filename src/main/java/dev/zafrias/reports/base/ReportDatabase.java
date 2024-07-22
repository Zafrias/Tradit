package dev.zafrias.reports.base;

import org.bson.conversions.Bson;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface ReportDatabase {

    //CRUD

    void connect();

    CompletableFuture<Void> saveReport(Report report);

    CompletableFuture<Report> loadReport(Criteria... criteria);

    CompletableFuture<Collection<Report>> loadAllReports(Criteria... criteria);

    CompletableFuture<Void> updateReports(Criteria criteria, Bson updates);

    CompletableFuture<Void> deleteReport(Criteria criteria);

    void disconnect();
}
