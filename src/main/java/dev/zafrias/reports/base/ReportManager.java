package dev.zafrias.reports.base;

import dev.zafrias.reports.Tradit;
import dev.zafrias.reports.base.impl.ReportDatabaseImpl;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class ReportManager {

    private final ReportDatabase database;
    private final Map<UUID, Set<Report>> targetReports = new HashMap<>();

    public ReportManager(Tradit plugin, FileConfiguration configuration) {
        //connect to database in here
        this.database = new ReportDatabaseImpl(configuration);
        try {
            plugin.getLogger().info("Attempting to connect to MongoDB");
            database.connect();
            plugin.getLogger().info("Connected to database successfully !");
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, ex, ()-> "Failed to connect to the database !");
            database.disconnect();
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
        //load data from db into map here
        database.loadAllReports()
                .whenComplete((reports, ex) -> {
                    for (Report report : reports) {
                        targetReports.compute(report.getTarget(), ((uuid, reps) -> {
                            if (reps == null) {
                                Set<Report> newReports = new HashSet<>();
                                newReports.add(report);
                                return newReports;
                            }
                            reps.add(report);
                            return reps;
                        }));
                    }
                });
    }

    public Set<Report> getTargetReports(UUID targetUUID) {
        return targetReports.get(targetUUID);
    }

    public CompletableFuture<Void> addReport(UUID reporterUUID, UUID targetUUID, String reason) {
        Report report = new Report(reporterUUID, targetUUID, reason);
        targetReports.compute(targetUUID, ((uuid, reports) -> {
            if (reports == null) {
                Set<Report> newReports = new HashSet<>();
                newReports.add(report);
                return newReports;
            }
            reports.add(report);
            return reports;
        }));

        return database.saveReport(report);
    }


}
