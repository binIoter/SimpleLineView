package com.robog.library.painter;

import android.util.Log;

import com.robog.library.Action;
import com.robog.library.PixelPoint;
import com.robog.library.PixelShape;
import com.robog.library.Util;

/**
 * @Author: yuxingdong
 * @Time: 2018/2/10
 */

public class SegProgressPainter extends SegmentPainter {

    private float[] mPercent;

    public SegProgressPainter() {
        this(new float[]{0, 1.0f}, null, 0, false);
    }

    public SegProgressPainter(Painter painter) {
        this(painter, new float[]{0, 1.0f});
    }

    public SegProgressPainter(Painter painter, float[] percent) {
        this(percent, painter.getShape(), painter.duration(), painter.close());
    }

    public SegProgressPainter(float[] percent, PixelShape pixelShape, int duration, boolean close) {

        super(pixelShape, duration, close);

        if (percent == null || percent.length != 2) {
            throw new IllegalArgumentException("the length of percent must be 2!");
        }
        mPercent = percent;
    }

    public Painter  setPercent(float[] mPercent) {
        this.mPercent = mPercent;
        return this;
    }

    @Override
    public void performDraw(Action action) {

        float progress = (float) action.getProgress() / 100;
        float startPercent = mPercent[0];
        float endPercent = mPercent[1];
        float offsetPercent = endPercent - startPercent;

        Util.resetPoint(pointList);

        if (offsetPercent < 0) {
            throw new IllegalArgumentException("percent[1] must be greater than percent[0]!");
        }

        if (progress < startPercent) {
            return;
        }
        if (progress > endPercent) {
            progress = endPercent;
        }

        float realProgress = (progress - startPercent) / offsetPercent;

        float distance = Util.calDistance(pointList, close());
        float allFraction = 0;

        for (int i = 0; i < pointList.size(); i++) {

            PixelPoint current = pointList.get(i);
            PixelPoint next;
            if (i < pointList.size() - 1) {
                next = pointList.get(i + 1);
            } else {
                next = pointList.get(0);
            }

            // 当前点与下一点连线占总长度的百分比
            float fraction = Util.calFraction(distance, current, next);
            allFraction += fraction;

            if (realProgress >= allFraction) {
                current.setPathFinish(true);

                // 更新最后一次，保证图像都绘制
                if (i == pointList.size() - 1) {
                    action.update(this);
                }
            } else {
                float startX = current.getStartX();
                float startY = current.getStartY();

                float endX = next.getStartX();
                float endY = next.getStartY();

                float deltaX = endX - startX;
                float deltaY = endY - startY;

                float dis = distance * (fraction - allFraction + realProgress);
                float degree = (float) Math.atan2(deltaY, deltaX);

                float moveX = (float) (startX + dis * Math.cos(degree));
                float moveY = (float) (startY + dis * Math.sin(degree));

                current.setEndX(moveX);
                current.setEndY(moveY);

                action.update(this);
                break;
            }

        }
    }

}