package linear.linear.controller;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import linear.linear.model.Simplex;
import linear.linear.model.Variavel;

@Controller
public class LinearController {
	static Pattern patternVariavel = Pattern.compile("[+-]*\\d*[a-z]\\d*|[=]\\d*[a-z]?");
	static Pattern patternLetra = Pattern.compile("[a-z]");
	static Pattern patternValor = Pattern.compile("[+-]*[\\d]*");

	static ArrayList<Variavel> listaVariaveisGlobal = new ArrayList<Variavel>();
	static int numeroColunasGlobal;
	static int numeroLinhasGlobal = 0;

	static boolean minimizacao = true;

	boolean naoOtimo = false;

	Simplex simplex;

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ModelAndView form(@RequestParam(required = false) String zFinal, String saFinal, String nFinal) {

		ModelAndView mv = new ModelAndView("/result");

		//zFinal = zFinal != null ? "" : zFinal;
		//saFinal = saFinal != null ? "" : saFinal;
		//nFinal = nFinal != null ? "" : nFinal;
		
		
		zFinal ="max=3x + 2y ";
		saFinal = "x <= 4 ; 2y <= 12 ; 3x+2y<=18 ";
		nFinal = "x>=0;y>=0";
		float[][] tabelaInicial = preparaMontarTabela(zFinal, saFinal, nFinal);

		simplex = new Simplex(tabelaInicial, listaVariaveisGlobal);

		mv.addObject("simplex", simplex);

		return mv;
	}

//
	@RequestMapping(value = "/a")
	public ModelAndView escalonar() {

		ModelAndView mv = new ModelAndView("/result");

		simplex.compute();

		mv.addObject("simplex", simplex);
		return mv;
	}

	public static float[][] preparaMontarTabela(String objetiva, String restricoesSA, String nulidades) {
		// prepara a funcao objetiva e defina se é de min ou max
		objetiva = preparaObjetiva(objetiva);
		// prepara as Restricoes
		StringTokenizer restricoesSujeitoAToken = preparaRestricoes(restricoesSA);
		// prepara as nulidades
		StringTokenizer restricoesNulidadeToken = preparaRestricoes(nulidades);

		return MontarTabela(objetiva, restricoesSujeitoAToken, restricoesNulidadeToken);
	}

	private static String preparaObjetiva(String objetiva) {
		objetiva = objetiva.replaceAll(" ", "");
		objetiva = objetiva.toLowerCase();
		if (objetiva.contains("max")) {
			minimizacao = false;
		}
		objetiva = objetiva.substring(4);// retirar o "max="
		if (minimizacao) {
			System.out.println("O problema é de minimização");
		} else {
			System.out.println("O problema é de maximização");
		}
		return objetiva;
	}

	private static StringTokenizer preparaRestricoes(String restricoes) {
		restricoes = restricoes.replaceAll(" ", "");
		StringTokenizer listaRestricoesToken = new StringTokenizer(restricoes, ";");
		return listaRestricoesToken;
	}

	private static float[][] MontarTabela(String objetiva, StringTokenizer listaRestricoesToken,
			StringTokenizer restricoesNulidadeToken) {
		float[] linhaZ = montaLinhaZ(objetiva);
		listaRestricoesToken = new StringTokenizer(ajustaRestricao(listaRestricoesToken), ";");
		int quantidadeRestricoes = listaRestricoesToken.countTokens();
		float[][] tabela = new float[numeroLinhasGlobal + 1][numeroColunasGlobal + 1];
		for (int i = 0; i < quantidadeRestricoes; i++) {
			float[] restricao = montaLinhaRestricao(listaRestricoesToken.nextToken());
			System.arraycopy(restricao, 0, tabela[i], 0, restricao.length);
		}

		System.arraycopy(linhaZ, 0, tabela[tabela.length - 1], 0, linhaZ.length);// copia a linha z que já ta pronta
																					// para a ultima linha da matriz
		for (int i = 0; i < listaVariaveisGlobal.size(); i++) {
			if (Pattern.matches("[a]\\d*", listaVariaveisGlobal.get(i).getNome()) == true) {
				tabela[numeroLinhasGlobal][i] = Simplex.bigMValor;
			}
		}
		setNulidades(restricoesNulidadeToken);
		return tabela;
	}

	private static float[] montaLinhaZ(String objetiva) {
		Matcher m = getVariaveisByRegex(objetiva);
		ArrayList<Float> linhaObjetiva = new ArrayList<>();
		while (m.find()) {
			// System.out.println(m.group());
			Variavel v = new Variavel();
			String nomeVariavel = m.group().substring(m.group().length() - 1);
			String valorVariavel = m.group().replaceAll(nomeVariavel, "");
			if (valorVariavel.equalsIgnoreCase("+") || valorVariavel.equalsIgnoreCase("")) {
				valorVariavel = "1";
			}
			if (valorVariavel.equalsIgnoreCase("-")) {
				valorVariavel = "-1";
			}
			v.setNome(nomeVariavel);
			listaVariaveisGlobal.add(v);
			linhaObjetiva.add(Float.parseFloat(valorVariavel));
			numeroColunasGlobal++;
		}
		float[] linhaObjetivaVetor = new float[linhaObjetiva.size()];
		for (int i = 0; i < linhaObjetiva.size(); i++) {
			linhaObjetivaVetor[i] = linhaObjetiva.get(i);
		}

		return linhaObjetivaVetor;
	}

