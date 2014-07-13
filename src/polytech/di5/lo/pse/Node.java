package polytech.di5.lo.pse;

import java.util.ArrayList;

import polytech.di5.lo.ag.Individu;
import polytech.di5.lo.communication.Communication;
import polytech.di5.lo.donnees.Donnees;

/**
 * 
 * @author ZHOU Hao
 * 
 */
public class Node {
	private ArrayList<Integer> placedJobList;
	private ArrayList<Integer> placedJobListBeginDate;
	private ArrayList<Integer> placedJobListFinalDate;
	private ArrayList<Integer> eligibleList;
	private ArrayList<Integer> earliestDate;
	private ArrayList<Integer> latestDate;
	private ArrayList<ArrayList<Integer>> ressourcesConsoInstant;
	private int LB;
	private int cMax;
	private int criticalLB;
	private int sumPj;
	private Donnees data;

	/**
	 * Builder with parameters
	 * 
	 * @param data
	 *            : data of the insance
	 * @param criticalLB
	 *            : critical LB
	 * @param sumPj
	 *            : sum of Pj
	 * @param earliestDate
	 *            : list of the earliest date for each job
	 * @param latestDate
	 *            : list of the latest date for each job
	 */
	public Node(Donnees data, int criticalLB, int sumPj,
			ArrayList<Integer> earliestDate, ArrayList<Integer> latestDate) {
		placedJobList = new ArrayList<Integer>();
		placedJobListBeginDate = new ArrayList<Integer>();
		for (int i = 0; i < data.nbJobs; i++) {
			placedJobListBeginDate.add(0);
		}
		placedJobListFinalDate = new ArrayList<Integer>();
		ressourcesConsoInstant = new ArrayList<ArrayList<Integer>>();
		eligibleList = new ArrayList<Integer>();
		this.earliestDate = earliestDate;
		this.latestDate = latestDate;
		this.data = data;
		this.criticalLB = criticalLB;
		LB = criticalLB;
		this.sumPj = sumPj*2;
		cMax = Integer.MAX_VALUE;
	}

	public Node() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Setter of the variable placedJobList
	 * 
	 * @param placedJobList
	 *            : a list of task placed
	 */
	public void setPlacedJobList(ArrayList<Integer> placedJobList) {
		this.placedJobList = placedJobList;
	}

	/**
	 * Find the eligible jobs
	 */
	public void updateEligibleList() {
		// For all job
		for (int i = 0; i < data.nbJobs; i++) {
			// If the job i isn't in placedJobList and eligibleList
			if (!placedJobList.contains(i) && !eligibleList.contains(i)) {
				// We verify if the job is eligible
				boolean eligible = true;
				for (int j = 0; j < data.tableauPredecesseurs.get(i).size(); j++) {
					if (!placedJobList.contains(data.tableauPredecesseurs
							.get(i).get(j))) {
						eligible = false;
					}
				}
				if (eligible) {
					eligibleList.add(i);
				}
			}
		}
	}

	/**
	 * Add a job in placedJobList and calculate the begin of the new job. This
	 * function admit that the job "numJob" is in eligibleList
	 */
	public void addJobInPJL(int numJob) {
		
		placedJobList.add(new Integer(numJob));

		if (!eligibleList.isEmpty()) {
			eligibleList.remove(eligibleList.indexOf(numJob));
		}

		if (ressourcesConsoInstant.isEmpty()) {
			for (int i = 0; i < data.nbTypesRessources; i++) {
				ressourcesConsoInstant.add(new ArrayList<Integer>());
				for (int j = 0; j < sumPj; j++) {
					ressourcesConsoInstant.get(i).add(0);
				}
			}
		}

		/** Calcul of the begin date **/
		// Declaration of variable
		int rjMax = 0;
		int predecesseur = -1;
		int predCj = 0;
		// Find the latest predecessor
		for (int j = 0; j < placedJobList.size(); j++) {
			if (data.tableauPredecesseurs.get(numJob).contains(
					placedJobList.get(j))
					&& placedJobListFinalDate.get(j) >= predCj) {
				predecesseur = placedJobList.get(j);
				predCj = placedJobListFinalDate.get(j);
			}
		}

		// If this is not the first job
		if (predecesseur != -1) {
			rjMax = predCj;
			// For all ressources
			for (int indexRessource = 0; indexRessource < data.nbTypesRessources; indexRessource++) {
				// While we don't find an empty space
				for (int j = rjMax;; j++) {
					// If the ressource is available at j
					if (data.nbRessources.get(indexRessource)
							- ressourcesConsoInstant.get(indexRessource).get(j) >= data.besoinsRessources
							.get(numJob).get(indexRessource)) {
						// We verify if the other ressources are available to
						// affect the job
						boolean ok = true;
						for (int k = j; k < j + data.dureesJobs.get(numJob); k++) {
							if (data.nbRessources.get(indexRessource)
									- ressourcesConsoInstant
											.get(indexRessource).get(k) < data.besoinsRessources
									.get(numJob).get(indexRessource)) {
								ok = false;
								break;
							}
						}
						if (ok && j > rjMax) {
							indexRessource = -1;
							rjMax = j;
							break;
						} else if (ok)
							break;
					}
				}
			}
		}

		for (int j = 0; j < data.nbTypesRessources; j++) {
			for (int k = rjMax; k < rjMax + data.dureesJobs.get(numJob); k++) {
				ressourcesConsoInstant.get(j).set(k,ressourcesConsoInstant.get(j).get(k)
								+ data.besoinsRessources.get(numJob).get(j));
			}
		}

		// Update placedJobListBeginDate and placedJobListFinalDate
		placedJobListBeginDate.set(numJob, rjMax);
		placedJobListFinalDate.add(rjMax + data.dureesJobs.get(numJob));
		calculateLB();
		updateEligibleList();
	}

