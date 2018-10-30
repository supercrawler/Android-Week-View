package com.alamkanak.weekview.drawing;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.style.StyleSpan;

import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.model.WeekViewEvent;
import com.alamkanak.weekview.ui.WeekView;

import java.util.Calendar;
import java.util.List;

public class EventsDrawer {

    private WeekViewConfig config;
    private WeekViewDrawingConfig drawingConfig;
    private EventChipRectCalculator rectCalculator;

    public EventsDrawer(WeekViewConfig config) {
        this.config = config;
        this.drawingConfig = config.drawingConfig;
        this.rectCalculator = new EventChipRectCalculator(config);
    }

    public void drawSingleEvents(List<EventChip> eventChips,
                                 DrawingContext drawingContext, Canvas canvas) {
        float startPixel = drawingContext.startPixel;

        // Draw single events
        for (Calendar day : drawingContext.dayRange) {
            if (config.isSingleDay()) {
                // Add a margin at the start if we're in day view. Otherwise, screen space is too
                // precious and we refrain from doing so.
                startPixel = startPixel + config.eventMarginHorizontal;
            }

            drawEventsForDate(eventChips, day, startPixel, canvas);

            // In the next iteration, start from the next day.
            startPixel += drawingConfig.widthPerDay + config.columnGap;
        }
    }

    private void drawEventsForDate(List<EventChip> eventChips, Calendar date,
                                   float startFromPixel, Canvas canvas) {
        if (eventChips == null) {
            return;
        }

        for (int i = 0; i < eventChips.size(); i++) {
            EventChip eventChip = eventChips.get(i);
            WeekViewEvent event = eventChip.event;
            if (!event.isSameDay(date)) {
                continue;
            }

            RectF chipRect = rectCalculator.calculateSingleEvent(eventChip, startFromPixel);
            if (isValidSingleEventRect(chipRect)) {
                eventChip.rect = chipRect;
                eventChip.draw(config, canvas);
            } else {
                eventChip.rect = null;
            }
        }
    }

    /**
     * Draw all the all-day events of a particular day.
     *
     * @param eventChips The list of {@link EventChip}s to draw
     * @param drawingContext The {@link DrawingContext} to use for drawing
     * @param canvas         The canvas to draw upon.
     */
    public void drawAllDayEvents(List<EventChip> eventChips,
                                 DrawingContext drawingContext, Canvas canvas) {
        if (eventChips == null) {
            return;
        }

        float startPixel = drawingContext.startPixel;

        for (Calendar day : drawingContext.dayRange) {
            if (config.isSingleDay()) {
                startPixel = startPixel + config.eventMarginHorizontal;
            }

            for (EventChip eventChip : eventChips) {
                WeekViewEvent event = eventChip.event;
                if (!event.isSameDay(day)) {
                    continue;
                }

                drawAllDayEvent(eventChip, startPixel, canvas);
            }

            startPixel += drawingConfig.widthPerDay + config.columnGap;
        }
    }

    private void drawAllDayEvent(EventChip eventChip, float startFromPixel, Canvas canvas) {
        RectF chipRect = rectCalculator.calculateAllDayEvent(eventChip, startFromPixel);
        if (isValidAllDayEventRect(chipRect)) {
            eventChip.rect = chipRect;
            int lineHeight = calculateTextHeight(eventChip);
            int chipHeight = lineHeight + (config.eventPadding * 2);

            eventChip.rect = new RectF(chipRect.left, chipRect.top, chipRect.right, chipRect.top + chipHeight);
            eventChip.draw(config, canvas);
        } else {
            eventChip.rect = null;
        }
    }

    private boolean isValidSingleEventRect(RectF rect) {
        float totalHeaderHeight = drawingConfig.headerHeight
                + config.headerRowPadding * 2
                + drawingConfig.headerMarginBottom;

        return rect.left < rect.right
                && rect.left < WeekView.getViewWidth()
                && rect.top < WeekView.getViewHeight()
                && rect.right > drawingConfig.headerColumnWidth
                && rect.bottom > totalHeaderHeight + drawingConfig.timeTextHeight / 2;
    }

    private boolean isValidAllDayEventRect(RectF rect) {
        return rect.left < rect.right
                && rect.left < WeekView.getViewWidth()
                && rect.top < WeekView.getViewHeight()
                && rect.right > drawingConfig.headerColumnWidth
                && rect.bottom > 0;
    }

    private int calculateTextHeight(EventChip eventChip) {
        WeekViewEvent event = eventChip.event;
        float left = eventChip.rect.left;
        float top = eventChip.rect.top;
        float right = eventChip.rect.right;
        float bottom = eventChip.rect.bottom;

        boolean negativeWidth = (right - left - config.eventPadding * 2) < 0;
        boolean negativeHeight = (bottom - top - config.eventPadding * 2) < 0;
        if (negativeWidth || negativeHeight) {
            return 0;
        }

        // Prepare the name of the event.
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        if (event.getTitle() != null) {
            stringBuilder.append(event.getTitle());
            stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, stringBuilder.length(), 0);
        }

        // Prepare the location of the event.
        if (event.getLocation() != null) {
            stringBuilder.append(' ');
            stringBuilder.append(event.getLocation());
        }

        int availableHeight = (int) (bottom - top - config.eventPadding * 2);
        int availableWidth = (int) (right - left - config.eventPadding * 2);

        // TODO: Code quality
        // Get text dimensions.
        StaticLayout textLayout = new StaticLayout(stringBuilder, drawingConfig.eventTextPaint,
                availableWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        int lineHeight = textLayout.getHeight() / textLayout.getLineCount();

        if (availableHeight >= lineHeight) {
            // Calculate available number of line counts.
            int availableLineCount = availableHeight / lineHeight;
            do {
                // TODO: Code quality
                // TODO: Don't truncate
                // Ellipsize text to fit into event rect.
                int availableArea = availableLineCount * availableWidth;
                CharSequence ellipsized = TextUtils.ellipsize(stringBuilder, drawingConfig.eventTextPaint, availableArea, TextUtils.TruncateAt.END);
                textLayout = new StaticLayout(ellipsized, drawingConfig.eventTextPaint, (int) (right - left - config.eventPadding * 2), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

                // Reduce line count.
                availableLineCount--;

                // Repeat until text is short enough.
            } while (textLayout.getHeight() > availableHeight);
        }

        return lineHeight;
    }

}
