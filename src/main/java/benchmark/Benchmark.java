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

import ch.ethz.globis.mtfobu.domains.Conference;
import ch.ethz.globis.mtfobu.domains.Person;
import ch.ethz.globis.mtfobu.domains.Publication;
import ch.ethz.globis.mtfobu.odb_project.db.Database;

public class Benchmark {
	String outputFileNameSuffix = "/home/granchgen/benchmark";
	int iterations = 5;
	final Database db;

	public Benchmark(Database db) {
		this.db = db;
	}

	public void benchmark() throws Exception {
		final String filename = outputFileNameSuffix + " " + db.getDBTechnology() + " " + LocalDateTime.now() + ".csv";
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
		List<Conference> confs = db.getConferences();
		int random;

		bWriter.write(String.format(
				"Timings for Database: %s\nNumber of iterations per run: %d\nInputs where choosen at random\n",
				db.getDBTechnology(), iterations));
		bWriter.write("Task;average time in milliseconds\n");

		// task 1 inputs
		String ids[] = new String[iterations];
		// task 2+3 inputs
		int task2and3IntervalLenght = 500;
		int task2and3substringLength = 3;
		String titles[] = new String[iterations];
		int begin_offset[] = new int[iterations];
		int end_offset[] = new int[iterations];
		int offset = pubs.size() - 1;
		// task 4, 13 inputs
		String names[] = new String[iterations];
		// task 5 inputs
		String authorsA[] = new String[iterations];
		String authorsB[] = new String[iterations];
		// task 7, 14 inputs intervall
		int task7and14IntervalLenght = 20;
		int yearLB[] = new int[iterations];
		int yearUB[] = new int[iterations];
		// task 8, 9, 10, 11 input
		System.out.println("Initialization...");
		String conferences[] = new String[iterations];
		for (int i = 0; i < iterations; ++i) {
			random = ThreadLocalRandom.current().nextInt(0, pubs.size() - 1);
			ids[i] = pubs.get(random).getId();
			random = ThreadLocalRandom.current().nextInt(0, pubs.size() - 1);
			String title = pubs.get(random).getTitle();
			int index1 = ThreadLocalRandom.current().nextInt(0, title.length() - 1 - task2and3substringLength);
			int index2 = index1 + task2and3substringLength;
			titles[i] = title.substring(index1, index2);
			// random = ThreadLocalRandom.current().nextInt(0,
			// Integer.min(task2and3IntervalLenght, 0));
			begin_offset[i] = 0;
			// random = ThreadLocalRandom.current().nextInt(random, pubs.size()
			// - 1);
			random = task2and3IntervalLenght;
			end_offset[i] = random;
			random = ThreadLocalRandom.current().nextInt(0, pers.size() - 1);
			names[i] = pers.get(random).getName();
			// TODO: this is not optimal for task 5
			random = ThreadLocalRandom.current().nextInt(0, pers.size() - 1);
			Person distCoAuthors[] = getTwoAuthorsWithDist(2);
			authorsA[i] = distCoAuthors[0].getId();
			if (distCoAuthors[1] != null){
			authorsB[i] = distCoAuthors[1].getId();
			}
			else authorsB[i] = distCoAuthors[0].getId();
			// task 7 14
			random = ThreadLocalRandom.current().nextInt(1900, Integer.max(2017 - task7and14IntervalLenght, 1900));
			yearLB[i] = random;
			random = random + task7and14IntervalLenght;
			yearUB[i] = random;
			// task 8 9 10 11
			random = ThreadLocalRandom.current().nextInt(0, confs.size() - 1);
			conferences[i] = confs.get(random).getId();

		}
		String inputs = new String();

		timings[0] = benchmarkTask1(ids);
		for (String id : ids)
			inputs += " , " + id;
		System.out.println(String.format("Task %d inputs:%s", 1, inputs));
		timings[1] = benchmarkTask2(titles, offset);
		inputs = "";
		for (int i = 0; i < iterations; ++i)
			inputs += "title: " + titles[i] + " begin_offset: " + begin_offset[i] + " end_offset: " + end_offset[i]
					+ ", ";
		System.out.println(String.format("Task %s inputs:%s", "2 and 3", inputs));
		timings[2] = benchmarkTask3(titles, begin_offset, end_offset);
		timings[3] = benchmarkTask4(names);
		inputs = "";
		for (int i = 0; i < iterations; ++i)
			inputs += names[i] + " , ";
		System.out.println(String.format("Task %s inputs:%s", "4", inputs));
		timings[4] = benchmarkTask5(authorsA, authorsB);
		inputs = "";
		for (int i = 0; i < iterations; ++i)
			inputs += authorsA[i] + " and " + authorsB[i] + " , ";
		System.out.println(String.format("Task %s inputs:%s", "5", inputs));
		timings[5] = benchmarkTask6();
		System.out.println(String.format("Task %s inputs: none (not required)", "6"));
		timings[6] = benchmarkTask7(yearLB, yearUB);
		inputs = "";
		for (int i = 0; i < iterations; ++i)
			inputs += "years: " + yearLB[i] +  " to " + yearUB[i] + " , ";
		System.out.println(String.format("Task %s inputs:%s", "7", inputs));
		timings[7] = benchmarkTask8(conferences);
		inputs = "";
		for (int i = 0; i < iterations; ++i)
			inputs += conferences[i] + " , ";
		System.out.println(String.format("Task %s inputs:%s", "8, 9, 10 and 11", inputs));
		timings[8] = benchmarkTask9(conferences);
		timings[9] = benchmarkTask10(conferences);
		timings[10] = benchmarkTask11(conferences);
		timings[11] = benchmarkTask12();
		System.out.println(String.format("Task %s inputs: none (not required)", "12"));
		timings[12] = benchmarkTask13(names);
		inputs = "";
		for (int i = 0; i < iterations; ++i)
			inputs += names[i] + " , ";
		System.out.println(String.format("Task %s inputs:%s", "13", inputs));
		timings[13] = benchmarkTask14(yearLB, yearUB);
		inputs = "";
		for (int i = 0; i < iterations; ++i)
			inputs += "years: " + yearLB[i] +  " to " + yearUB[i] + " , ";
		System.out.println(String.format("Task %s inputs:%s", "14", inputs));
		writeBenchmarkTimings(bWriter, timings);
		bWriter.close();
	}

