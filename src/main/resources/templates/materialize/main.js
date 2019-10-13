//Funcao adiciona uma nova linha na tabela
function adicionaLinha(idTabela) {
    var tabela = document.getElementById(idTabela);
    var numeroLinhas = tabela.rows.length;
    var linha = tabela.insertRow(numeroLinhas);
    var celula1 = linha.insertCell(0);
    var celula2 = linha.insertCell(1);
    celula1.innerHTML = "<input type='text' name='"+ idTabela+ "Valor' >";
    celula2.innerHTML = "<button class='btn-floating' onclick='removeLinha(this)'><i class='material-icons center-align'>delete_forever</i></button>";
}

// funcao remove uma linha da tabela
function removeLinha(linha) {
    var i = linha.parentNode.parentNode.rowIndex;
    var idTabela = linha.parentNode.parentNode.parentNode.parentNode.id;
    if (idTabela === "nulidade") {
        document.getElementById('nulidade').deleteRow(i);
    } else {
        document.getElementById('restricao').deleteRow(i);
    }
}

function montaString() {
    var radios = document.getElementsByName('z');
    var zFuncao = document.getElementById("funcaoZ").value;
    var z;
    for (var i = 0, length = radios.length; i < length; i++) {
        if (radios[i].checked) {
            var zTipo = radios[i].value;
            z = zTipo + zFuncao;
            break;
        }
    }
    var restricoes = document.getElementsByName('restricaoValor');
    var restricaoString = "";
    for (var j = 0, size1 = restricoes.length; j < size1; j++) {
        restricaoString+= restricoes[j].value;
        if (j !== size1-1) {
            restricaoString+=";";
    
        }
    }
    
    var nulidades = document.getElementsByName('nulidadeValor');
    var nulidadeString = "";
    for (var k = 0, size2 = nulidades.length; k < size2; k++) {
        nulidadeString += nulidades[k].value;
        if (k !== size1-1) {
            nulidadeString+=";";
        }
    }
    console.log(z);
    console.log(restricaoString);
    console.log(nulidadeString);
}