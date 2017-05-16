package benchmark;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

import com.mongodb.Function;

import ch.ethz.globis.mtfobu.domains.Person;
import ch.ethz.globis.mtfobu.domains.Publication;
import ch.ethz.globis.mtfobu.odb_project.db.Database;

public class Benchmark {
	String outputFileNameSuffix = "benchmark";
	int iterations = 20;
	final Database db;

	public Benchmark(Database db) {
		this.db = db;
	}

	public void benchmark() throws Exception {
		final String filename = "benchmark " + db.getDBTechnology() + " " + LocalDateTime.now() + ".csv";
		BufferedWriter bWriter;
		try {
			bWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename)));
		} catch (FileNotFoundException e) {
			System.err.println("Benchmark failture");
			e.printStackTrace();
			return;
		}
		List<String> results;
		double timings[] = new double[14];
		List<Publication> pubs = db.getPublications();
		List<Person> pers = db.getPeople();
		int random;

		bWriter.write(String.format(
				"Timings for Database: %s\nNumber of iterations per run: %d\nInputs where choosen at random\n",
				db.getDBTechnology(), iterations));
		bWriter.write("Task;average time in milliseconds\n");

		// task 1 inputs
		String ids[] = new String[iterations];
		// task 2+3 inputs
		String titles[] = new String[iterations];
		int begin_offset[] = new int[iterations];
		int end_offset[] = new int[iterations];
		int offset = pubs.size();
		// task 4 inputs
		String names[] = new String[iterations];
		// task 5 inputs
		String authorsA[] = new String[iterations];
		String authorsB[] = new String[iterations];
		for (int i = 0; i < iterations; ++i) {
			random = ThreadLocalRandom.current().nextInt(0, pubs.size() - 1);
			ids[i] = pubs.get(random).getId();
			random = ThreadLocalRandom.current().nextInt(0, pubs.size() - 1);
			titles[i] = pubs.get(random).getTitle();
			random = ThreadLocalRandom.current().nextInt(0, pubs.size() - 1);
			begin_offset[i] = random;
			random = ThreadLocalRandom.current().nextInt(random, pubs.size() - 1);
			end_offset[i] = random;
			random = ThreadLocalRandom.current().nextInt(0, pers.size() - 1);
			names[i] = pers.get(random).getName();
			//TODO: this is not optimal for task 5
			random = ThreadLocalRandom.current().nextInt(0, pers.size() - 1);
			authorsA[i] = pers.get(random).getId();
			random = ThreadLocalRandom.current().nextInt(0, pers.size() - 1);
			authorsB[i] = pers.get(random).getId();
		}
		timings[0] = benchmarkTask1(ids);
		timings[1] = benchmarkTask2(titles, offset);
		timings[2] = benchmarkTask3(titles, begin_offset, end_offset);
		timings[3] = benchmarkTask4(names);
		timings[4] = benchmarkTask5(authorsA, authorsB);
		timings[5] = benchmarkTask6();
		writeBenchmarkTimings(bWriter, timings);
		bWriter.close();
	}

	private void writeBenchmarkTimings(BufferedWriter bw, double[] timings) throws IOException {
		for (int task = 1; task <= 14; ++task) {
			bw.write(String.format("%d;%.8f\n",task ,timings[task-1]));
		}
	}

	private double benchmarkTask1(String[] ids) throws Exception {
		assert ids.length == iterations;
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getPublicationById(ids[i]);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime));// / ((double) iterations);
		return elapsedTime;
	}
	private double benchmarkTask2(String[] titles, int offset) throws Exception {
		
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getPublicationsByFilter(titles[i], 0, offset);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}
	private double benchmarkTask3(String[] titles, int[] begin_offset, int[] end_offset) throws Exception {
		
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getPublicationsByFilter(titles[i], begin_offset[i], end_offset[i]);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}
	private double benchmarkTask4(String[] names) throws Exception {
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getCoAuthors(names[i]);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}
	private double benchmarkTask5(String[] authorIdA, String[] authorIdB) throws Exception {
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.authorDistance(authorIdA[i], authorIdB[i]);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}
	private double benchmarkTask6() throws Exception {
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getAvgAuthorsInProceedings();
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}
	private double benchmarkTask7(String[] ids) throws Exception {
		assert ids.length == iterations;
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getPublicationById(ids[i]);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}
	private double benchmarkTask8(String[] ids) throws Exception {
		assert ids.length == iterations;
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getPublicationById(ids[i]);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}
	private double benchmarkTask9(String[] ids) throws Exception {
		assert ids.length == iterations;
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getPublicationById(ids[i]);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}
	private double benchmarkTask10(String[] ids) throws Exception {
		assert ids.length == iterations;
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getPublicationById(ids[i]);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}
	private double benchmarkTask11(String[] ids) throws Exception {
		assert ids.length == iterations;
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getPublicationById(ids[i]);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}
	private double benchmarkTask12(String[] ids) throws Exception {
		assert ids.length == iterations;
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getPublicationById(ids[i]);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}
	private double benchmarkTask13(String[] ids) throws Exception {
		assert ids.length == iterations;
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getPublicationById(ids[i]);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}
	private double benchmarkTask14(String[] ids) throws Exception {
		assert ids.length == iterations;
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getPublicationById(ids[i]);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}
}
