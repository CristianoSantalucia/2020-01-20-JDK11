package it.polito.tdp.artsmia.model;

import java.util.*; 
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.*;

import it.polito.tdp.artsmia.db.Adiacenza;
import it.polito.tdp.artsmia.db.ArtsmiaDAO;

public class Model
{
	private ArtsmiaDAO dao;
	private Map<Integer, Artist> vertici; 
	private Graph<Artist, DefaultWeightedEdge> grafo; 

	public Model()
	{
		this.dao = new ArtsmiaDAO();
	}

	public List<String> getRuoli()
	{
		return dao.getRoles();
	}

	public void creaGrafo(String ruolo)
	{
		// ripulisco mappa e grafo
		this.vertici = new HashMap<>(); 
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class); // 

		/// vertici 
		this.dao.getVertici(vertici, ruolo);  
		Graphs.addAllVertices(this.grafo, this.vertici.values()); 

		/// archi
		adiacenze = new ArrayList<>(this.dao.getAdiacenze(vertici));
		for (Adiacenza a : adiacenze)
		{ 
			Graphs.addEdge(this.grafo, a.getA1(), a.getA2(), a.getPeso());
		}
	}

	List<Adiacenza> adiacenze ; 
	public String getConnessioni()
	{
		List<Adiacenza> list = new ArrayList<>(this.adiacenze);
		list.sort((a1,a2)->-(a1.getPeso().compareTo(a2.getPeso())));
		String s = ""; 
		for (Adiacenza a : list)
			s += "\n" + a; 
		return s; 
	}

	public int getNumVertici()
	{
		return this.grafo.vertexSet().size();
	}
	public int getNumArchi()
	{
		return this.grafo.edgeSet().size();
	}
	public Collection<Artist> getVertici()
	{
		return this.grafo.vertexSet();
	}
	public Collection<DefaultWeightedEdge> getArchi()
	{
		return this.grafo.edgeSet();
	}

	//ricorsione 

	List<Artist> percorso; 
	Artist partenza;
	Integer numEsposizioni; 
	public String percorso(Integer idArtist)
	{
		numEsposizioni = 0;
		
		partenza = this.vertici.get(idArtist); 
		if (partenza == null)
			throw new IllegalArgumentException(); 

		percorso = new ArrayList<>();
		List<Artist> parziale = new ArrayList<>();
		parziale.add(partenza); 
		this.cerca(parziale); 

//		System.out.println(this.percorso);
//		System.out.println(this.numEsposizioni);
		return String.format("\n***PERCORSO: \n%s \nNUMERO ESIBIZIONI: %d", this.percorso.toString(), this.numEsposizioni); 
	}


	private void cerca(List<Artist> parziale)
	{
		//stato terminale: controllo chi sia il best
		if(parziale.size()>this.percorso.size())
		{
			this.percorso = new ArrayList<>(parziale); 
		}

		for(Artist a : this.grafo.vertexSet())
		{
			Artist ultimo = parziale.get(parziale.size()-1);
			Integer pesoArcoPotenziale = this.getPeso(ultimo, a);

			if (pesoArcoPotenziale != null)
			{
				//stato iniziale: posso aggiungere un qualunque vertice
				if(ultimo.equals(this.partenza)) 
				{
					parziale.add(a); 
					this.cerca(parziale);
					parziale.remove(a);
				}
				//stato intermedio: posso aggiungere solo vertici di archi con lo stesso peso
				else 
				{
					Artist penultimo = parziale.get(parziale.size()-2);
					Integer pesoUltimo = this.getPeso(penultimo, ultimo);
					if(pesoArcoPotenziale == pesoUltimo)
					{
						parziale.add(a); 
						this.cerca(parziale);
						parziale.remove(a);
					}
				}
			}
			else return; 
		}
	}

	private Integer getPeso(Artist a1, Artist a2)
	{
		DefaultWeightedEdge e = this.grafo.getEdge(a1, a2);
		if (e != null)
		{
			Integer peso = (int) this.grafo.getEdgeWeight(e); 
			this.numEsposizioni += peso;
			return peso;
		}
		else return null;
	} 
}
