package polytech.di5.lo.pse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import polytech.di5.lo.ag.Individu;
import polytech.di5.lo.communication.Communication;
import polytech.di5.lo.donnees.Donnees;

public class BranchAndBound extends Communication implements Runnable {

	int sumPj;
	Donnees data;
	private ArrayList<Node> nodeHeap;
	private ArrayList<Integer> earliestDate;
	private ArrayList<Integer> latestDate;
	private Individu criticalPath;
	private int energeticLB;
	public static volatile Node bestNode;


	/**
	 * Builder with parameter
	 * 
	 * @param data
	 *            : data of the problem
	 */
	public BranchAndBound(Donnees data) {
		this.data = data;
		nodeHeap = new ArrayList<Node>();
		earliestDate = new ArrayList<Integer>();
		latestDate = new ArrayList<Integer>();

		for (int i = 0; i < data.dureesJobs.size(); i++) {
			sumPj += data.dureesJobs.get(i);
		}
	}

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

		energeticLB = earliestDate.get(earliestDate.size() - 1);
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
	 * Separate the father's node in sons
	 * 
	 * @param pere
	 */
	public void separateNode(Node father) {
		ArrayList<Node> sonList = new ArrayList<Node>();
		
		for (int i = 0; i < father.getEligibleList().size(); i++) {
			Node son = new Node(data, energeticLB, sumPj, earliestDate, latestDate);
			son.setPlacedJobList(father.getPlacedJobList());
			son.setPlacedJobListBeginDate(father.getPlacedJobListBeginDate());
			son.setPlacedJobListFinalDate(father.getPlacedJobListFinalDate());
			son.setRessourcesConsoInstant(father.getRessourcesConsoInstant());
			son.addJobInPJL(father.getEligibleList().get(i));
			sonList.add(son);
		}
		Collections.sort(sonList, new Comparator<Node>() {
			@Override
			public int compare(Node node1, Node node2) {
				return -Integer.compare(node1.getLB(), node2.getLB());
			}
		});
		
		while(!sonList.isEmpty()) {
			nodeHeap.add(sonList.get(0));
			sonList.remove(0);
		}
	}

	/**
	 * PSE
	 * 
	 */
	public void processing() {
		RCPSPLB lb = new RCPSPLB(data);
		energeticLB = lb.calculerBIEnergetiqueAmeliore();
		earliestDate = lb.getR();
		latestDate = lb.getD();
		System.out.println("Borne inf : "+energeticLB);
		
		// Initialisation of the first node
		Node firstNode = new Node(data, energeticLB, sumPj, earliestDate, latestDate);
		firstNode.addJobInPJL(0);
		nodeHeap.add(firstNode);
		int numberNode = 0;
		
		while (!nodeHeap.isEmpty()) {
			Node currentNode = new Node();
			// Deap
			/*currentNode = nodeHeap.get(nodeHeap.size() - 1);
			nodeHeap.remove(nodeHeap.size() - 1);*/
			
			// Progressive
			currentNode = nodeHeap.get(nodeHeap.size()-1);
			nodeHeap.remove(nodeHeap.size()-1);
			
			if (currentNode.getPlacedJobList().size() == data.nbJobs
					&& currentNode.getCMax() < Communication.UB) {
				bestNode = currentNode;
				Communication.newResultForDisplay = true;
				Communication.newIsAG = false;
				Communication.modifierUB(currentNode.getCMax(), "PSE");
				Individu individu = new Individu(data);
				individu.setSequence(currentNode.getPlacedJobList());
				individu.evaluation();
				System.out.println(currentNode.getPlacedJobList());
				System.out.println(currentNode.getCMax());
			} else if (currentNode.getLB() < Communication.UB) {
				separateNode(currentNode);
				if (numberNode % 10000 == 0) {
					System.out.println("Nombre noeuds explorés: " + numberNode);
					System.out.println("Nombre noeuds dans la pile: " + nodeHeap.size());
					System.out.println("-------------------------------------------------------------");
				}
				numberNode++;
			}
		}
		System.out.println("PSE is over! Nombre de noeuds explorés : "+numberNode);
	}

	public void run() {
		processing();
	}

}
