package polytech.di5.lo.pse;

import java.util.ArrayList;
import polytech.di5.lo.donnees.Donnees;

public class RCPSPLB {

	private boolean biOK;
	private boolean flagModif;
	private int d;
	private Donnees donnees;
	private ArrayList<Integer> listT1, listT2;
	private ArrayList<Integer> R; // R: date de début au plut tôt
	private ArrayList<Integer> D; // d: date de fin au plus tard
	private ArrayList<Integer> P; // durée job

	public RCPSPLB(Donnees donnees) {
		this.donnees = donnees;
		listT1 = new ArrayList<Integer>();
		listT2 = new ArrayList<Integer>();
		R = new ArrayList<Integer>();
		D = new ArrayList<Integer>();
		P = new ArrayList<Integer>();
		P = donnees.dureesJobs;
	}

	public int calculerBICheminCritique() {
		int LB = 0;
		// initialisation
		R.clear();
		for (int i = 0; i < donnees.nbJobs; i++) {
			R.add(0);
		}
		calculerR();
		LB = R.get(R.size() - 1);
		return LB;
	}

	public int calculerBIEnergetique() {
		d = calculerBICheminCritique();
		biOK = true;

		// initialisation
		R.clear();
		for (int i = 0; i < donnees.nbJobs; i++) {
			R.add(0);
		}
		calculerR();
		
		while (biOK == true) {
			biOK = testEnergetique(d);
			System.out.println("test D = " + d);
			d++;
		}
		return d - 1;
	}
	
	public int calculerBIEnergetiqueAmeliore() {
		d = calculerBICheminCritique();
		biOK = true;

		while (biOK == true) {
			flagModif = true;
			biOK = testEnergetiqueAmeliore(d);
			d++;
		}
		return d - 1;
	}

	// return true si d est une bi, false sinon
	private boolean testEnergetiqueAmeliore(int d) {
		D.clear();
		R.clear();
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
			D.add(d);
		}
		calculerD();
		// print
		for (int i = 0; i < donnees.nbJobs; i++) {
			System.out.println("Job " + i + "[" + R.get(i) + " , " + D.get(i)
					+ "]");
		}
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
							System.out.print("T1 ");
							for(int iBoucle = 0; iBoucle < listT1.size(); iBoucle++){
								System.out.print(listT1.get(iBoucle) + " ");
							}
							System.out.print("  T2 ");
							for(int iBoucle = 0; iBoucle < listT2.size(); iBoucle++){
								System.out.print(listT2.get(iBoucle) + " ");
							}
							System.out.println();
							System.out.print("Besoin Conso = " + sommeConsoRes + " Sur Res " + k);
							System.out.print(" entre [ "+t1 + "," + t2 +" ]");
							System.out.println(". Res Dispo est " + donnees.nbRessources.get(k) * (t2 - t1) + ".");
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
 		for (int i = donnees.tableauPredecesseurs.size() - 1; i >= 0; i--) { 
			// pour chaque job
			ArrayList<Integer> jobIPred = donnees.tableauPredecesseurs.get(i);
			for (int j = 0; j < jobIPred.size(); j++) { 
				// pour tous les successeurs
				int job = jobIPred.get(j);
				D.set(job, Math.min(D.get(job), D.get(i) - P.get(i)));
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
	
	public ArrayList<Integer> getR() {
		return new ArrayList<Integer>(R);
	}
	
	public ArrayList<Integer> getD() {
		ArrayList<Integer> tmp = new ArrayList<Integer>(D);
		for(int i = 0; i < tmp.size(); i++) {
			tmp.set(i, tmp.get(i) - donnees.dureesJobs.get(i));
		}
		return tmp;
	}

}
