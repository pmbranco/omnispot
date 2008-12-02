/*
 * Disclaimer:
 * Copyright 2008 - Ke.S.Di.P. E.P.E - All rights reserved.
 * eof Disclaimer
 */
package com.kesdip.player;

import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Helper thread that monitors deployments. Whenever the timing is such that
 * a new deployment has to be deployed, this thread, will signal this to the
 * player thread in order for the transition to take place.
 * 
 * @author Pafsanias Ftakas
 */
public class TimingMonitor implements Runnable {
	private static final Logger logger = Logger.getLogger(TimingMonitor.class);
	
	/**
	 * Helper class. Implementation of the quartz Job interface to represent
	 * jobs that will schedule a particular deployment layout.
	 * 
	 * @author Pafsanias Ftakas
	 */
	private class LayoutJob implements Job {
		private DeploymentLayout layout;
		
		public LayoutJob(DeploymentLayout layout) {
			this.layout = layout;
		}

		@Override
		public void execute(JobExecutionContext ctx)
				throws JobExecutionException {
			logger.info("Scheduler starting layout: " + layout.getName());
			startLayout(layout);
		}
		
	}
	
	private Player player;
	private Scheduler scheduler;

	/**
	 * Initializing constructor.
	 * 
	 * @param player The player associated with this timing monitor.
	 * @throws SchedulerException Iff something goes wrong starting the
	 * scheduler.
	 */
	public TimingMonitor(Player player) throws SchedulerException {
		this.player = player;
		this.scheduler = StdSchedulerFactory.getDefaultScheduler();
		this.scheduler.start();
	}
	
	/* TRANSIENT STATE */
	@SuppressWarnings("unused")
	private Date deploymentStart;
	private Date layoutStart;
	private DeploymentLayout currentLayout;
	
	/**
	 * Callback. Called by the player when a layout is starting to keep the
	 * transient state of the timing monitor up to date.
	 * @param layout the layout that just started playing
	 */
	public synchronized void startingLayout(DeploymentLayout layout) {
		this.currentLayout = layout;
		this.layoutStart = new Date();
	}
	
	/**
	 * Accessor for the currently playing layout.
	 * @return The current deployment layout.
	 */
	private synchronized DeploymentLayout getCurrentLayout() {
		return currentLayout;
	}
	
	/**
	 * Helper method to remove all scheduled jobs from the quartz scheduler.
	 * @throws SchedulerException iff something goes wrong.
	 */
	private void removeAllJobs() throws SchedulerException {
		String[] groupNames = scheduler.getJobGroupNames();
		for (String groupName : groupNames) {
			String[] jobNames = scheduler.getJobNames(groupName);
			for (String jobName : jobNames) {
				scheduler.deleteJob(jobName, groupName);
			}
		}
	}
	
	/**
	 * Helper method to start "playing" a deployment.
	 * @param contextPath The deployment descriptor path.
	 * @throws ParseException Iff some parsing error occurs.
	 * @throws SchedulerException Iff some problem occurs while scheduling
	 * jobs for the layouts that have a cron expression associated with them.
	 */
	private void startDeployment(String contextPath)
			throws ParseException, SchedulerException {
		ApplicationContext ctx = new ClassPathXmlApplicationContext(
				contextPath);
		DeploymentSettings settings =
			(DeploymentSettings) ctx.getBean("deploymentSettings");
		DeploymentContents contents =
			(DeploymentContents) ctx.getBean("deploymentContents");
		if (settings == null)
			throw new BeanInitializationException("The application context " +
					"factory should contain a bean with ID 'deploymentSettings'.");
		if (contents == null)
			throw new BeanInitializationException("The application context " +
					"factory should contain a bean with ID 'deploymentContents'.");
		
		removeAllJobs();
		for (DeploymentLayout layout : contents.getLayouts()) {
			if (layout.getCronExpression() == null)
				continue;
			
			Trigger trigger = new CronTrigger(layout.getName() + "_trigger",
					"layout", layout.getCronExpression());
			JobDetail jobDetail = new JobDetail(layout.getName() + "_job",
					"layout", LayoutJob.class);
			scheduler.scheduleJob(jobDetail, trigger);
		}

		player.startDeployment(ctx, settings, contents);
		deploymentStart = new Date();
	}
	
	/**
	 * Helper method to instruct the player to start a particular layout.
	 * @param layout The deployment layout to start "playing".
	 */
	private void startLayout(DeploymentLayout layout) {
		player.startLayout(layout);
	}
	
	/**
	 * Helper method to check if another deployment is available, when to
	 * schedule it, and when appropriate instructs the player to switch to
	 * the new deployment.
	 */
	private void monitorDeployments() {
		// TODO Implement fetching new deployments through the database.
	}
	
	/**
	 * Helper method to check if a particular layout has expired. This
	 * method is only interested in deployment layouts that have a duration
	 * specified. If the current layout has a duration specified and has been
	 * "playing" for more than the duration time, then this method instruct
	 * the player thread to stop the current layout.
	 */
	private void checkLayoutExpiry() {
		DeploymentLayout currentLayout = getCurrentLayout();
		if (currentLayout == null)
			return;
		
		// The layout did not specify a duration. Skip test.
		if (currentLayout.getDuration() == 0)
			return;
		
		Date now = new Date();
		Date expiry = new Date(layoutStart.getTime() + currentLayout.getDuration() * 1000L);
		if (now.after(expiry)) {
			// The layout duration has expired.
			player.completeLayout();
		}
	}

	@Override
	public void run() {
		try {
			// start default deployment
			startDeployment("appContext.xml");
			
			while (true) {
				monitorDeployments();
				
				checkLayoutExpiry();
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) { }
			}
		} catch (Throwable t) {
			logger.error("Error during the monitor thread main loop", t);
		}
	}

}