package polytech.di5.lo.ag;

import java.util.ArrayList;
import java.util.Random;

import polytech.di5.lo.communication.Communication;
import polytech.di5.lo.donnees.Donnees;

/**
 * 
 * @author Baptiste Mille & Raphael Roger
 * 
 */
public class Genetic implements Runnable {
	private ArrayList<Individu> population;
	private int populationSize;
	private Donnees data;
	public static int bestCMax = Integer.MAX_VALUE;
	public static volatile Individu bestIndividual;
	public double mutationRate = 0.02;
	private ArrayList<Integer> earliestDate;
	private ArrayList<Integer> latestDate;
	private Individu criticalPath;
	private int criticalPathLB;

	/**
	 * Calculates the critical path, the earliest date and the date at the latest Gives a lower
	 * bound
	 */
	public void calculateCriticalPath() {
		// Calculate earliest dates in first
		earliestDate.clear();
		for (int i = 0; i < data.nbJobs; i++) {
			earliestDate.add(0);
		}
		calculateEarliestDate();

		// Calculate dates at the least
		latestDate.clear();
		for (int i = 0; i < data.nbJobs; i++) {
			latestDate.add(Integer.MAX_VALUE);
		}
		latestDate.set(latestDate.size() - 1, earliestDate.get(earliestDate.size() - 1));
		calculateDateAtTheLeast();

		criticalPath = new Individu(this.data);
		for (int i = 0; i < data.nbJobs; i++) {
			if (earliestDate.get(i) == latestDate.get(i)) {
				criticalPath.addJobInSequence(i);
			}
		}

		criticalPathLB = earliestDate.get(earliestDate.size() - 1);
	}

	/**
	 * Calculate the earliest dates
	 */
	private void calculateEarliestDate() {
		for (int i = 0; i < data.nbJobs; i++) {
			ArrayList<Integer> jobISuc = data.tableauSuccesseurs.get(i);
			for (int j = 0; j < jobISuc.size(); j++) {
				int job = jobISuc.get(j);
				earliestDate.set(
						job,
						Math.max(earliestDate.get(job),
								earliestDate.get(i) + data.dureesJobs.get(i)));
			}
		}
	}

	/**
	 * Calculate dates at the least
	 */
	private void calculateDateAtTheLeast() {
		for (int i = data.tableauPredecesseurs.size() - 1; i >= 0; i--) {
			ArrayList<Integer> jobIPred = data.tableauPredecesseurs.get(i);
			for (int j = 0; j < jobIPred.size(); j++) {
				int job = jobIPred.get(j);
				latestDate
						.set(job,
								Math.min(latestDate.get(job),
										latestDate.get(i) - data.dureesJobs.get(job)));
			}
		}
	}

	/**
	 * Builder with parameters
	 * 
	 * @param populationSize
	 *            : size of the population
	 * @param data
	 *            : data of instance
	 */
	public Genetic(int populationSize, Donnees data) {
		this.populationSize = populationSize;
		this.data = data;
		population = new ArrayList<Individu>();
		earliestDate = new ArrayList<Integer>();
		latestDate = new ArrayList<Integer>();
	}

	/**
	 * This function update the global UB
	 * 
	 * @param individual
	 *            : the solution with the new UB
	 */
	public void updateUB(Individu individual) {
		bestCMax = individual.cMax;
		bestIndividual = individual;
		// If the new Cmax is better than the global UB
		if (bestCMax < Communication.UB) {
			mutationRate = 0.02;
			Communication.modifierUB(bestCMax, "AG");
			Communication.newResultForDisplay = true;
			Communication.newIsAG = true;
		}
	}

	/**
	 * Generation of the first population
	 */
	public void genererPopulation() {
		calculateCriticalPath();
		// We create each individual using critical path
		for (int i = 0; i < populationSize; ++i) {
			ArrayList<Integer> eligibleList = new ArrayList<Integer>();
			Individu individual = new Individu(data);
			individual.radical = 0;
			eligibleList.add(0);

			// While we have job in not placed
			do {
				// Declaration of variable
				boolean jobInCriticalPath = false;
				int selectedJob = 0;
				ArrayList<Integer> criticalJobFound = new ArrayList<Integer>();
				// We search jobs which are in the critical path
				for (int j = 0; j < eligibleList.size(); j++) {
					if (criticalPath.getSequence().contains(eligibleList.get(j)))
						;
					jobInCriticalPath = true;
					criticalJobFound.add(j);
				}

				// We select in priority jobs in critical path
				if (jobInCriticalPath) {
					Random random = new Random();
					selectedJob = criticalJobFound.get(random.nextInt(criticalJobFound.size()));
				} else {
					Random random = new Random();
					selectedJob = random.nextInt(eligibleList.size());
				}
				// We add the job selected in the sequence and we remove this job in eligibleList
				individual.addJobInSequence(eligibleList.get(selectedJob));
				eligibleList.remove(selectedJob);

				// We search the new eligible jobs
				for (int j = 0; j < data.tableauPredecesseurs.size(); j++) {
					// If the job is neither in sequence nor in eligibleList
					if (!eligibleList.contains(j) && !individual.getSequence().contains(j)) {
						// We check if the job is eligible
						boolean eligible = true;
						for (int k = 0; k < data.tableauPredecesseurs.get(j).size(); k++) {
							if (!individual.getSequence().contains(
									data.tableauPredecesseurs.get(j).get(k))) {
								eligible = false;
								break;
							}
						}
						// If the job is eligible we add this job in eligibleList
						if (eligible) {
							eligibleList.add(j);
						}
					}
				}
			} while (eligibleList.size() > 0);

			// We evaluate the individual
			individual.evaluation();
			if (individual.cMax < this.bestCMax)
				updateUB(individual);
			// We add the individual in population
			population.add(individual);
		}

	}

