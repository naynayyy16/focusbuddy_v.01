package com.focusbuddy.services.pomodoro;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.function.BiConsumer;
import com.focusbuddy.observers.timer.*;
import java.util.ArrayList;
import java.util.List;

public class PomodoroTimer {
    private static final int FOCUS_DURATION = 25 * 60; // 25 minutes in seconds
    private static final int SHORT_BREAK_DURATION = 5 * 60;  // 5 minutes in seconds
    private static final int LONG_BREAK_DURATION = 15 * 60;  // 15 minutes in seconds
    private static final int CYCLES_BEFORE_LONG_BREAK = 4;
    
    private Timeline timeline;
    private int currentSeconds;
    private int totalSeconds;
    private boolean isRunning;
    private boolean isFocusSession;
    private int cycleCount;

    private List<TimerObserver> observers = new ArrayList<>();
    
    private BiConsumer<Integer, Integer> onTimeUpdate;
    private Runnable onTimerComplete;
    
    public PomodoroTimer() {
        reset();
        setupTimeline();
    }
    
    private void setupTimeline() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (currentSeconds > 0) {
                currentSeconds--;
                updateDisplay();
            } else {
                complete();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void addObserver(TimerObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(TimerObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(String event) {
        String timerType = isFocusSession ? "FOCUS" : "BREAK";
        switch (event) {
            case "START" -> observers.forEach(o -> o.onTimerStart(timerType));
            case "PAUSE" -> observers.forEach(o -> o.onTimerPause(timerType));
            case "RESET" -> observers.forEach(o -> o.onTimerReset(timerType));
            case "COMPLETE" -> observers.forEach(o -> o.onTimerComplete(timerType, totalSeconds - currentSeconds));
        }
    }
    
    public void start() {
        if (!isRunning) {
            isRunning = true;
            timeline.play();
            notifyObservers("START");
        }
    }
    
    public void pause() {
        if (isRunning) {
            isRunning = false;
            timeline.pause();
            notifyObservers("PAUSE");
        }
    }
    
    public void reset() {
        if (timeline != null) {
            timeline.stop();
        }
        isRunning = false;
        isFocusSession = true;
        cycleCount = 0;
        currentSeconds = FOCUS_DURATION;
        totalSeconds = FOCUS_DURATION;
        updateDisplay();
        notifyObservers("RESET");
    }
    
    private void complete() {
        timeline.stop();
        isRunning = false;
        
        notifyObservers("COMPLETE");
        
        if (onTimerComplete != null) {
            onTimerComplete.run();
        }
        
        if (isFocusSession) {
            cycleCount++;
            if (cycleCount % CYCLES_BEFORE_LONG_BREAK == 0) {
                isFocusSession = false;
                currentSeconds = LONG_BREAK_DURATION;
            } else {
                isFocusSession = false;
                currentSeconds = SHORT_BREAK_DURATION;
            }
        } else {
            isFocusSession = true;
            currentSeconds = FOCUS_DURATION;
        }
        totalSeconds = currentSeconds;
        updateDisplay();
    }
    
    private void updateDisplay() {
        if (onTimeUpdate != null) {
            int minutes = currentSeconds / 60;
            int seconds = currentSeconds % 60;
            onTimeUpdate.accept(minutes, seconds);
        }
    }
    
    public double getProgress() {
        return 1.0 - ((double) currentSeconds / totalSeconds);
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public boolean isFocusSession() {
        return isFocusSession;
    }
    
    public void setOnTimeUpdate(BiConsumer<Integer, Integer> onTimeUpdate) {
        this.onTimeUpdate = onTimeUpdate;
    }
    
    public void setOnTimerComplete(Runnable onTimerComplete) {
        this.onTimerComplete = onTimerComplete;
    }
}
