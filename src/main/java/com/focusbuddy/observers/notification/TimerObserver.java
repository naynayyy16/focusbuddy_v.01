package com.focusbuddy.observers.notification;

public interface TimerObserver {
    void onTimerComplete(String timerType, int duration);
    void onTimerStart(String timerType);
    void onTimerPause(String timerType);
    void onTimerReset(String timerType);
}
