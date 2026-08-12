package br.com.frotasPro.api.domain;

import br.com.frotasPro.api.util.FusoHorarioUtils;

import br.com.frotasPro.api.domain.enums.TipoPlataformaDispositivo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "tb_usuario",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_usuario_nome", columnNames = "nome"),
        }
)
public class Usuario extends AuditoriaBase{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, length = 150, unique = true)
    private String nome;

    @Column(nullable = false, length = 50, unique = true)
    private String login;

    @Column(nullable = false, length = 150)
    private String senha;

    @Column(nullable = false)
    private boolean ativo = true;

    /** Senha ainda é a padrão (ou foi resetada por um admin) — obriga a troca antes de liberar o resto da API. */
    @Column(name = "senha_temporaria", nullable = false)
    private boolean senhaTemporaria = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tb_usuario_acesso",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "acesso_id"))
    private List<Acesso> acesso= new ArrayList<>();

    @Column(name = "dispositivo_app_versao", length = 30)
    private String dispositivoAppVersao;

    @Enumerated(EnumType.STRING)
    @Column(name = "dispositivo_app_plataforma", length = 20)
    private TipoPlataformaDispositivo dispositivoAppPlataforma;

    @Column(name = "dispositivo_app_reportado_em")
    private LocalDateTime dispositivoAppReportadoEm;

    @Column(name = "ultimo_login_em")
    private LocalDateTime ultimoLoginEm;

    @Column(name = "total_logins", nullable = false)
    private long totalLogins = 0;

    public void registrarLogin() {
        this.ultimoLoginEm = FusoHorarioUtils.agoraBrasil();
        this.totalLogins++;
    }

    public void adicionarAcesso(Acesso acesso){
        this.acesso.add(acesso);
    }
}
