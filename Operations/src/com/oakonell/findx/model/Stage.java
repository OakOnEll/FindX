package com.oakonell.findx.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Stage {
    private final int bgMusicId;
    private final String id;
    private final int titleId;
    private final List<Level> levels = Collections.synchronizedList(new ArrayList<Level>());
    private final Stage previousStage;

    public Stage(String id, int titleId, int bgMusicRes, Stage previousStage) {
        this.id = id;
        this.titleId = titleId;
        bgMusicId = bgMusicRes;
        this.previousStage = previousStage;
    }

    public String getId() {
        return id;
    }

    public int getTitleId() {
        return titleId;
    }

    public void addLevel(Level level) {
        levels.add(level);
    }

    public Level getNextLevel(Level level) {
        int indexOf = levels.indexOf(level);
        if (indexOf < -1) {
            throw new IllegalArgumentException("No such level in this stage");
        }
        int nextIndex = indexOf + 1;
        if (nextIndex >= levels.size()) {
            return null;
        }
        return levels.get(nextIndex);
    }

    public Level getPreviousLevel(Level level) {
        int indexOf = levels.indexOf(level);
        if (indexOf < -1) {
            throw new IllegalArgumentException("No such level in this stage");
        }
        int nextIndex = indexOf - 1;
        if (nextIndex < 0) {
            return null;
        }
        return levels.get(nextIndex);
    }

    public List<Level> getLevels() {
        return levels;
    }

    public Level getLevel(String key) {
        for (Level each : levels) {
            if (each.getId().equals(key)) {
                return each;
            }
        }
        return null;
    }

    public int getBgMusicId() {
        return bgMusicId;
    }

    public boolean isUnlocked() {
        if (previousStage == null) {
            return true;
        }
        List<Level> prevLevels = previousStage.getLevels();
        return prevLevels.get(prevLevels.size() - 1).isUnlocked();
    }
}
