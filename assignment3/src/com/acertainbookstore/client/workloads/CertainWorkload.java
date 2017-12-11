/**
 * 
 */
package com.acertainbookstore.client.workloads;

import java.awt.Font;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * 
 * CertainWorkload class runs the workloads by different workers concurrently.
 * It configures the environment for the workers using WorkloadConfiguration
 * objects and reports the metrics
 * 
 */
public class CertainWorkload {

	/**
	 * @param args
	 */
	private static int INITIAL_ISBN = 3044561;
	private static int NUM_COPIES = 10;
	private static XYSeriesCollection dataSetThroughPut = new XYSeriesCollection();
	private static XYSeriesCollection dataSetLantency = new XYSeriesCollection();
	private static JFreeChart chart;

	
	public static void main(String[] args) throws Exception {
		int numConcurrentWorkloadThreads = 10;
		String serverAddress = "http://localhost:8081";
		boolean localTest = true;

		List<List<Future<WorkerRunResult>>> allRunResult = new ArrayList<List<Future<WorkerRunResult>>>();
		List<List<WorkerRunResult>> totalWorkersRunResults = new ArrayList<List<WorkerRunResult>>();
		List<List<WorkerRunResult>> totalWorkersRunResultsRPC = new ArrayList<List<WorkerRunResult>>();
		
		// Initialize the RPC interfaces if its not a localTest, the variable is
		// overriden if the property is set
		String localTestProperty = System
				.getProperty(BookStoreConstants.PROPERTY_KEY_LOCAL_TEST);
		localTest = (localTestProperty != null) ? Boolean
				.parseBoolean(localTestProperty) : localTest;

		BookStore bookStore = null, bookStoreRPC = null;
		StockManager stockManager = null, stockManagerRPC = null;
		
		CertainBookStore store = new CertainBookStore();
		bookStore = store;
		stockManager = store;
		
		stockManagerRPC = new StockManagerHTTPProxy(serverAddress + "/stock");
		bookStoreRPC = new BookStoreHTTPProxy(serverAddress);
		
		stockManager.removeAllBooks();
		stockManagerRPC.removeAllBooks();

		// Generate data in the bookstore before running the workload
		initializeBookStoreData(bookStore, stockManager);
		initializeBookStoreData(bookStoreRPC, stockManagerRPC);

		ExecutorService exec = Executors
				.newFixedThreadPool(numConcurrentWorkloadThreads);

		for (int i = 0; i < numConcurrentWorkloadThreads; i++) {
//			stockManager.removeAllBooks();
//			initializeBookStoreData(bookStore, stockManager);
			List<Future<WorkerRunResult>> runResults = new ArrayList<Future<WorkerRunResult>>();
			List<WorkerRunResult> workerRunResults = new ArrayList<WorkerRunResult>();
			
			List<Future<WorkerRunResult>> runResultsRPC = new ArrayList<Future<WorkerRunResult>>();
			List<WorkerRunResult> workerRunResultsRPC = new ArrayList<WorkerRunResult>();
			
			for (int j = 0; j <= i; j++){
				WorkloadConfiguration config = new WorkloadConfiguration(bookStore,
						stockManager);
				Worker workerTask = new Worker(config);
				
				WorkloadConfiguration configRPC = new WorkloadConfiguration(bookStoreRPC,
						stockManagerRPC);
				Worker workerTaskRPC = new Worker(configRPC);
				
				// Keep the futures to wait for the result from the thread
				runResults.add(exec.submit(workerTask));
				runResultsRPC.add(exec.submit(workerTaskRPC));
			}
			System.out.println("size"+runResults.size()+"\n");
	     	// Get the results from the threads using the futures returned
			
			for (Future<WorkerRunResult> futureRunResult : runResults) {
				WorkerRunResult runResult = futureRunResult.get(); // blocking call
				workerRunResults.add(runResult);
				System.out.println("success"+runResult.getSuccessfulInteractions()+"\n");
			}
			
			for (Future<WorkerRunResult> futureRunResultRPC : runResultsRPC) {
				WorkerRunResult runResultRPC = futureRunResultRPC.get(); // blocking call
				workerRunResultsRPC.add(runResultRPC);
				System.out.println("success"+runResultRPC.getSuccessfulInteractions()+"\n");
			}
			
			totalWorkersRunResults.add(workerRunResults);
			totalWorkersRunResultsRPC.add(workerRunResultsRPC);
			stockManager.removeAllBooks();
			stockManagerRPC.removeAllBooks();
		}
			
			exec.shutdownNow(); // shutdown the executor

		// Finished initialization, stop the clients if not localTest
			((BookStoreHTTPProxy) bookStoreRPC).stop();
			((StockManagerHTTPProxy) stockManagerRPC).stop();
	        reportMetric(totalWorkersRunResults,totalWorkersRunResultsRPC);
	}