	/**
	 * Binary tournamentselection to get parents
	 */
	public void selection() {
		// Declaration of variable
		ArrayList<Individu> populationTournament = new ArrayList<Individu>();
		ArrayList<Individu> newPopulation = new ArrayList<Individu>();
		Random random = new Random();

		// Binary tournament, we want to have a size of parents population equal as populationSize /
		// 2
		while (!population.isEmpty() && newPopulation.size() < populationSize / 2) {

			// Management of uneven size of population
			while (populationTournament.size() < 2 && !population.isEmpty()) {
				// We add a random individual of population in populationTournament
				int randomIndice = random.nextInt(population.size());
				populationTournament.add(population.get(randomIndice));
				population.remove(randomIndice);
			}

			// Uneven size of population
			if (populationTournament.size() == 1) {
				newPopulation.add(populationTournament.get(0));
				populationTournament.clear();
			}
			// Normal case, we compare individuals and choose the best
			else {
				if (populationTournament.get(0).cMax < populationTournament.get(1).cMax) {
					newPopulation.add(populationTournament.get(0));
				} else {
					newPopulation.add(populationTournament.get(1));
				}
				populationTournament.clear();
			}
		}
		// We update the population
		population = newPopulation;
	}

	/**
	 * Crossing between father and mother to create son and daughter
	 */
	public void crossing() {
		// Declaration of variables
		int sizeParentsPopulation = population.size();
		Random random = new Random();

		// For each couple father-daughter
		for (int i = 0; i < sizeParentsPopulation; i = i + 2) {
			// Declaration of parents and children
			Individu pere = population.get(i);
			Individu mere = population.get((i + 1) % sizeParentsPopulation);
			Individu son = new Individu(data);
			Individu daughter = new Individu(data);

			// The children inherit the radical of the parents
			son.setRadical(pere.getRadical());
			daughter.setRadical(mere.getRadical());

			// Crosing Points
			int crossingPointSon = random.nextInt(data.nbJobs * 2 / 3 - son.getRadical())
					+ son.getRadical();
			int crossingPointSon2 = random.nextInt(data.nbJobs - son.getRadical()
					- crossingPointSon)
					+ son.getRadical() + crossingPointSon;
			int crossingPointDaughter = random.nextInt(data.nbJobs * 2 / 3 - daughter.getRadical())
					+ daughter.getRadical();
			int crossingPointDaughter2 = random.nextInt(data.nbJobs - daughter.getRadical()
					- crossingPointDaughter)
					+ daughter.getRadical() + crossingPointDaughter;

			/** Son Begin **/
			// Jobs before crossingPointSon are in the same order than father
			for (int j = 0; j < crossingPointSon; j++) {
				son.addJobInSequence(pere.getSequence().get(j));
			}

			// Jobs between crossingPointSon and crossingPointSon2 are the same than the father but
			// in mother's order
			for (int j = 0; j < mere.getSequence().size(); j++) {
				for (int k = crossingPointSon; k < crossingPointSon2; k++) {
					if (mere.getSequence().get(j) == pere.getSequence().get(k)) {
						son.addJobInSequence(mere.getSequence().get(j));
					}
				}
			}

			// Jobs after crossingPointSon2 are in the same order than father
			for (int j = crossingPointSon2; j < data.nbJobs; j++) {
				son.addJobInSequence(pere.getSequence().get(j));
			}

			// We evaluate, add the son and update the global UB if the son UB is better
			son.evaluation();
			if (son.cMax < this.bestCMax)
				updateUB(son);
			population.add(son);
			/** Son End **/

			/** Daughter Begin **/
			// Jobs before crossingPointDaughter are in the same order than mother
			for (int j = 0; j < crossingPointDaughter; j++) {
				daughter.addJobInSequence(mere.getSequence().get(j));
			}

			// Jobs between crossingPointDaughter and crossingPointDaughter2 are the same than the
			// mother but in father's order
			for (int j = 0; j < pere.getSequence().size(); j++) {
				for (int k = crossingPointDaughter; k < crossingPointDaughter2; k++) {
					if (pere.getSequence().get(j) == mere.getSequence().get(k)) {
						daughter.addJobInSequence(pere.getSequence().get(j));
					}
				}
			}

			// Jobs after crossingPointSon2 are in the same order than father
			for (int j = crossingPointDaughter2; j < data.nbJobs; j++) {
				daughter.addJobInSequence(mere.getSequence().get(j));
			}

			// We evaluate, add the daughter and update the global UB if the son UB is better
			daughter.evaluation();
			if (daughter.cMax < this.bestCMax)
				updateUB(daughter);
			population.add(daughter);
		}

	}

