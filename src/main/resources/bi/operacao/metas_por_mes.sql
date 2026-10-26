WITH mt AS (
 SELECT m.id meta_id, m.codigo, m.data_inicio, m.data_fim, m.tipo_meta, m.valor_meta, k.id caminhao_id, k.placa, COALESCE(cc.descricao, 'Sem categoria') categoria, 'Individual' origem
 FROM tb_meta m JOIN tb_caminhao k ON k.id = m.caminhao_id LEFT JOIN tb_categoria_caminhao cc ON cc.id = k.categoria_caminhao_id
 UNION ALL
 SELECT m.id, m.codigo, m.data_inicio, m.data_fim, m.tipo_meta, m.valor_meta, k.id, k.placa, COALESCE(cc.descricao, 'Sem categoria'), 'Categoria'
 FROM tb_meta m JOIN tb_caminhao k ON k.categoria_caminhao_id = m.categoria_caminhao_id AND k.ativo LEFT JOIN tb_categoria_caminhao cc ON cc.id = k.categoria_caminhao_id
 WHERE m.caminhao_id IS NULL AND NOT EXISTS (SELECT 1 FROM tb_meta mi WHERE mi.caminhao_id = k.id AND mi.tipo_meta = m.tipo_meta AND mi.data_inicio = m.data_inicio)),
cl AS (SELECT p.carga_id, sum(a.qt_litros) litros FROM tb_abastecimento a JOIN tb_parada_carga p ON p.id = a.parada_id GROUP BY 1),
mr AS (SELECT mt.*,
  CASE WHEN mt.tipo_meta = 'TONELADA' THEN (SELECT sum(c.peso_carga) FROM tb_carga c WHERE c.caminhao_id = mt.caminhao_id AND c.status_carga = 'FINALIZADA' AND COALESCE(c.dt_competencia, c.data_saida) BETWEEN mt.data_inicio AND mt.data_fim)
       ELSE (SELECT round(sum(CASE WHEN c.km_final - c.km_inicial BETWEEN 0 AND 3000 THEN c.km_final - c.km_inicial ELSE 0 END)::numeric / NULLIF(sum(COALESCE(cl.litros, 0)), 0), 2) FROM tb_carga c LEFT JOIN cl ON cl.carga_id = c.id WHERE c.caminhao_id = mt.caminhao_id AND c.status_carga = 'FINALIZADA' AND COALESCE(c.dt_competencia, c.data_saida) BETWEEN mt.data_inicio AND mt.data_fim) END realizado,
  (SELECT r.valor_realizado FROM tb_meta_resultado r WHERE r.meta_id = mt.meta_id AND r.caminhao_id = mt.caminhao_id ORDER BY r.periodo_fim DESC, r.calculado_em DESC LIMIT 1) realizado_sistema,
  (SELECT r.meta_atingida FROM tb_meta_resultado r WHERE r.meta_id = mt.meta_id AND r.caminhao_id = mt.caminhao_id ORDER BY r.periodo_fim DESC, r.calculado_em DESC LIMIT 1) atingida_sistema,
  (current_date BETWEEN mt.data_inicio AND mt.data_fim) em_andamento
 FROM mt WHERE mt.data_inicio <= current_date
  AND mt.data_fim >= :inicio AND mt.data_inicio <= :fim)
SELECT to_char(data_inicio, 'YYYY-MM') AS mes,
 round(100.0 * count(*) FILTER (WHERE tipo_meta = 'CONSUMO_COMBUSTIVEL' AND realizado >= valor_meta) / NULLIF(count(*) FILTER (WHERE tipo_meta = 'CONSUMO_COMBUSTIVEL' AND realizado IS NOT NULL), 0), 1) AS consumo_pct,
 round(100.0 * count(*) FILTER (WHERE tipo_meta = 'TONELADA' AND realizado >= valor_meta) / NULLIF(count(*) FILTER (WHERE tipo_meta = 'TONELADA' AND realizado IS NOT NULL), 0), 1) AS toneladas_pct,
 count(*) FILTER (WHERE realizado IS NULL) AS sem_dados
FROM mr GROUP BY data_inicio ORDER BY data_inicio
