package com.alamkanak.weekview.drawing;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;

import com.alamkanak.weekview.utils.DateTimeInterpreter;
import com.alamkanak.weekview.model.WeekViewConfig;
import com.alamkanak.weekview.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class WeekViewDrawingConfig {
    
    public Paint timeTextPaint;
    public float timeTextWidth;
    public float timeTextHeight;

    public Paint headerTextPaint;
    public float headerTextHeight;
    public float headerHeight;

    public PointF currentOrigin = new PointF(0f, 0f);
    public Paint headerBackgroundPaint;
    public float widthPerDay;
    public Paint dayBackgroundPaint;
    public Paint hourSeparatorPaint;
    public float headerMarginBottom;

    public Paint todayBackgroundPaint;
    public Paint futureBackgroundPaint;
    public Paint pastBackgroundPaint;
    public Paint futureWeekendBackgroundPaint;
    public Paint pastWeekendBackgroundPaint;

    public Paint nowLinePaint;
    public Paint todayHeaderTextPaint;
    public Paint eventBackgroundPaint;
    public float headerColumnWidth;
    public TextPaint eventTextPaint;
    public Paint headerColumnBackgroundPaint;
    public int defaultEventColor;

    public int newHourHeight = -1;

    public DateTimeInterpreter dateTimeInterpreter;
    
    public WeekViewDrawingConfig(Context context, WeekViewConfig config) {
        // Measure settings for time column.
        timeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timeTextPaint.setTextAlign(Paint.Align.RIGHT);
        timeTextPaint.setTextSize(config.textSize);
        timeTextPaint.setColor(config.headerColumnTextColor);

        Rect rect = new Rect();
        timeTextPaint.getTextBounds("00 PM", 0, "00 PM".length(), rect);
        timeTextHeight = rect.height();
        headerMarginBottom = timeTextHeight / 2;
        initTextTimeWidth(context);

        // Measure settings for header row.
        headerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerTextPaint.setColor(config.headerColumnTextColor);
        headerTextPaint.setTextAlign(Paint.Align.CENTER);
        headerTextPaint.setTextSize(config.textSize);
        headerTextPaint.getTextBounds("00 PM", 0, "00 PM".length(), rect);
        headerTextHeight = rect.height();
        headerTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        // Prepare header background paint.
        headerBackgroundPaint = new Paint();
        headerBackgroundPaint.setColor(config.headerRowBackgroundColor);

        // Prepare day background color paint.
        dayBackgroundPaint = new Paint();
        dayBackgroundPaint.setColor(config.dayBackgroundColor);
        futureBackgroundPaint = new Paint();
        futureBackgroundPaint.setColor(config.futureBackgroundColor);
        pastBackgroundPaint = new Paint();
        pastBackgroundPaint.setColor(config.pastBackgroundColor);
        futureWeekendBackgroundPaint = new Paint();
        futureWeekendBackgroundPaint.setColor(config.futureWeekendBackgroundColor);
        pastWeekendBackgroundPaint = new Paint();
        pastWeekendBackgroundPaint.setColor(config.pastWeekendBackgroundColor);

        // Prepare hour separator color paint.
        hourSeparatorPaint = new Paint();
        hourSeparatorPaint.setStyle(Paint.Style.STROKE);
        hourSeparatorPaint.setStrokeWidth(config.hourSeparatorStrokeWidth);
        hourSeparatorPaint.setColor(config.hourSeparatorColor);

        // Prepare the "now" line color paint
        nowLinePaint = new Paint();
        nowLinePaint.setStrokeWidth(config.nowLineThickness);
        nowLinePaint.setColor(config.nowLineColor);

        // Prepare today background color paint.
        todayBackgroundPaint = new Paint();
        todayBackgroundPaint.setColor(config.todayBackgroundColor);

        // Prepare today header text color paint.
        todayHeaderTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        todayHeaderTextPaint.setTextAlign(Paint.Align.CENTER);
        todayHeaderTextPaint.setTextSize(config.textSize);
        todayHeaderTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        todayHeaderTextPaint.setColor(config.todayHeaderTextColor);

        // Prepare event background color.
        eventBackgroundPaint = new Paint();
        eventBackgroundPaint.setColor(Color.rgb(174, 208, 238));

        // Prepare header column background color.
        headerColumnBackgroundPaint = new Paint();
        headerColumnBackgroundPaint.setColor(config.headerColumnBackgroundColor);

        // Prepare event text size and color.
        eventTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        eventTextPaint.setStyle(Paint.Style.FILL);
        eventTextPaint.setColor(config.eventTextColor);
        eventTextPaint.setTextSize(config.eventTextSize);

        // Set default event color.
        defaultEventColor = Color.parseColor("#9fc6e7");
    }

    public void resetOrigin() {
        currentOrigin = new PointF(0, 0);
    }

    /**
     * Initialize time column width. Calculate value with all possible hours (supposed widest text).
     */
    public void initTextTimeWidth(Context context) {
        DateTimeInterpreter interpreter = getDateTimeInterpreter(context);
        timeTextWidth = 0;
        for (int i = 0; i < 24; i++) {
            String time = interpreter.interpretTime(i);
            if (time == null) {
                throw new IllegalStateException("A DateTimeInterpreter must not return null time");
            }
            timeTextWidth = Math.max(timeTextWidth, timeTextPaint.measureText(time));
        }
    }

    public DateTimeInterpreter getDateTimeInterpreter(Context context) {
        if (dateTimeInterpreter == null) {
            dateTimeInterpreter = buildDateTimeInterpreter(context);
        }

        return dateTimeInterpreter;
    }

    private DateTimeInterpreter buildDateTimeInterpreter(final Context context) {
        return new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                try {
                    SimpleDateFormat sdf = DateUtils.getDateFormat();
                    return sdf.format(date.getTime()).toUpperCase();
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            }

            @Override
            public String interpretTime(int hour) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, 0);

                try {
                    SimpleDateFormat sdf = DateUtils.getTimeFormat(context);
                    return sdf.format(calendar.getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            }
        };
    }
    
}
