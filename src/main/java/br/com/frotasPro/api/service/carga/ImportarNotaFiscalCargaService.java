package br.com.frotasPro.api.service.carga;

import br.com.frotasPro.api.controller.response.CargaResponse;
import br.com.frotasPro.api.domain.Arquivo;
import br.com.frotasPro.api.domain.Carga;
import br.com.frotasPro.api.domain.CargaNota;
import br.com.frotasPro.api.excption.BusinessException;
import br.com.frotasPro.api.excption.ObjectNotFound;
import br.com.frotasPro.api.integracao.dto.NotaFiscalXmlDto;
import br.com.frotasPro.api.mapper.CargaMapper;
import br.com.frotasPro.api.repository.CargaRepository;
import br.com.frotasPro.api.service.arquivo.SalvarArquivoService;
import br.com.frotasPro.api.service.integracao.IntegracaoWinThorConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * Alternativa manual à sincronização com o WinThor: em vez de vir pronta da
 * integração, a informação de clientes/notas de uma carga é lida direto do
 * XML da NFe (já emitida em outro sistema) que o usuário sobe pelo sistema.
 * Não emite nem assina nota nenhuma — só lê o que o XML já traz.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportarNotaFiscalCargaService {

    private final CargaRepository cargaRepository;
    private final SalvarArquivoService salvarArquivoService;
    private final NotaFiscalXmlParser parser;
    private final OrdemEntregaCidadeService ordemEntregaCidadeService;
    private final IntegracaoWinThorConfigService integracaoWinThorConfigService;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "carga_buscar_numero", allEntries = true),
            @CacheEvict(value = "carga_buscar_codigo_externo", allEntries = true),
            @CacheEvict(value = "carga_listar", allEntries = true)
    })
    public CargaResponse importar(String numeroCarga, List<MultipartFile> arquivosXml) {
        if (arquivosXml == null || arquivosXml.isEmpty()) {
            throw new BusinessException("Nenhum arquivo XML enviado.");
        }

        Carga carga = cargaRepository.findByNumeroCarga(numeroCarga.trim())
                .orElseThrow(() -> new ObjectNotFound("Carga não encontrada para o código: " + numeroCarga));

        // Lê e valida todos os XMLs primeiro — se um vier inválido, a
        // importação inteira falha (nada fica aplicado pela metade).
        List<NotaFiscalXmlDto> notasLidas = arquivosXml.stream().map(parser::parse).toList();

        for (int i = 0; i < arquivosXml.size(); i++) {
            aplicarNota(carga, notasLidas.get(i), arquivosXml.get(i));
        }

        ordemEntregaCidadeService.aplicar(carga);

        Carga salvo = cargaRepository.save(carga);

        boolean integracaoAtiva = integracaoWinThorConfigService.isCargaIntegracaoAtiva();
        CargaResponse response = CargaMapper.toResponse(salvo);
        CargaMapper.aplicarNumeroExibicao(response, salvo, integracaoAtiva);
        return response;
    }

    private void aplicarNota(Carga carga, NotaFiscalXmlDto dto, MultipartFile arquivoXml) {
        String cliente = dto.nomeCliente();
        String nota = dto.numeroNota();

        CargaNota existente = carga.getNotas().stream()
                .filter(n -> n.getCliente().equals(cliente) && n.getNota().equals(nota))
                .findFirst()
                .orElse(null);

        Arquivo arquivo = salvarArquivoService.salvar(arquivoXml, "CARGA_" + carga.getNumeroCarga(), "NOTA_FISCAL_XML");

        if (existente != null) {
            // Nota já cadastrada (reenvio do mesmo XML, ou já veio de outra
            // fonte) — só garante o vínculo com o arquivo, sem somar peso/
            // valor de novo na carga.
            if (existente.getArquivo() == null) {
                existente.setArquivo(arquivo);
            }
            if (existente.getCidade() == null && dto.cidadeCliente() != null) {
                existente.setCidade(dto.cidadeCliente());
            }
            log.info("Nota {} do cliente {} já existia na carga {} — só vinculando o XML.",
                    nota, cliente, carga.getNumeroCarga());
            return;
        }

        CargaNota cargaNota = new CargaNota();
        cargaNota.setCarga(carga);
        cargaNota.setCliente(cliente);
        cargaNota.setNota(nota);
        cargaNota.setCidade(dto.cidadeCliente());
        cargaNota.setArquivo(arquivo);
        carga.getNotas().add(cargaNota);

        if (dto.pesoBruto() != null) {
            BigDecimal atual = carga.getPesoCarga() != null ? carga.getPesoCarga() : BigDecimal.ZERO;
            carga.setPesoCarga(atual.add(dto.pesoBruto()));
        }
        if (dto.valorTotal() != null) {
            BigDecimal atual = carga.getValorTotal() != null ? carga.getValorTotal() : BigDecimal.ZERO;
            carga.setValorTotal(atual.add(dto.valorTotal()));
        }

        log.info("Nota {} do cliente {} importada via XML pra carga {}.", nota, cliente, carga.getNumeroCarga());
    }
}
