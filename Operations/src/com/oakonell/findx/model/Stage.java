package com.oakonell.findx.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Stage {
    private final int bgMusicId;
    private final int bgImageResId;
    private final String id;
    private final int titleId;
    private final List<ILevel> levels = Collections.synchronizedList(new ArrayList<ILevel>());
    private final Stage previousStage;

    public Stage(String id, int titleId, int bgMusicRes, int bgImage, Stage previousStage) {
        this.id = id;
        this.titleId = titleId;
        bgMusicId = bgMusicRes;
        bgImageResId = bgImage;
        this.previousStage = previousStage;
    }

    public String getId() {
        return id;
    }

    public int getTitleId() {
        return titleId;
    }

    public void addLevel(ILevel level) {
        levels.add(level);
    }

    public ILevel getNextLevel(ILevel level) {
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

    public ILevel getPreviousLevel(ILevel level) {
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

    public List<ILevel> getLevels() {
        return levels;
    }

    public ILevel getLevel(String key) {
        for (ILevel each : levels) {
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
        List<ILevel> prevLevels = previousStage.getLevels();
        return prevLevels.get(prevLevels.size() - 1).isUnlocked();
    }

	public int getBackgroundDrawableResourceId() {
		return bgImageResId;
	}
}
