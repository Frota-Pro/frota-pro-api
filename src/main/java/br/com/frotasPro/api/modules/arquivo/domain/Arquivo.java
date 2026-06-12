package br.com.frotasPro.api.modules.arquivo.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

import br.com.frotasPro.api.shared.domain.AuditoriaBase;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_arquivo")
public class Arquivo extends AuditoriaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "nome_original", nullable = false, length = 255)
    private String nomeOriginal;

    @Column(name = "caminho", nullable = false, length = 500)
    private String caminho;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "tamanho_bytes")
    private Long tamanhoBytes;

    @Column(name = "hash", length = 100)
    private String hash;
}