	/**
	 * Mutation of individual
	 */
	public void mutation() {
		// All individual in population can be mutated
		for (int i = 0; i < population.size(); ++i) {
			// Declaration of variable
			int indexPredecesseur = 0;
			int successorIndex = 0;
			int intervalSize = 0;
			double random = Math.random();

			// If the individual mutates
			if (random <= 0.02) {
				// We keep the original individual
				population.add(new Individu(population.get(i)));
				// We copy the individual
				Individu choseIndividual = population.get(i);
				// We chose the job to move
				int choseJob = (int) (Math.random()
						* ((choseIndividual.getSequence().size() - choseIndividual.radical)) + choseIndividual.radical);
				int indexChoseJob = choseIndividual.getSequence().indexOf(choseJob);

				// We search the latest predecessor
				for (int j = 0; j < indexChoseJob; ++j) {
					// We search the index of the sequence in the tab of predecessors
					if (data.tableauPredecesseurs.get(choseJob).contains(
							choseIndividual.getSequence().get(j))) {
						indexPredecesseur = j;
					}
				}

				// We search the first successor
				for (int j = indexChoseJob; j < choseIndividual.getSequence().size(); ++j) {
					// We search the index of the sequence in the tab of successor
					if (data.tableauSuccesseurs.get(choseJob).contains(
							choseIndividual.getSequence().get(j))) {
						indexPredecesseur = j;
						break;
					}
				}

				// We define the size of the interval
				if (successorIndex == 0)
					break;
				successorIndex = choseIndividual.getSequence().size();
				intervalSize = successorIndex - indexPredecesseur - 2;

				// We define the new place
				int newPlace = (int) (Math.random() * intervalSize);

				// We delete the job before moving
				choseIndividual.deleteJobInSequence(indexChoseJob);

				// We place the job in a random place in the interval
				for (int j = indexPredecesseur + 1; j < successorIndex; j++) {
					int k = 0;
					if (k == newPlace) {
						choseIndividual.addJobInSequence(j, choseJob);
					}
					k++;
				}
			}
		}
	}

	// public void rafraichirPopulation(ArrayList<Noeud> noeuds) {
	/*
	 * Random random = new Random(); for (int i = 0; i < taillePopulation; ++i) { ArrayList<Integer>
	 * eligibleListe = new ArrayList<Integer>(); Individu individu = new Individu(donnees);
	 * individu.setSequence(noeuds.get(random.nextInt(noeuds.size()))); individu.radical =
	 * individu.getSequence().size();
	 * 
	 * do { // On cherche les nouveaux jobs é–˜igibles for (int j = 1; j <
	 * donnees.tableauPredecesseurs.size() - 1; j++) { // Si les jobs ne sont pas pré–Ÿent ni dans
	 * é–˜igible ni dans // sequence if (!eligibleListe.contains(j) &&
	 * !individu.getSequence().contains(j)) { // On vé–žifie si notre job est é–˜igible boolean
	 * eligible = true; for (int k = 0; k < donnees.tableauPredecesseurs.get(j).size(); k++) { if
	 * (!individu.getSequence().contains( donnees.tableauPredecesseurs.get(j).get(k))) { eligible =
	 * false; break; } } // On ajoute notre job s'il est é–˜igible if (eligible) {
	 * eligibleListe.add(j); } } }
	 * 
	 * // On selectionne et ajoute le job ï¿½mettre dans notre sé–�uence int selectedJob =
	 * random.nextInt(eligibleListe.size());
	 * individu.addJobInSequence(eligibleListe.get(selectedJob)); eligibleListe.remove(selectedJob);
	 * 
	 * } while (eligibleListe.size() > 0);
	 * 
	 * // On é–¢alue et ajoute notre individu individu.evaluation(); if(individu.cMax<this.bestCMax)
	 * updateUB(individu); population.add(individu); }
	 */
	// }

	/**
	 * Run of the thread
	 */
	public void run() {
		processing();
	}

	/**
	 * Genetic algorithm
	 */
	public void processing() {
		// Generates the initial population
		this.genererPopulation();
		int i = 0;
		// Genetic
		while (!Communication.pseIsOver) {

			if (Communication.newNodesForAG == true) {
				Communication.newNodesForAG = false;
			}
			if (i % 100 == 0) {
				mutationRate += 0.01;
			}
			if (i == 1000) {
				System.out.println("NewPopulation");
				population.clear();
				genererPopulation();
				mutationRate = 0.02;
				i = 0;
			}

			this.selection();
			this.crossing();
			this.mutation();
			i++;
		}
	}
}
