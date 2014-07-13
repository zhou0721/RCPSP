package polytech.di5.lo.affichage;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JPanel;

import polytech.di5.lo.donnees.Donnees;

public class Panneau extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int nbJobs;
	private int nbTypesRessources;
	private ArrayList<Integer> nbRessources;
	private ArrayList<Integer> datesDebut;
	private ArrayList<Integer> dureesJob;
	private ArrayList<ArrayList<Integer>> besoinsRessources;
	private ArrayList<ArrayList<ArrayList<Integer>>> unitesDisponibles;
	private int cmax;
	private ArrayList<Color> couleurs;
	private int espace = 20;
	
	public Panneau(Donnees donnees, int ub) {
		nbJobs = donnees.nbJobs;
		nbTypesRessources = donnees.nbTypesRessources;
		nbRessources = donnees.nbRessources;
		dureesJob = donnees.dureesJobs;
		besoinsRessources = donnees.besoinsRessources;
		datesDebut = donnees.datesDebutTachesPlacees;
		cmax = ub;
		
		couleurs = new ArrayList<Color>();
		
		couleurs.add(new Color(255, 102, 102));
		couleurs.add(new Color(255, 178, 102));
		couleurs.add(new Color(255, 255, 102));
		couleurs.add(new Color(178, 255, 102));
		couleurs.add(new Color(102, 255, 102));
		couleurs.add(new Color(102, 178, 255));
		couleurs.add(new Color(102, 102, 255));
		couleurs.add(new Color(178, 102, 255));
		couleurs.add(new Color(255, 102, 255));
		couleurs.add(new Color(255, 102, 178));
		couleurs.add(new Color(255,76,15));
		couleurs.add(new Color(255,255,0));
		couleurs.add(new Color(12,255,29));
		couleurs.add(new Color(178,178,178));
		couleurs.add(new Color(255,50,255));
		couleurs.add(new Color(49,181,115));
		couleurs.add(new Color(200,164,109));
	}
	
	private int compterRessources() {
		int cmp = 0;
		for(int numType = 0; numType < nbTypesRessources; numType++) {
			cmp += nbRessources.get(numType);
		}
		return cmp;
	}
	
	private void initialiserUnitesDisponibles() {
		unitesDisponibles = new ArrayList<ArrayList<ArrayList<Integer>>>();
		for(int numType = 0; numType < nbTypesRessources; numType++) {
			ArrayList<ArrayList<Integer>> j = new ArrayList<ArrayList<Integer>>();
			for(int t = 0; t < cmax; t++) {
				ArrayList<Integer> k = new ArrayList<Integer>();
				for(int numUnite = 0; numUnite < nbRessources.get(numType); numUnite++) {
					k.add(1);
				}
				j.add(k);
			}
			unitesDisponibles.add(j);
		}
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		int y = espace;
		int unite_ressource = (this.getHeight() - (nbTypesRessources + 1) * espace - nbTypesRessources * 2) / compterRessources();
		int unite_temps = (this.getWidth() - 60) / cmax;
		initialiserUnitesDisponibles();
		Font font = new Font("Verdana", Font.BOLD, 10);
		g2.setFont(font);
		
		for(int numType = 0; numType < nbTypesRessources; numType++) {
			g2.drawString("R" + (numType + 1), 10, y + (nbRessources.get(numType) * unite_ressource) / 2 + 5);
			for(int r = 0; r < nbRessources.get(numType); r++) {
				g2.drawLine(35, y, 40, y);
				g2.drawLine(40, y, 40, y + unite_ressource);
				y += unite_ressource;
			}
			int x = 40;
			for(int t = 0; t < cmax; t++) {
				g2.drawLine(x, y, x + unite_temps, y);
				g2.drawLine(x + unite_temps, y, x + unite_temps, y + 2);
				for(int numJob = 0; numJob < nbJobs; numJob++) {
					if(datesDebut.get(numJob) == t) {
						int unitesRestantes = besoinsRessources.get(numJob).get(numType);
						if(unitesRestantes != 0) {
							g2.drawString("" + t, x - 5, y + 15);
							// boucle inf ici
							while(unitesRestantes != 0) {
								for(int numUnite = 0; numUnite < nbRessources.get(numType); numUnite++) {
									int nbUnitesDisponibles = 0;
									for(int i = numUnite; i < nbRessources.get(numType); i++) {
										if(unitesDisponibles.get(numType).get(t).get(i) == 1) {
											nbUnitesDisponibles++;
											for(int j = 0; j < dureesJob.get(numJob); j++) {
												if(t+j < cmax)
													unitesDisponibles.get(numType).get(t + j).set(i, 0);
											}
											if(nbUnitesDisponibles == unitesRestantes) break;
										}
										else break;
									}
									if(nbUnitesDisponibles != 0) {
										int hauteur = nbUnitesDisponibles * unite_ressource;
										int largeur = dureesJob.get(numJob) * unite_temps;
										g2.setColor(couleurs.get(numJob%couleurs.size()));
										g2.fillRect(x + 1, y - hauteur - (numUnite * unite_ressource), largeur, hauteur);
										g2.setColor(Color.BLACK);
										g2.drawString("J" + numJob, x + largeur/2 - 5, y - hauteur/2 + 5 - (numUnite * unite_ressource));
										g2.drawString("" + (t + dureesJob.get(numJob)), x + largeur - 5, y + 15);
										unitesRestantes -= nbUnitesDisponibles;
										if(unitesRestantes == 0) break;
									}
								}
							}
						}
					}
				}
				x += unite_temps;
			}
			y += espace;
		}
	}
}
