package br.com.snapsales.models;

/**
 *
 * @author auxiliar.tirs
 */
public class Categoria {
    private long id;
    private String nomeCategoria;

    public Categoria(long id, String nomeCategoria) {
        this.id = id;
        this.nomeCategoria = nomeCategoria;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNomeCategoria() {
        return nomeCategoria;
    }

    public void setNomeCategoria(String nomeCategoria) {
        this.nomeCategoria = nomeCategoria;
    }
    
}
