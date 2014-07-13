package polytech.di5.lo.pse;

import java.util.ArrayList;

import polytech.di5.lo.donnees.Donnees;

public class PSELB {

	private boolean biOK;
	private boolean flagModif;
	private int d;
	private Donnees donnees;
	private ArrayList<Integer> listT1, listT2;
	private ArrayList<Integer> R; // R: date de début au plut tôt
	private ArrayList<Integer> D; // d: date de fin au plus tard
	private ArrayList<Integer> P; // durée job
	private ArrayList<Integer> dateDebut; // date de début pour chaque job
	private ArrayList<Integer> seq; // sequence partielle de job placé

	public PSELB(Donnees donnees) {
		this.donnees = donnees;
		listT1 = new ArrayList<Integer>();
		listT2 = new ArrayList<Integer>();
		R = new ArrayList<Integer>();
		D = new ArrayList<Integer>();
		P = new ArrayList<Integer>();
		P = donnees.dureesJobs;
		dateDebut = new ArrayList<Integer>();
		seq = new ArrayList<Integer>();
	}
	
	// true pour couper le noeud
	public boolean coupeNoeud(int UBBest){
		if(testEnergetique(UBBest-1) == true){
			return true;
		}
		else{
			return testEnergetique(UBBest-1);
		}
	}


