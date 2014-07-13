package polytech.di5.lo.communication;

import java.util.ArrayList;

import polytech.di5.lo.donnees.Donnees;
import polytech.di5.lo.pse.*;
import polytech.di5.lo.affichage.Fenetre;
import polytech.di5.lo.ag.Genetic;
import polytech.di5.lo.ag.Individu;


public class Communication implements Runnable {
	
	/* Variables partagées */
	public static volatile ArrayList<Node> listePile = new ArrayList<Node>();
	public static volatile ArrayList<Node> nodesList = new ArrayList<Node>();
	public static volatile int UB = Integer.MAX_VALUE; // Updated by the genetic algorithm
	public Thread tPSE, tAG;
	public volatile Donnees donnees;
	public static volatile boolean pseIsOver = false;
	public static volatile boolean newNodesForAG = false;
	public static volatile boolean newResultForDisplay = false;
	public static volatile boolean newIsAG = false;
	
	/* Contrôle sur AG */
	private int taillePopulation;
	
	public synchronized static void modifierUB(int nUB, String nomSource) {
		if(nUB < UB) {
			UB = nUB;
			System.out.println(nomSource + " " + UB); // afficher nom d'expéditeur
		}
	}
	
	public Communication(){
		
	}
	
	public Communication(Donnees d){
		donnees = d;
		taillePopulation = d.nbJobs*2;
	}

	public void processing() {
		Communication.UB = Integer.MAX_VALUE-1;
		
		tAG = new Thread(new Genetic(taillePopulation, donnees));
		tAG.start();
		
		tPSE = new Thread(new BranchAndBound(donnees));
		tPSE.start();
		
		while (tPSE.getState() != Thread.State.TERMINATED) { // Si PSE est en cours d'exécution
			// Mise à jour de l'affichage
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Flag to indicate to the genetic algorithm to finish
		pseIsOver = true;

		// Attente la fin de lancement PSE et AG 
		try {
			tPSE.join();
			tAG.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//System.out.println("Genetique, "+Genetique.bestCMax+" : "+Genetique.bestIndividu.getSequence());
		
	}
	
	public void run()
	{
		processing();
	}
}
