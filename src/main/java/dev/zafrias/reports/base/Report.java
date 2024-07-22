package dev.zafrias.reports.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.Document;

import java.util.UUID;

@Getter
@AllArgsConstructor
public final class Report implements DocumentCovertible {
    private final String id;

    private final UUID reporter, target;
    private final String reason;

    private static String createId() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        //simple shuffling
        while (id1.equals(id2)) {
            id1 = UUID.randomUUID();
        }

        String id1Format = id1.toString().replace("-", "");
        String id2Format = id2.toString().replace("-", "");

        //we take first 5 chars from both ids and merge them into 1
        return id1Format.substring(0, 6) + id2Format.substring(0, 6);
    }


    public Report(UUID reporter, UUID target, String reason) {
        this(createId(), reporter, target, reason);
    }

    public static Report fromDocument(Document document) {
        if (document == null) return null;
        return new Report(document.getString("id"),
                UUID.fromString(document.getString("reporter")),
                UUID.fromString(document.getString("target")),
                document.getString("reason"));
    }

    @Override
    public Document toDocument() {
        Document document = new Document();
        return document.append("id", id)
                .append("reporter", reporter.toString())
                .append("target", target.toString())
                .append("reason", reason);
    }

}
