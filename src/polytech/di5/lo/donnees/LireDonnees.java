package polytech.di5.lo.donnees;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class LireDonnees extends Donnees {
	
	public LireDonnees(String nomFichier){
		dureesJobs = new ArrayList<Integer>();
		nbRessources = new ArrayList<Integer>();
		tableauSuccesseurs = new ArrayList<ArrayList<Integer>>();
		tableauPredecesseurs = new ArrayList<ArrayList<Integer>>();
		besoinsRessources = new ArrayList<ArrayList<Integer>>();
		
		LireFichier(nomFichier);
	}
	
	private boolean LireFichier(String nomFichier) {
		BufferedReader br;
		try {	
			br = new BufferedReader(new InputStreamReader(new FileInputStream(nomFichier)));
			
			if(!lireNbJobs(br)) {
				br.close();
				JOptionPane.showMessageDialog(null, "Fichier erroné", "Erreur", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			if(!lireNbRessources(br)) {
				br.close();
				JOptionPane.showMessageDialog(null, "Fichier erroné", "Erreur", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			if(!lireSuccesseurs(br)) {
				br.close();
				JOptionPane.showMessageDialog(null, "Fichier erroné", "Erreur", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			if(!lireBesoins(br)) {
				br.close();
				JOptionPane.showMessageDialog(null, "Fichier erroné", "Erreur", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			if(!lireNbRessourcesMax(br)) {
				br.close();
				JOptionPane.showMessageDialog(null, "Fichier erroné", "Erreur", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			br.close();
			
			chercherPredecesseurs();
			
			return true;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, "Fichier non trouvé", "Erreur", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	private boolean lireNbJobs(BufferedReader br){
		String ligne;
		try {
			while((ligne = br.readLine()) != null) {
				if(ligne.startsWith("jobs (incl. supersource/sink )")) {
					String[] tmp = ligne.split(":");
					
					nbJobs = Integer.parseInt(tmp[1].replaceAll(" ", ""));
					
					return true;
				}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	private boolean lireNbRessources(BufferedReader br){
		String ligne;
		try {
			while((ligne = br.readLine()) != null) {
				if(ligne.startsWith("  - renewable")) {
					String[] tmp = ligne.split(":");
					
					nbTypesRessources = Integer.parseInt(tmp[1].replaceAll("R", "").replaceAll(" ", ""));
					
					return true;
				}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	private boolean lireSuccesseurs(BufferedReader br){
		String ligne;
		try {
			while((ligne = br.readLine()) != null) {
				if(ligne.startsWith("PRECEDENCE RELATIONS:")) {
					br.readLine();
					
					for(int numJob = 0; numJob < nbJobs; numJob++) {
						ligne = br.readLine();
						ligne = ligne.trim().replaceAll("( )+", " ");
						String[] tmp = ligne.split(" ");
						
						int nbSuccesseurs = Integer.parseInt(tmp[2]);
						ArrayList<Integer> successeurs = new ArrayList<Integer>();
						for(int numSuccesseur = 0; numSuccesseur < nbSuccesseurs; numSuccesseur++) {
							int successeur = Integer.parseInt(tmp[3 + numSuccesseur]);
							successeurs.add(successeur-1);
						}
						tableauSuccesseurs.add(successeurs);
					}
					
					return true;
				}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	private void chercherPredecesseurs(){
		for(int numSucc = 0; numSucc < nbJobs; numSucc++) {
			ArrayList<Integer> predecesseurs = new ArrayList<Integer>();
			for(int numJob = 0; numJob < nbJobs; numJob++) {
				if(tableauSuccesseurs.get(numJob).contains(numSucc)) {
					predecesseurs.add(numJob);
				}
			}
			tableauPredecesseurs.add(predecesseurs);
		}
	}
	
	private boolean lireBesoins(BufferedReader br){
		String ligne;
		try {
			while((ligne = br.readLine()) != null) {
				if(ligne.startsWith("REQUESTS/DURATIONS:")) {
					br.readLine();
					br.readLine();
					
					for(int numJob = 0; numJob < nbJobs; numJob++) {
						ligne = br.readLine();
						ligne = ligne.trim().replaceAll("( )+", " ");
						String[] tmp = ligne.split(" ");
						
						dureesJobs.add(Integer.parseInt(tmp[2]));
						ArrayList<Integer> besoins = new ArrayList<Integer>();
						for(int numType = 0; numType < nbTypesRessources; numType++) {
							int besoin = Integer.parseInt(tmp[3 + numType]);
							besoins.add(besoin);
						}
						besoinsRessources.add(besoins);
					}
					
					return true;
				}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	private boolean lireNbRessourcesMax(BufferedReader br){
		String ligne;
		try {
			while((ligne = br.readLine()) != null) {
				if(ligne.startsWith("RESOURCEAVAILABILITIES:")) {
					br.readLine();
					ligne = br.readLine();
					ligne = ligne.trim().replaceAll("( )+", " ");
					String[] tmp = ligne.split(" ");
					
					for(int numType = 0; numType < nbTypesRessources; numType++) {
						int nbMax = Integer.parseInt(tmp[numType]);
						nbRessources.add(nbMax);
					}
					
					return true;
				}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
}
