package com.wuba.wpaxos.sample.latency;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.wuba.wpaxos.sample.echo.EchoClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

public class LatencyTestMain implements Runnable {
	private static Logger logger;
	private static final int[] total = { 3 };

	public static final String host = "127.0.0.1:";
	public static final int portStart = 30000;
	private static String nodeList = "";

	private long runtime = 1000;

	private static final Map<Integer, Process> processes = new HashMap<>();

	/**
	 * 
	 * @param args[0] Integer number of node in paxos
	 * @param args[1] Float percentage of node will send proposal
	 * @param args[2] Long time that the paxos will run before force terminate
	 * @param args[3] Boolean will the nodes drop itself (default 30% change to drop
	 *                itself, lifetime = rand(5000, 15000)
	 */
	public static void main(String[] args) throws IOException {
		if (logger == null) {
			String log4jConfig = "conf" + File.separator + "log4j.xml";
			System.out.println(log4jConfig);
			// ConfigurationSource src = new ConfigurationSource(new
			// FileInputStream(log4jConfig));
			Configurator.initialize(null, log4jConfig);
			// System.out.println(LatencyClient.class.getClassLoader().toString());

			logger = LogManager.getLogger(LatencyTestMain.class);
		}
		deleteDbFiles(new File("./LatencyDb"));
		moveDbFile(new File("./logs"));
		if (args.length != 4) {
			System.out.println(
					"wrong usage: [int-number of node] [float - around Persentage of Proposing Node] [long-runtime] [boolean-dropable]");
			return;
		}
		total[0] = Integer.parseInt(args[0]);
		float percentage = Float.parseFloat(args[1]);
		long runtime = Long.parseLong(args[2]);
		boolean droppable = Boolean.parseBoolean(args[3]);
		String testplan = total[0] + "-" + percentage + "-" + droppable;

		nodeList = createNodeList(total[0]);
		createFolder(testplan);
		createProcesses(nodeList, 0, percentage, droppable, testplan);

		new LatencyTestMain(runtime).run();
		System.out.println("end");
		return;
	}

	public synchronized static String createNodeList(int number) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < number; i += 1) {
			if (sb.length() != 0) {
				sb.append(",");
			}
			String addr = host + (portStart + i);
			sb.append(addr);
		}
		return sb.toString();
	}

	public synchronized static void createProcesses(String list, int currentCycle, float percentage, boolean droppable,
			String testplan) {
		String[] addrs = list.split(",");
		int count = 0;
		int total = addrs.length;
		Random rng = new Random();
		int counter = 0;
		boolean canRandom = true;
		for (String s : addrs) {
			boolean willDrop = false;
			boolean propose = true;

			float currentPercentage = (float) count / total;
			if (currentPercentage < percentage) {
				int remaining = (int) (Math.ceil(percentage * total) - count);
				if (canRandom && ((total - counter) > remaining)) {
					propose = rng.nextBoolean();
				} else {
					canRandom = false;
				}
			} else {
				propose = false;
			}
			if (propose) {
				count += 1;
			}
			counter += 1;

			if (droppable && rng.nextInt(10) < 4) {
				willDrop = true;
			}

			Process p = createProcess(s, currentCycle, propose, willDrop, testplan);
			processes.put(processes.size() + 1, p);
		}
		System.out.println(
				MessageFormat.format("number of Proposer : {0} ({1}%)", count, ((float) count / total) * 100f));
	}

	public synchronized static void cleanUp() {
		System.out.println("cleaning up");
		for (Entry<Integer, Process> set : processes.entrySet()) {
			if (set.getValue() != null) {
				set.getValue().destroyForcibly();
			}
		}
	}

	public synchronized static Process createProcess(String addr, int currentCycle, boolean isProposable,
			boolean droppable, String testplan) {
		try {
			String cmd = "java -cp target/wpaxos.sample-1.0.0.jar:target/dependency/* com.wuba.wpaxos.sample.latency.LatencyClient "
					+ addr + " " + nodeList + " " + isProposable + " " + droppable + " " + testplan;
			logger.info("Start Process: {}", cmd);
			return Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private synchronized static void deleteDbFiles(File folder) {
		if (folder != null) {
			File[] files = folder.listFiles();
			if (files != null) {
				for (File f : files) {
					deleteDbFiles(f);
					f.delete();
				}
			}
		}
	}

	private synchronized static void moveDbFile(File folder) {
		String logAddr = folder + "/appLatency.log";
		System.out.println("Move DB File at: " + folder.getAbsolutePath());
		if (folder != null) {
			try {
				int len = folder.listFiles().length;
				if (new File(logAddr).exists()) {
					Path source = Paths.get(logAddr);
					Files.move(source, source.resolveSibling("appLatency-" + len + ".log"));
				}
			} catch (IOException e) {
			}
		}
	}

	public static synchronized void createFolder(String testplan) {
		String folder = "./logs/latency/" + testplan;
		File directory = new File(folder);
		if (!directory.exists()) {
			directory.mkdir();
		}
	}

	public LatencyTestMain(long time) {
		this.runtime = time;
	}

	@Override
	public synchronized void run() {
		try {
			// wait 3s for node to settle
			this.wait(this.runtime + LatencyClient.settleTime + 5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// wait to end
		try {
			this.wait(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cleanUp();
	}
}
