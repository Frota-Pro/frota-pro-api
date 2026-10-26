WITH cl AS (SELECT p.carga_id, sum(a.qt_litros) litros FROM tb_abastecimento a JOIN tb_parada_carga p ON p.id = a.parada_id GROUP BY 1),
ck AS (SELECT c.numero_carga carga, COALESCE(c.dt_competencia, c.data_saida) data, k.placa, COALESCE(cc.descricao, 'Sem categoria') categoria, mo.nome motorista, COALESCE(tit.nome, mo.nome) titular,
  CASE WHEN c.km_final - c.km_inicial BETWEEN 0 AND 3000 THEN c.km_final - c.km_inicial ELSE 0 END km, COALESCE(cl.litros, 0) litros,
  COALESCE((SELECT mi.valor_meta FROM tb_meta mi WHERE mi.tipo_meta = 'CONSUMO_COMBUSTIVEL' AND mi.caminhao_id = c.caminhao_id AND COALESCE(c.dt_competencia, c.data_saida) BETWEEN mi.data_inicio AND mi.data_fim ORDER BY mi.data_inicio DESC LIMIT 1),
           (SELECT mc.valor_meta FROM tb_meta mc WHERE mc.tipo_meta = 'CONSUMO_COMBUSTIVEL' AND mc.categoria_caminhao_id = k.categoria_caminhao_id AND COALESCE(c.dt_competencia, c.data_saida) BETWEEN mc.data_inicio AND mc.data_fim ORDER BY mc.data_inicio DESC LIMIT 1)) meta_kml
  FROM tb_carga c JOIN tb_caminhao k ON k.id = c.caminhao_id LEFT JOIN tb_categoria_caminhao cc ON cc.id = k.categoria_caminhao_id JOIN tb_motorista mo ON mo.id = c.motorista_id LEFT JOIN tb_motorista tit ON tit.id = c.titular_no_periodo_id
  LEFT JOIN cl ON cl.carga_id = c.id
  WHERE c.status_carga = 'FINALIZADA' AND COALESCE(c.dt_competencia, c.data_saida) BETWEEN :inicio AND :fim)
SELECT categoria, round(sum(km) / NULLIF(sum(litros), 0), 2) AS km_por_litro,
  round(sum(km) FILTER (WHERE meta_kml IS NOT NULL) / NULLIF(sum(km / meta_kml) FILTER (WHERE meta_kml IS NOT NULL), 0), 2) AS meta_km_por_litro,
  round(100 * sum(km / meta_kml) FILTER (WHERE meta_kml IS NOT NULL) / NULLIF(sum(litros) FILTER (WHERE meta_kml IS NOT NULL), 0), 1) AS percentual_meta,
  count(*) AS viagens
FROM ck GROUP BY 1 ORDER BY 2
