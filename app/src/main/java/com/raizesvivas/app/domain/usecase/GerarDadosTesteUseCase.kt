package com.raizesvivas.app.domain.usecase

import com.raizesvivas.app.data.repository.FamiliaZeroRepository
import com.raizesvivas.app.data.repository.PessoaRepository
import com.raizesvivas.app.domain.model.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * UseCase para gerar dados de teste com 3 gerações de familiares
 * 
 * Gera uma árvore genealógica completa com:
 * - Geração 1: Avós (Família Zero)
 * - Geração 2: Pais (filhos dos avós)
 * - Geração 3: Netos (filhos dos pais)
 */
class GerarDadosTesteUseCase @Inject constructor(
    private val pessoaRepository: PessoaRepository,
    private val familiaZeroRepository: FamiliaZeroRepository
) {
    
    // Nomes brasileiros para geração aleatória
    private val nomesMasculinos = listOf(
        "João", "José", "Antônio", "Francisco", "Carlos", "Paulo", "Pedro", "Lucas",
        "Luís", "Marcos", "Rafael", "Fernando", "Ricardo", "Roberto", "Eduardo",
        "Miguel", "André", "Felipe", "Bruno", "Gustavo", "Rafael", "Daniel", "Diego"
    )
    
    private val nomesFemininos = listOf(
        "Maria", "Ana", "Fernanda", "Juliana", "Patrícia", "Mariana", "Camila",
        "Beatriz", "Gabriela", "Amanda", "Carolina", "Larissa", "Bruna", "Isabela",
        "Luciana", "Renata", "Vanessa", "Priscila", "Tatiana", "Daniela", "Cristina",
        "Sandra", "Adriana", "Claudia"
    )
    
    private val sobrenomes = listOf(
        "Silva", "Santos", "Oliveira", "Souza", "Rodrigues", "Ferreira", "Alves",
        "Pereira", "Lima", "Gomes", "Ribeiro", "Carvalho", "Almeida", "Martins",
        "Rocha", "Costa", "Monteiro", "Mendes", "Barbosa", "Araújo", "Nascimento",
        "Moreira", "Freitas", "Cavalcanti"
    )
    
    private val profissoes = listOf(
        "Agricultor", "Professor", "Médico", "Advogado", "Engenheiro", "Comerciante",
        "Costureira", "Doméstica", "Aposentado", "Pescador", "Carpinteiro", "Pedreiro",
        "Motorista", "Vendedor", "Contador", "Enfermeiro", "Dentista", "Veterinário"
    )
    
    private val locais = listOf(
        "São Paulo, SP", "Rio de Janeiro, RJ", "Belo Horizonte, MG", "Salvador, BA",
        "Curitiba, PR", "Porto Alegre, RS", "Recife, PE", "Fortaleza, CE",
        "Brasília, DF", "Goiânia, GO", "Campinas, SP", "Florianópolis, SC",
        "Vitória, ES", "Natal, RN", "João Pessoa, PB", "Maceió, AL"
    )
    
    /**
     * Gera dados de teste com 3 gerações
     */
    suspend fun gerarDadosTeste(usuarioId: String): Result<Unit> {
        return try {
            Timber.d("🎲 Iniciando geração de dados de teste...")
            
            // Verificar se já existe Família Zero
            val familiaZeroExiste = familiaZeroRepository.existe()
            val pessoasExistentes = pessoaRepository.buscarTodas()
            
            Timber.d("📊 Estado atual: Família Zero existe: $familiaZeroExiste, Pessoas: ${pessoasExistentes.size}")
            
            val random = Random()
            val calendar = Calendar.getInstance()
            
            // Geração 1: Avós (Família Zero) - nascidos em 1920-1940
            val avoHomem: Pessoa
            val avoMulher: Pessoa
            
            if (familiaZeroExiste) {
                // Buscar Família Zero existente
                val familiaZero = familiaZeroRepository.buscar()
                if (familiaZero != null) {
                    avoHomem = pessoasExistentes.find { it.id == familiaZero.pai } ?: run {
                        criarPessoaGeracao1(usuarioId, Genero.MASCULINO, random, calendar)
                    }
                    avoMulher = pessoasExistentes.find { it.id == familiaZero.mae } ?: run {
                        criarPessoaGeracao1(usuarioId, Genero.FEMININO, random, calendar)
                    }
                } else {
                    avoHomem = criarPessoaGeracao1(usuarioId, Genero.MASCULINO, random, calendar)
                    avoMulher = criarPessoaGeracao1(usuarioId, Genero.FEMININO, random, calendar)
                }
            } else {
                avoHomem = criarPessoaGeracao1(usuarioId, Genero.MASCULINO, random, calendar)
                avoMulher = criarPessoaGeracao1(usuarioId, Genero.FEMININO, random, calendar)
            }
            
            // Criar Família Zero se não existir
            if (!familiaZeroExiste) {
                val familiaZero = FamiliaZero(
                    pai = avoHomem.id,
                    mae = avoMulher.id,
                    fundadoPor = usuarioId,
                    arvoreNome = "${avoHomem.nome.split(" ").first()} & ${avoMulher.nome.split(" ").first()}"
                )
                familiaZeroRepository.salvar(familiaZero)
                Timber.d("✅ Família Zero criada: ${familiaZero.arvoreNome}")
            }
            
            // Salvar avós
            salvarPessoa(avoHomem)
            salvarPessoa(avoMulher)
            
            // Atualizar relacionamento de casal
            val avoHomemAtualizado = avoHomem.copy(
                conjugeAtual = avoMulher.id,
                estadoCivil = EstadoCivil.CASADO,
                dataCasamento = gerarDataCasamento(1920, 1945, random, calendar)
            )
            val avoMulherAtualizado = avoMulher.copy(
                conjugeAtual = avoHomem.id,
                estadoCivil = EstadoCivil.CASADO,
                dataCasamento = avoHomemAtualizado.dataCasamento
            )
            
            salvarPessoa(avoHomemAtualizado)
            salvarPessoa(avoMulherAtualizado)
            
            // Geração 2: Pais (filhos dos avós) - nascidos em 1950-1970
            val filhosAvos = mutableListOf<Pessoa>()
            val numFilhos = random.nextInt(3) + 3 // 3 a 5 filhos (3 + 0..2)
            val filhosIds = mutableListOf<String>()
            
            for (i in 0 until numFilhos) {
                val genero = if (random.nextBoolean()) Genero.MASCULINO else Genero.FEMININO
                val filho = criarPessoaGeracao2(
                    usuarioId = usuarioId,
                    genero = genero,
                    paiId = avoHomem.id,
                    maeId = avoMulher.id,
                    random = random,
                    calendar = calendar,
                    ordemNascimento = i + 1
                )
                
                filhosAvos.add(filho)
                filhosIds.add(filho.id)
                salvarPessoa(filho)
            }
            
            // Atualizar lista de filhos dos avós uma única vez
            val avoHomemComFilhos = avoHomemAtualizado.copy(
                filhos = filhosIds
            )
            val avoMulherComFilhos = avoMulherAtualizado.copy(
                filhos = filhosIds
            )
            
            salvarPessoa(avoHomemComFilhos)
            salvarPessoa(avoMulherComFilhos)
            
            // Geração 3: Netos (filhos dos pais) - nascidos em 1980-2000
            filhosAvos.forEach { pai ->
                // Alguns filhos podem ter cônjuge e filhos próprios
                if (random.nextFloat() > 0.3f) { // 70% têm cônjuge
                    val generoConjuge = if (pai.genero == Genero.MASCULINO) Genero.FEMININO else Genero.MASCULINO
                    val conjuge = criarPessoaGeracao2(
                        usuarioId = usuarioId,
                        genero = generoConjuge,
                        paiId = null, // Cônjuge pode ter pais diferentes
                        maeId = null,
                        random = random,
                        calendar = calendar,
                        ordemNascimento = null
                    )
                    
                    salvarPessoa(conjuge)
                    
                    // Criar casal
                    val paiComConjuge = pai.copy(
                        conjugeAtual = conjuge.id,
                        estadoCivil = EstadoCivil.CASADO,
                        dataCasamento = gerarDataCasamento(1970, 1990, random, calendar)
                    )
                    val conjugeComPai = conjuge.copy(
                        conjugeAtual = pai.id,
                        estadoCivil = EstadoCivil.CASADO,
                        dataCasamento = paiComConjuge.dataCasamento
                    )
                    
                    salvarPessoa(paiComConjuge)
                    salvarPessoa(conjugeComPai)
                    
                    // Criar netos (filhos do casal)
                    val numNetos = random.nextInt(3) + 1 // 1 a 3 filhos (1 + 0..2)
                    val netosIds = mutableListOf<String>()
                    
                    for (i in 0 until numNetos) {
                        val generoNeto = if (random.nextBoolean()) Genero.MASCULINO else Genero.FEMININO
                        val neto = criarPessoaGeracao3(
                            usuarioId = usuarioId,
                            genero = generoNeto,
                            paiId = if (pai.genero == Genero.MASCULINO) pai.id else conjuge.id,
                            maeId = if (pai.genero == Genero.FEMININO) pai.id else conjuge.id,
                            random = random,
                            calendar = calendar,
                            ordemNascimento = i + 1
                        )
                        
                        netosIds.add(neto.id)
                        salvarPessoa(neto)
                    }
                    
                    // Atualizar lista de filhos do casal uma única vez
                    val paiComNetos = paiComConjuge.copy(
                        filhos = netosIds
                    )
                    val conjugeComNetos = conjugeComPai.copy(
                        filhos = netosIds
                    )
                    
                    salvarPessoa(paiComNetos)
                    salvarPessoa(conjugeComNetos)
                }
            }
            
            Timber.d("✅ Dados de teste gerados com sucesso!")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao gerar dados de teste")
            Result.failure(e)
        }
    }
    
    private fun criarPessoaGeracao1(
        usuarioId: String,
        genero: Genero,
        random: Random,
        calendar: Calendar
    ): Pessoa {
        val nome = if (genero == Genero.MASCULINO) {
            "${nomesMasculinos.random(random)} ${sobrenomes.random(random)} ${sobrenomes.random(random)}"
        } else {
            "${nomesFemininos.random(random)} ${sobrenomes.random(random)} ${sobrenomes.random(random)}"
        }
        
        val dataNasc = gerarDataNascimento(1920, 1940, random, calendar)
        val localNasc = locais.random(random)
        val profissao = profissoes.random(random)
        
        return Pessoa(
            id = UUID.randomUUID().toString(),
            nome = nome,
            dataNascimento = dataNasc,
            localNascimento = localNasc,
            profissao = profissao,
            genero = genero,
            estadoCivil = EstadoCivil.CASADO,
            ehFamiliaZero = true,
            distanciaFamiliaZero = 0,
            criadoPor = usuarioId,
            criadoEm = Date(),
            modificadoPor = usuarioId,
            modificadoEm = Date(),
            aprovado = true
        )
    }
    
    private fun criarPessoaGeracao2(
        usuarioId: String,
        genero: Genero,
        paiId: String?,
        maeId: String?,
        random: Random,
        calendar: Calendar,
        ordemNascimento: Int?
    ): Pessoa {
        val nome = if (genero == Genero.MASCULINO) {
            "${nomesMasculinos.random(random)} ${sobrenomes.random(random)} ${sobrenomes.random(random)}"
        } else {
            "${nomesFemininos.random(random)} ${sobrenomes.random(random)} ${sobrenomes.random(random)}"
        }
        
        val dataNasc = gerarDataNascimento(1950, 1970, random, calendar)
        val localNasc = locais.random(random)
        val profissao = if (random.nextFloat() > 0.7f) profissoes.random(random) else null
        
        return Pessoa(
            id = UUID.randomUUID().toString(),
            nome = nome,
            dataNascimento = dataNasc,
            localNascimento = localNasc,
            profissao = profissao,
            genero = genero,
            pai = paiId,
            mae = maeId,
            distanciaFamiliaZero = 1,
            criadoPor = usuarioId,
            criadoEm = Date(),
            modificadoPor = usuarioId,
            modificadoEm = Date(),
            aprovado = true,
            ordemNascimento = ordemNascimento
        )
    }
    
    private fun criarPessoaGeracao3(
        usuarioId: String,
        genero: Genero,
        paiId: String?,
        maeId: String?,
        random: Random,
        calendar: Calendar,
        ordemNascimento: Int?
    ): Pessoa {
        val nome = if (genero == Genero.MASCULINO) {
            "${nomesMasculinos.random(random)} ${sobrenomes.random(random)} ${sobrenomes.random(random)}"
        } else {
            "${nomesFemininos.random(random)} ${sobrenomes.random(random)} ${sobrenomes.random(random)}"
        }
        
        val dataNasc = gerarDataNascimento(1980, 2000, random, calendar)
        val localNasc = locais.random(random)
        val profissao = if (random.nextFloat() > 0.5f) profissoes.random(random) else null
        
        return Pessoa(
            id = UUID.randomUUID().toString(),
            nome = nome,
            dataNascimento = dataNasc,
            localNascimento = localNasc,
            profissao = profissao,
            genero = genero,
            pai = paiId,
            mae = maeId,
            distanciaFamiliaZero = 2,
            criadoPor = usuarioId,
            criadoEm = Date(),
            modificadoPor = usuarioId,
            modificadoEm = Date(),
            aprovado = true,
            ordemNascimento = ordemNascimento
        )
    }
    
    private fun gerarDataNascimento(anoInicio: Int, anoFim: Int, random: Random, calendar: Calendar): Date {
        val ano = random.nextInt(anoFim - anoInicio + 1) + anoInicio
        val mes = random.nextInt(12)
        val dia = random.nextInt(28) + 1
        
        calendar.set(ano, mes, dia)
        return calendar.time
    }
    
    private fun gerarDataCasamento(anoInicio: Int, anoFim: Int, random: Random, calendar: Calendar): Date {
        val ano = random.nextInt(anoFim - anoInicio + 1) + anoInicio
        val mes = random.nextInt(12)
        val dia = random.nextInt(28) + 1
        
        calendar.set(ano, mes, dia)
        return calendar.time
    }
    
    private suspend fun salvarPessoa(pessoa: Pessoa) {
        try {
            pessoaRepository.salvar(pessoa, ehAdmin = true).onSuccess {
                Timber.d("✅ Pessoa salva: ${pessoa.nome}")
            }.onFailure { error ->
                Timber.e(error, "❌ Erro ao salvar pessoa: ${pessoa.nome}")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Erro ao salvar pessoa: ${pessoa.nome}")
        }
    }
    
    // Extensão para escolher aleatoriamente de uma lista
    private fun <T> List<T>.random(random: Random): T {
        return this[random.nextInt(this.size)]
    }
}