	/**
	 * Computes the metrics and prints them
	 * 
	 * @param workerRunResults
	 */
	public static void reportMetric(List<List<WorkerRunResult>> totalWorkersRunResults,List<List<WorkerRunResult>> totalWorkersRunResultsRPC) {
		// TODO: You should aggregate metrics and output them for plotting here

		double averageTime = 0;
		long totalRunTime = 0;
		float aggThroughPut = 0;
		float interactions,runTimes;

		XYSeries series1 = new XYSeries("ThroughPut");
		XYSeries series2 = new XYSeries("Latency");
		
		XYSeries seriesRPC1 = new XYSeries("RPC ThroughPut");
		XYSeries seriesRPC2 = new XYSeries("RPC Latency");
		
		for (List<WorkerRunResult> workerRunResults : totalWorkersRunResults){
			for (WorkerRunResult runResult : workerRunResults){

				interactions = runResult.getSuccessfulInteractions();
				runTimes = runResult.getElapsedTimeInNanoSecs();
				aggThroughPut +=  (float)interactions/runTimes;
				totalRunTime += runResult.getElapsedTimeInNanoSecs();
			}
			averageTime = totalRunTime/workerRunResults.size();
			
			series1.add(workerRunResults.size(),Math.sqrt(Math.sqrt(aggThroughPut)));
			series2.add(workerRunResults.size(),Math.log(averageTime));

		}
		
		double averageTimeRPC = 0;
		long totalRunTimeRPC = 0;
		float aggThroughPutRPC = 0;
		float interactionsRPC,runTimesRPC;
		
		for (List<WorkerRunResult> workerRunResultsRPC : totalWorkersRunResultsRPC){
			for (WorkerRunResult runResultRPC : workerRunResultsRPC){

				interactionsRPC = runResultRPC.getSuccessfulInteractions();
				runTimesRPC = runResultRPC.getElapsedTimeInNanoSecs();
				aggThroughPutRPC +=  (float)interactionsRPC/runTimesRPC;
				totalRunTimeRPC += runResultRPC.getElapsedTimeInNanoSecs();
			}
			averageTimeRPC = totalRunTimeRPC/workerRunResultsRPC.size();
			
			seriesRPC1.add(workerRunResultsRPC.size(),Math.sqrt(Math.sqrt(aggThroughPutRPC)));
			seriesRPC2.add(workerRunResultsRPC.size(),Math.log(averageTimeRPC));

		}
		
		dataSetThroughPut.addSeries(series1);
		dataSetThroughPut.addSeries(seriesRPC1);
		dataSetLantency.addSeries(series2);
		dataSetLantency.addSeries(seriesRPC2);

		ChartFrame frameThroughPut = new ChartFrame("Perforamance", createLineChart(dataSetThroughPut,"Agg ThroughPut"));
		ChartFrame frameLatency = new ChartFrame("Perforamance", createLineChart(dataSetLantency,"Latency"));
		frameThroughPut.pack();
		frameThroughPut.setVisible(true);
		frameLatency.pack();
		frameLatency.setVisible(true);
	}
	
	protected static JFreeChart createLineChart(XYSeriesCollection dataset, String str){
		   
		 // Right y axis for latency
			chart = ChartFactory.createXYLineChart("Plot for Performance", "clients",
					str, dataset, PlotOrientation.VERTICAL, true, true,
					false);
			
			XYPlot plot;
			plot = chart.getXYPlot();
			plot.setDomainPannable(true);
			plot.setRangePannable(true);
			
			chart.getLegend().setItemFont(new Font("Courier New", 12, 12));
			
			return chart;		
	 } 
		
	/**
	 * Generate the data in bookstore before the workload interactions are run
	 * 
	 * Ignores the serverAddress if its a localTest
	 * 
	 */
	public static void initializeBookStoreData(BookStore bookStore,
			StockManager stockManager) throws BookStoreException {

		// TODO: You should initialize data for your bookstore here
		StockBook initial_book = new ImmutableStockBook(INITIAL_ISBN, "Harry Potter and JUnit", "JK Unit", (float) 10, NUM_COPIES, 0, 0, 0,
				false);
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(initial_book);
		stockManager.addBooks(booksToAdd);
	}
	
}
