package polytech.di5.lo.ag;

import java.util.ArrayList;

import polytech.di5.lo.donnees.Donnees;

public class Individu {

	int radical;
	ArrayList<Integer> sequence;
	volatile ArrayList<Integer> dateDebut;
	ArrayList<ArrayList<Integer>> ressourcesConsoInstant;
	int cMax;
	Donnees donnees;
	private int sumCj;

	/***
	 * -----------------------------Interface constructor(I am a gorgeous
	 * separation line)----------------------------------
	 ***/
	public Individu(Donnees donnees) {
		this.donnees = donnees;
		sequence = new ArrayList<Integer>();
		dateDebut = new ArrayList<Integer>();
		sumCj = 0;
		for (int i = 0; i < donnees.dureesJobs.size(); i++) {
			sumCj += donnees.dureesJobs.get(i);
		}
		radical = 1;
		ressourcesConsoInstant = new ArrayList<ArrayList<Integer>>();
	}

	public Individu(Individu individu) {
		this.donnees = individu.donnees;
		sequence = individu.getSequence();
		dateDebut = individu.getDateDebut();
		sumCj = individu.getSumCj();
		radical = individu.getRadical();
		ressourcesConsoInstant = individu.getRessourcesConsoInstant();
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
	
	public int getSumCj(){
		return this.sumCj;
	}
	/***
	 * -----------------------------Interface getter(I am a gorgeous separation
	 * line)----------------------------------
	 ***/
	public void setRadical(int radical) {
		this.radical = radical;
	}
	
	public int getRadical() {
		return this.radical;
	}

	public void setSequence(ArrayList<Integer> sequence) {
		this.sequence = sequence;
	}

	public ArrayList<Integer> getSequence() {
		return new ArrayList<Integer>(this.sequence);
	}

	public void setcMax(int cMax) {
		this.cMax = cMax;
	}
	
	public int getcMax() {
		return this.cMax;
	}

	public void setDonnees(Donnees donnees) {
		this.donnees = donnees;
	}
	
	public Donnees getDonnees() {
		return this.donnees;
	}

	public void setDateDebut(ArrayList<Integer> dateDebut) {
		this.dateDebut = dateDebut;
	}

	public ArrayList<Integer> getDateDebut() {
		return this.dateDebut;
	}

	public void addJobInSequence(int job) {
		sequence.add(job);
	}

	public void addJobInSequence(int index, int job) {
		sequence.add(index, job);
	}
	
	public void deleteJobInSequence(int job) {
		sequence.remove(job);
	}


	public void evaluation() {
		ressourcesConsoInstant = new ArrayList<ArrayList<Integer>>();
		dateDebut.clear();
		for (int i = 0; i < donnees.nbTypesRessources; i++) {
			ressourcesConsoInstant.add(new ArrayList<Integer>());
			for (int j = 0; j < sumCj; j++) {
				ressourcesConsoInstant.get(i).add(0);
			}
		}

		int cMaxTmp = 0;
		int cjMax = 0;
		ArrayList<Integer> taskEndTime = new ArrayList<Integer>();
		for (int i = 0; i < sequence.size(); i++) {
			int predecesseur = -1;
			int predCj = 0;
			for (int j = 0; j < i; j++) {
				if (donnees.tableauPredecesseurs.get(sequence.get(i)).contains(
						sequence.get(j)) && taskEndTime.get(j) >= predCj) {
					predecesseur = sequence.get(j);
					predCj = taskEndTime.get(j);
				}
			}
			if (predecesseur == -1) {
				taskEndTime.add(0);
			} else {
				cjMax = predCj;
				for (int indexRessource = 0; indexRessource < donnees.nbTypesRessources; indexRessource++) {
				
					for (int j = cjMax;; j++) {
					
						if (donnees.nbRessources.get(indexRessource)-ressourcesConsoInstant.get(indexRessource).get(j) 
								>= donnees.besoinsRessources.get(sequence.get(i)).get(indexRessource)) {
							boolean ok = true;
							for (int k = j; k<j
									+ donnees.dureesJobs.get(sequence.get(i)); k++) {
								if (donnees.nbRessources.get(indexRessource)
										- ressourcesConsoInstant.get(
												indexRessource).get(k) < donnees.besoinsRessources
										.get(sequence.get(i)).get(
												indexRessource)) {
									ok = false;
									break;
								}
							}
							if (ok && j > cjMax) {
								indexRessource = -1;
								cjMax = j;
								break;
							}
							else if(ok)
								break;
						}
						
					}
					
				}
				taskEndTime.add(cjMax
						+ donnees.dureesJobs.get(sequence.get(i)));

				for (int j = 0; j < donnees.nbTypesRessources; j++) {
					for (int k = cjMax; k < cjMax
							+ donnees.dureesJobs.get(sequence.get(i)); k++) {
						ressourcesConsoInstant.get(j).set(
								k,
								ressourcesConsoInstant.get(j).get(k)
										+ donnees.besoinsRessources.get(
												sequence.get(i)).get(j));
					}
					if (cMaxTmp < cjMax + donnees.dureesJobs.get(sequence.get(i))) {
						cMaxTmp = cjMax + donnees.dureesJobs.get(sequence.get(i));
					}
				}
			}
		}
		this.cMax = cMaxTmp;
		// Mise à jour des dates de debut
		for(int i = 0; i < sequence.size(); ++i){
			int indexTaskI = sequence.indexOf(i);
			dateDebut.add(taskEndTime.get(indexTaskI)-donnees.dureesJobs.get(i));
		}
	}

}
