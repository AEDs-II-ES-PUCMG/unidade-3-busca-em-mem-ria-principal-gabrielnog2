import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class ABB<K, V> implements IMapeamento<K, V>{

    private No<K, V> raiz; // referência à raiz da árvore.
    private Comparator<K> comparador; //comparador empregado para definir "menores" e "maiores".
    private int tamanho;
    private long comparacoes;
    private long inicio;
    private long termino;
    
    /**
     * Método auxiliar para inicialização da árvore binária de busca.
     * * Este método define a raiz da árvore como {@code null} e seu tamanho como 0.
     * Utiliza o comparador fornecido para definir a organização dos elementos na árvore.
     * @param comparador o comparador para organizar os elementos da árvore.
     */
    private void init(Comparator<K> comparador) {
        raiz = null;
        tamanho = 0;
        this.comparador = comparador;
    }

    /**
     * Construtor da classe.
     * O comparador padrão de ordem natural será utilizado.
     */ 
    @SuppressWarnings("unchecked")
    public ABB() {
        init((Comparator<K>) Comparator.naturalOrder());
    }

    /**
     * Construtor da classe.
     * Esse construtor cria uma nova árvore binária de busca vazia.
     * * @param comparador o comparador a ser utilizado para organizar os elementos da árvore.  
     */
    public ABB(Comparator<K> comparador) {
        init(comparador);
    }

    /**
     * Construtor da classe.
     * Esse construtor cria uma nova árvore binária de busca a partir de uma outra árvore binária de busca,
     * com os mesmos itens, mas usando uma nova chave.
     * @param original a árvore binária de busca original.
     * @param funcaoChave a função que irá extrair a nova chave de cada item para a nova árvore.
     */
    @SuppressWarnings("unchecked")
    public ABB(ABB<?, V> original, Function<V, K> funcaoChave) {
        ABB<K, V> nova = new ABB<>();
        nova = copiarArvore(original.raiz, funcaoChave, nova);
        this.raiz = nova.raiz;
        this.comparador = (Comparator<K>) Comparator.naturalOrder();
    }
    
    /**
     * Recursivamente, copia os elementos da árvore original para esta, num processo análogo ao caminhamento em ordem.
     * @param <T> Tipo da nova chave.
     * @param raizArvore raiz da árvore original que será copiada.
     * @param funcaoChave função extratora da nova chave para cada item da árvore.
     * @param novaArvore Nova árvore. Parâmetro usado para permitir o retorno da recursividade.
     * @return A nova árvore com os itens copiados e usando a chave indicada pela função extratora.
     */
    private <T> ABB<T, V> copiarArvore(No<?, V> raizArvore, Function<V, T> funcaoChave, ABB<T, V> novaArvore) {
        
        if (raizArvore != null) {
            novaArvore = copiarArvore(raizArvore.getEsquerda(), funcaoChave, novaArvore);
            V item = raizArvore.getItem();
            T chave = funcaoChave.apply(item);
            novaArvore.inserir(chave, item);
            novaArvore = copiarArvore(raizArvore.getDireita(), funcaoChave, novaArvore);
        }
        return novaArvore;
    }
    
    /**
     * Método booleano que indica se a árvore está vazia ou não.
     * @return
     * verdadeiro: se a raiz da árvore for null, o que significa que a árvore está vazia.
     * falso: se a raiz da árvore não for null, o que significa que a árvore não está vazia.
     */
    public Boolean vazia() {
        return (this.raiz == null);
    }
    
    @Override
    /**
     * Método que encapsula a pesquisa recursiva de itens na árvore.
     * @param chave a chave do item que será pesquisado na árvore.
     * @return o valor associado à chave.
     */
    public V pesquisar(K chave) {
        comparacoes = 0;
        inicio = System.nanoTime();
        V procurado = pesquisar(raiz, chave);
        termino = System.nanoTime();
        return procurado;
    }
    
    private V pesquisar(No<K, V> raizArvore, K procurado) {
        
        int comparacao;
        
        comparacoes++;
        if (raizArvore == null)
            /// Se a raiz da árvore ou sub-árvore for null, a árvore/sub-árvore está vazia e então o item não foi encontrado.
            throw new NoSuchElementException("O item não foi localizado na árvore!");
        
        comparacao = comparador.compare(procurado, raizArvore.getChave());
        
        if (comparacao == 0)
            /// O item procurado foi encontrado.
            return raizArvore.getItem();
        else if (comparacao < 0)
            /// Se o item procurado for menor do que o item armazenado na raiz da árvore:
            /// pesquise esse item na sub-árvore esquerda.    
            return pesquisar(raizArvore.getEsquerda(), procurado);
        else
            /// Se o item procurado for maior do que o item armazenado na raiz da árvore:
            /// pesquise esse item na sub-árvore direita.
            return pesquisar(raizArvore.getDireita(), procurado);
    }
    
    @Override
    /**
     * Método que encapsula a adição recursiva de itens à árvore, associando-o à chave fornecida.
     * @param chave a chave associada ao item que será inserido na árvore.
     * @param item o item que será inserido na árvore.
     * * @return o tamanho atualizado da árvore após a execução da operação de inserção.
     */
    public int inserir(K chave, V item) {
        // TODO
        return tamanho;
    }

    @Override 
    public String toString(){
        return percorrer();
    }

    @Override
    public String percorrer() {
        // TODO
        return null;
    }

    @Override
    /**
     * Método que encapsula a remoção recursiva de um item da árvore.
     * @param chave a chave do item que deverá ser localizado e removido da árvore.
     * @return o valor associado ao item removido.
     */
    public V remover(K chave) {
        // TODO
        return null;
    }

    
    public Lista<V> recortar(K chaveDeOnde, K chaveAteOnde) {
        Lista<V> resultado = new Lista<>();
        recortar(raiz, chaveDeOnde, chaveAteOnde, resultado);
        return resultado;
    }

    private void recortar(No<K, V> no, K chaveDeOnde, K chaveAteOnde, Lista<V> lista) {
        if (no == null) {
            return;
        }

        int compDeOnde = comparador.compare(chaveDeOnde, no.getChave());
        int compAteOnde = comparador.compare(chaveAteOnde, no.getChave());

        if (compDeOnde < 0) {
            recortar(no.getEsquerda(), chaveDeOnde, chaveAteOnde, lista);
        }

        if (compDeOnde <= 0 && compAteOnde >= 0) {
            lista.adicionar(no.getItem());
        }

        if (compAteOnde > 0) {
            recortar(no.getDireita(), chaveDeOnde, chaveAteOnde, lista);
        }
    }

    @Override
    public int tamanho() {
        return tamanho;
    }
    
    @Override
    public long getComparacoes() {
        return comparacoes;
    }

    @Override
    public double getTempo() {
        return (termino - inicio) / 1_000_000;
    }
}