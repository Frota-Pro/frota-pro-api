package br.com.frotasPro.api.service.planejamento;

import br.com.frotasPro.api.controller.response.PlanejamentoPlanilhaResponse;
import br.com.frotasPro.api.controller.response.PlanejamentoPlanilhaResponse.Box;
import br.com.frotasPro.api.controller.response.PlanejamentoPlanilhaResponse.Carregamento;
import br.com.frotasPro.api.controller.response.PlanejamentoPlanilhaResponse.Conflito;
import br.com.frotasPro.api.controller.response.PlanejamentoPlanilhaResponse.Pessoa;
import br.com.frotasPro.api.controller.response.PlanejamentoPlanilhaResponse.Rota;
import br.com.frotasPro.api.controller.response.PlanejamentoPlanilhaResponse.Veiculo;
import br.com.frotasPro.api.controller.response.PlanejamentoPlanilhaResumoResponse;
import br.com.frotasPro.api.domain.Ajudante;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.PlanejamentoBox;
import br.com.frotasPro.api.domain.PlanejamentoItem;
import br.com.frotasPro.api.domain.PlanejamentoPlanilha;
import br.com.frotasPro.api.domain.PlanejamentoRemovido;
import br.com.frotasPro.api.domain.enums.DiaSaidaPlanejamento;
import br.com.frotasPro.api.domain.enums.StatusPlanilhaPlanejamento;
import br.com.frotasPro.api.domain.enums.TurnoSaidaPlanejamento;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ConflictException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.repository.AjudanteRepository;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.repository.PlanejamentoBoxRepository;
import br.com.frotasPro.api.repository.PlanejamentoItemRepository;
import br.com.frotasPro.api.repository.PlanejamentoPlanilhaRepository;
import br.com.frotasPro.api.repository.PlanejamentoRemovidoRepository;
import br.com.frotasPro.api.service.planejamento.PlanejamentoWinThorClient.CarregamentoWinThor;
import br.com.frotasPro.api.service.usuario.UsuarioAutenticadoService;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Planejamento de cargas: a planilha que a logística monta antes do faturamento. Os carregamentos vêm do WinThor
 * (só leitura); aqui eles são agrupados em rotas (um principal e seus complementos) e a rota ganha motorista e
 * caminhão. É um cadastro à parte: não mexe em cargas, na sincronização nem nas metas.
 *
 * Regras:
 * - um dia, uma planilha; entram só os carregamentos montados naquele dia (nada de dias anteriores);
 * - venda de balcão não entra (ver {@link #ehVendaBalcao}): sem motorista e sem veículo, ou "balcão" no destino;
 * - carregamento "tirado" (venda balcão etc.) não volta mais em planilha nenhuma;
 * - carregamento faturado não muda de rota nem de motorista (só pode ser tirado);
 * - complemento é só de um principal (um nível); motorista e caminhão ficam no principal;
 * - planilha fechada não muda; só reabre no mesmo dia.
 */
@Service
@RequiredArgsConstructor
public class PlanejamentoCargasService {

    private static final BigDecimal MIL = new BigDecimal("1000");

    private final PlanejamentoWinThorClient winThorClient;
    private final PlanejamentoPlanilhaRepository planilhaRepository;
    private final PlanejamentoItemRepository itemRepository;
    private final PlanejamentoRemovidoRepository removidoRepository;
    private final MotoristaRepository motoristaRepository;
    private final AjudanteRepository ajudanteRepository;
    private final PlanejamentoBoxRepository boxRepository;
    private final CaminhaoRepository caminhaoRepository;
    private final CargaRepository cargaRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final PlatformTransactionManager transactionManager;

    // ---------------------------------------------------------------------------------------------------------
    // Consultas
    // ---------------------------------------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<PlanejamentoPlanilhaResumoResponse> listar() {
        List<PlanejamentoPlanilha> planilhas = planilhaRepository.findAllByOrderByDataDesc(PageRequest.of(0, 90));
        if (planilhas.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<PlanejamentoItem>> itensPorPlanilha = itemRepository
                .findByPlanilhaIdIn(planilhas.stream().map(PlanejamentoPlanilha::getId).toList()).stream()
                .collect(Collectors.groupingBy(i -> i.getPlanilha().getId()));

        return planilhas.stream().map(p -> {
            List<PlanejamentoItem> itens = itensPorPlanilha.getOrDefault(p.getId(), List.of());
            return PlanejamentoPlanilhaResumoResponse.builder()
                    .id(p.getId())
                    .data(p.getData())
                    .status(p.getStatus().name())
                    .totalCarregamentos(itens.size())
                    .totalRotas((int) itens.stream().filter(i -> !i.isComplemento()).count())
                    .pesoToneladas(paraToneladas(somaPesoKg(itens)))
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public PlanejamentoPlanilhaResponse obter(UUID id) {
        return montar(buscarPlanilha(id));
    }

    // ---------------------------------------------------------------------------------------------------------
    // Criar e atualizar a partir do WinThor (a consulta ao WinThor roda FORA de transação)
    // ---------------------------------------------------------------------------------------------------------

    public PlanejamentoPlanilhaResponse criar(LocalDate data) {
        LocalDate hoje = FusoHorarioUtils.hojeBrasil();
        LocalDate dia = data != null ? data : hoje;
        if (dia.isAfter(hoje)) {
            throw new BusinessException("Não dá para criar a planilha de um dia que ainda não chegou.");
        }
        if (planilhaRepository.existsByData(dia)) {
            throw new ConflictException("Já existe uma planilha para esse dia. Abra a que já existe.");
        }

        List<CarregamentoWinThor> doWinThor = winThorClient.carregamentosDoPeriodo(dia, dia);
        String usuario = usuarioAutenticadoService.getLogin();

        TransactionTemplate template = new TransactionTemplate(transactionManager);
        return template.execute(status -> {
            if (planilhaRepository.existsByData(dia)) {
                throw new ConflictException("Já existe uma planilha para esse dia. Abra a que já existe.");
            }
            PlanejamentoPlanilha planilha = new PlanejamentoPlanilha();
            planilha.setData(dia);
            planilha.setStatus(StatusPlanilhaPlanejamento.ABERTA);
            planilha.setCriadaPor(usuario);
            planilha.setCriadaEm(agora());
            planilhaRepository.save(planilha);

            incorporar(planilha, doWinThor, List.of());
            return montar(planilha);
        });
    }

    /** Busca de novo no WinThor: acrescenta os carregamentos que apareceram e atualiza peso/pedidos/faturamento. */
    public PlanejamentoPlanilhaResponse atualizar(UUID id) {
        PlanejamentoPlanilha existente = planilhaRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFound("Planilha não encontrada."));
        exigirAberta(existente);
        LocalDate dia = existente.getData();

        List<CarregamentoWinThor> doWinThor = winThorClient.carregamentosDoPeriodo(dia, dia);

        TransactionTemplate template = new TransactionTemplate(transactionManager);
        return template.execute(status -> {
            PlanejamentoPlanilha planilha = buscarPlanilha(id);
            exigirAberta(planilha);
            incorporar(planilha, doWinThor, itemRepository.findByPlanilhaIdOrderByNumeroCarregamento(id));
            return montar(planilha);
        });
    }

    /** Põe na planilha os carregamentos que entram e atualiza o retrato dos que já estão nela. */
    private void incorporar(PlanejamentoPlanilha planilha, List<CarregamentoWinThor> doWinThor, List<PlanejamentoItem> existentes) {
        Map<Integer, PlanejamentoItem> porNumero = existentes.stream()
                .collect(Collectors.toMap(PlanejamentoItem::getNumeroCarregamento, i -> i, (a, b) -> a));
        Set<Integer> removidos = removidoRepository.findAll().stream()
                .map(PlanejamentoRemovido::getNumeroCarregamento).collect(Collectors.toSet());

        for (CarregamentoWinThor c : doWinThor) {
            if (c.getNumCar() == null) {
                continue;
            }
            PlanejamentoItem item = porNumero.get(c.getNumCar());
            if (item != null) {
                copiarDoWinThor(item, c);
                itemRepository.save(item);
            } else if (deveEntrar(c, planilha.getData(), removidos)) {
                PlanejamentoItem novo = new PlanejamentoItem();
                novo.setPlanilha(planilha);
                novo.setNumeroCarregamento(c.getNumCar());
                copiarDoWinThor(novo, c);
                itemRepository.save(novo);
            }
        }
        // Rotas que já tinham complementos antes de existir cor (ou que ficaram sem) ganham a delas agora.
        itemRepository.flush();
        ajustarCores(planilha.getId());
    }

    /**
     * Entra na planilha do dia só o que foi montado naquele dia (faturado ou não: o status aparece na tela).
     * Quem foi "tirado" não volta.
     */
    static boolean deveEntrar(CarregamentoWinThor c, LocalDate dia, Set<Integer> removidos) {
        if (c.getNumCar() == null || removidos.contains(c.getNumCar())) {
            return false;
        }
        return c.getDataMontagem() != null && c.getDataMontagem().isEqual(dia) && !ehVendaBalcao(c);
    }

    /**
     * Venda de balcão não é rota. Dois sinais, porque o nome é digitado à mão (pode vir errado ou diferente):
     * o carregamento não tem motorista nem veículo (zero ou vazio) — nenhuma rota real é assim —, ou o destino
     * tem "BALC" (sem diferenciar maiúscula nem acento: "VENDA BALCAO", "Balcão", "vda balcao"...).
     */
    static boolean ehVendaBalcao(CarregamentoWinThor c) {
        boolean semMotorista = c.getCodMotorista() == null || c.getCodMotorista() == 0;
        boolean semVeiculo = c.getCodVeiculo() == null || c.getCodVeiculo() == 0;
        if (semMotorista && semVeiculo) {
            return true;
        }
        String destino = c.getDestino() == null ? "" : Normalizer.normalize(c.getDestino(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT);
        return destino.contains("BALC");
    }

    private static void copiarDoWinThor(PlanejamentoItem item, CarregamentoWinThor c) {
        item.setDestino(c.getDestino());
        item.setPesoKg(c.getPesoKg() != null ? BigDecimal.valueOf(c.getPesoKg()).setScale(3, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        item.setTotalPedidos(c.getTotalPedidos() != null ? c.getTotalPedidos() : 0);
        item.setCodigosClientes(c.getCodigosClientes());
        item.setFaturada(c.isFaturada());
        item.setValorTotal(c.getValorTotal() != null ? c.getValorTotal().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        item.setCodMotoristaWinthor(c.getCodMotorista());
        item.setCodVeiculoWinthor(c.getCodVeiculo());
        item.setNomeMotoristaWinthor(c.getNomeMotorista());
        item.setPlacaVeiculoWinthor(c.getPlacaVeiculo());
        item.setDataMontagem(c.getDataMontagem());
    }

    // ---------------------------------------------------------------------------------------------------------
    // Edição da planilha
    // ---------------------------------------------------------------------------------------------------------

    /** "Tirar": o carregamento sai da planilha e não volta mais (ex.: cliente vem buscar). */
    @Transactional
    public PlanejamentoPlanilhaResponse remover(UUID planilhaId, Integer numero) {
        return removerVarios(planilhaId, List.of(numero));
    }

    /** "Tirar" vários de uma vez. Não volta mais em planilha nenhuma. Faturado também pode ser tirado. */
    @Transactional
    public PlanejamentoPlanilhaResponse removerVarios(UUID planilhaId, List<Integer> numeros) {
        PlanejamentoPlanilha planilha = buscarPlanilha(planilhaId);
        exigirAberta(planilha);
        Set<Integer> alvo = escolhidos(numeros);

        List<PlanejamentoItem> todos = itemRepository.findByPlanilhaIdOrderByNumeroCarregamento(planilhaId);
        Map<Integer, PlanejamentoItem> porNumero = todos.stream()
                .collect(Collectors.toMap(PlanejamentoItem::getNumeroCarregamento, i -> i, (a, b) -> a));
        for (Integer n : alvo) {
            if (!porNumero.containsKey(n)) {
                throw new ObjectNotFound("O carregamento " + n + " não está nesta planilha.");
            }
        }

        // Quem era complemento de um carregamento que saiu (e não saiu junto) volta a ser carregamento solto.
        for (PlanejamentoItem outro : todos) {
            if (outro.getPrincipalNumero() != null && alvo.contains(outro.getPrincipalNumero()) && !alvo.contains(outro.getNumeroCarregamento())) {
                outro.setPrincipalNumero(null);
                itemRepository.save(outro);
            }
        }

        for (Integer n : alvo) {
            itemRepository.delete(porNumero.get(n));
            if (!removidoRepository.existsById(n)) {
                PlanejamentoRemovido removido = new PlanejamentoRemovido();
                removido.setNumeroCarregamento(n);
                removido.setRemovidoPor(usuarioAutenticadoService.getLogin());
                removido.setRemovidoEm(agora());
                removidoRepository.save(removido);
            }
        }
        itemRepository.flush();
        ajustarCores(planilhaId);
        itemRepository.flush();
        return montar(planilha);
    }

    /** Marca o carregamento como complemento de um principal da mesma planilha (ou desfaz, com principal vazio). */
    @Transactional
    public PlanejamentoPlanilhaResponse definirComplemento(UUID planilhaId, Integer numero, Integer principalNumero) {
        return principalNumero == null ? separar(planilhaId, List.of(numero)) : juntar(planilhaId, principalNumero, List.of(numero));
    }

    /** Vários carregamentos viram complemento do mesmo principal (tudo ou nada). */
    @Transactional
    public PlanejamentoPlanilhaResponse juntar(UUID planilhaId, Integer principalNumero, List<Integer> numeros) {
        PlanejamentoPlanilha planilha = buscarPlanilha(planilhaId);
        exigirAberta(planilha);
        if (principalNumero == null) {
            throw new BusinessException("Escolha o carregamento principal da rota.");
        }
        Set<Integer> alvo = escolhidos(numeros);
        if (alvo.contains(principalNumero)) {
            throw new BusinessException("Um carregamento não pode ser complemento dele mesmo.");
        }

        PlanejamentoItem principal = itemRepository.findByPlanilhaIdAndNumeroCarregamento(planilhaId, principalNumero)
                .orElseThrow(() -> new BusinessException("O carregamento principal " + principalNumero + " não está nesta planilha."));
        exigirNaoFaturado(principal);
        if (principal.isComplemento()) {
            throw new BusinessException("O " + principalNumero + " já é complemento de outro. Complemento de complemento não existe: escolha o principal da rota.");
        }

        List<PlanejamentoItem> todos = itemRepository.findByPlanilhaIdOrderByNumeroCarregamento(planilhaId);
        List<PlanejamentoItem> itens = new ArrayList<>();
        for (Integer n : alvo) {
            PlanejamentoItem item = todos.stream().filter(i -> i.getNumeroCarregamento().equals(n)).findFirst()
                    .orElseThrow(() -> new ObjectNotFound("O carregamento " + n + " não está nesta planilha."));
            exigirNaoFaturado(item);
            boolean temComplementos = todos.stream().anyMatch(i -> n.equals(i.getPrincipalNumero()));
            if (temComplementos) {
                throw new BusinessException("O " + n + " já tem complementos. Desfaça os complementos dele antes de torná-lo complemento de outro.");
            }
            itens.add(item);
        }

        for (PlanejamentoItem item : itens) {
            item.setPrincipalNumero(principalNumero);
            // A rota inteira usa o motorista, o ajudante e o caminhão do principal.
            item.setMotorista(null);
            item.getAjudantes().clear();
            item.setBox(null);
            item.setDiaSaida(null);
            item.setTurnoSaida(null);
            item.setCaminhao(null);
            itemRepository.save(item);
        }
        itemRepository.flush();
        ajustarCores(planilhaId);
        itemRepository.flush();
        return montar(planilha);
    }

    /** Desfaz o complemento de vários: cada um volta a ser um carregamento solto. */
    @Transactional
    public PlanejamentoPlanilhaResponse separar(UUID planilhaId, List<Integer> numeros) {
        PlanejamentoPlanilha planilha = buscarPlanilha(planilhaId);
        exigirAberta(planilha);
        Set<Integer> alvo = escolhidos(numeros);

        List<PlanejamentoItem> todos = itemRepository.findByPlanilhaIdOrderByNumeroCarregamento(planilhaId);
        for (Integer n : alvo) {
            PlanejamentoItem item = todos.stream().filter(i -> i.getNumeroCarregamento().equals(n)).findFirst()
                    .orElseThrow(() -> new ObjectNotFound("O carregamento " + n + " não está nesta planilha."));
            exigirNaoFaturado(item);
            item.setPrincipalNumero(null);
            item.setMotorista(null);
            item.getAjudantes().clear();
            item.setBox(null);
            item.setDiaSaida(null);
            item.setTurnoSaida(null);
            item.setCaminhao(null);
            itemRepository.save(item);
        }
        itemRepository.flush();
        ajustarCores(planilhaId);
        itemRepository.flush();
        return montar(planilha);
    }

    /** Escolhe o motorista, o ajudante e o caminhão da rota (no carregamento principal). Vazio limpa. */
    @Transactional
    public PlanejamentoPlanilhaResponse definirRota(UUID planilhaId, Integer numero, String motoristaCodigo,
                                                    String caminhaoCodigo, List<String> ajudantesCodigos) {
        PlanejamentoPlanilha planilha = buscarPlanilha(planilhaId);
        exigirAberta(planilha);
        PlanejamentoItem item = buscarItem(planilhaId, numero);
        if (item.isComplemento()) {
            throw new BusinessException("O motorista, o ajudante e o caminhão são da rota: escolha no carregamento principal (" + item.getPrincipalNumero() + ").");
        }
        exigirNaoFaturado(item);

        Motorista motorista = null;
        if (motoristaCodigo != null && !motoristaCodigo.isBlank()) {
            motorista = motoristaRepository.findByCodigo(motoristaCodigo.trim())
                    .orElseThrow(() -> new ObjectNotFound("Motorista não encontrado."));
            if (!motorista.isAtivo()) {
                throw new BusinessException("O motorista " + motorista.getNome() + " está inativo.");
            }
        }
        Caminhao caminhao = null;
        if (caminhaoCodigo != null && !caminhaoCodigo.isBlank()) {
            caminhao = caminhaoRepository.findByCodigo(caminhaoCodigo.trim())
                    .orElseThrow(() -> new ObjectNotFound("Caminhão não encontrado."));
            if (!caminhao.isAtivo()) {
                throw new BusinessException("O caminhão " + caminhao.getPlaca() + " está inativo.");
            }
        }
        Set<Ajudante> ajudantes = new LinkedHashSet<>();
        if (ajudantesCodigos != null) {
            for (String codigo : ajudantesCodigos) {
                if (codigo == null || codigo.isBlank()) {
                    continue;
                }
                ajudantes.add(ajudanteRepository.findByCodigoAndAtivoTrue(codigo.trim())
                        .orElseThrow(() -> new ObjectNotFound("Ajudante não encontrado.")));
            }
        }

        item.setMotorista(motorista);
        item.getAjudantes().clear();
        item.getAjudantes().addAll(ajudantes);
        item.setCaminhao(caminhao);
        itemRepository.save(item);
        itemRepository.flush();
        return montar(planilha);
    }

    /** Quantas cores a tela tem na paleta (a tela usa o mesmo número). */
    static final int TAMANHO_PALETA = 10;

    /**
     * Cada rota que tem complementos ganha uma cor (a menor da paleta que nenhuma outra rota da planilha usa) e a
     * guarda enquanto continuar sendo rota; carregamento solto não tem cor. Assim a cor não muda quando outras rotas
     * nascem ou se desfazem.
     */
    private void ajustarCores(UUID planilhaId) {
        List<PlanejamentoItem> todos = itemRepository.findByPlanilhaIdOrderByNumeroCarregamento(planilhaId);
        Set<Integer> comComplementos = todos.stream()
                .filter(PlanejamentoItem::isComplemento)
                .map(PlanejamentoItem::getPrincipalNumero)
                .collect(Collectors.toSet());

        Set<Integer> usadas = new HashSet<>();
        for (PlanejamentoItem i : todos) {
            if (!i.isComplemento() && comComplementos.contains(i.getNumeroCarregamento()) && i.getCor() != null) {
                usadas.add(i.getCor());
            }
        }
        for (PlanejamentoItem i : todos) {
            boolean temRota = !i.isComplemento() && comComplementos.contains(i.getNumeroCarregamento());
            if (!temRota && i.getCor() != null) {
                i.setCor(null);
                itemRepository.save(i);
            } else if (temRota && i.getCor() == null) {
                int cor = proximaCor(usadas);
                i.setCor(cor);
                usadas.add(cor);
                itemRepository.save(i);
            }
        }
    }

    static int proximaCor(Set<Integer> usadas) {
        for (int c = 0; c < TAMANHO_PALETA; c++) {
            if (!usadas.contains(c)) {
                return c;
            }
        }
        return usadas.size() % TAMANHO_PALETA;
    }

    /**
     * Box e saída (dia + turno) da rota, no carregamento principal. Vazios limpam. O box é da rota inteira e fica
     * ocupado até ela ser faturada: outra rota da planilha não pode usar o mesmo box enquanto isso.
     */
    @Transactional
    public PlanejamentoPlanilhaResponse definirSaida(UUID planilhaId, Integer numero, Integer boxNumero,
                                                     String diaSaida, String turnoSaida) {
        PlanejamentoPlanilha planilha = buscarPlanilha(planilhaId);
        exigirAberta(planilha);
        PlanejamentoItem item = buscarItem(planilhaId, numero);
        if (item.isComplemento()) {
            throw new BusinessException("O box e a saída são da rota: escolha no carregamento principal (" + item.getPrincipalNumero() + ").");
        }
        exigirNaoFaturado(item);

        PlanejamentoBox box = null;
        if (boxNumero != null) {
            box = boxRepository.findByNumeroAndAtivoTrue(boxNumero)
                    .orElseThrow(() -> new ObjectNotFound("Box não encontrado."));
            for (PlanejamentoItem outro : itemRepository.findByPlanilhaIdOrderByNumeroCarregamento(planilhaId)) {
                boolean ocupa = !outro.isComplemento() && !outro.isFaturada()
                        && !outro.getNumeroCarregamento().equals(numero)
                        && outro.getBox() != null && boxNumero.equals(outro.getBox().getNumero());
                if (ocupa) {
                    throw new BusinessException("O " + box.getNome() + " já está com a rota " + outro.getNumeroCarregamento()
                            + ", que ainda não foi faturada.");
                }
            }
        }

        item.setBox(box);
        item.setDiaSaida(converter(DiaSaidaPlanejamento.class, diaSaida, "o dia da saída"));
        item.setTurnoSaida(converter(TurnoSaidaPlanejamento.class, turnoSaida, "o turno"));
        itemRepository.save(item);
        itemRepository.flush();
        return montar(planilha);
    }

    private static <E extends Enum<E>> E converter(Class<E> tipo, String valor, String rotulo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(tipo, valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Valor inválido para " + rotulo + ".");
        }
    }

    private static Set<Integer> escolhidos(List<Integer> numeros) {
        Set<Integer> alvo = new LinkedHashSet<>();
        if (numeros != null) {
            numeros.stream().filter(Objects::nonNull).forEach(alvo::add);
        }
        if (alvo.isEmpty()) {
            throw new BusinessException("Escolha pelo menos um carregamento.");
        }
        return alvo;
    }

    @Transactional
    public PlanejamentoPlanilhaResponse fechar(UUID planilhaId) {
        PlanejamentoPlanilha planilha = buscarPlanilha(planilhaId);
        exigirAberta(planilha);
        planilha.setStatus(StatusPlanilhaPlanejamento.FECHADA);
        planilha.setFechadaPor(usuarioAutenticadoService.getLogin());
        planilha.setFechadaEm(agora());
        planilhaRepository.save(planilha);
        return montar(planilha);
    }

    /** Só dá para reabrir no mesmo dia da planilha: depois disso ela é só consulta e impressão. */
    @Transactional
    public PlanejamentoPlanilhaResponse reabrir(UUID planilhaId) {
        PlanejamentoPlanilha planilha = buscarPlanilha(planilhaId);
        if (planilha.isAberta()) {
            throw new BusinessException("A planilha já está aberta.");
        }
        if (!podeReabrir(planilha)) {
            throw new BusinessException("Só dá para reabrir a planilha no próprio dia dela. Agora ela é só para consulta e impressão.");
        }
        planilha.setStatus(StatusPlanilhaPlanejamento.ABERTA);
        planilha.setFechadaPor(null);
        planilha.setFechadaEm(null);
        planilhaRepository.save(planilha);
        return montar(planilha);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Montagem da resposta
    // ---------------------------------------------------------------------------------------------------------

    private PlanejamentoPlanilhaResponse montar(PlanejamentoPlanilha planilha) {
        List<PlanejamentoItem> itens = itemRepository.findByPlanilhaIdOrderByNumeroCarregamento(planilha.getId());

        Map<Integer, List<PlanejamentoItem>> complementosPorPrincipal = new HashMap<>();
        for (PlanejamentoItem i : itens) {
            if (i.isComplemento()) {
                complementosPorPrincipal.computeIfAbsent(i.getPrincipalNumero(), k -> new ArrayList<>()).add(i);
            }
        }

        List<PlanejamentoItem> principais = itens.stream()
                .filter(i -> !i.isComplemento())
                .sorted(Comparator.comparing(PlanejamentoItem::getNumeroCarregamento))
                .toList();

        List<Rota> rotas = new ArrayList<>();
        for (PlanejamentoItem principal : principais) {
            List<PlanejamentoItem> grupo = new ArrayList<>();
            grupo.add(principal);
            grupo.addAll(complementosPorPrincipal.getOrDefault(principal.getNumeroCarregamento(), List.of()).stream()
                    .sorted(Comparator.comparing(PlanejamentoItem::getNumeroCarregamento)).toList());
            Totais totais = totais(grupo);
            List<String> destinos = destinosDistintos(grupo);
            List<Conflito> conflitosDaRota = conflitos(principal, principais);

            rotas.add(Rota.builder()
                    .numeroPrincipal(principal.getNumeroCarregamento())
                    .motorista(pessoa(principal.getMotorista()))
                    .ajudantes(principal.getAjudantes().stream()
                            .sorted(Comparator.comparing(a -> a.getNome() == null ? "" : a.getNome()))
                            .map(a -> pessoa(a)).toList())
                    .box(boxResumo(principal.getBox(), null))
                    .diaSaida(principal.getDiaSaida() == null ? null : principal.getDiaSaida().name())
                    .turnoSaida(principal.getTurnoSaida() == null ? null : principal.getTurnoSaida().name())
                    .caminhao(veiculo(principal.getCaminhao()))
                    .cor(principal.getCor())
                    .pesoToneladas(paraToneladas(totais.pesoKg()))
                    .valorTotal(totais.valor())
                    .totalClientes(totais.clientes())
                    .totalPedidos(totais.pedidos())
                    .destinos(destinos)
                    .totalCidades(destinos.size())
                    .conflitos(conflitosDaRota)
                    .avisos(conflitosDaRota.stream().map(Conflito::getTexto).toList())
                    .carregamentos(grupo.stream().map(i -> carregamento(i, i == principal)).toList())
                    .build());
        }

        List<Box> boxes = boxRepository.findByAtivoTrueOrderByNumero().stream()
                .map(b -> boxResumo(b, principais.stream()
                        .filter(p -> !p.isFaturada() && p.getBox() != null && p.getBox().getId().equals(b.getId()))
                        .map(PlanejamentoItem::getNumeroCarregamento)
                        .findFirst().orElse(null)))
                .toList();

        return PlanejamentoPlanilhaResponse.builder()
                .id(planilha.getId())
                .data(planilha.getData())
                .status(planilha.getStatus().name())
                .criadaPor(planilha.getCriadaPor())
                .criadaEm(planilha.getCriadaEm())
                .fechadaPor(planilha.getFechadaPor())
                .fechadaEm(planilha.getFechadaEm())
                .podeReabrir(podeReabrir(planilha))
                .totalCarregamentos(itens.size())
                .totalRotas(principais.size())
                .pesoToneladas(paraToneladas(somaPesoKg(itens)))
                .valorTotal(somaValor(itens))
                .rotas(rotas)
                .boxes(boxes)
                .build();
    }

    private Carregamento carregamento(PlanejamentoItem i, boolean principal) {
        String statusCarga = null;
        String motoristaCarga = null;
        String caminhaoCarga = null;
        if (i.isFaturada()) {
            Carga carga = cargaRepository.findByNumeroCargaExterno(String.valueOf(i.getNumeroCarregamento())).orElse(null);
            if (carga != null) {
                statusCarga = carga.getStatusCarga() != null ? carga.getStatusCarga().name() : null;
                motoristaCarga = carga.getMotorista() != null ? carga.getMotorista().getNome() : null;
                caminhaoCarga = carga.getCaminhao() != null ? carga.getCaminhao().getPlaca() : null;
            }
        }
        return Carregamento.builder()
                .numero(i.getNumeroCarregamento())
                .principal(principal)
                .destino(i.getDestino())
                .pesoToneladas(paraToneladas(i.getPesoKg()))
                .valorTotal(i.getValorTotal())
                .totalClientes(i.getCodigosClientes().size())
                .codigosClientes(new ArrayList<>(i.getCodigosClientes()))
                .totalPedidos(i.getTotalPedidos())
                .dataMontagem(i.getDataMontagem())
                .codMotoristaWinThor(i.getCodMotoristaWinthor())
                .codVeiculoWinThor(i.getCodVeiculoWinthor())
                .nomeMotoristaWinThor(i.getNomeMotoristaWinthor())
                .placaVeiculoWinThor(i.getPlacaVeiculoWinthor())
                .faturada(i.isFaturada())
                .statusCarga(statusCarga)
                .motoristaCarga(motoristaCarga)
                .caminhaoCarga(caminhaoCarga)
                .build();
    }

    /** Destinos distintos da rota, na ordem em que aparecem (o nome é o que o WinThor tem, sem mexer). */
    static List<String> destinosDistintos(List<PlanejamentoItem> grupo) {
        Map<String, String> unicos = new LinkedHashMap<>();
        for (PlanejamentoItem i : grupo) {
            String d = i.getDestino() == null ? "" : i.getDestino().trim();
            if (!d.isEmpty()) {
                unicos.putIfAbsent(d.toUpperCase(Locale.ROOT), d);
            }
        }
        return new ArrayList<>(unicos.values());
    }

    /**
     * Inconsistências da rota: o mesmo motorista, ajudante ou caminhão em outra rota que ainda não saiu e que pode
     * sair na mesma hora. "Pode" = o dia e o turno são iguais ou algum dos dois ainda não foi definido (a mesma
     * pessoa pode fazer uma rota de manhã e outra à tarde; por isso, depois de definir horários diferentes, o aviso
     * some). Rota já faturada não entra na conta.
     */
    static List<Conflito> conflitos(PlanejamentoItem principal, List<PlanejamentoItem> principais) {
        List<Conflito> conflitos = new ArrayList<>();
        if (principal.isFaturada()) {
            return conflitos;
        }
        List<PlanejamentoItem> outras = principais.stream()
                .filter(o -> !o.getNumeroCarregamento().equals(principal.getNumeroCarregamento()))
                .filter(o -> !o.isFaturada())
                .filter(o -> podeSerNaMesmaHora(principal, o))
                .toList();

        if (principal.getMotorista() != null) {
            UUID id = principal.getMotorista().getId();
            List<PlanejamentoItem> mesmos = outras.stream()
                    .filter(o -> o.getMotorista() != null && id.equals(o.getMotorista().getId())).toList();
            conflito("MOTORISTA", "O motorista", principal.getMotorista().getCodigo(), principal.getMotorista().getNome(), principal, mesmos)
                    .ifPresent(conflitos::add);
        }
        for (Ajudante a : principal.getAjudantes()) {
            List<PlanejamentoItem> mesmos = outras.stream()
                    .filter(o -> o.getAjudantes().stream().anyMatch(x -> x.getId().equals(a.getId()))).toList();
            conflito("AJUDANTE", "O ajudante", a.getCodigo(), a.getNome(), principal, mesmos).ifPresent(conflitos::add);
        }
        if (principal.getCaminhao() != null) {
            UUID id = principal.getCaminhao().getId();
            List<PlanejamentoItem> mesmos = outras.stream()
                    .filter(o -> o.getCaminhao() != null && id.equals(o.getCaminhao().getId())).toList();
            conflito("CAMINHAO", "O caminhão", principal.getCaminhao().getCodigo(), principal.getCaminhao().getPlaca(), principal, mesmos)
                    .ifPresent(conflitos::add);
        }
        return conflitos;
    }

    private static boolean podeSerNaMesmaHora(PlanejamentoItem a, PlanejamentoItem b) {
        return igualOuVazio(a.getDiaSaida(), b.getDiaSaida()) && igualOuVazio(a.getTurnoSaida(), b.getTurnoSaida());
    }

    private static boolean igualOuVazio(Object x, Object y) {
        return x == null || y == null || x.equals(y);
    }

    private static boolean horarioDefinidoEIgual(PlanejamentoItem a, PlanejamentoItem b) {
        return a.getDiaSaida() != null && a.getDiaSaida() == b.getDiaSaida()
                && a.getTurnoSaida() != null && a.getTurnoSaida() == b.getTurnoSaida();
    }

    private static java.util.Optional<Conflito> conflito(String tipo, String rotulo, String codigo, String nome,
                                                         PlanejamentoItem principal, List<PlanejamentoItem> mesmos) {
        if (mesmos.isEmpty()) {
            return java.util.Optional.empty();
        }
        List<Integer> rotas = mesmos.stream().map(PlanejamentoItem::getNumeroCarregamento).sorted().toList();
        boolean certo = mesmos.stream().allMatch(o -> horarioDefinidoEIgual(principal, o));
        String lista = rotas.stream().map(String::valueOf).collect(Collectors.joining(", "));
        String texto = rotulo + " " + nome + " também está na rota " + lista
                + (certo ? " no mesmo dia e turno." : ". Se os horários forem diferentes, escolha o dia e o turno de saída de cada rota.");
        return java.util.Optional.of(Conflito.builder()
                .tipo(tipo).codigo(codigo).nome(nome).rotas(rotas).mesmoHorario(certo).texto(texto).build());
    }

    record Totais(BigDecimal pesoKg, BigDecimal valor, int clientes, int pedidos) {
    }

    /** Peso, valor, clientes DISTINTOS e pedidos de uma rota (principal + complementos). */
    static Totais totais(List<PlanejamentoItem> grupo) {
        Set<Integer> clientes = new HashSet<>();
        int pedidos = 0;
        for (PlanejamentoItem i : grupo) {
            clientes.addAll(i.getCodigosClientes());
            pedidos += i.getTotalPedidos();
        }
        return new Totais(somaPesoKg(grupo), somaValor(grupo), clientes.size(), pedidos);
    }

    private static BigDecimal somaValor(List<PlanejamentoItem> itens) {
        return itens.stream().map(PlanejamentoItem::getValorTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal somaPesoKg(List<PlanejamentoItem> itens) {
        return itens.stream().map(PlanejamentoItem::getPesoKg).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** O sistema todo guarda peso em TONELADAS (3 casas); o WinThor manda quilos. */
    static BigDecimal paraToneladas(BigDecimal kg) {
        return (kg == null ? BigDecimal.ZERO : kg).divide(MIL, 3, RoundingMode.HALF_UP);
    }

    private static Box boxResumo(PlanejamentoBox b, Integer ocupadoPorRota) {
        return b == null ? null : Box.builder().numero(b.getNumero()).nome(b.getNome()).ocupadoPorRota(ocupadoPorRota).build();
    }

    private static Pessoa pessoa(Ajudante a) {
        return a == null ? null : Pessoa.builder().id(a.getId()).codigo(a.getCodigo()).nome(a.getNome()).build();
    }

    private static Pessoa pessoa(Motorista m) {
        return m == null ? null : Pessoa.builder().id(m.getId()).codigo(m.getCodigo()).nome(m.getNome()).build();
    }

    private static Veiculo veiculo(Caminhao c) {
        return c == null ? null : Veiculo.builder().id(c.getId()).codigo(c.getCodigo()).placa(c.getPlaca()).descricao(c.getDescricao()).build();
    }

    // ---------------------------------------------------------------------------------------------------------
    // Apoio
    // ---------------------------------------------------------------------------------------------------------

    private static boolean podeReabrir(PlanejamentoPlanilha planilha) {
        return !planilha.isAberta() && planilha.getData().isEqual(FusoHorarioUtils.hojeBrasil());
    }

    private PlanejamentoPlanilha buscarPlanilha(UUID id) {
        return planilhaRepository.findById(id).orElseThrow(() -> new ObjectNotFound("Planilha não encontrada."));
    }

    private PlanejamentoItem buscarItem(UUID planilhaId, Integer numero) {
        return itemRepository.findByPlanilhaIdAndNumeroCarregamento(planilhaId, numero)
                .orElseThrow(() -> new ObjectNotFound("O carregamento " + numero + " não está nesta planilha."));
    }

    private static void exigirAberta(PlanejamentoPlanilha planilha) {
        if (!planilha.isAberta()) {
            throw new BusinessException("A planilha está fechada. Reabra para alterar.");
        }
    }

    private static void exigirNaoFaturado(PlanejamentoItem item) {
        if (item.isFaturada()) {
            throw new BusinessException("O carregamento " + item.getNumeroCarregamento() + " já foi faturado e não muda mais de rota nem de motorista.");
        }
    }

    private static LocalDateTime agora() {
        return FusoHorarioUtils.agoraBrasil();
    }
}
