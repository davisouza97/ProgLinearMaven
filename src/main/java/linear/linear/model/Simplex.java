package linear.linear.model;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan
public class Simplex {

    //variaveis de formatacao
    //private static final String fundoVerde = "\u001B[42m";
    //private static final String reset = "\u001B[0m";
    DecimalFormat df = new DecimalFormat("0E0");
    //variaveis de formatacao

    private int numeroLinhas, numeroColunas;
    private int linhaZ;
    public float[][] tabela; // tabela simplex
    float[] variaveisLinha;
    Variavel[] variaveisBasicas;

    public String situacao;
    public static float bigMValor = 99999f;
    
    public ArrayList<Variavel> listaVariaveisGlobal;

    public static enum TIPO {
        NAO_OTIMO,
        OTIMO,
        NAO_FINITO,
        MULTIPLO
    };

    public Simplex(float[][] dados, ArrayList<Variavel> listaVariaveisGlobal) {//numeroVariaveis = variaveis da funcao +numero de f + numero de a
        numeroLinhas = dados.length;
        linhaZ = numeroLinhas - 1;
        numeroColunas = dados[0].length;
        tabela = dados;
        this.listaVariaveisGlobal = listaVariaveisGlobal;

        variaveisBasicas = setVariaveisBasicas();
        for (int i = 0; i < numeroColunas; i++) {
            if (tabela[linhaZ][i] != bigMValor) {
                tabela[linhaZ][i] = tabela[numeroLinhas - 1][i] * -1;
            }
        }
        situacao = "tabela inicial";
    }

    public TIPO compute() {
        while (verificaBigM()) {
            tabela = bigM();
        }
        if (verificaOtimo()) {
            if (verificaMultiplasSolucoes()) {
                situacao = "Multiplas Soluções";
            	return TIPO.MULTIPLO;
            } else {
            	situacao = "Solução Otima";
            	return TIPO.OTIMO; // solução já esta otima
            }
        }
        int colunaPivo = encontraColunaPivo();
        System.out.println("ColunaPivo: " + listaVariaveisGlobal.get(colunaPivo).getNome() + "(" + colunaPivo + ")");

        int linhaPivo = encontarLinhaPivo(colunaPivo);
        System.out.println("Linha Pivo"+linhaPivo);
        if (linhaPivo == -1) {
        	situacao = "Não Finito";
            return TIPO.NAO_FINITO;
        }
        escalonar(linhaPivo, colunaPivo);
        situacao = "Não Otimo";
        return TIPO.NAO_OTIMO;
    }

    private void escalonar(int linhaPivo, int colunaPivo) {
        trocaVariavelBasica(linhaPivo, colunaPivo);
        float celulaPivo = tabela[linhaPivo][colunaPivo];
        float[] linhaPivoEscalonada = new float[numeroColunas];
        System.arraycopy(tabela[linhaPivo], 0, linhaPivoEscalonada, 0, numeroColunas);
        for (int i = 0; i < numeroColunas; i++) {
            linhaPivoEscalonada[i] = linhaPivoEscalonada[i] / celulaPivo;
        }
        for (int linha = 0; linha < numeroLinhas; linha++) {
            float[] linhaAnterior = new float[numeroColunas];
            System.arraycopy(tabela[linha], 0, linhaAnterior, 0, numeroColunas);
            if (linha != linhaPivo) {
                for (int coluna = 0; coluna < numeroColunas; coluna++) {
                    tabela[linha][coluna] = linhaAnterior[coluna] - linhaAnterior[colunaPivo] * linhaPivoEscalonada[coluna];
                }
            }
        }
        System.arraycopy(linhaPivoEscalonada, 0, tabela[linhaPivo], 0, numeroColunas);
    }

    private int encontarLinhaPivo(int colunaPivo) {
        int index = -1;
        float menor = Float.MAX_VALUE;
        for (int i = 0; i < numeroLinhas - 1; i++) {
            if (tabela[i][colunaPivo] > variaveisBasicas[i].getMaiorQue()) {
                if (tabela[i][numeroColunas - 1] / tabela[i][colunaPivo] < menor) {
                    index = i;
                    menor = tabela[i][numeroColunas - 1] / tabela[i][colunaPivo];
                }
            }
        }
        return index;
    }

