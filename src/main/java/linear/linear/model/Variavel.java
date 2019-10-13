package linear.linear.model;

//autores: Davi, Isaac e Walter
public class Variavel {

  private String nome;
  private float maiorQue;
  private String tipo;

  public String getNome() {
      return nome;
  }

  public void setNome(String nome) {
      this.nome = nome;
  }

  public float getMaiorQue() {
      return maiorQue;
  }

  public void setMaiorQue(float maiorQue) {
      this.maiorQue = maiorQue;
  }

  public String getTipo() {
      return tipo;
  }

  public void setTipo() {
      this.tipo = nome.replaceAll("\\d", tipo);
  }
  
  @Override
  public boolean equals(Object obj) {
      if(obj instanceof String){
          if(((String) obj).equalsIgnoreCase(nome)){
          return true;
          }
      }
      return false;
  }
     
}