	// return true si d est une bi, false sinon
	private boolean testEnergetiqueAmeliore(int d) {
		while (flagModif == true) {
			flagModif = false;
			for (int k = 0; k < donnees.nbTypesRessources; k++) {
				for (int i = 0; i < donnees.nbJobs; i++) {
					listT1.clear();
					listT2.clear();
					calculerT1(i);
					calculerT2(i);

					for (int iT1 = 0; iT1 < listT1.size(); iT1++) {
						int t1 = listT1.get(iT1);
						for (int iT2 = 0; iT2 < listT2.size(); iT2++) {
							int t2 = listT2.get(iT2);
							if (t2 <= t1) {// pas besoin de calculer
								continue;
							}
							int sommeConsoRes = 0;
							for (int j = 0; j < donnees.nbJobs; j++) {
								int w = calculerW(j, t1, t2);
								sommeConsoRes += w
										* donnees.besoinsRessources.get(j).get(
												k);
							}
							if (sommeConsoRes > donnees.nbRessources.get(k)
									* Math.abs(t2 - t1)) {
								return true; // pas de solution réalisable, donc
												// d
												// est une bi
							}
							// TODO calculer deltaj, ajuster, propager
							else {
								for (int j = 0; j < donnees.nbJobs; j++) {
									int deltaJ = calculerDeltaJ(t1, t2, j, k);
									if (deltaJ  ==  -1) {
										return true; // pas de place pour job j,
														// donc pas de solution
									}
									else if(deltaJ == -2){ // pas de besoin sur res k
										continue;
									}
									int wLeft = calculerWLeft(j, t1, t2);
									int wRight = calculerWRight(j, t1, t2);
									if (deltaJ < wLeft) {
										if(propagerR(j, t2 - deltaJ) == true) return true;
									}
									if (deltaJ < wRight) {
										if(propagerD(j, t1 + deltaJ) == true) return true;
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	// return true si d est une bi, false sinon
	private boolean testEnergetique(int d) {
		D.clear();
		// initialisation
		for (int i = 0; i < donnees.nbJobs; i++) {
			R.add(0);
			D.add(d);
		}
		calculerR();
		calculerD();
		// print
//		for (int i = 0; i < donnees.nbJobs; i++) {
//			System.out.println("Job " + i + "[" + R.get(i) + " , "
//					+ D.get(i) + "]");
//		}
		for (int k = 0; k < donnees.nbTypesRessources; k++) {
			for (int i = 0; i < donnees.nbJobs; i++) {
				listT1.clear();
				listT2.clear();
				calculerT1(i);
				calculerT2(i);
				
				for (int iT1 = 0; iT1 < listT1.size(); iT1++) {
					int t1 = listT1.get(iT1);
					for (int iT2 = 0; iT2 < listT2.size(); iT2++) {
						int t2 = listT2.get(iT2);
						if(t2 <= t1){
							// pas besoin de calculer
							continue;
						}
						int sommeConsoRes = 0;
						int w = 0;
						for(int j = 0; j < donnees.nbJobs; j++){
							w = calculerW(j, t1, t2);
							sommeConsoRes += w * donnees.besoinsRessources.get(j).get(k);
						}
						if (sommeConsoRes > donnees.nbRessources.get(k) * Math.abs(t2 - t1)) {
							// print 
//							System.out.print("T1 ");
//							for(int iBoucle = 0; iBoucle < listT1.size(); iBoucle++){
//								System.out.print(listT1.get(iBoucle) + " ");
//							}
//							System.out.print("  T2 ");
//							for(int iBoucle = 0; iBoucle < listT2.size(); iBoucle++){
//								System.out.print(listT2.get(iBoucle) + " ");
//							}
//							System.out.println();
//							System.out.print("Besoin Conso = " + sommeConsoRes + " Sur Res " + k);
//							System.out.print(" entre [ "+t1 + "," + t2 +" ]");
//							System.out.println(". Res Dispo est " + donnees.nbRessources.get(k) * (t2 - t1) + ".");
							return true; // pas de solution réalisable, donc d est une bi
						}
					}
				}
			}
		}
		return false;
	}

	private void calculerT1(int i) {
		listT1.add(R.get(i));
		listT1.add(R.get(i) + P.get(i));
		listT1.add(D.get(i) - P.get(i));
	}
	
	private void calculerT2(int i) {
		listT2.add(D.get(i));
		listT2.add(R.get(i) + P.get(i));
		listT2.add(D.get(i) - P.get(i));
	}

	private void calculerR() {
		// initialiser R, D pour les tâches placées
		for (int i = 0; i < seq.size(); i++) {
			int tache = seq.get(i);
			R.set(tache, dateDebut.get(i));
			// pour les successeurs de ces tâches
			for (int j = 0; j < donnees.tableauSuccesseurs.get(tache).size(); j++) {
				int jobsuc = donnees.tableauSuccesseurs.get(tache).get(j);
				R.set(jobsuc, Math.max(R.get(jobsuc), R.get(tache) + P.get(tache)));
			}
		}
		for (int i = 0; i < donnees.tableauSuccesseurs.size(); i++) {
			// pour chaque job
			ArrayList<Integer> jobISuc = donnees.tableauSuccesseurs.get(i);
			for (int j = 0; j < jobISuc.size(); j++) {
				// pour tous les successeurs
				int job = jobISuc.get(j);
				R.set(job, Math.max(R.get(job), R.get(i) + P.get(i)));
			}
		}
	}
	
	private boolean propagerR(int j, int val){
		if(val > R.get(j)){ // ajuster
			flagModif = true;
			R.set(j, val);
			ArrayList<Integer> pileSuc = new ArrayList<Integer>();
			pileSuc.add(j);
			while(pileSuc.size() != 0){
				int job = pileSuc.get(0);
				pileSuc.remove(0);
				for(int suc = 0; suc < donnees.tableauSuccesseurs.get(job).size(); suc++){
					int jobsuc = donnees.tableauSuccesseurs.get(job).get(suc);
					if(R.get(job)+P.get(job) > R.get(jobsuc)){
						R.set(jobsuc, R.get(job)+P.get(job));
						if(R.get(jobsuc) + P.get(jobsuc) > D.get(jobsuc)){
							return true;
						}
						for(int sucsuc = 0; sucsuc < donnees.tableauSuccesseurs.get(jobsuc).size(); sucsuc++){
							pileSuc.add(donnees.tableauSuccesseurs.get(jobsuc).get(sucsuc));
						}
					}
				}
			}
		}
		return false; // not sure
	}

	private void calculerD() {
		// initialiser D pour les tâches placées
		for (int i = 0; i < seq.size(); i++) {
			int tache = seq.get(i);
			D.set(tache, dateDebut.get(i) + P.get(i));
		}
		for (int i = donnees.tableauPredecesseurs.size() - 1; i >= 0; i--) {
			// pour chaque job
			ArrayList<Integer> jobIPred = donnees.tableauPredecesseurs.get(i);
			for (int j = 0; j < jobIPred.size(); j++) {
				// pour tous les predecesseurs
				int job = jobIPred.get(j);
				if(seq.contains(job) == false){
					D.set(job, Math.min(D.get(job), D.get(i) - P.get(i)));
				}
			}
		}
	}
 	
 	private boolean propagerD(int j, int val){
 		if(val < D.get(j)){ // ajuster
			flagModif = true;
			D.set(j, val);
			ArrayList<Integer> pilePred = new ArrayList<Integer>();
			pilePred.add(j);
			while(pilePred.size() != 0){
				int job = pilePred.get(0);
				pilePred.remove(0);
				for(int pred = 0; pred < donnees.tableauPredecesseurs.get(job).size(); pred++){
					int jobPred = donnees.tableauPredecesseurs.get(job).get(pred);
					if(D.get(job)-P.get(job) < D.get(jobPred)){
						D.set(jobPred, D.get(job)-P.get(job));
						if(R.get(jobPred) + P.get(jobPred) > D.get(jobPred)){
							return true;
						}
						for(int predpred = 0; predpred < donnees.tableauPredecesseurs.get(jobPred).size(); predpred++){
							pilePred.add(donnees.tableauPredecesseurs.get(jobPred).get(predpred));
						}
					}
				}
			}
		}
		return false; // not sure
 	}
	
	// renvoie l'espace pour job j, renvoie -1 si pas possible
	private int calculerDeltaJ(int t1, int t2, int j, int k){
		int deltaJ = 0, sommeConsoRes = 0, ecart = 0;
		for(int i = 0; i < donnees.nbJobs; i++){
			if(i != j){
				sommeConsoRes += calculerW(i,t1,t2) * donnees.besoinsRessources.get(i).get(k);
			}
		}
		ecart = donnees.nbRessources.get(k) * Math.abs(t2 - t1) - sommeConsoRes;
		if(ecart < 0) return -1;
		if(donnees.besoinsRessources.get(j).get(k) == 0) return -2;
		deltaJ = (int) Math.floor(ecart/donnees.besoinsRessources.get(j).get(k));
		return deltaJ;
	}

	private int calculerW(int i, int t1, int t2) {
		return Math.min(
				Math.min(P.get(i), t2 - t1),
				Math.min(Math.max(0, R.get(i) + P.get(i) - t1),
						Math.max(0, t2 - (D.get(i) - P.get(i)))));
	}
	
	private int calculerWLeft(int i, int t1, int t2) {
		return Math.min(
				Math.min(P.get(i), t2 - t1),
				Math.max(0, R.get(i) + P.get(i) - t1));
	}
	
	private int calculerWRight(int i, int t1, int t2) {
		return Math.min(
				Math.min(P.get(i), t2 - t1),
				Math.max(0, t2 - (D.get(i) - P.get(i))));
	}

	public void setDateDebut(ArrayList<Integer> placedJobListBeginDate) {
		this.dateDebut = placedJobListBeginDate;
		
	}

	public void setSequence(ArrayList<Integer> placedJobList) {
		this.seq = placedJobList;
		
	}

}