    private int encontraColunaPivo() {
        float negativo = 0;
        int index = -1;
        for (int i = 0; i < numeroColunas - 2; i++) {
            if (tabela[linhaZ][i] < negativo) {
                negativo = tabela[linhaZ][i];
                index = i;
            }
        }
        return index;
    }

    // Procura valor negativo na linha Z
    public boolean verificaOtimo() {
        for (int i = 0; i < numeroColunas - 1; i++) {
            float val = tabela[linhaZ][i];
            if (val < 0) {
                return false;
            }
        }
        return true;
    }

    private boolean verificaMultiplasSolucoes() {
        for (int i = 0; i < tabela[linhaZ].length - 2; i++) {
            if (!verificaVariavelBasica(listaVariaveisGlobal.get(i).getNome())) {
                if (tabela[linhaZ][i] == 0) {
                    for (int j = 0; j < numeroLinhas - 1; j++) {
                        if (tabela[j][i] > 0) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean verificaVariavelBasica(String variavel) {
        for (int i = 0; i < variaveisBasicas.length; i++) {
            if (variaveisBasicas[i].getNome().equalsIgnoreCase(variavel)) {
                return true;
            }
        }
        return false;
    }

    private float[][] bigM() {
        int colunaBigM = -1;
        int linhaBigM = -1;
        for (int i = 0; i < tabela[0].length; i++) {
            if (tabela[linhaZ][i] == bigMValor) {
                colunaBigM = i;
            }
        }
        for (int i = 0; i < tabela.length; i++) {
            if (tabela[i][colunaBigM] == 1) {
                linhaBigM = i;
            }
        }
        for (int i = 0; i < tabela[0].length; i++) {
            tabela[linhaZ][i] = tabela[linhaZ][i] - bigMValor * tabela[linhaBigM][i];
        }
        return tabela;
    }

    private boolean verificaBigM() {
        for (int i = 0; i < tabela[0].length; i++) {
            if (tabela[linhaZ][i] == bigMValor) {
                return true;
            }
        }
        return false;
    }

    private Variavel[] setVariaveisBasicas() {
        Variavel[] varBasicas = new Variavel[tabela.length - 1];
        for (int i = 0; i < linhaZ; i++) {
            for (int j = varBasicas.length - 1; j < numeroColunas - 1; j++) {
                if (tabela[i][j] == 1) {
                    varBasicas[i] = listaVariaveisGlobal.get(j);
                }
            }
        }
        return varBasicas;
    }

    private void trocaVariavelBasica(int linhaPivo, int colunaPivo) {
        variaveisBasicas[linhaPivo] = listaVariaveisGlobal.get(colunaPivo);
    }
    
    public ArrayList<ArrayList<Float>> getTabelaArray() {
        ArrayList<ArrayList<Float>> tabelaResultado= new ArrayList<>();
        for (int i = 0; i < tabela.length; i++) {
        	ArrayList<Float> linha = new ArrayList<>();
        	for (int j = 0; j < tabela[i].length; j++) {
				linha.add(tabela[i][j]);
			}
        	tabelaResultado.add(linha);
        	
		}
        return tabelaResultado;
    }    
    
    public ArrayList<Variavel> getVariaveisBasicasArray() {
    	ArrayList<Variavel> resultado = new ArrayList<>();
    	for (int i = 0; i < variaveisBasicas.length; i++) {
			resultado.add(variaveisBasicas[i]);
		}
    	Variavel z = new Variavel();
    	z.setNome("Z");
    	resultado.add(z);
    	return resultado;
    	
    }
    
    public String toString() {
    	String resultado = "";
    	for (int i = 0; i < tabela.length; i++) {
			for (int j = 0; j < tabela[0].length; j++) {
				resultado +=tabela[i][j];
			}
			resultado+="\n";
		}
    	
    	
    	return resultado;
    }
}
