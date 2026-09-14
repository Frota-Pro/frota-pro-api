package br.com.frotasPro.api.service.cliente;

import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.CargaNota;
import br.com.frotasPro.api.domain.Cliente;
import br.com.frotasPro.api.integracao.dto.NotaFiscalXmlDto;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.repository.ClienteRepository;
import br.com.frotasPro.api.service.carga.NotaFiscalXmlParser;
import br.com.frotasPro.api.util.DocumentoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Cadastro de cliente de verdade (CNPJ/CPF + endereço completo) — fonte
 * única usada tanto pelo cadastro manual quanto pela roteirização (em vez da
 * lista de nomes por cidade sem endereço que existia antes). Alimentado de
 * duas formas: automaticamente, a partir do cadastro do cliente no WinThor
 * (pcclient), em toda sincronização normal de carga (ver upsertFromWinThor,
 * chamado por SincronizarCargaService); e pelo XML da NFe, no upload manual
 * ou quando alguém abre o XML de uma nota específica (upsertFromXml).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final CargaRepository cargaRepository;
    private final NotaFiscalXmlParser parser;

    /**
     * Cria o cliente na primeira vez que seu documento aparece; nas vezes
     * seguintes, atualiza o endereço/contato com o que veio de mais novo
     * (o cliente pode ter se mudado) sem nunca duplicar pelo documento.
     * Sem documento no XML, não dá pra cadastrar com segurança — ignora.
     */
    @Transactional
    public Optional<Cliente> upsertFromXml(NotaFiscalXmlDto dto, String codigoExternoWinThor) {
        String documento = DocumentoUtils.normalizar(dto.documentoCliente());
        if (documento == null) {
            log.warn("XML da nota {} sem CNPJ/CPF do destinatário — cliente \"{}\" não cadastrado.",
                    dto.numeroNota(), dto.nomeCliente());
            return Optional.empty();
        }

        Cliente cliente = clienteRepository.findByDocumento(documento).orElseGet(Cliente::new);
        boolean novo = cliente.getId() == null;

        cliente.setDocumento(documento);
        cliente.setNome(dto.nomeCliente());
        cliente.setLogradouro(dto.logradouroCliente());
        cliente.setNumero(dto.numeroCliente());
        cliente.setComplemento(dto.complementoCliente());
        cliente.setBairro(dto.bairroCliente());
        cliente.setCidade(dto.cidadeCliente());
        cliente.setUf(dto.ufCliente());
        cliente.setCep(dto.cepCliente());
        if (dto.telefoneCliente() != null) {
            cliente.setTelefone(dto.telefoneCliente());
        }
        if (dto.emailCliente() != null) {
            cliente.setEmail(dto.emailCliente());
        }
        if (codigoExternoWinThor != null) {
            cliente.setCodigoExterno(codigoExternoWinThor);
        }

        Cliente salvo = clienteRepository.save(cliente);
        log.info("Cliente {} ({}) {}.", salvo.getNome(), documento, novo ? "cadastrado" : "atualizado");
        return Optional.of(salvo);
    }

    /**
     * Cria/atualiza o cliente a partir do cadastro dele no WinThor (pcclient),
     * durante a sincronização normal de cargas — sem precisar de XML nem de
     * chamada extra ao WinThor (o endereço já vem junto na mesma consulta que
     * traz cliente/cidade da carga). WinThor é tratado como fonte de verdade:
     * sempre sobrescreve o que já estiver cadastrado (se o cliente se mudou,
     * o cadastro acompanha). Sem documento (CNPJ/CPF) no WinThor, não cadastra.
     */
    @Transactional
    public Optional<Cliente> upsertFromWinThor(String documento, String nome, String logradouro, String numero,
                                                 String bairro, String cidade, String uf, String cep,
                                                 String codigoExternoWinThor) {
        String doc = DocumentoUtils.normalizar(documento);
        if (doc == null) {
            return Optional.empty();
        }

        Cliente cliente = clienteRepository.findByDocumento(doc).orElseGet(Cliente::new);
        boolean novo = cliente.getId() == null;

        cliente.setDocumento(doc);
        cliente.setNome(nome);
        cliente.setLogradouro(logradouro);
        cliente.setNumero(numero);
        cliente.setBairro(bairro);
        cliente.setCidade(cidade);
        cliente.setUf(uf);
        cliente.setCep(cep);
        if (codigoExternoWinThor != null) {
            cliente.setCodigoExterno(codigoExternoWinThor);
        }

        Cliente salvo = clienteRepository.save(cliente);
        log.info("Cliente {} ({}) {} a partir do sync WinThor.", salvo.getNome(), doc, novo ? "cadastrado" : "atualizado");
        return Optional.of(salvo);
    }

    /**
     * Aproveita um XML de nota já buscado do WinThor sob demanda (tela de
     * "ver documentos fiscais"/emailar nota) pra também cadastrar o Cliente
     * e vincular a CargaNota correspondente — de graça, sem nenhuma chamada
     * extra ao WinThor. Refaz a busca da carga aqui dentro (transação
     * própria) de propósito: quem chama (NotaFiscalService) não abre
     * transação nem mantém a carga "viva" depois de buscá-la, então
     * mexer na coleção de notas dela só é seguro dentro de uma sessão
     * nova. Nunca pode quebrar quem chamou — qualquer erro é só logado.
     */
    @Transactional
    public void registrarClienteDoXmlWinThor(String numeroCarga, Long numeroNota, String xml) {
        if (xml == null || xml.isBlank()) {
            return;
        }

        try {
            Carga carga = cargaRepository.findByNumeroCarga(numeroCarga.trim()).orElse(null);
            if (carga == null) {
                return;
            }

            NotaFiscalXmlDto dto = parser.parse(xml);
            String notaStr = String.valueOf(numeroNota);

            String codigoExterno = carga.getNotas().stream()
                    .filter(n -> n.getNota().equals(notaStr))
                    .findFirst()
                    .map(n -> extrairCodigoClienteSeguro(n.getCliente()))
                    .orElse(null);

            Cliente cliente = upsertFromXml(dto, codigoExterno).orElse(null);
            if (cliente == null) {
                return;
            }

            boolean alterou = false;
            for (CargaNota nota : carga.getNotas()) {
                if (nota.getNota().equals(notaStr) && nota.getClienteRef() == null) {
                    nota.setClienteRef(cliente);
                    alterou = true;
                }
            }
            if (alterou) {
                cargaRepository.save(carga);
            }
        } catch (Exception e) {
            log.warn("Não foi possível registrar o cliente a partir do XML da nota {} (carga {}): {}",
                    numeroNota, numeroCarga, e.getMessage());
        }
    }

    /** Mesmo formato "CODCLI - NOME" usado em CargaNota.cliente — null se não conseguir extrair. */
    private String extrairCodigoClienteSeguro(String cliente) {
        if (cliente == null || cliente.isBlank()) {
            return null;
        }
        try {
            return String.valueOf(Integer.valueOf(cliente.split("-", 2)[0].trim()));
        } catch (Exception e) {
            return null;
        }
    }
}
