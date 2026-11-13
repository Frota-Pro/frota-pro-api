package br.com.frotasPro.api.service.planejamento;

import br.com.frotasPro.api.controller.response.PlanejamentoPlanilhaResponse;
import br.com.frotasPro.api.domain.Ajudante;
import br.com.frotasPro.api.domain.Caminhao;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.Motorista;
import br.com.frotasPro.api.domain.PlanejamentoBox;
import br.com.frotasPro.api.domain.PlanejamentoItem;
import br.com.frotasPro.api.domain.PlanejamentoPlanilha;
import br.com.frotasPro.api.domain.PlanejamentoRemovido;
import br.com.frotasPro.api.domain.enums.DiaSaidaPlanejamento;
import br.com.frotasPro.api.domain.enums.Status;
import br.com.frotasPro.api.domain.enums.TurnoSaidaPlanejamento;
import br.com.frotasPro.api.domain.enums.StatusPlanilhaPlanejamento;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ConflictException;
import br.com.frotasPro.api.repository.AjudanteRepository;
import br.com.frotasPro.api.repository.CaminhaoRepository;
import br.com.frotasPro.api.repository.PlanejamentoBoxRepository;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.MotoristaRepository;
import br.com.frotasPro.api.repository.PlanejamentoItemRepository;
import br.com.frotasPro.api.repository.PlanejamentoPlanilhaRepository;
import br.com.frotasPro.api.repository.PlanejamentoRemovidoRepository;
import br.com.frotasPro.api.service.planejamento.PlanejamentoWinThorClient.CarregamentoWinThor;
import br.com.frotasPro.api.service.usuario.UsuarioAutenticadoService;
import br.com.frotasPro.api.util.FusoHorarioUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanejamentoCargasServiceTest {

    @Mock private PlanejamentoWinThorClient winThorClient;
    @Mock private PlanejamentoPlanilhaRepository planilhaRepository;
    @Mock private PlanejamentoItemRepository itemRepository;
    @Mock private PlanejamentoRemovidoRepository removidoRepository;
    @Mock private MotoristaRepository motoristaRepository;
    @Mock private AjudanteRepository ajudanteRepository;
    @Mock private PlanejamentoBoxRepository boxRepository;
    @Mock private CaminhaoRepository caminhaoRepository;
    @Mock private CargaRepository cargaRepository;
    @Mock private UsuarioAutenticadoService usuarioAutenticadoService;
    @Mock private PlatformTransactionManager transactionManager;
    @InjectMocks private PlanejamentoCargasService service;

    private static final LocalDate HOJE = FusoHorarioUtils.hojeBrasil();

    private PlanejamentoPlanilha planilha;
    private List<PlanejamentoItem> itens;

    @BeforeEach
    void setUp() {
        planilha = new PlanejamentoPlanilha();
        planilha.setId(UUID.randomUUID());
        planilha.setData(HOJE);
        planilha.setStatus(StatusPlanilhaPlanejamento.ABERTA);
        itens = new ArrayList<>();

        lenient().when(planilhaRepository.findById(planilha.getId())).thenReturn(Optional.of(planilha));
        lenient().when(itemRepository.findByPlanilhaIdOrderByNumeroCarregamento(planilha.getId())).thenAnswer(i -> itens);
        lenient().when(itemRepository.findByPlanilhaIdAndNumeroCarregamento(any(), any())).thenAnswer(inv -> {
            Integer numero = inv.getArgument(1);
            return itens.stream().filter(i -> i.getNumeroCarregamento().equals(numero)).findFirst();
        });
    }

    // ---- helpers --------------------------------------------------------------------------------------------

    private PlanejamentoItem item(int numero, double pesoKg, int pedidos, Integer... clientes) {
        PlanejamentoItem i = new PlanejamentoItem();
        i.setId(UUID.randomUUID());
        i.setPlanilha(planilha);
        i.setNumeroCarregamento(numero);
        i.setDestino("DESTINO " + numero);
        i.setPesoKg(BigDecimal.valueOf(pesoKg));
        i.setTotalPedidos(pedidos);
        i.setCodigosClientes(List.of(clientes));
        i.setDataMontagem(HOJE);
        itens.add(i);
        return i;
    }

    private static CarregamentoWinThor winThor(int numCar, LocalDate montagem, boolean faturada) {
        CarregamentoWinThor c = new CarregamentoWinThor();
        c.setNumCar(numCar);
        c.setDataMontagem(montagem);
        c.setDestino("D" + numCar);
        c.setCodMotorista(14);
        c.setCodVeiculo(79);
        c.setPesoKg(2161.0);
        c.setTotalPedidos(3);
        c.setCodigosClientes(List.of(1, 2));
        c.setFaturada(faturada);
        return c;
    }

    private static Motorista motorista(String nome) {
        Motorista m = new Motorista();
        m.setId(UUID.randomUUID());
        m.setNome(nome);
        m.setAtivo(true);
        return m;
    }

    private static Caminhao caminhao(String placa) {
        Caminhao c = new Caminhao();
        c.setId(UUID.randomUUID());
        c.setPlaca(placa);
        c.setAtivo(true);
        return c;
    }

    // ---- quem entra na planilha -----------------------------------------------------------------------------

    @Test
    void entraOQueFoiMontadoNoDiaMesmoJaFaturado() {
        assertTrue(PlanejamentoCargasService.deveEntrar(winThor(1, HOJE, true), HOJE, Set.of()));
        assertTrue(PlanejamentoCargasService.deveEntrar(winThor(2, HOJE, false), HOJE, Set.of()));
    }

    @Test
    void carregamentoDeOutroDiaNaoEntra() {
        LocalDate ontem = HOJE.minusDays(1);
        assertFalse(PlanejamentoCargasService.deveEntrar(winThor(1, ontem, false), HOJE, Set.of()));
        assertFalse(PlanejamentoCargasService.deveEntrar(winThor(2, ontem, true), HOJE, Set.of()));
        assertFalse(PlanejamentoCargasService.deveEntrar(winThor(3, null, false), HOJE, Set.of()));
    }

    @Test
    void carregamentoTiradoNaoVoltaNemDeDiaFuturo() {
        assertFalse(PlanejamentoCargasService.deveEntrar(winThor(1, HOJE, false), HOJE, Set.of(1)));
        assertFalse(PlanejamentoCargasService.deveEntrar(winThor(2, HOJE.plusDays(1), false), HOJE, Set.of()));
    }

    @Test
    void vendaBalcaoNaoEntraNemComNomeDigitadoDiferente() {
        // sem motorista e sem veículo (como vem do WinThor: motorista vazio, veículo 0), mesmo com nome de rota
        CarregamentoWinThor semNada = winThor(1, HOJE, false);
        semNada.setCodMotorista(null);
        semNada.setCodVeiculo(0);
        semNada.setDestino("CAJAZEIRAS");
        assertFalse(PlanejamentoCargasService.deveEntrar(semNada, HOJE, Set.of()));

        // com motorista/veículo mas o nome diz balcão, em qualquer grafia
        for (String nome : new String[]{"VENDA BALCAO", "venda balcão", "Vda Balcao", "BALCÃO", "balcao."}) {
            CarregamentoWinThor c = winThor(2, HOJE, false);
            c.setDestino(nome);
            assertFalse(PlanejamentoCargasService.deveEntrar(c, HOJE, Set.of()), nome);
        }
    }

    @Test
    void rotaComMotoristaEVeiculoEntraEUmSoZeroNaoBasta() {
        CarregamentoWinThor soSemMotorista = winThor(1, HOJE, false);
        soSemMotorista.setCodMotorista(0);
        assertTrue(PlanejamentoCargasService.deveEntrar(soSemMotorista, HOJE, Set.of()));
        assertTrue(PlanejamentoCargasService.deveEntrar(winThor(2, HOJE, false), HOJE, Set.of()));
    }

    // ---- totais da rota -------------------------------------------------------------------------------------

    @Test
    void totalDaRotaContaClientesDistintosEntrePrincipalEComplemento() {
        PlanejamentoItem principal = item(10, 6297, 28, 1, 2, 3);
        PlanejamentoItem complemento = item(11, 2161, 11, 3, 4);

        PlanejamentoCargasService.Totais t = PlanejamentoCargasService.totais(List.of(principal, complemento));

        assertEquals(0, new BigDecimal("8458").compareTo(t.pesoKg()));
        assertEquals(4, t.clientes());
        assertEquals(39, t.pedidos());
    }

    @Test
    void pesoDoWinThorEmQuilosViraToneladasComTresCasas() {
        assertEquals(new BigDecimal("2.161"), PlanejamentoCargasService.paraToneladas(new BigDecimal("2161")));
        assertEquals(new BigDecimal("0.000"), PlanejamentoCargasService.paraToneladas(null));
    }

    // ---- principal e complemento ----------------------------------------------------------------------------

    @Test
    void marcarComplementoLigaAoPrincipalELimpaMotoristaECaminhaoDele() {
        PlanejamentoItem principal = item(10, 6297, 28, 1);
        PlanejamentoItem outro = item(11, 2161, 11, 2);
        outro.setMotorista(motorista("Francisco"));
        outro.setCaminhao(caminhao("JNB1373"));

        service.definirComplemento(planilha.getId(), 11, 10);

        assertEquals(10, outro.getPrincipalNumero());
        assertNull(outro.getMotorista());
        assertNull(outro.getCaminhao());
        assertNull(principal.getPrincipalNumero());
    }

    @Test
    void complementoDeComplementoNaoPode() {
        item(10, 1, 1, 1);
        PlanejamentoItem comp = item(11, 1, 1, 2);
        comp.setPrincipalNumero(10);
        item(12, 1, 1, 3);

        BusinessException e = assertThrows(BusinessException.class, () -> service.definirComplemento(planilha.getId(), 12, 11));
        assertTrue(e.getMessage().contains("Complemento de complemento"));
    }

    @Test
    void quemJaTemComplementosNaoViraComplementoDeOutro() {
        item(10, 1, 1, 1);
        PlanejamentoItem p = item(11, 1, 1, 2);
        PlanejamentoItem comp = item(12, 1, 1, 3);
        comp.setPrincipalNumero(11);

        assertThrows(BusinessException.class, () -> service.definirComplemento(planilha.getId(), 11, 10));
        assertNull(p.getPrincipalNumero());
    }

    @Test
    void carregamentoFaturadoNaoMudaDeRota() {
        item(10, 1, 1, 1);
        PlanejamentoItem faturado = item(11, 1, 1, 2);
        faturado.setFaturada(true);

        BusinessException e = assertThrows(BusinessException.class, () -> service.definirComplemento(planilha.getId(), 11, 10));
        assertTrue(e.getMessage().contains("já foi faturado"));
        assertNull(faturado.getPrincipalNumero());
    }

    @Test
    void naoLigaAUmPrincipalFaturado() {
        PlanejamentoItem faturado = item(10, 1, 1, 1);
        faturado.setFaturada(true);
        item(11, 1, 1, 2);

        assertThrows(BusinessException.class, () -> service.definirComplemento(planilha.getId(), 11, 10));
    }

    @Test
    void desfazerComplementoSolta() {
        item(10, 1, 1, 1);
        PlanejamentoItem comp = item(11, 1, 1, 2);
        comp.setPrincipalNumero(10);

        service.definirComplemento(planilha.getId(), 11, null);

        assertNull(comp.getPrincipalNumero());
    }

    // ---- motorista e caminhão da rota -----------------------------------------------------------------------

    @Test
    void motoristaECaminhaoSaoDaRotaNoPrincipal() {
        PlanejamentoItem principal = item(10, 1, 1, 1);
        Motorista francisco = motorista("Francisco");
        Caminhao jnb = caminhao("JNB1373");
        when(motoristaRepository.findByCodigo("MOT-1")).thenReturn(Optional.of(francisco));
        when(caminhaoRepository.findByCodigo("CAM-1")).thenReturn(Optional.of(jnb));

        service.definirRota(planilha.getId(), 10, "MOT-1", "CAM-1", null);

        assertSame(francisco, principal.getMotorista());
        assertSame(jnb, principal.getCaminhao());
    }

    @Test
    void naoEscolheMotoristaNoComplemento() {
        item(10, 1, 1, 1);
        PlanejamentoItem comp = item(11, 1, 1, 2);
        comp.setPrincipalNumero(10);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.definirRota(planilha.getId(), 11, "MOT-1", null, null));
        assertTrue(e.getMessage().contains("principal (10)"));
    }

    @Test
    void naoEscolheMotoristaInativo() {
        item(10, 1, 1, 1);
        Motorista inativo = motorista("Zé");
        inativo.setAtivo(false);
        when(motoristaRepository.findByCodigo("MOT-2")).thenReturn(Optional.of(inativo));

        assertThrows(BusinessException.class, () -> service.definirRota(planilha.getId(), 10, "MOT-2", null, null));
    }

    @Test
    void mesmoMotoristaEmDuasRotasGeraAviso() {
        Motorista francisco = motorista("Francisco");
        item(10, 1, 1, 1).setMotorista(francisco);
        item(11, 1, 1, 2).setMotorista(francisco);

        PlanejamentoPlanilhaResponse resposta = service.obter(planilha.getId());

        assertEquals(1, resposta.getRotas().get(0).getAvisos().size());
        assertTrue(resposta.getRotas().get(0).getAvisos().get(0).contains("Francisco"));
        assertTrue(resposta.getRotas().get(0).getAvisos().get(0).contains("11"));
    }

    // ---- tirar ---------------------------------------------------------------------------------------------

    @Test
    void tirarRegistraParaNuncaMaisVoltarESoltaOsComplementos() {
        PlanejamentoItem principal = item(10, 1, 1, 1);
        PlanejamentoItem comp = item(11, 1, 1, 2);
        comp.setPrincipalNumero(10);
        when(removidoRepository.existsById(10)).thenReturn(false);

        service.remover(planilha.getId(), 10);

        verify(itemRepository).delete(principal);
        assertNull(comp.getPrincipalNumero());
        ArgumentCaptor<PlanejamentoRemovido> captor = ArgumentCaptor.forClass(PlanejamentoRemovido.class);
        verify(removidoRepository).save(captor.capture());
        assertEquals(10, captor.getValue().getNumeroCarregamento());
    }

    @Test
    void carregamentoFaturadoPodeSerTirado() {
        PlanejamentoItem balcao = item(10, 1, 1, 1);
        balcao.setFaturada(true);

        service.remover(planilha.getId(), 10);

        verify(itemRepository).delete(balcao);
    }

    // ---- em lote, ajudante, valor e cidades -----------------------------------------------------------------

    private static Ajudante ajudante(String nome) {
        Ajudante a = new Ajudante();
        a.setId(UUID.randomUUID());
        a.setCodigo("AJU-" + nome);
        a.setNome(nome);
        return a;
    }

    @Test
    void tirarVariosDeUmaVezRegistraTodosParaNaoVoltar() {
        PlanejamentoItem a = item(10, 1, 1, 1);
        PlanejamentoItem b = item(11, 1, 1, 2);
        PlanejamentoItem c = item(12, 1, 1, 3);

        service.removerVarios(planilha.getId(), List.of(10, 11));

        verify(itemRepository).delete(a);
        verify(itemRepository).delete(b);
        verify(itemRepository, never()).delete(c);
        ArgumentCaptor<PlanejamentoRemovido> captor = ArgumentCaptor.forClass(PlanejamentoRemovido.class);
        verify(removidoRepository, times(2)).save(captor.capture());
        assertEquals(List.of(10, 11), captor.getAllValues().stream().map(PlanejamentoRemovido::getNumeroCarregamento).toList());
    }

    @Test
    void tirarVariosSemEscolherNadaOuComUmQueNaoExisteFalha() {
        item(10, 1, 1, 1);
        assertThrows(BusinessException.class, () -> service.removerVarios(planilha.getId(), List.of()));
        assertThrows(br.com.frotasPro.api.excption.ObjectNotFound.class, () -> service.removerVarios(planilha.getId(), List.of(10, 99)));
        verify(itemRepository, never()).delete(any());
    }

    @Test
    void tirarOPrincipalJuntoComOsComplementosNaoDeixaNinguemPendurado() {
        PlanejamentoItem principal = item(10, 1, 1, 1);
        PlanejamentoItem comp = item(11, 1, 1, 2);
        comp.setPrincipalNumero(10);
        PlanejamentoItem outro = item(12, 1, 1, 3);
        outro.setPrincipalNumero(10);

        service.removerVarios(planilha.getId(), List.of(10, 11));

        // o 12 ficou: como o principal dele saiu, volta a ser solto
        assertNull(outro.getPrincipalNumero());
        verify(itemRepository).delete(principal);
        verify(itemRepository).delete(comp);
    }

    @Test
    void juntarVariosNumPrincipalSoDeUmaVez() {
        item(10, 1, 1, 1);
        PlanejamentoItem a = item(11, 1, 1, 2);
        PlanejamentoItem b = item(12, 1, 1, 3);
        a.setMotorista(motorista("Zé"));

        service.juntar(planilha.getId(), 10, List.of(11, 12));

        assertEquals(10, a.getPrincipalNumero());
        assertEquals(10, b.getPrincipalNumero());
        assertNull(a.getMotorista());
    }

    @Test
    void juntarEhTudoOuNadaSeUmDelesJaFoiFaturado() {
        item(10, 1, 1, 1);
        PlanejamentoItem ok = item(11, 1, 1, 2);
        PlanejamentoItem faturado = item(12, 1, 1, 3);
        faturado.setFaturada(true);

        assertThrows(BusinessException.class, () -> service.juntar(planilha.getId(), 10, List.of(11, 12)));
        assertNull(ok.getPrincipalNumero());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void juntarNaoAceitaOPrincipalNaPropriaLista() {
        item(10, 1, 1, 1);
        item(11, 1, 1, 2);

        assertThrows(BusinessException.class, () -> service.juntar(planilha.getId(), 10, List.of(10, 11)));
    }

    @Test
    void separarVariosSoltaTodosELimpaARota() {
        item(10, 1, 1, 1);
        PlanejamentoItem a = item(11, 1, 1, 2);
        a.setPrincipalNumero(10);
        PlanejamentoItem b = item(12, 1, 1, 3);
        b.setPrincipalNumero(10);

        service.separar(planilha.getId(), List.of(11, 12));

        assertNull(a.getPrincipalNumero());
        assertNull(b.getPrincipalNumero());
    }

    @Test
    void ajudanteEEscolhidoNaRotaEEntraNaResposta() {
        PlanejamentoItem principal = item(10, 1, 1, 1);
        Ajudante joao = ajudante("João");
        when(ajudanteRepository.findByCodigoAndAtivoTrue("AJU-João")).thenReturn(Optional.of(joao));

        PlanejamentoPlanilhaResponse resposta = service.definirRota(planilha.getId(), 10, null, null, List.of("AJU-João"));

        assertTrue(principal.getAjudantes().contains(joao));
        assertEquals("João", resposta.getRotas().get(0).getAjudantes().get(0).getNome());
    }

    @Test
    void ajudanteDesconhecidoOuInativoNaoEAceito() {
        item(10, 1, 1, 1);
        when(ajudanteRepository.findByCodigoAndAtivoTrue("AJU-X")).thenReturn(Optional.empty());

        assertThrows(br.com.frotasPro.api.excption.ObjectNotFound.class,
                () -> service.definirRota(planilha.getId(), 10, null, null, List.of("AJU-X")));
    }

    @Test
    void mesmoAjudanteEmDuasRotasGeraAviso() {
        Ajudante joao = ajudante("João");
        item(10, 1, 1, 1).getAjudantes().add(joao);
        item(11, 1, 1, 2).getAjudantes().add(joao);

        PlanejamentoPlanilhaResponse resposta = service.obter(planilha.getId());

        assertTrue(resposta.getRotas().get(0).getAvisos().get(0).contains("ajudante João"));
    }

    @Test
    void valorECidadesDaRotaSomamOPrincipalEOsComplementos() {
        PlanejamentoItem principal = item(10, 1000, 2, 1);
        principal.setValorTotal(new BigDecimal("1500.50"));
        principal.setDestino("CACHOEIRA");
        PlanejamentoItem comp = item(11, 500, 1, 2);
        comp.setValorTotal(new BigDecimal("300.25"));
        comp.setDestino("cachoeira");
        comp.setPrincipalNumero(10);
        PlanejamentoItem comp2 = item(12, 500, 1, 3);
        comp2.setValorTotal(new BigDecimal("100"));
        comp2.setDestino("SANTA CRUZ");
        comp2.setPrincipalNumero(10);

        PlanejamentoPlanilhaResponse resposta = service.obter(planilha.getId());

        PlanejamentoPlanilhaResponse.Rota rota = resposta.getRotas().get(0);
        assertEquals(0, new BigDecimal("1900.75").compareTo(rota.getValorTotal()));
        assertEquals(List.of("CACHOEIRA", "SANTA CRUZ"), rota.getDestinos());
        assertEquals(2, rota.getTotalCidades());
        assertEquals(0, new BigDecimal("1900.75").compareTo(resposta.getValorTotal()));
    }

    @Test
    void criarGuardaValorEOMotoristaEVeiculoDoWinThor() {
        CarregamentoWinThor c = winThor(100, HOJE, false);
        c.setValorTotal(new BigDecimal("18420.5"));
        c.setCodMotorista(10);
        c.setCodVeiculo(2);
        when(winThorClient.carregamentosDoPeriodo(any(), any())).thenReturn(List.of(c));
        when(removidoRepository.findAll()).thenReturn(List.of());

        service.criar(HOJE);

        ArgumentCaptor<PlanejamentoItem> captor = ArgumentCaptor.forClass(PlanejamentoItem.class);
        verify(itemRepository).save(captor.capture());
        PlanejamentoItem salvo = captor.getValue();
        assertEquals(0, new BigDecimal("18420.50").compareTo(salvo.getValorTotal()));
        assertEquals(10, salvo.getCodMotoristaWinthor());
        assertEquals(2, salvo.getCodVeiculoWinthor());
    }

    // ---- cor da rota e nome/placa do WinThor ------------------------------------------------------------------

    @Test
    void juntarDaCorARotaEEstaPermaneceQuandoOutraRotaNasce() {
        PlanejamentoItem a = item(10, 1, 1, 1);
        item(11, 1, 1, 2);
        PlanejamentoItem c = item(12, 1, 1, 3);
        item(13, 1, 1, 4);

        service.juntar(planilha.getId(), 10, List.of(11));
        assertEquals(0, a.getCor());

        service.juntar(planilha.getId(), 12, List.of(13));
        assertEquals(1, c.getCor());
        assertEquals(0, a.getCor()); // a cor da primeira rota não mudou
    }

    @Test
    void rotaSozinhaNaoTemCorEDesfazerARotaLimpaACor() {
        PlanejamentoItem a = item(10, 1, 1, 1);
        PlanejamentoItem b = item(11, 1, 1, 2);
        PlanejamentoItem solto = item(12, 1, 1, 3);

        service.juntar(planilha.getId(), 10, List.of(11));
        assertEquals(0, a.getCor());
        assertNull(solto.getCor());
        assertNull(b.getCor());

        service.separar(planilha.getId(), List.of(11));

        assertNull(a.getCor());
    }

    @Test
    void corLiberadaPorUmaRotaDesfeitaVoltaAserUsada() {
        PlanejamentoItem a = item(10, 1, 1, 1);
        item(11, 1, 1, 2);
        PlanejamentoItem c = item(12, 1, 1, 3);
        item(13, 1, 1, 4);
        PlanejamentoItem e = item(14, 1, 1, 5);
        item(15, 1, 1, 6);
        service.juntar(planilha.getId(), 10, List.of(11));
        service.juntar(planilha.getId(), 12, List.of(13));

        service.separar(planilha.getId(), List.of(11)); // a rota 10 acabou, liberou a cor 0
        service.juntar(planilha.getId(), 14, List.of(15));

        assertNull(a.getCor());
        assertEquals(1, c.getCor());
        assertEquals(0, e.getCor());
    }

    @Test
    void tirarOPrincipalDeixaOsComplementosSemCorNemRotaFantasma() {
        PlanejamentoItem a = item(10, 1, 1, 1);
        PlanejamentoItem b = item(11, 1, 1, 2);
        service.juntar(planilha.getId(), 10, List.of(11));
        assertEquals(0, a.getCor());

        service.removerVarios(planilha.getId(), List.of(10));

        assertNull(b.getPrincipalNumero());
        assertNull(b.getCor());
    }

    @Test
    void depoisDeAcabarAPaletaAsCoresRepetem() {
        assertEquals(0, PlanejamentoCargasService.proximaCor(Set.of()));
        assertEquals(3, PlanejamentoCargasService.proximaCor(Set.of(0, 1, 2)));
        java.util.Set<Integer> todas = new java.util.HashSet<>();
        for (int i = 0; i < PlanejamentoCargasService.TAMANHO_PALETA; i++) todas.add(i);
        assertEquals(0, PlanejamentoCargasService.proximaCor(todas));
    }

    @Test
    void respostaTrazACorDaRotaENomeEPlacaDoWinThor() {
        PlanejamentoItem a = item(10, 1, 1, 1);
        a.setNomeMotoristaWinthor("CLIENTE VEM BUSCAR");
        a.setPlacaVeiculoWinthor("XXX0000");
        item(11, 1, 1, 2);
        service.juntar(planilha.getId(), 10, List.of(11));

        PlanejamentoPlanilhaResponse resposta = service.obter(planilha.getId());

        assertEquals(0, resposta.getRotas().get(0).getCor());
        assertEquals("CLIENTE VEM BUSCAR", resposta.getRotas().get(0).getCarregamentos().get(0).getNomeMotoristaWinThor());
        assertEquals("XXX0000", resposta.getRotas().get(0).getCarregamentos().get(0).getPlacaVeiculoWinThor());
    }

    @Test
    void criarGuardaONomeDoMotoristaEAPlacaDoWinThor() {
        CarregamentoWinThor c = winThor(100, HOJE, false);
        c.setNomeMotorista("FRANCISCO");
        c.setPlacaVeiculo("JNB1373");
        when(winThorClient.carregamentosDoPeriodo(any(), any())).thenReturn(List.of(c));
        when(removidoRepository.findAll()).thenReturn(List.of());

        service.criar(HOJE);

        ArgumentCaptor<PlanejamentoItem> captor = ArgumentCaptor.forClass(PlanejamentoItem.class);
        verify(itemRepository).save(captor.capture());
        assertEquals("FRANCISCO", captor.getValue().getNomeMotoristaWinthor());
        assertEquals("JNB1373", captor.getValue().getPlacaVeiculoWinthor());
    }

    // ---- box, vários ajudantes e saída ------------------------------------------------------------------------

    private static PlanejamentoBox box(int numero) {
        PlanejamentoBox b = new PlanejamentoBox();
        b.setId(UUID.randomUUID());
        b.setNumero(numero);
        b.setNome("Box " + numero);
        b.setAtivo(true);
        return b;
    }

    @Test
    void umaRotaPodeTerVariosAjudantes() {
        PlanejamentoItem principal = item(10, 1, 1, 1);
        Ajudante joao = ajudante("João");
        Ajudante pedro = ajudante("Pedro");
        Ajudante lucas = ajudante("Lucas");
        when(ajudanteRepository.findByCodigoAndAtivoTrue("AJU-João")).thenReturn(Optional.of(joao));
        when(ajudanteRepository.findByCodigoAndAtivoTrue("AJU-Pedro")).thenReturn(Optional.of(pedro));
        when(ajudanteRepository.findByCodigoAndAtivoTrue("AJU-Lucas")).thenReturn(Optional.of(lucas));

        PlanejamentoPlanilhaResponse resposta = service.definirRota(planilha.getId(), 10, null, null, List.of("AJU-João", "AJU-Pedro", "AJU-Lucas"));

        assertEquals(3, principal.getAjudantes().size());
        assertEquals(List.of("João", "Lucas", "Pedro"), resposta.getRotas().get(0).getAjudantes().stream().map(p -> p.getNome()).toList());
    }

    @Test
    void trocarAListaDeAjudantesSubstituiOsAntigosELimparTiraTodos() {
        PlanejamentoItem principal = item(10, 1, 1, 1);
        principal.getAjudantes().add(ajudante("João"));
        Ajudante pedro = ajudante("Pedro");
        when(ajudanteRepository.findByCodigoAndAtivoTrue("AJU-Pedro")).thenReturn(Optional.of(pedro));

        service.definirRota(planilha.getId(), 10, null, null, List.of("AJU-Pedro"));
        assertEquals(1, principal.getAjudantes().size());
        assertTrue(principal.getAjudantes().contains(pedro));

        service.definirRota(planilha.getId(), 10, null, null, List.of());
        assertTrue(principal.getAjudantes().isEmpty());
    }

    @Test
    void boxEDiaETurnoDaSaidaSaoDaRota() {
        PlanejamentoItem principal = item(10, 1, 1, 1);
        PlanejamentoBox b3 = box(3);
        when(boxRepository.findByNumeroAndAtivoTrue(3)).thenReturn(Optional.of(b3));

        PlanejamentoPlanilhaResponse resposta = service.definirSaida(planilha.getId(), 10, 3, "AMANHA", "manha");

        assertSame(b3, principal.getBox());
        assertEquals(DiaSaidaPlanejamento.AMANHA, principal.getDiaSaida());
        assertEquals(TurnoSaidaPlanejamento.MANHA, principal.getTurnoSaida());
        assertEquals(3, resposta.getRotas().get(0).getBox().getNumero());
        assertEquals("AMANHA", resposta.getRotas().get(0).getDiaSaida());
        assertEquals("MANHA", resposta.getRotas().get(0).getTurnoSaida());
    }

    @Test
    void limparBoxEDiaETurno() {
        PlanejamentoItem principal = item(10, 1, 1, 1);
        principal.setBox(box(3));
        principal.setDiaSaida(DiaSaidaPlanejamento.HOJE);
        principal.setTurnoSaida(TurnoSaidaPlanejamento.NOITE);

        service.definirSaida(planilha.getId(), 10, null, null, "");

        assertNull(principal.getBox());
        assertNull(principal.getDiaSaida());
        assertNull(principal.getTurnoSaida());
    }

    @Test
    void boxOcupadoPorOutraRotaNaoFaturadaNaoPodeSerUsado() {
        PlanejamentoBox b3 = box(3);
        item(10, 1, 1, 1).setBox(b3);
        item(11, 1, 1, 2);
        when(boxRepository.findByNumeroAndAtivoTrue(3)).thenReturn(Optional.of(b3));

        BusinessException e = assertThrows(BusinessException.class, () -> service.definirSaida(planilha.getId(), 11, 3, "HOJE", "TARDE"));

        assertTrue(e.getMessage().contains("Box 3"));
        assertTrue(e.getMessage().contains("rota 10"));
    }

    @Test
    void boxDeRotaJaFaturadaEstaLivreDeNovo() {
        PlanejamentoBox b3 = box(3);
        PlanejamentoItem faturada = item(10, 1, 1, 1);
        faturada.setBox(b3);
        faturada.setFaturada(true);
        PlanejamentoItem nova = item(11, 1, 1, 2);
        when(boxRepository.findByNumeroAndAtivoTrue(3)).thenReturn(Optional.of(b3));

        service.definirSaida(planilha.getId(), 11, 3, "HOJE", "TARDE");

        assertSame(b3, nova.getBox());
    }

    @Test
    void aMesmaRotaPodeReescolherOProprioBox() {
        PlanejamentoBox b3 = box(3);
        PlanejamentoItem principal = item(10, 1, 1, 1);
        principal.setBox(b3);
        when(boxRepository.findByNumeroAndAtivoTrue(3)).thenReturn(Optional.of(b3));

        service.definirSaida(planilha.getId(), 10, 3, "HOJE", "NOITE");

        assertEquals(TurnoSaidaPlanejamento.NOITE, principal.getTurnoSaida());
    }

    @Test
    void complementoEFaturadoNaoEscolhemBoxNemSaida() {
        item(10, 1, 1, 1);
        PlanejamentoItem comp = item(11, 1, 1, 2);
        comp.setPrincipalNumero(10);
        PlanejamentoItem faturado = item(12, 1, 1, 3);
        faturado.setFaturada(true);

        assertThrows(BusinessException.class, () -> service.definirSaida(planilha.getId(), 11, null, "HOJE", null));
        assertThrows(BusinessException.class, () -> service.definirSaida(planilha.getId(), 12, null, "HOJE", null));
    }

    @Test
    void boxInexistenteEValoresInvalidosDeSaidaNaoPassam() {
        item(10, 1, 1, 1);
        when(boxRepository.findByNumeroAndAtivoTrue(9)).thenReturn(Optional.empty());

        assertThrows(br.com.frotasPro.api.excption.ObjectNotFound.class, () -> service.definirSaida(planilha.getId(), 10, 9, null, null));
        assertThrows(BusinessException.class, () -> service.definirSaida(planilha.getId(), 10, null, "ONTEM", null));
        assertThrows(BusinessException.class, () -> service.definirSaida(planilha.getId(), 10, null, null, "MEIO_DIA"));
    }

    @Test
    void juntarLimpaBoxAjudantesEDiaDoQueViraComplemento() {
        item(10, 1, 1, 1);
        PlanejamentoItem outro = item(11, 1, 1, 2);
        outro.setBox(box(2));
        outro.setDiaSaida(DiaSaidaPlanejamento.AMANHA);
        outro.setTurnoSaida(TurnoSaidaPlanejamento.MANHA);
        outro.getAjudantes().add(ajudante("João"));

        service.juntar(planilha.getId(), 10, List.of(11));

        assertNull(outro.getBox());
        assertNull(outro.getDiaSaida());
        assertNull(outro.getTurnoSaida());
        assertTrue(outro.getAjudantes().isEmpty());
    }

    @Test
    void respostaListaOsBoxesEQuemOcupaCada() {
        PlanejamentoBox b1 = box(1);
        PlanejamentoBox b2 = box(2);
        item(10, 1, 1, 1).setBox(b1);
        PlanejamentoItem faturada = item(11, 1, 1, 2);
        faturada.setBox(b2);
        faturada.setFaturada(true);
        when(boxRepository.findByAtivoTrueOrderByNumero()).thenReturn(List.of(b1, b2, box(3)));

        PlanejamentoPlanilhaResponse resposta = service.obter(planilha.getId());

        assertEquals(3, resposta.getBoxes().size());
        assertEquals(10, resposta.getBoxes().get(0).getOcupadoPorRota());
        assertNull(resposta.getBoxes().get(1).getOcupadoPorRota()); // a rota 11 já foi faturada: box livre
        assertNull(resposta.getBoxes().get(2).getOcupadoPorRota());
    }

    // ---- inconsistências: mesma pessoa/caminhão em duas rotas ao mesmo tempo -------------------------------------

    private PlanejamentoPlanilhaResponse.Rota rotaDa(PlanejamentoPlanilhaResponse resposta, int numero) {
        return resposta.getRotas().stream().filter(r -> r.getNumeroPrincipal() == numero).findFirst().orElseThrow();
    }

    @Test
    void mesmoAjudanteEmDuasRotasSemHorarioDefinidoEInconsistencia() {
        Ajudante joao = ajudante("João");
        item(10, 1, 1, 1).getAjudantes().add(joao);
        item(11, 1, 1, 2).getAjudantes().add(joao);

        PlanejamentoPlanilhaResponse resposta = service.obter(planilha.getId());

        PlanejamentoPlanilhaResponse.Conflito c = rotaDa(resposta, 10).getConflitos().get(0);
        assertEquals("AJUDANTE", c.getTipo());
        assertEquals("AJU-João", c.getCodigo());
        assertEquals(List.of(11), c.getRotas());
        assertFalse(c.isMesmoHorario());
        assertTrue(c.getTexto().contains("escolha o dia e o turno"));
        assertEquals(1, rotaDa(resposta, 11).getConflitos().size());
    }

    @Test
    void mesmoDiaETurnoDaOMesmoAvisoMasCertoDeQueEConflito() {
        Ajudante joao = ajudante("João");
        PlanejamentoItem a = item(10, 1, 1, 1);
        a.getAjudantes().add(joao);
        a.setDiaSaida(DiaSaidaPlanejamento.AMANHA);
        a.setTurnoSaida(TurnoSaidaPlanejamento.MANHA);
        PlanejamentoItem b = item(11, 1, 1, 2);
        b.getAjudantes().add(joao);
        b.setDiaSaida(DiaSaidaPlanejamento.AMANHA);
        b.setTurnoSaida(TurnoSaidaPlanejamento.MANHA);

        PlanejamentoPlanilhaResponse.Conflito c = rotaDa(service.obter(planilha.getId()), 10).getConflitos().get(0);

        assertTrue(c.isMesmoHorario());
        assertTrue(c.getTexto().endsWith("no mesmo dia e turno."));
    }

    @Test
    void turnosDiferentesNaoSaoInconsistencia() {
        Ajudante joao = ajudante("João");
        PlanejamentoItem a = item(10, 1, 1, 1);
        a.getAjudantes().add(joao);
        a.setDiaSaida(DiaSaidaPlanejamento.AMANHA);
        a.setTurnoSaida(TurnoSaidaPlanejamento.MANHA);
        PlanejamentoItem b = item(11, 1, 1, 2);
        b.getAjudantes().add(joao);
        b.setDiaSaida(DiaSaidaPlanejamento.AMANHA);
        b.setTurnoSaida(TurnoSaidaPlanejamento.TARDE);

        PlanejamentoPlanilhaResponse resposta = service.obter(planilha.getId());

        assertTrue(rotaDa(resposta, 10).getConflitos().isEmpty());
        assertTrue(rotaDa(resposta, 11).getAvisos().isEmpty());
    }

    @Test
    void diasDiferentesTambemNaoSaoInconsistencia() {
        Motorista francisco = motorista("Francisco");
        PlanejamentoItem a = item(10, 1, 1, 1);
        a.setMotorista(francisco);
        a.setDiaSaida(DiaSaidaPlanejamento.HOJE);
        a.setTurnoSaida(TurnoSaidaPlanejamento.NOITE);
        PlanejamentoItem b = item(11, 1, 1, 2);
        b.setMotorista(francisco);
        b.setDiaSaida(DiaSaidaPlanejamento.AMANHA);
        b.setTurnoSaida(TurnoSaidaPlanejamento.NOITE);

        assertTrue(rotaDa(service.obter(planilha.getId()), 10).getConflitos().isEmpty());
    }

    @Test
    void seSoUmaDasRotasTemHorarioAindaAvisaPorqueNaoDaParaGarantir() {
        Motorista francisco = motorista("Francisco");
        PlanejamentoItem a = item(10, 1, 1, 1);
        a.setMotorista(francisco);
        a.setDiaSaida(DiaSaidaPlanejamento.AMANHA);
        a.setTurnoSaida(TurnoSaidaPlanejamento.MANHA);
        item(11, 1, 1, 2).setMotorista(francisco);

        PlanejamentoPlanilhaResponse resposta = service.obter(planilha.getId());

        assertEquals("MOTORISTA", rotaDa(resposta, 10).getConflitos().get(0).getTipo());
        assertFalse(rotaDa(resposta, 10).getConflitos().get(0).isMesmoHorario());
    }

    @Test
    void caminhaoRepetidoTambemEInconsistencia() {
        Caminhao jnb = caminhao("JNB1373");
        item(10, 1, 1, 1).setCaminhao(jnb);
        item(11, 1, 1, 2).setCaminhao(jnb);

        PlanejamentoPlanilhaResponse.Conflito c = rotaDa(service.obter(planilha.getId()), 10).getConflitos().get(0);

        assertEquals("CAMINHAO", c.getTipo());
        assertEquals("JNB1373", c.getNome());
        assertTrue(c.getTexto().startsWith("O caminhão JNB1373 também está na rota 11"));
    }

    @Test
    void rotaJaFaturadaNaoGeraInconsistenciaParaNinguem() {
        Ajudante joao = ajudante("João");
        PlanejamentoItem saiu = item(10, 1, 1, 1);
        saiu.getAjudantes().add(joao);
        saiu.setFaturada(true);
        item(11, 1, 1, 2).getAjudantes().add(joao);

        PlanejamentoPlanilhaResponse resposta = service.obter(planilha.getId());

        assertTrue(rotaDa(resposta, 10).getConflitos().isEmpty());
        assertTrue(rotaDa(resposta, 11).getConflitos().isEmpty());
    }

    @Test
    void tresRotasComAMesmaPessoaListamAsOutrasDuas() {
        Ajudante joao = ajudante("João");
        item(10, 1, 1, 1).getAjudantes().add(joao);
        item(11, 1, 1, 2).getAjudantes().add(joao);
        item(12, 1, 1, 3).getAjudantes().add(joao);

        PlanejamentoPlanilhaResponse.Conflito c = rotaDa(service.obter(planilha.getId()), 10).getConflitos().get(0);

        assertEquals(List.of(11, 12), c.getRotas());
        assertTrue(c.getTexto().contains("11, 12"));
    }

    // ---- fechar e reabrir -----------------------------------------------------------------------------------

    @Test
    void planilhaFechadaNaoAceitaEdicao() {
        item(10, 1, 1, 1);
        planilha.setStatus(StatusPlanilhaPlanejamento.FECHADA);

        assertThrows(BusinessException.class, () -> service.remover(planilha.getId(), 10));
        assertThrows(BusinessException.class, () -> service.definirComplemento(planilha.getId(), 10, null));
        assertThrows(BusinessException.class, () -> service.definirRota(planilha.getId(), 10, null, null, null));
        assertThrows(BusinessException.class, () -> service.fechar(planilha.getId()));
    }

    @Test
    void fecharGuardaQuemEQuando() {
        when(usuarioAutenticadoService.getLogin()).thenReturn("operador");

        service.fechar(planilha.getId());

        assertEquals(StatusPlanilhaPlanejamento.FECHADA, planilha.getStatus());
        assertEquals("operador", planilha.getFechadaPor());
        assertTrue(planilha.getFechadaEm() != null);
    }

    @Test
    void reabrirNoMesmoDiaVoltaAAbrir() {
        planilha.setStatus(StatusPlanilhaPlanejamento.FECHADA);

        service.reabrir(planilha.getId());

        assertEquals(StatusPlanilhaPlanejamento.ABERTA, planilha.getStatus());
        assertNull(planilha.getFechadaEm());
    }

    @Test
    void reabrirDeUmDiaQueJaPassouNaoPode() {
        planilha.setData(HOJE.minusDays(1));
        planilha.setStatus(StatusPlanilhaPlanejamento.FECHADA);

        assertThrows(BusinessException.class, () -> service.reabrir(planilha.getId()));
        assertEquals(StatusPlanilhaPlanejamento.FECHADA, planilha.getStatus());
    }

    // ---- criar ---------------------------------------------------------------------------------------------

    @Test
    void naoCriaPlanilhaDeDiaFuturoNemDuplicada() {
        assertThrows(BusinessException.class, () -> service.criar(HOJE.plusDays(1)));

        when(planilhaRepository.existsByData(HOJE)).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.criar(HOJE));
        verify(winThorClient, never()).carregamentosDoPeriodo(any(), any());
    }

    @Test
    void criarTrazOQueEntraEIgnoraOQueNaoDeve() {
        LocalDate ontem = HOJE.minusDays(1);
        when(winThorClient.carregamentosDoPeriodo(any(), any())).thenReturn(List.of(
                winThor(100, HOJE, false),   // do dia
                winThor(101, HOJE, true),    // do dia, já faturado: entra (com o status)
                winThor(102, ontem, false),  // de ontem, mesmo sem faturar: não (planilha é só do dia)
                winThor(103, ontem, true),   // de ontem e já faturado: não
                winThor(104, HOJE, false)    // tirado antes: não
        ));
        PlanejamentoRemovido removido = new PlanejamentoRemovido();
        removido.setNumeroCarregamento(104);
        when(removidoRepository.findAll()).thenReturn(List.of(removido));

        service.criar(HOJE);

        ArgumentCaptor<PlanejamentoItem> captor = ArgumentCaptor.forClass(PlanejamentoItem.class);
        verify(itemRepository, times(2)).save(captor.capture());
        assertEquals(List.of(100, 101), captor.getAllValues().stream().map(PlanejamentoItem::getNumeroCarregamento).toList());
        PlanejamentoItem faturado = captor.getAllValues().get(1);
        assertTrue(faturado.isFaturada());
        assertEquals(0, new BigDecimal("2161").compareTo(faturado.getPesoKg()));
        assertEquals(Set.of(1, 2), faturado.getCodigosClientes());
    }

    @Test
    void atualizarNaoDuplicaEAtualizaOQueJaEstaNaPlanilha() {
        PlanejamentoItem existente = item(100, 500, 1, 9);
        when(winThorClient.carregamentosDoPeriodo(any(), any())).thenReturn(List.of(
                winThor(100, HOJE, true),
                winThor(105, HOJE, false)));
        when(removidoRepository.findAll()).thenReturn(List.of());

        service.atualizar(planilha.getId());

        assertTrue(existente.isFaturada());
        assertEquals(0, new BigDecimal("2161").compareTo(existente.getPesoKg()));
        ArgumentCaptor<PlanejamentoItem> captor = ArgumentCaptor.forClass(PlanejamentoItem.class);
        verify(itemRepository, times(2)).save(captor.capture());
        assertEquals(List.of(100, 105), captor.getAllValues().stream().map(PlanejamentoItem::getNumeroCarregamento).toList());
    }

    // ---- status da carga no FrotaPRO -----------------------------------------------------------------------

    @Test
    void faturadoMostraOStatusEOMotoristaRealDaCarga() {
        PlanejamentoItem faturado = item(10, 1, 1, 1);
        faturado.setFaturada(true);
        Carga carga = new Carga();
        carga.setStatusCarga(Status.EM_ROTA);
        carga.setMotorista(motorista("Venilson"));
        carga.setCaminhao(caminhao("OFX3415"));
        when(cargaRepository.findByNumeroCargaExterno("10")).thenReturn(Optional.of(carga));

        PlanejamentoPlanilhaResponse resposta = service.obter(planilha.getId());

        PlanejamentoPlanilhaResponse.Carregamento c = resposta.getRotas().get(0).getCarregamentos().get(0);
        assertEquals("EM_ROTA", c.getStatusCarga());
        assertEquals("Venilson", c.getMotoristaCarga());
        assertEquals("OFX3415", c.getCaminhaoCarga());
    }
}