	/**
	 * Calculate the LB using the criticalLB
	 */
	public void calculateLB() {
		PSELB pseLB = new PSELB(this.data);
		pseLB.setDateDebut(this.getPlacedJobListBeginDate());
		pseLB.setSequence(this.getPlacedJobList());
		int LBtmp = criticalLB;
		if(pseLB.coupeNoeud(Communication.UB)) {
			LB = Integer.MAX_VALUE;
		} else {
			LB = criticalLB;
		}
		
		// Declaration of variables
		/*int max = 0;
		// We search the latest job
		for (int i = 0; i < placedJobList.size(); i++) {
			if (max < (placedJobListBeginDate.get(placedJobList.get(i)) - latestDate
					.get(placedJobList.get(i)))) {
				max = placedJobListBeginDate.get(placedJobList.get(i))
						- latestDate.get(placedJobList.get(i));
			}
		}
		LB = criticalLB + max;*/
	}

	/**
	 * Getter of cMax
	 * 
	 * @return cMax
	 */
	public int getCMax() {
		return this.placedJobListBeginDate
				.get(placedJobListBeginDate.size() - 1);
	}

	/**
	 * Getter of LB
	 * 
	 * @return LB
	 */
	public int getLB() {
		return this.LB;
	}

	
	/**
	 * Setter of placedJobListBeginDate
	 * 
	 * @return placedJobListBeginDate
	 */
	public void setPlacedJobListBeginDate(
			ArrayList<Integer> placedJobListBeginDate) {
		this.placedJobListBeginDate = placedJobListBeginDate;
	}

	/**
	 * Getter of placedJobListBeginDate
	 * 
	 * @return placedJobListBeginDate
	 */
	public ArrayList<Integer> getPlacedJobListBeginDate() {
		return new ArrayList<Integer>(this.placedJobListBeginDate);
	}

	/**
	 * Setter of placedJobListFinalDate
	 * 
	 * @return placedJobListFinalDate
	 */
	public void setPlacedJobListFinalDate(
			ArrayList<Integer> placedJobListFinalDate) {
		this.placedJobListFinalDate = placedJobListFinalDate;
	}

	/**
	 * Getter of placedJobListFinalDate
	 * 
	 * @return placedJobListFinalDate
	 */
	public ArrayList<Integer> getPlacedJobListFinalDate() {
		return new ArrayList<Integer>(this.placedJobListFinalDate);
	}

	/**
	 * Setter of ressourcesConsoInstant
	 * 
	 * @return ressourcesConsoInstant
	 */
	public void setRessourcesConsoInstant(
			ArrayList<ArrayList<Integer>> ressourcesConsoInstant) {
		this.ressourcesConsoInstant = ressourcesConsoInstant;
	}

	/**
	 * Getter of ressourcesConsoInstant
	 * 
	 * @return ressourcesConsoInstant
	 */
	public ArrayList<ArrayList<Integer>> getRessourcesConsoInstant() {
		ArrayList<ArrayList<Integer>> tmp = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i < ressourcesConsoInstant.size(); i++){
			tmp.add(new ArrayList<Integer>(ressourcesConsoInstant.get(i)));
		}
		return tmp;
	}
	
	/**
	 * Getter of eligibleList
	 * 
	 * @return eligibleList
	 */
	public ArrayList<Integer> getEligibleList() {
		return new ArrayList<Integer>(this.eligibleList);
	}

	/**
	 * Getter of placedJobList
	 * 
	 * @return placedJobList
	 */
	public ArrayList<Integer> getPlacedJobList() {
		return new ArrayList<Integer>(this.placedJobList);
	}


}