	private void writeBenchmarkTimings(BufferedWriter bw, double[] timings) throws IOException {
		for (int task = 1; task <= 14; ++task) {
			bw.write(String.format("%d;%.8f\n", task, timings[task - 1]));
		}
	}

	public Person[] getTwoAuthorsWithDist(int distance) {
		Person pers[] = new Person[2];
		List<Person> people = db.getPeople();
		int random = ThreadLocalRandom.current().nextInt(0, people.size() - 1);

		pers[0] = db.getPersonById("267315475", false);
		people = db.getCoAuthors(pers[0].getName());
		for (int i = 0; i < distance; ++i) {
			int numCoAuthors = people.size();
			if (numCoAuthors > 0)
				random = ThreadLocalRandom.current().nextInt(0, numCoAuthors - 1);
			else {
				System.out.println("stopped prematurly");
				break;
			}
			Person per = db.getPersonById(people.get(i).getId(), false);
			assert per != null : "getPersonById failed to retrieve valid id";
			people = db.getCoAuthors(per.getName());
			pers[1] = per;
		}
		return pers;
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
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
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

	private double benchmarkTask7(int[] yearLB, int[] yearUB) throws Exception {
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getNumberPublicationsPerYearInterval(yearLB[i], yearUB[i]);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}

	private double benchmarkTask8(String[] conferenceNames) throws Exception {
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getNumberOfPublicationsPerConferenceByName(conferenceNames[i]);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}

	private double benchmarkTask9(String[] conferenceNames) throws Exception {
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getAllAuthorsOfConferenceByName(conferenceNames[i]);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}

	private double benchmarkTask10(String[] conferenceNames) throws Exception {

		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getAllAuthorsOfConferenceByName(conferenceNames[i]);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}

	private double benchmarkTask11(String[] conferenceNames) throws Exception {
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getAllPublicationsOfConferenceByName(conferenceNames[i]);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}

	private double benchmarkTask12() throws Exception {
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getPeopleThatAreAuthorsAndEditors();
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}

	private double benchmarkTask13(String[] names) throws Exception {
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.getPublicationsWhereAuthorIsLast(names[i]);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}

	private double benchmarkTask14(int[] yearLB, int[] yearUB) throws Exception {
		long startTime, endTime;
		double elapsedTime;

		startTime = System.currentTimeMillis();
		for (int i = 0; i < iterations; ++i) {
			db.task14(yearLB[i], yearUB[i]);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = ((double) (endTime - startTime)) / ((double) iterations);
		return elapsedTime;
	}
}
