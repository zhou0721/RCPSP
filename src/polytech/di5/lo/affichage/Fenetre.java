package polytech.di5.lo.affichage;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import polytech.di5.lo.ag.Genetic;
import polytech.di5.lo.ag.Individu;
import polytech.di5.lo.communication.Communication;
import polytech.di5.lo.donnees.Donnees;
import polytech.di5.lo.donnees.LireDonnees;
import polytech.di5.lo.pse.BranchAndBound;

public class Fenetre extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contenu;
	private JMenuBar menu = new JMenuBar();
	private JMenu itemFichier = new JMenu("Fichier");
	private JMenuItem itemOuvrir = new JMenuItem("Ouvrir un fichier");
	private JMenuItem itemQuitter = new JMenuItem("Quitter");
	public Thread tCommunication;
	public String fichier;
	public Donnees donnees;
	
	public Fenetre(){
		this.setTitle("RCPSP");
		this.setExtendedState(Frame.MAXIMIZED_BOTH);
		this.setMinimumSize(new Dimension(1024, 768));
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.itemFichier.add(itemOuvrir);
		this.itemFichier.addSeparator();
		this.itemFichier.add(itemQuitter);
		this.menu.add(itemFichier);
		this.setJMenuBar(menu);
		
		contenu = (JPanel)this.getContentPane();
		this.setVisible(true);
		
		
		itemOuvrir.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				//String fichier = new String("test.sm");
			    fichier = JOptionPane.showInputDialog(null, "Fichier de données", "Ouvrir un fichier", JOptionPane.QUESTION_MESSAGE);
			    if(fichier != null && fichier.length() != 0) {
			    	donnees = new LireDonnees(fichier);
			    	
			    	tCommunication = new Thread(new Communication(donnees));
					tCommunication.start();
			    }
			}
		});
		
		itemQuitter.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				int choix = JOptionPane.showConfirmDialog(null, "Quitter l'application?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(choix == JOptionPane.OK_OPTION){
					System.exit(0);
				}
			}        
		});
		
		while(!Communication.pseIsOver)
    	{
    		try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		if(Communication.newResultForDisplay){
    			contenu.removeAll();
    			Panneau panneau;
		    	if(Communication.newIsAG) {
    				donnees.datesDebutTachesPlacees = Genetic.bestIndividual.getDateDebut();
    				panneau = new Panneau(donnees, Genetic.bestIndividual.getcMax());
    			} else {
    				donnees.datesDebutTachesPlacees = BranchAndBound.bestNode.getPlacedJobListBeginDate();
    				panneau = new Panneau(donnees, BranchAndBound.bestNode.getCMax());
    		    }
    			contenu.add(panneau);
		    	contenu.revalidate();
		    	Communication.newResultForDisplay = false;
		    	Communication.newIsAG = false;
    		}	
    	}
		
    	try {
			tCommunication.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
