package mc.yqt.fastplace.util;

import java.util.function.BooleanSupplier;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import mc.yqt.fastplace.FastPlace;

public class Run {

	private int delay = 1;
	private int period = 1;
	private FastPlace main;
	private Runnable run;
	private BooleanSupplier cancelable;
	
	private int cycles = 1;
	private int cycled = 0;
	
	public Run(FastPlace main, int delay, int period, int cycles) {
		this.main = main;
		this.delay = delay;
		this.period = period;
		this.cycled = cycles;
	}
	
	/**
	 * Default constructor.
	 * @param main plugin class
	 */
	public Run(FastPlace main) {
		this.main = main;
	}
	
	public static Run make(FastPlace main) {
		return new Run(main);
	}
	
	/**
	 * Specifies first-run delay in ticks
	 * @param ticks
	 * @return Builder
	 */
	public Run delay(int ticks) {
		if(ticks < 1)
			ticks = 1;
		this.delay = ticks;
		return this;
	}
	
	/**
	 * Specifies interval delay in ticks
	 * @param ticks
	 * @return Builder
	 */
	public Run interval(int ticks) {
		if(ticks < 1)
			ticks = 1;
		this.period = ticks;
		return this;
	}
	
	/**
	 * Specifies the amount of times the runnable should run
	 * @param cycles
	 * @return Builder
	 */
	public Run limit(int cycles) {
		this.cycles = cycles;
		return this;
	}
	
	/**
	 * Specifies that the runnable will run until canceled or server shut down
	 * @return Builder
	 */
	public Run unlim() {
		this.cycles = 0;
		return this;
	}
	
	/**
	 * Starts the built runnable with the given runnable fucntion
	 * @param run
	 * @return The generated task
	 */
	public BukkitTask run(Runnable run) {
		this.run = run;
		
		// make unlimited timer
		if(cycles < 1)
			return buildUnlimTimer();
		
		// make cycle limited timer
		else if(cycles > 1)
			return buildLimitedTimer();
		
		// make delayed task
		else
			return buildDelayedTask();
	}
	
	/**
	 * Starts the given boolean supplier in the runnable.
	 * If the given supplier returns false, the task will stop repeating no matter how many cycles it has left.
	 * @param run
	 * @return The generated task
	 */
	public BukkitTask cancelable(BooleanSupplier run) {
		this.cancelable = run;
		
		// make unlimited timer
		if(cycles < 1)
			return buildUnlimCancelableTimer();
		
		// make cycle limited timer
		else if(cycles > 1)
			return buildLimCancelableTimer();
		
		// make delayed task
		else
			return buildDelayedCancelableTask();
	}
	
	private BukkitTask buildLimitedTimer() {
		return new BukkitRunnable() {
			@Override
			public void run() {
				if(cycles <= cycled) {
					cancel();
					return;
				}
				
				cycled++;
				
				run.run();
			}
		}.runTaskTimer(main, delay, period);
	}
	
	private BukkitTask buildUnlimTimer() {
		return new BukkitRunnable() {
			@Override
			public void run() {
				run.run();
			}
		}.runTaskTimer(main, delay, period);
	}
	
	private BukkitTask buildDelayedTask() {
		return new BukkitRunnable() {
			@Override
			public void run() {
				run.run();
			}
		}.runTaskLater(main, delay);
	}
	
	private BukkitTask buildLimCancelableTimer() {
		return new BukkitRunnable() {
			@Override
			public void run() {
				if(cycles <= cycled) {
					cancel();
					return;
				}
				
				cycled++;
				
				if(!cancelable.getAsBoolean())
					cancel();
			}
		}.runTaskTimer(main, delay, period);
	}
	
	private BukkitTask buildUnlimCancelableTimer() {
		return new BukkitRunnable() {
			@Override
			public void run() {
				if(!cancelable.getAsBoolean())
					cancel();
			}
		}.runTaskTimer(main, delay, period);
	}
	
	private BukkitTask buildDelayedCancelableTask() {
		// this doesn't really make sense since it will never repeat but I like consistency
		return new BukkitRunnable() {
			@Override
			public void run() {
				if(!cancelable.getAsBoolean())
					cancel();
			}
		}.runTaskLater(main, delay);
	}
}
