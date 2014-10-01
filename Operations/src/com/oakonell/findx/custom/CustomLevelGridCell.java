package com.oakonell.findx.custom;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.oakonell.findx.custom.model.CustomLevel;
import com.oakonell.utils.activity.dragndrop.DragController;
import com.oakonell.utils.activity.dragndrop.DragSource;
import com.oakonell.utils.activity.dragndrop.DragView;
import com.oakonell.utils.activity.dragndrop.DropTarget;
import com.oakonell.utils.activity.dragndrop.OnDropListener;

public class CustomLevelGridCell extends LinearLayout implements DragSource, DropTarget {
    private OnDropListener onDropListener;

    public void setOnDropListener(OnDropListener onDropListener) {
        this.onDropListener = onDropListener;
    }

    // broken MVC?
    private CustomLevel level;

    public CustomLevelGridCell(Context context) {
        super(context);
    }

    public CustomLevelGridCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Check if a drop action can occur at, or near, the requested location.
     * This may be called repeatedly during a drag, so any calls should return
     * quickly.
     * 
     * @param source
     *            DragSource where the drag started
     * @param x
     *            X coordinate of the drop location
     * @param y
     *            Y coordinate of the drop location
     * @param xOffset
     *            Horizontal offset with the object being dragged where the
     *            original touch happened
     * @param yOffset
     *            Vertical offset with the object being dragged where the
     *            original touch happened
     * @param dragView
     *            The DragView that's being dragged around on screen.
     * @param dragInfo
     *            Data associated with the object being dragged
     * @return True if the drop will be accepted, false otherwise.
     */
    @Override
    public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo)
    {
        // An ImageCell accepts a drop if it is empty and if it is part of a
        // grid.
        // A free-standing ImageCell does not accept drops.
        // return mEmpty && (mCellNumber >= 0);
        // return (mCellNumber >= 0);
        return true;
    }

    /**
     * React to a dragged object entering the area of this DropSpot. Provide the
     * user with some visual feedback.
     */
    @Override
    public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        int bg = android.R.color.background_light;
        setBackgroundResource(bg);
    }

    /**
     * React to a drag
     */
    @Override
    public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        setBackgroundResource(0);
        // invalidate();
    }

    @Override
    public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
        if (onDropListener != null) {
            onDropListener.onDrop(this, source, x, y, xOffset, yOffset, dragView, dragInfo);
        }
    }

    @Override
    public void
            onDragOver(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
    }

    @Override
    public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView,
            Object dragInfo, Rect recycle) {
        return null;
    }

    @Override
    public boolean allowDrag() {
        return true;
    }

    @Override
    public void setDragController(DragController dragger) {
    }

    @Override
    public void onDropCompleted(View target, boolean success) {
    }

    public void setLevel(CustomLevel level) {
        this.level = level;
    }

    public CustomLevel getLevel() {
        return level;
    }

	@Override
	public void onDropCanceled(DragView view) {
		// do nothing		
	}

}
