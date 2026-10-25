package br.com.snapsales.factories;

import br.com.snapsales.models.Clientes;
import br.com.snapsales.models.Encomenda;
import br.com.snapsales.models.Entrega;
import br.com.snapsales.models.ItemEncomenda;
import br.com.snapsales.models.Pagamentos;
import java.util.List;

/**
 *
 * @author Daniel Dutra
 */
public class EncomendaFactory {
    
    public static Encomenda criarEncomenda(
        long id,
        Clientes cliente,
        String status,
        double valorEntrada,
        boolean retirada,
        List<ItemEncomenda> itens,
        Pagamentos pagamento,
        Entrega entrega){
        
       //Cria encoemenda
       Encomenda encomenda = new Encomenda( id, status, cliente, valorEntrada, 0.0, retirada);
        
       //Associa outros objetos necessarios
       encomenda.setPagamento(pagamento);
       encomenda.setEntrega(entrega);
       encomenda.setItens(itens);
       
       //Calcula valor total
       double totalCalculo = 0.0;
       if(itens != null){
           for(ItemEncomenda item : itens){
              totalCalculo   += item.getPrecoMomento() * item.getQuantidadeItem();
           }
       }
       
       encomenda.setValorTotal(totalCalculo);

       //Vinculo bidirecional dos itens
       if(itens != null){
           for(ItemEncomenda item : itens){
               item.setEncomenda(encomenda);
           }
       }
       return encomenda;
    }
}
