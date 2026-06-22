import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Function;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class App {

    /**
     * Nome do arquivo de dados. O arquivo deve estar localizado na raiz do projeto
     */
    static String nomeArquivoDados;

    /** Scanner para leitura de dados do teclado */
    static Scanner teclado;

    /** Quantidade de produtos cadastrados atualmente na lista */
    static int quantosProdutos = 0;

    static ABB<Integer, Produto> produtosPorId;
    static ABB<String, Produto> produtosPorNome;
    static TabelaHash<Produto, Lista<Pedido>> pedidosPorProduto;

    /** Limpa o buffer do console, simulando uma limpeza de tela num terminal vt-100 */
    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /** Gera um efeito de pausa na CLI. Espera por um enter para continuar */
    static void pausa() {
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

    /** Cabeçalho principal da CLI do sistema */
    static void cabecalho() {
        limparTela();
        System.out.println("AEDs II COMÉRCIO DE COISINHAS");
        System.out.println("=============================");
    }

    /**
     * Método genérico para ler dados numéricos do teclado
     * @param <T> A classe de retorno (tipicamente int ou double)
     * @param mensagem Mensagem a ser exibida na leitura
     * @param classe Classe do tipo T para uso na reflaxão Java
     * @return Um valor numérico da classe desejada
     */
    static <T extends Number> T lerOpcao(String mensagem, Class<T> classe) {

        T valor;

        System.out.println(mensagem);
        try {
            valor = classe.getConstructor(String.class).newInstance(teclado.nextLine());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
        return valor;
    }

    /**
     * Imprime o menu principal, lê a opção do usuário e a retorna (int).
     * Perceba que poderia haver uma melhor modularização com a criação de uma
     * classe Menu.
     * 
     * @return Um inteiro com a opção do usuário.
     */
    static int menu() {
        cabecalho();
        System.out.println("1 - Procurar produtos, por id");
        System.out.println("2 - Recortar produtos, por descrição");
        System.out.println("3 - Pedidos de um produto, em arquivo");
        System.out.println("0 - Sair");
        System.out.print("Digite sua opção: ");
        return Integer.parseInt(teclado.nextLine());
    }

    /**
     * Lê os dados de um arquivo-texto e retorna uma árvore de produtos.
     * Arquivo-texto no formato
     * N (quantidade de produtos) <br/>
     * tipo;descrição;preçoDeCusto;margemDeLucro;[dataDeValidade] <br/>
     * Deve haver uma linha para cada um dos produtos. Retorna uma árvore vazia em
     * caso de problemas com o arquivo.
     * 
     * @param nomeArquivoDados Nome do arquivo de dados a ser aberto.
     * @return Uma árvore com os produtos carregados, ou vazia em caso de problemas
     *         de leitura.
     */
    static <T> ABB<T, Produto> lerProdutos(String nomeArquivoDados,
            Function<Produto, T> extratorDeChave) {

        Scanner arquivo = null;
        int numProdutos;
        String linha;
        Produto produto;
        ABB<T, Produto> produtosCadastrados = new AVL<>();

        try {
            arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"));

            numProdutos = Integer.parseInt(arquivo.nextLine());
            produtosCadastrados = new AVL<>();

            for (int i = 0; i < numProdutos; i++) {
                linha = arquivo.nextLine();
                produto = Produto.criarDoTexto(linha);
                if (produto != null) {
                    T chave = extratorDeChave.apply(produto);
                    try {
                        produtosCadastrados.inserir(chave, produto);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Aviso: Produto duplicado ignorado - " + e.getMessage());
                    }
                }
            }
            quantosProdutos = produtosCadastrados.tamanho();

        } catch (IOException excecaoArquivo) {
            System.err.println("Erro ao ler arquivo: " + excecaoArquivo.getMessage());
            produtosCadastrados = new AVL<>();
        } catch (NumberFormatException e) {
            System.err.println("Erro de formato no arquivo: " + e.getMessage());
            produtosCadastrados = new AVL<>();
        } finally {
            if (arquivo != null) {
                arquivo.close();
            }
        }

        return produtosCadastrados;
    }

    /**
     * Localiza um produto na árvore de produtos organizados por id, a partir do
     * código de produto informado pelo usuário, e o retorna.
     * Em caso de não encontrar o produto, retorna null
     */
    static Produto localizarProdutoID() {
        cabecalho();
        System.out.println("LOCALIZANDO POR ID");
        int ID = lerOpcao("Digite o ID para busca", Integer.class);
        Produto localizado = localizarProduto(produtosPorId, ID);
        mostrarProduto(localizado);
        return localizado;
    }

    static <K> Produto localizarProduto(ABB<K, Produto> produtosCadastrados, K chave) {
        cabecalho();
        try {
            Produto localizado = produtosCadastrados.pesquisar(chave);
            System.out.println("Tempo: " + produtosCadastrados.getTempo());
            System.out.println("Comparações: " + produtosCadastrados.getComparacoes());
            pausa();
            return localizado;
        } catch (NoSuchElementException e) {
            System.out.println("Produto não encontrado na árvore.");
            System.out.println("Tempo: " + produtosCadastrados.getTempo());
            System.out.println("Comparações: " + produtosCadastrados.getComparacoes());
            pausa();
            return null;
        } catch (IllegalArgumentException e) {
            System.err.println("Erro na busca: " + e.getMessage());
            pausa();
            return null;
        }
    }

    private static void mostrarProduto(Produto produto) {

        cabecalho();
        String mensagem = "Dados inválidos para o produto!";

        if (produto != null) {
            mensagem = String.format("Dados do produto:\n%s", produto);
        }

        System.out.println(mensagem);
    }

    private static Lista<Pedido> gerarPedidos(int quantidade) {
        Lista<Pedido> pedidos = new Lista<>();
        Random sorteio = new Random(42);
        int quantProdutos;
        int pedidosGerados = 0;
        
        for (int i = 0; i < quantidade; i++) {
            try {
                Pedido ped = new Pedido();
                quantProdutos = sorteio.nextInt(8) + 1;
                boolean pedidoValido = true;
                
                for (int j = 0; j < quantProdutos; j++) {
                    try {
                        int id = sorteio.nextInt(7750) + 10_000;
                        Produto prod = produtosPorId.pesquisar(id);
                        ped.incluirProduto(prod);
                        inserirNaTabela(prod, ped);
                    } catch (NoSuchElementException e) {
                        // Produto não existe, continua para o próximo
                        pedidoValido = false;
                        break;
                    }
                }
                
                if (pedidoValido && ped.getItens().tamanho() > 0) {
                    pedidos.inserir(ped);
                    pedidosGerados++;
                }
            } catch (Exception e) {
                System.err.println("Erro ao gerar pedido " + i + ": " + e.getMessage());
            }
        }
        
        System.out.println("Total de pedidos gerados com sucesso: " + pedidosGerados + " de " + quantidade);
        return pedidos;
    }

    private static void inserirNaTabela(Produto produto, Pedido pedido) {
      
        if (produto == null || pedido == null) {
            System.err.println("Aviso: Tentativa de inserir produto ou pedido nulo na tabela.");
            return;
        }

        try {
            Lista<Pedido> pedidosDoProduto = pedidosPorProduto.pesquisar(produto);

            if (pedidosDoProduto != null) {
                if (!pedidosDoProduto.contarRepeticoes(p -> p.equals(pedido)) > 0) {
                    pedidosDoProduto.inserir(pedido);
                }
            }
        } catch (NoSuchElementException e) {
            Lista<Pedido> pedidosDoProduto = new Lista<>();
            pedidosDoProduto.inserir(pedido);
            try {
                pedidosPorProduto.inserir(produto, pedidosDoProduto);
            } catch (IllegalArgumentException ex) {
                System.err.println("Aviso: Produto já existe na tabela de pedidos.");
            }
        }
    }

    private static void recortarArvore(ABB<String, Produto> arvore) {

        cabecalho();
        System.out.print("Digite ponto de início do filtro: ");
        String descIni = teclado.nextLine();
        System.out.print("Digite ponto de fim do filtro: ");
        String descFim = teclado.nextLine();

        System.out.println(arvore.recortar(descIni, descFim));
    }

   static void pedidosDoProduto() {
        Produto produto = localizarProdutoID();
        
        if (produto == null) {
            System.out.println("Operação cancelada: Produto não encontrado.");
            pausa();
            return;
        }

        Lista<Pedido> listaProd = null;
        try {
            listaProd = pedidosPorProduto.pesquisar(produto);
        } catch (NoSuchElementException e) {
            System.out.println("Não há pedidos registrados para este produto.");
            pausa();
            return;
        }
     
        if (listaProd == null || listaProd.tamanho() == 0) {
            System.out.println("Não há pedidos registrados para este produto.");
            pausa();
            return;
        }

        String nomeArquivo = "RelatorioProduto_" + produto.getId() + ".txt"; 

        try (FileWriter arquivoRelatorio = new FileWriter(nomeArquivo)) {
          
            arquivoRelatorio.write("Relatório de Pedidos do Produto ID: " + produto.getId() + "\n");
            arquivoRelatorio.write("Produto: " + produto.getDescricao() + "\n");
            arquivoRelatorio.write("==================================================\n\n");
            
            arquivoRelatorio.write(listaProd.toString() + "\n");
            
            System.out.println("Relatório gerado com sucesso! Dados salvos no arquivo: " + nomeArquivo);
        } catch (IOException e) {
            System.err.println("Problemas para criar ou gravar no arquivo " + nomeArquivo + ".");
            System.err.println("Detalhe do erro: " + e.getMessage());
        } finally {
            pausa();
        }
    }

    public static void main(String[] args) {
        teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        nomeArquivoDados = "produtos.txt";
        
        try {
            produtosPorId = lerProdutos(nomeArquivoDados, Produto::getId);
            if (produtosPorId == null || produtosPorId.tamanho() == 0) {
                System.err.println("Erro: Nenhum produto foi carregado. Encerrando...");
                teclado.close();
                return;
            }
            
            produtosPorNome = new AVL<>(produtosPorId, prod -> prod.getDescricao(), String::compareTo);
            pedidosPorProduto = new TabelaHash<>((int) (quantosProdutos * 1.25));
            gerarPedidos(25000);
        } catch (Exception e) {
            System.err.println("Erro durante inicialização: " + e.getMessage());
            teclado.close();
            return;
        }

        int opcao = -1;

        do {
            try {
                opcao = menu();
                switch (opcao) {
                    case 1 -> localizarProdutoID();
                    case 2 -> recortarArvore(produtosPorNome);
                    case 3 -> pedidosDoProduto();
                    case 0 -> System.out.println("Encerrando aplicação...");
                    default -> System.out.println("Opção inválida!");
                }
            } catch (Exception e) {
                System.err.println("Erro durante operação: " + e.getMessage());
            }
            if (opcao != 0) pausa();
        } while (opcao != 0);

        teclado.close();
    }
}
