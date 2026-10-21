-- Fechamento de meta com prazo em dias úteis do mês seguinte + confirmação do motorista.
--
-- dias_uteis_fechamento_meta: quantos dias úteis (seg-sex) depois do fim do mês a meta
-- mensal continua aberta. 0 = desativado (comportamento de antes: fecha no dia seguinte
-- ao fim do mês) — por isso o padrão é 0, o deploy não muda nada até o admin ligar.
ALTER TABLE tb_parametro_sistema
    ADD COLUMN IF NOT EXISTS dias_uteis_fechamento_meta integer NOT NULL DEFAULT 0;

-- Mês em que a carga conta pras metas/relatórios quando ele NÃO é o mês de dt_saida
-- (carga que só foi finalizada depois do mês dela ter fechado — conta no mês aberto
-- seguinte). Nulo = vale a data de saída (todo o histórico existente).
ALTER TABLE tb_carga
    ADD COLUMN IF NOT EXISTS dt_competencia date;

-- Motorista confirmou (no app) que a meta daquele mês está ok: o mês fecha pra ele
-- antes do prazo geral, e as cargas que ele finalizar dali em diante contam no mês seguinte.
CREATE TABLE IF NOT EXISTS tb_fechamento_meta_motorista (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    motorista_id     uuid         NOT NULL,
    mes_referencia   date         NOT NULL,
    confirmado_em    timestamp without time zone NOT NULL,
    confirmado_por   varchar(100),
    origem           varchar(20)  NOT NULL DEFAULT 'APP',
    reaberto_em      timestamp without time zone,
    reaberto_por     varchar(100),

    CONSTRAINT fk_fechamento_meta_motorista
        FOREIGN KEY (motorista_id) REFERENCES tb_motorista (id)
        ON UPDATE NO ACTION ON DELETE CASCADE,
    CONSTRAINT uk_fechamento_meta_motorista_mes UNIQUE (motorista_id, mes_referencia)
);
