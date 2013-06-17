package com.oakonell.findx.custom.model;

import com.oakonell.findx.model.Level;
import com.oakonell.findx.model.Stage;

public class CustomLevel extends Level {
    private long dbId;
    private int sequence;
    private boolean isImported;
    private String author;

    public CustomLevel(CustomLevelBuilder builder, Stage stage) {

        // long dbId, int sequence, Stage stage, String id, String name,
        // Equation equation, List<Operation> operations,
        // int maxMoves, int minMoves) {
        super(stage, builder.getTitle(), builder.getMoves().get(0).getStartEquation(), builder.getOperations(),
                builder.getMoveOperationIndices());
        dbId = builder.getId();
        sequence = builder.getSequence();
        isImported = builder.isImported();
        author = builder.getAuthor();
    }

    public long getDbId() {
        return dbId;
    }

    public int getSequence() {
        return sequence;
    }

    public boolean isImported() {
        return isImported;
    }

    public String getAuthor() {
        return author;
    }
}