	private static float[] montaLinhaRestricao(String restricao) {
		Matcher matcherVariaveis = getVariaveisByRegex(restricao);// todas as variaveis e a igualdade
		float[] linhaRestricao = new float[numeroColunasGlobal + 1];
		ArrayList<String> variaveisRestricao = new ArrayList<>();
		while (matcherVariaveis.find()) {
			variaveisRestricao.add(matcherVariaveis.group());
		}

		for (int i = 0; i < variaveisRestricao.size(); i++) {
			String interacao = variaveisRestricao.get(i);
			if (Pattern.matches("[=]{1}\\d*", variaveisRestricao.get(i)) == true) {// lado direito
				interacao = variaveisRestricao.get(i).replaceAll("=", "");
				linhaRestricao[numeroColunasGlobal] = Float.parseFloat(interacao);
			} else {
				String nomeVariavel;
				String valorVariavel;
				if (Pattern.matches("[+-][af][\\d]", interacao)) { // variavel a ou f
					nomeVariavel = interacao.replaceAll("[+-]", "");
					valorVariavel = "1";
				} else {
					nomeVariavel = interacao.replaceAll("[0-9]|[+-]", "");
					valorVariavel = interacao.replaceAll(nomeVariavel, "");
				}
				if (valorVariavel.equalsIgnoreCase("+") || valorVariavel.equalsIgnoreCase("")) {
					valorVariavel = "1";
				}
				if (valorVariavel.equalsIgnoreCase("-")) {
					valorVariavel = "-1";
				}
				for (int j = 0; j < listaVariaveisGlobal.size(); j++) {
					if (nomeVariavel.equalsIgnoreCase(listaVariaveisGlobal.get(j).getNome())) {
						linhaRestricao[j] = Float.parseFloat(valorVariavel);
						break;
					}
				}
			}
		}
		return linhaRestricao;
	}

	private static Matcher getVariaveisByRegex(String objetiva) {
		return patternVariavel.matcher(objetiva);
	}

	private static String ajustaRestricao(StringTokenizer listaRestricoesToken) {
		int quantArtificialMaisFolga = 0;
		int numeroRestircaoLocal = 0;
		String restricaoReturn = "";
		while (listaRestricoesToken.hasMoreTokens()) {
			numeroRestircaoLocal++;
			String restricao = listaRestricoesToken.nextToken();
			System.out.println(restricao);
			if (restricao.contains("<=")) {// +f
				Variavel f = new Variavel();
				f.setNome("f" + numeroRestircaoLocal);
				listaVariaveisGlobal.add(f);
				quantArtificialMaisFolga++;
				restricao = restricao.substring(0, restricao.indexOf("<")) + "+f" + numeroRestircaoLocal
						+ restricao.substring(restricao.indexOf("="));
				System.out.println("Como o problema é de <= adicionamos um variavel de folga " + f.getNome());
			} else {
				if (restricao.contains(">=")) {// -f+a
					Variavel f = new Variavel();
					f.setNome("f" + numeroRestircaoLocal);
					listaVariaveisGlobal.add(f);
					quantArtificialMaisFolga++;
					Variavel a = new Variavel();
					a.setNome("a" + numeroRestircaoLocal);
					listaVariaveisGlobal.add(a);
					quantArtificialMaisFolga++;
					restricao = restricao.substring(0, restricao.indexOf(">")) + "-f" + numeroRestircaoLocal + "+a"
							+ numeroRestircaoLocal + restricao.substring(restricao.indexOf("="));
					System.out.println("Como o problema é de >= adicionamos um variavel de folga " + f.getNome()
							+ " e uma variavel artificial " + a.getNome());
				} else {// +a
					Variavel a = new Variavel();
					a.setNome("a" + numeroRestircaoLocal);
					listaVariaveisGlobal.add(a);
					quantArtificialMaisFolga++;
					restricao = restricao.substring(0, restricao.indexOf("=")) + "+a" + numeroRestircaoLocal
							+ restricao.substring(restricao.indexOf("="));
					System.out.println("Como o problema é de = adicionamos um variavel artificial " + a.getNome());
				}
			}
			System.out.println(restricao);
			System.out.println("");
			restricaoReturn += restricao + ";";
		}

		numeroColunasGlobal += quantArtificialMaisFolga;
		numeroLinhasGlobal = numeroRestircaoLocal;
		return restricaoReturn;
	}

	private static void setNulidades(StringTokenizer restricoesNulidadeToken) {

		while (restricoesNulidadeToken.hasMoreElements()) {
			String nulidade = restricoesNulidadeToken.nextElement().toString();
			Matcher varNulidade = getVariaveisByRegex(nulidade);
			String nome = "";
			if (varNulidade.find()) {
				nome = varNulidade.group();
				nulidade = nulidade.replaceAll(">=", "");
				nulidade = nulidade.replaceAll(nome, "");
			}
			float valor = Float.parseFloat(nulidade);
			for (Variavel variavel : listaVariaveisGlobal) {
				if (variavel.getNome().equalsIgnoreCase(nome)) {
					variavel.setMaiorQue(valor);
				}
			}

		}
		System.out.println();

	}

}
