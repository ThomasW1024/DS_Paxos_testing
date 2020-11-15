package com.wuba.wpaxos.sample.runtime;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class RuntimeTimer  extends Thread{

	public interface TimerDelegate {

		public void tick();
		public void timeUp();
	}

	private static boolean isTimerRunning;
	private final String timerId;
	private final boolean isDaemonTimer;
	private final long delayInSec;
	private final long durationInSec;
	private final TimerDelegate callback;

	public RuntimeTimer(String timerId, boolean isDaemonTimer, long delayInSec, long durationInSec, TimerDelegate callback) {
		this.timerId = timerId;
		this.isDaemonTimer = isDaemonTimer;
		this.delayInSec = delayInSec;
		this.durationInSec = durationInSec;
		this.callback = callback;
	}

	@Override
	public void run() {
		final Timer timer = new Timer(timerId, isDaemonTimer);
		AtomicInteger counter = new AtomicInteger(0);
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				assert callback != null;
				if (counter.incrementAndGet() >= durationInSec) {
					callback.timeUp();
					timer.cancel();
				} else {
					callback.tick();
				}
			}
		};
		timer.schedule(timerTask, delayInSec * 1000, 1000);
	}
}
